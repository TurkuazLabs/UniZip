<?php
// 📄 Dosya Yolu: upload/catalog/controller/extension/module/turkuaz_entitlement_api.php
// 📌 Amac: Turkuaz Entitlement API JSON endpointlerini saglamak
// 📌 Modul - FileType
// Version: 0.2.0
// Aciklama: UniZip lisans girisi, token dogrulama ve Ragnar benzeri oyun hak teslim endpointleri
// Bagimli Oldugu Katman: Controller
class ControllerExtensionModuleTurkuazEntitlementApi extends Controller {
    private const DEFAULT_PROJECT_CODE = 'UNIZIP';

    public function login() {
        $this->load->model('account/customer');
        $this->load->model('extension/module/turkuaz_entitlement_api');

        if (!$this->enabled()) {
            return $this->json(array('success' => false, 'code' => 'disabled', 'message' => 'Turkuaz Entitlement API disabled.'));
        }

        $input = $this->input();
        $email = isset($input['email']) ? trim($input['email']) : '';
        $password = isset($input['password']) ? (string)$input['password'] : '';
        $project_code = $this->projectCode($input);
        $device_id = isset($input['device_id']) ? trim($input['device_id']) : '';
        $device_name = isset($input['device_name']) ? trim($input['device_name']) : 'Turkuaz Device';

        if (!$email || !$password || !$device_id) {
            return $this->json(array('success' => false, 'code' => 'missing_input', 'message' => 'Email, password and device_id are required.'));
        }

        if (!$this->model_account_customer->login($email, $password)) {
            return $this->json(array('success' => false, 'code' => 'invalid_login', 'message' => 'Email or password is invalid.'));
        }

        $customer = $this->model_account_customer->getCustomerByEmail($email);
        if (!$customer) {
            return $this->json(array('success' => false, 'code' => 'customer_missing', 'message' => 'Customer not found.'));
        }

        $project_map = $this->projectMap();
        $order_product = $this->model_extension_module_turkuaz_entitlement_api->findActiveOrderProduct(
            (int)$customer['customer_id'],
            (int)$this->config->get('config_store_id'),
            $project_code,
            $project_map,
            $this->config->get('module_turkuaz_entitlement_api_order_status_ids')
        );

        if (!$order_product || !isset($project_map[(int)$order_product['product_id']])) {
            return $this->json(array('success' => false, 'code' => 'no_entitlement', 'message' => 'Active entitlement order not found for project ' . $project_code . '.'));
        }

        $map = $project_map[(int)$order_product['product_id']];
        $entitlement = $this->model_extension_module_turkuaz_entitlement_api->ensureEntitlement(
            (int)$customer['customer_id'],
            (int)$this->config->get('config_store_id'),
            (int)$order_product['order_id'],
            (int)$order_product['product_id'],
            $map
        );

        if (!$entitlement) {
            return $this->json(array('success' => false, 'code' => 'entitlement_create_failed', 'message' => 'Entitlement could not be created.'));
        }

        if ($entitlement['entitlement_type'] === 'SOFTWARE_LICENSE') {
            $device_limit = max(1, (int)$entitlement['device_limit']);
            $existing_device = $this->model_extension_module_turkuaz_entitlement_api->findDevice((int)$entitlement['entitlement_id'], $device_id);
            $active_count = $this->model_extension_module_turkuaz_entitlement_api->activeDeviceCount((int)$entitlement['entitlement_id']);

            if (!$existing_device && $active_count >= $device_limit) {
                return $this->json(array('success' => false, 'code' => 'device_limit', 'message' => 'Device limit reached.'));
            }

            $token = $this->token();
            $refresh_token = $this->token();
            $this->model_extension_module_turkuaz_entitlement_api->activateDevice($entitlement, $device_id, $device_name, $token);

            return $this->json($this->successPayload($customer['email'], $entitlement, $token, $refresh_token));
        }

        return $this->json($this->successPayload($customer['email'], $entitlement, '', ''));
    }

