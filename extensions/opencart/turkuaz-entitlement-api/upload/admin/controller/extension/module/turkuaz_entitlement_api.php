<?php
// 📄 Dosya Yolu: upload/admin/controller/extension/module/turkuaz_entitlement_api.php
// 📌 Amac: Turkuaz Entitlement API modul ayarlarini yonetmek ve tablo kurulumunu yapmak
// 📌 Modul - FileType
// Version: 0.2.0
// Aciklama: OpenCart 3.x admin ayarlari, multi-store entitlement tablolari ve izin kontrolu
// Bagimli Oldugu Katman: Controller
class ControllerExtensionModuleTurkuazEntitlementApi extends Controller {
    private $error = array();

    public function index() {
        $this->load->language('extension/module/turkuaz_entitlement_api');
        $this->document->setTitle($this->language->get('heading_title'));
        $this->load->model('setting/setting');

        if (($this->request->server['REQUEST_METHOD'] == 'POST') && $this->validate()) {
            $this->model_setting_setting->editSetting('module_turkuaz_entitlement_api', $this->request->post);
            $this->session->data['success'] = $this->language->get('text_success');
            $this->response->redirect($this->url->link('marketplace/extension', 'user_token=' . $this->session->data['user_token'] . '&type=module', true));
        }

        $language_keys = array(
            'heading_title', 'text_edit', 'text_enabled', 'text_disabled', 'entry_status',
            'entry_project_map', 'entry_order_status_ids', 'entry_default_device_limit',
            'entry_offline_grace_days', 'entry_api_secret', 'help_project_map',
            'help_order_status_ids', 'help_api_secret', 'button_save', 'button_cancel'
        );

        foreach ($language_keys as $key) {
            $data[$key] = $this->language->get($key);
        }

        $data['error_warning'] = isset($this->error['warning']) ? $this->error['warning'] : '';

        $data['breadcrumbs'] = array();
        $data['breadcrumbs'][] = array(
            'text' => $this->language->get('text_home'),
            'href' => $this->url->link('common/dashboard', 'user_token=' . $this->session->data['user_token'], true)
        );
        $data['breadcrumbs'][] = array(
            'text' => $this->language->get('text_extension'),
            'href' => $this->url->link('marketplace/extension', 'user_token=' . $this->session->data['user_token'] . '&type=module', true)
        );
        $data['breadcrumbs'][] = array(
            'text' => $this->language->get('heading_title'),
            'href' => $this->url->link('extension/module/turkuaz_entitlement_api', 'user_token=' . $this->session->data['user_token'], true)
        );

        $data['action'] = $this->url->link('extension/module/turkuaz_entitlement_api', 'user_token=' . $this->session->data['user_token'], true);
        $data['cancel'] = $this->url->link('marketplace/extension', 'user_token=' . $this->session->data['user_token'] . '&type=module', true);

        $defaults = array(
            'module_turkuaz_entitlement_api_status' => 0,
            'module_turkuaz_entitlement_api_project_map' => "# product_id|project_code|entitlement_type|entitlement_code|device_limit|quantity|duration_days\n# 12|UNIZIP|SOFTWARE_LICENSE|UNIZIP_PRO|1|1|365\n# 34|RAGNAR|GAME_ITEM|GOLD_1000|0|1000|0",
            'module_turkuaz_entitlement_api_order_status_ids' => '5',
            'module_turkuaz_entitlement_api_default_device_limit' => 1,
            'module_turkuaz_entitlement_api_offline_grace_days' => 7,
            'module_turkuaz_entitlement_api_secret' => ''
        );

        foreach ($defaults as $key => $default) {
            if (isset($this->request->post[$key])) {
                $data[$key] = $this->request->post[$key];
            } else {
                $value = $this->config->get($key);
                $data[$key] = $value !== null ? $value : $default;
            }
        }

        $data['header'] = $this->load->controller('common/header');
        $data['column_left'] = $this->load->controller('common/column_left');
        $data['footer'] = $this->load->controller('common/footer');

        $this->response->setOutput($this->load->view('extension/module/turkuaz_entitlement_api', $data));
    }

