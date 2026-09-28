# 📄 Dosya Yolu: docs/v0.1.59/license-opencart-core.md
# 📌 Amac: UniZip Desktop lisans cekirdegi ve OpenCart API entegrasyonunu belgelemek
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: Email/sifre login, token saklama, cihaz aktivasyonu ve Community/Pro durum modeli
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Config

v0.1.59 ile UniZip icine lisans cekirdegi eklendi.

## Desktop akisi

```text
UniZip Desktop
  -> LicenseDialog
  -> LicenseService
  -> LicenseApiTool
  -> OpenCart License API
```

## Guvenlik karari

OpenCart musteri sifresi yerelde saklanmaz. Sadece basarili giris sonrasi donen token `userdata/license.yml` icinde saklanir.

## Yerel lisans dosyasi

```text
userdata/license.yml
```

Saklanan bilgiler:

```text
api_base_url
customer_email
access_token
refresh_token
device_id
device_name
edition
status
last_checked_at
expires_at
offline_grace_days
```

## Menu

```text
Secenekler > Lisans
```

## Not

Bu surumde Pro ozellik kilidi altyapisi hazirlandi. Hangi ozelliklerin Community/Pro ayrilacagi sonraki surumlerde netlestirilecek.
