<?php
// 📄 Dosya Yolu: upload/catalog/model/extension/module/turkuaz_entitlement_api.php
// 📌 Amac: Turkuaz Entitlement API icin siparis, hak, cihaz ve teslimat verilerini yonetmek
// 📌 Modul - FileType
// Version: 0.2.0
// Aciklama: Multi-store ve project_code bazli lisans/item entitlement repository modelidir
// Bagimli Oldugu Katman: Repo/Model
class ModelExtensionModuleTurkuazEntitlementApi extends Model {
    public function parseProjectMap($raw_map, $default_device_limit) {
        $result = array();
        $lines = preg_split('/\r\n|\r|\n/', (string)$raw_map);

        foreach ($lines as $line) {
            $trimmed = trim($line);
            if ($trimmed === '' || strpos($trimmed, '#') === 0) {
                continue;
            }

            $parts = array_map('trim', explode('|', $trimmed));
            if (count($parts) < 4) {
                continue;
            }

            $product_id = (int)$parts[0];
            if ($product_id <= 0) {
                continue;
            }

            $device_limit = isset($parts[4]) && (int)$parts[4] > 0 ? (int)$parts[4] : (int)$default_device_limit;
            $quantity = isset($parts[5]) && (int)$parts[5] > 0 ? (int)$parts[5] : 1;
            $duration_days = isset($parts[6]) && (int)$parts[6] > 0 ? (int)$parts[6] : 0;

            $result[$product_id] = array(
                'product_id' => $product_id,
                'project_code' => strtoupper($parts[1]),
                'entitlement_type' => strtoupper($parts[2]),
                'entitlement_code' => strtoupper($parts[3]),
                'device_limit' => $device_limit,
                'quantity' => $quantity,
                'duration_days' => $duration_days
            );
        }

        return $result;
    }

    public function findActiveOrderProduct($customer_id, $store_id, $project_code, $project_map, $order_status_ids) {
        $project_code = strtoupper($project_code);
        $product_ids = array();

        foreach ($project_map as $product_id => $map) {
            if ($map['project_code'] === $project_code) {
                $product_ids[] = (int)$product_id;
            }
        }

        $order_status_ids = $this->cleanIdList($order_status_ids);
        if (!$product_ids || !$order_status_ids) {
            return false;
        }

        $sql = "SELECT op.product_id, o.order_id, o.store_id FROM `" . DB_PREFIX . "order` o INNER JOIN `" . DB_PREFIX . "order_product` op ON (o.order_id = op.order_id) WHERE o.customer_id = '" . (int)$customer_id . "' AND o.store_id = '" . (int)$store_id . "' AND o.order_status_id IN (" . implode(',', $order_status_ids) . ") AND op.product_id IN (" . implode(',', $product_ids) . ") ORDER BY o.date_added DESC LIMIT 1";
        $query = $this->db->query($sql);

        return $query->num_rows ? $query->row : false;
    }

    public function ensureEntitlement($customer_id, $store_id, $order_id, $product_id, $map) {
        $query = $this->db->query("SELECT * FROM `" . DB_PREFIX . "turkuaz_entitlement` WHERE store_id = '" . (int)$store_id . "' AND customer_id = '" . (int)$customer_id . "' AND order_id = '" . (int)$order_id . "' AND product_id = '" . (int)$product_id . "' AND project_code = '" . $this->db->escape($map['project_code']) . "' AND entitlement_code = '" . $this->db->escape($map['entitlement_code']) . "' LIMIT 1");

        if ($query->num_rows) {
            return $query->row;
        }

        $expires_sql = 'NULL';
        if ((int)$map['duration_days'] > 0) {
            $expires_sql = "DATE_ADD(NOW(), INTERVAL " . (int)$map['duration_days'] . " DAY)";
        }

        $this->db->query("INSERT INTO `" . DB_PREFIX . "turkuaz_entitlement` SET store_id = '" . (int)$store_id . "', project_code = '" . $this->db->escape($map['project_code']) . "', customer_id = '" . (int)$customer_id . "', order_id = '" . (int)$order_id . "', product_id = '" . (int)$product_id . "', entitlement_type = '" . $this->db->escape($map['entitlement_type']) . "', entitlement_code = '" . $this->db->escape($map['entitlement_code']) . "', quantity = '" . (int)$map['quantity'] . "', device_limit = '" . (int)$map['device_limit'] . "', status = '1', expires_at = " . $expires_sql . ", created_at = NOW(), updated_at = NOW()");

        $entitlement_id = (int)$this->db->getLastId();
        $created = $this->db->query("SELECT * FROM `" . DB_PREFIX . "turkuaz_entitlement` WHERE entitlement_id = '" . (int)$entitlement_id . "' LIMIT 1");
        return $created->num_rows ? $created->row : false;
    }

    public function activeDeviceCount($entitlement_id) {
        $query = $this->db->query("SELECT COUNT(*) AS total FROM `" . DB_PREFIX . "turkuaz_entitlement_device` WHERE entitlement_id = '" . (int)$entitlement_id . "' AND status = '1'");
        return (int)$query->row['total'];
    }

