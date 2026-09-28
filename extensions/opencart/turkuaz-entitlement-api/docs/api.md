# 📄 Dosya Yolu: docs/api.md
# 📌 Amac: Turkuaz Entitlement API JSON endpoint sozlesmesini tanimlamak
# 📌 Modul - FileType
# Version: 0.2.0
# Aciklama: UniZip lisans ve Ragnar teslim endpointleri icin request/response dokumu
Bagimli Oldugu Katman: Controller | Tool | Config

# Login

```http
POST /index.php?route=extension/module/turkuaz_entitlement_api/login
Content-Type: application/json
```

```json
{
  "project_code": "UNIZIP",
  "email": "customer@example.com",
  "password": "secret",
  "device_id": "device-hash",
  "device_name": "Windows PC",
  "app_version": "0.1.59"
}
```

Basarili yanit:

```json
{
  "success": true,
  "edition": "pro",
  "license_status": "active",
  "project_code": "UNIZIP",
  "entitlement_type": "SOFTWARE_LICENSE",
  "entitlement_code": "UNIZIP_PRO",
  "token": "...",
  "refresh_token": "..."
}
```

# Validate

```json
{
  "project_code": "UNIZIP",
  "token": "...",
  "device_id": "device-hash"
}
```

# Ragnar pending

```http
POST /index.php?route=extension/module/turkuaz_entitlement_api/pending
X-Turkuaz-Api-Secret: server-secret
```

```json
{
  "project_code": "RAGNAR",
  "customer_id": 10
}
```

# Ragnar deliver

```json
{
  "project_code": "RAGNAR",
  "entitlement_id": 5,
  "game_account_id": "account-1",
  "character_id": "char-1"
}
```