    public function install() {
        $this->db->query("CREATE TABLE IF NOT EXISTS `" . DB_PREFIX . "turkuaz_entitlement` (
            `entitlement_id` INT(11) NOT NULL AUTO_INCREMENT,
            `store_id` INT(11) NOT NULL DEFAULT 0,
            `project_code` VARCHAR(64) NOT NULL,
            `customer_id` INT(11) NOT NULL,
            `order_id` INT(11) NOT NULL DEFAULT 0,
            `product_id` INT(11) NOT NULL DEFAULT 0,
            `entitlement_type` VARCHAR(64) NOT NULL,
            `entitlement_code` VARCHAR(128) NOT NULL,
            `quantity` INT(11) NOT NULL DEFAULT 1,
            `device_limit` INT(11) NOT NULL DEFAULT 0,
            `status` TINYINT(1) NOT NULL DEFAULT 1,
            `expires_at` DATETIME NULL,
            `delivered_at` DATETIME NULL,
            `created_at` DATETIME NOT NULL,
            `updated_at` DATETIME NOT NULL,
            PRIMARY KEY (`entitlement_id`),
            UNIQUE KEY `uniq_order_product_project` (`store_id`, `customer_id`, `order_id`, `product_id`, `project_code`, `entitlement_code`),
            KEY `idx_customer_project` (`customer_id`, `project_code`, `status`),
            KEY `idx_store_project` (`store_id`, `project_code`, `status`)
        ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci");

        $this->db->query("CREATE TABLE IF NOT EXISTS `" . DB_PREFIX . "turkuaz_entitlement_device` (
            `entitlement_device_id` INT(11) NOT NULL AUTO_INCREMENT,
            `entitlement_id` INT(11) NOT NULL,
            `customer_id` INT(11) NOT NULL,
            `project_code` VARCHAR(64) NOT NULL,
            `device_id` VARCHAR(128) NOT NULL,
            `device_name` VARCHAR(255) NOT NULL,
            `token_hash` VARCHAR(64) NOT NULL,
            `status` TINYINT(1) NOT NULL DEFAULT 1,
            `activated_at` DATETIME NOT NULL,
            `last_seen_at` DATETIME NOT NULL,
            PRIMARY KEY (`entitlement_device_id`),
            KEY `idx_entitlement` (`entitlement_id`, `status`),
            KEY `idx_customer_project` (`customer_id`, `project_code`, `status`),
            KEY `idx_device` (`device_id`),
            KEY `idx_token_hash` (`token_hash`)
        ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci");

        $this->db->query("CREATE TABLE IF NOT EXISTS `" . DB_PREFIX . "turkuaz_entitlement_delivery` (
            `delivery_id` INT(11) NOT NULL AUTO_INCREMENT,
            `entitlement_id` INT(11) NOT NULL,
            `project_code` VARCHAR(64) NOT NULL,
            `game_account_id` VARCHAR(128) NOT NULL,
            `character_id` VARCHAR(128) NOT NULL,
            `item_code` VARCHAR(128) NOT NULL,
            `quantity` INT(11) NOT NULL DEFAULT 1,
            `status` TINYINT(1) NOT NULL DEFAULT 0,
            `delivered_at` DATETIME NULL,
            `created_at` DATETIME NOT NULL,
            PRIMARY KEY (`delivery_id`),
            KEY `idx_entitlement` (`entitlement_id`),
            KEY `idx_project_status` (`project_code`, `status`)
        ) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci");
    }

    public function uninstall() {
        $this->load->model('setting/setting');
        $this->model_setting_setting->deleteSetting('module_turkuaz_entitlement_api');
    }

    protected function validate() {
        if (!$this->user->hasPermission('modify', 'extension/module/turkuaz_entitlement_api')) {
            $this->error['warning'] = $this->language->get('error_permission');
        }

        return !$this->error;
    }
}