    public function findDevice($entitlement_id, $device_id) {
        $query = $this->db->query("SELECT * FROM `" . DB_PREFIX . "turkuaz_entitlement_device` WHERE entitlement_id = '" . (int)$entitlement_id . "' AND device_id = '" . $this->db->escape($device_id) . "' LIMIT 1");
        return $query->num_rows ? $query->row : false;
    }

    public function activateDevice($entitlement, $device_id, $device_name, $token) {
        $token_hash = $this->hashToken($token);
        $device = $this->findDevice((int)$entitlement['entitlement_id'], $device_id);

        if ($device) {
            $this->db->query("UPDATE `" . DB_PREFIX . "turkuaz_entitlement_device` SET token_hash = '" . $this->db->escape($token_hash) . "', device_name = '" . $this->db->escape($device_name) . "', status = '1', last_seen_at = NOW() WHERE entitlement_device_id = '" . (int)$device['entitlement_device_id'] . "'");
            return (int)$device['entitlement_device_id'];
        }

        $this->db->query("INSERT INTO `" . DB_PREFIX . "turkuaz_entitlement_device` SET entitlement_id = '" . (int)$entitlement['entitlement_id'] . "', customer_id = '" . (int)$entitlement['customer_id'] . "', project_code = '" . $this->db->escape($entitlement['project_code']) . "', device_id = '" . $this->db->escape($device_id) . "', device_name = '" . $this->db->escape($device_name) . "', token_hash = '" . $this->db->escape($token_hash) . "', status = '1', activated_at = NOW(), last_seen_at = NOW()");
        return (int)$this->db->getLastId();
    }

    public function findByToken($token, $device_id, $project_code) {
        $token_hash = $this->hashToken($token);
        $sql = "SELECT d.*, e.entitlement_type, e.entitlement_code, e.store_id, e.expires_at FROM `" . DB_PREFIX . "turkuaz_entitlement_device` d INNER JOIN `" . DB_PREFIX . "turkuaz_entitlement` e ON (d.entitlement_id = e.entitlement_id) WHERE d.token_hash = '" . $this->db->escape($token_hash) . "' AND d.device_id = '" . $this->db->escape($device_id) . "' AND d.status = '1' AND e.status = '1'";
        if ($project_code !== '') {
            $sql .= " AND d.project_code = '" . $this->db->escape(strtoupper($project_code)) . "'";
        }
        $sql .= " LIMIT 1";
        $query = $this->db->query($sql);
        return $query->num_rows ? $query->row : false;
    }

    public function touchDevice($entitlement_device_id) {
        $this->db->query("UPDATE `" . DB_PREFIX . "turkuaz_entitlement_device` SET last_seen_at = NOW() WHERE entitlement_device_id = '" . (int)$entitlement_device_id . "'");
    }

    public function deactivateToken($token, $device_id) {
        $token_hash = $this->hashToken($token);
        $this->db->query("UPDATE `" . DB_PREFIX . "turkuaz_entitlement_device` SET status = '0', last_seen_at = NOW() WHERE token_hash = '" . $this->db->escape($token_hash) . "' AND device_id = '" . $this->db->escape($device_id) . "'");
    }

    public function pendingGameEntitlements($project_code, $customer_id) {
        $sql = "SELECT * FROM `" . DB_PREFIX . "turkuaz_entitlement` WHERE project_code = '" . $this->db->escape(strtoupper($project_code)) . "' AND customer_id = '" . (int)$customer_id . "' AND entitlement_type IN ('GAME_ITEM', 'GAME_PREMIUM', 'GAME_CURRENCY') AND status = '1' AND delivered_at IS NULL ORDER BY created_at ASC";
        $query = $this->db->query($sql);
        return $query->rows;
    }

    public function markDelivered($entitlement_id, $project_code, $game_account_id, $character_id) {
        $query = $this->db->query("SELECT * FROM `" . DB_PREFIX . "turkuaz_entitlement` WHERE entitlement_id = '" . (int)$entitlement_id . "' AND project_code = '" . $this->db->escape(strtoupper($project_code)) . "' AND status = '1' LIMIT 1");
        if (!$query->num_rows) {
            return false;
        }

        $entitlement = $query->row;
        $this->db->query("INSERT INTO `" . DB_PREFIX . "turkuaz_entitlement_delivery` SET entitlement_id = '" . (int)$entitlement_id . "', project_code = '" . $this->db->escape($entitlement['project_code']) . "', game_account_id = '" . $this->db->escape($game_account_id) . "', character_id = '" . $this->db->escape($character_id) . "', item_code = '" . $this->db->escape($entitlement['entitlement_code']) . "', quantity = '" . (int)$entitlement['quantity'] . "', status = '1', delivered_at = NOW(), created_at = NOW()");
        $this->db->query("UPDATE `" . DB_PREFIX . "turkuaz_entitlement` SET delivered_at = NOW(), updated_at = NOW() WHERE entitlement_id = '" . (int)$entitlement_id . "'");
        return true;
    }

    public function hashToken($token) {
        return hash('sha256', (string)$token);
    }

    private function cleanIdList($value) {
        $result = array();
        foreach (explode(',', (string)$value) as $part) {
            $id = (int)trim($part);
            if ($id > 0) {
                $result[] = $id;
            }
        }
        return array_values(array_unique($result));
    }
}