    public function validate() {
        $this->load->model('extension/module/turkuaz_entitlement_api');

        if (!$this->enabled()) {
            return $this->json(array('success' => false, 'code' => 'disabled', 'message' => 'Turkuaz Entitlement API disabled.'));
        }

        $input = $this->input();
        $token = isset($input['token']) ? trim($input['token']) : $this->bearerToken();
        $device_id = isset($input['device_id']) ? trim($input['device_id']) : '';
        $project_code = isset($input['project_code']) ? strtoupper(trim($input['project_code'])) : '';

        if (!$token || !$device_id) {
            return $this->json(array('success' => false, 'code' => 'missing_token', 'message' => 'Token and device_id are required.'));
        }

        $device = $this->model_extension_module_turkuaz_entitlement_api->findByToken($token, $device_id, $project_code);
        if (!$device) {
            return $this->json(array('success' => false, 'code' => 'invalid_token', 'message' => 'Entitlement token is invalid.'));
        }

        $this->model_extension_module_turkuaz_entitlement_api->touchDevice((int)$device['entitlement_device_id']);
        $this->load->model('account/customer');
        $customer = $this->model_account_customer->getCustomer((int)$device['customer_id']);
        $email = $customer && isset($customer['email']) ? $customer['email'] : '';

        return $this->json($this->successPayload($email, $device, '', ''));
    }

    public function logout() {
        $this->load->model('extension/module/turkuaz_entitlement_api');
        $input = $this->input();
        $token = isset($input['token']) ? trim($input['token']) : $this->bearerToken();
        $device_id = isset($input['device_id']) ? trim($input['device_id']) : '';

        if ($token && $device_id) {
            $this->model_extension_module_turkuaz_entitlement_api->deactivateToken($token, $device_id);
        }

        return $this->json(array('success' => true, 'code' => 'logged_out', 'message' => 'Entitlement session closed.'));
    }

    public function pending() {
        if (!$this->authorizedServer()) {
            return $this->json(array('success' => false, 'code' => 'unauthorized', 'message' => 'Server secret is invalid.'));
        }

        $this->load->model('extension/module/turkuaz_entitlement_api');
        $input = $this->input();
        $project_code = $this->projectCode($input);
        $customer_id = isset($input['customer_id']) ? (int)$input['customer_id'] : 0;

        if ($customer_id <= 0) {
            return $this->json(array('success' => false, 'code' => 'missing_customer', 'message' => 'customer_id is required.'));
        }

        return $this->json(array(
            'success' => true,
            'code' => 'ok',
            'project_code' => $project_code,
            'entitlements' => $this->model_extension_module_turkuaz_entitlement_api->pendingGameEntitlements($project_code, $customer_id)
        ));
    }

    public function deliver() {
        if (!$this->authorizedServer()) {
            return $this->json(array('success' => false, 'code' => 'unauthorized', 'message' => 'Server secret is invalid.'));
        }

        $this->load->model('extension/module/turkuaz_entitlement_api');
        $input = $this->input();
        $project_code = $this->projectCode($input);
        $entitlement_id = isset($input['entitlement_id']) ? (int)$input['entitlement_id'] : 0;
        $game_account_id = isset($input['game_account_id']) ? trim($input['game_account_id']) : '';
        $character_id = isset($input['character_id']) ? trim($input['character_id']) : '';

        if ($entitlement_id <= 0 || $game_account_id === '') {
            return $this->json(array('success' => false, 'code' => 'missing_delivery_input', 'message' => 'entitlement_id and game_account_id are required.'));
        }

        $ok = $this->model_extension_module_turkuaz_entitlement_api->markDelivered($entitlement_id, $project_code, $game_account_id, $character_id);
        return $this->json(array('success' => $ok, 'code' => $ok ? 'delivered' : 'not_found', 'message' => $ok ? 'Entitlement delivered.' : 'Entitlement not found.'));
    }

