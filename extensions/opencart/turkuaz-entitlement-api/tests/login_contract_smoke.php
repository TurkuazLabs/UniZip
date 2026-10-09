<?php
// 📄 Dosya Yolu: /extensions/opencart/turkuaz-entitlement-api/tests/login_contract_smoke.php
// 📌 Amac: OpenCart 3.x controller customer service ve entitlement gate contract smoke testi
// 📌 Modul - PHP
// Version: 0.2.1
// Aciklama: Giris icin customer API ve expired entitlement engelini sahte bagimliliklarla dogrular
// Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool

class Controller {}

class CustomerServiceStub {
    public $calls = 0;

    public function login($email, $password) {
        $this->calls++;
        return $email === 'test@example.org' && $password === 'test-pass';
    }
}

class CustomerModelStub {
    public function getCustomerByEmail($email) {
        return array('customer_id' => 7, 'email' => $email);
    }
}

class EntitlementModelStub {
    public $active = true;
    public $activated = false;

    public function parseProjectMap($raw, $limit) {
        return array(12 => array(
            'project_code' => 'UNIZIP',
            'entitlement_type' => 'SOFTWARE_LICENSE',
            'entitlement_code' => 'UNIZIP_PRO'
        ));
    }

    public function findActiveOrderProduct($customer_id, $store_id, $project, $map, $statuses) {
        return array('order_id' => 30, 'product_id' => 12);
    }

    public function ensureEntitlement($customer_id, $store_id, $order_id, $product_id, $map) {
        return array(
            'entitlement_id' => 42,
            'entitlement_type' => 'SOFTWARE_LICENSE',
            'device_limit' => 1,
            'project_code' => 'UNIZIP',
            'entitlement_code' => 'UNIZIP_PRO'
        );
    }

    public function isEntitlementActive($entitlement) {
        return $this->active;
    }

    public function findDevice($id, $device) {
        return false;
    }

    public function activeDeviceCount($id) {
        return 0;
    }

    public function activateDevice($entitlement, $device, $device_name, $token) {
        $this->activated = true;
    }
}

class LoaderStub {
    public function model($route) {}
}

class ConfigStub {
    public function get($key) {
        $values = array(
            'module_turkuaz_entitlement_api_status' => true,
            'module_turkuaz_entitlement_api_project_map' => '',
            'module_turkuaz_entitlement_api_default_device_limit' => 1,
            'module_turkuaz_entitlement_api_order_status_ids' => '5',
            'module_turkuaz_entitlement_api_offline_grace_days' => 7,
            'config_store_id' => 1
        );
        return isset($values[$key]) ? $values[$key] : null;
    }
}

class ResponseStub {
    public $body = '';

    public function addHeader($header) {}
    public function setOutput($output) { $this->body = $output; }
}

require_once dirname(__DIR__) . '/upload/catalog/controller/extension/module/turkuaz_entitlement_api.php';

function runLoginCase($active) {
    $controller = new ControllerExtensionModuleTurkuazEntitlementApi();
    $controller->load = new LoaderStub();
    $controller->customer = new CustomerServiceStub();
    $controller->model_account_customer = new CustomerModelStub();
    $model = new EntitlementModelStub();
    $model->active = $active;
    $controller->model_extension_module_turkuaz_entitlement_api = $model;
    $controller->config = new ConfigStub();
    $controller->response = new ResponseStub();
    $controller->request = (object) array('post' => array(
        'email' => 'test@example.org',
        'password' => 'test-pass',
        'device_id' => 'smoke-device'
    ), 'server' => array());

    $controller->login();
    $result = json_decode($controller->response->body, true);
    if ($controller->customer->calls !== 1) {
        throw new RuntimeException('OpenCart customer service not invoked');
    }
    if ($active) {
        if (!$result['success'] || !$model->activated || empty($result['token'])) {
            throw new RuntimeException('Valid customer login failed');
        }
    } else {
        if ($result['success'] || $result['code'] !== 'entitlement_inactive'
            || $model->activated) {
            throw new RuntimeException('Expired entitlement was activated');
        }
    }
}

runLoginCase(true);
runLoginCase(false);
echo "OpenCart entitlement login smoke PASS\n";