    private function successPayload($email, $entitlement, $token, $refresh_token) {
        $edition = 'community';
        if (isset($entitlement['entitlement_type']) && $entitlement['entitlement_type'] === 'SOFTWARE_LICENSE') {
            $edition = 'pro';
        }

        $payload = array(
            'success' => true,
            'code' => 'ok',
            'message' => 'Entitlement active.',
            'edition' => $edition,
            'license_status' => 'active',
            'customer_email' => $email,
            'store_id' => isset($entitlement['store_id']) ? (int)$entitlement['store_id'] : (int)$this->config->get('config_store_id'),
            'project_code' => isset($entitlement['project_code']) ? $entitlement['project_code'] : self::DEFAULT_PROJECT_CODE,
            'entitlement_type' => isset($entitlement['entitlement_type']) ? $entitlement['entitlement_type'] : '',
            'entitlement_code' => isset($entitlement['entitlement_code']) ? $entitlement['entitlement_code'] : '',
            'offline_grace_days' => max(1, (int)$this->config->get('module_turkuaz_entitlement_api_offline_grace_days')),
            'expires_at' => isset($entitlement['expires_at']) && $entitlement['expires_at'] ? gmdate('c', strtotime($entitlement['expires_at'])) : gmdate('c', strtotime('+1 year'))
        );
        if ($token) {
            $payload['token'] = $token;
        }
        if ($refresh_token) {
            $payload['refresh_token'] = $refresh_token;
        }
        return $payload;
    }

    private function projectMap() {
        $this->load->model('extension/module/turkuaz_entitlement_api');
        return $this->model_extension_module_turkuaz_entitlement_api->parseProjectMap(
            $this->config->get('module_turkuaz_entitlement_api_project_map'),
            max(1, (int)$this->config->get('module_turkuaz_entitlement_api_default_device_limit'))
        );
    }

    private function enabled() {
        return (bool)$this->config->get('module_turkuaz_entitlement_api_status');
    }

    private function projectCode($input) {
        if (isset($input['project_code']) && trim($input['project_code']) !== '') {
            return strtoupper(trim($input['project_code']));
        }
        return self::DEFAULT_PROJECT_CODE;
    }

    private function authorizedServer() {
        $secret = (string)$this->config->get('module_turkuaz_entitlement_api_secret');
        if ($secret === '') {
            return false;
        }
        $input = $this->input();
        $given = isset($input['api_secret']) ? (string)$input['api_secret'] : '';
        if ($given === '' && isset($this->request->server['HTTP_X_TURKUAZ_API_SECRET'])) {
            $given = (string)$this->request->server['HTTP_X_TURKUAZ_API_SECRET'];
        }
        return function_exists('hash_equals') ? hash_equals($secret, $given) : $secret === $given;
    }

    private function input() {
        $raw = file_get_contents('php://input');
        $json = json_decode($raw, true);
        if (is_array($json)) {
            return $json;
        }
        return $this->request->post;
    }

    private function json($payload) {
        $this->response->addHeader('Content-Type: application/json; charset=utf-8');
        $this->response->setOutput(json_encode($payload));
    }

    private function bearerToken() {
        $header = '';
        if (isset($this->request->server['HTTP_AUTHORIZATION'])) {
            $header = $this->request->server['HTTP_AUTHORIZATION'];
        } elseif (isset($this->request->server['REDIRECT_HTTP_AUTHORIZATION'])) {
            $header = $this->request->server['REDIRECT_HTTP_AUTHORIZATION'];
        }
        if (stripos($header, 'Bearer ') === 0) {
            return trim(substr($header, 7));
        }
        return '';
    }

    private function token() {
        if (function_exists('random_bytes')) {
            return bin2hex(random_bytes(32));
        }
        return sha1(uniqid(mt_rand(), true)) . sha1(uniqid(mt_rand(), true));
    }
}
