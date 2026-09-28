# 📄 Dosya Yolu: docs/v0.1.59/opencart-license-api-extension.md
# 📌 Amac: OpenCart 3.x UniZip lisans API eklentisinin masaustu uygulama ile iliskisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: OpenCart musteri email/sifre girisi, siparis kontrolu ve cihaz limiti tasarimi
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | Config

Ayrica ayri bir OpenCart 3.x eklenti paketi hazirlandi:

```text
turkuaz_entitlement_api_v0.1.0_oc3.ocmod.zip
```

## Eklenti endpointleri

```text
/index.php?route=extension/module/turkuaz_entitlement_api/login
/index.php?route=extension/module/turkuaz_entitlement_api/validate
/index.php?route=extension/module/turkuaz_entitlement_api/logout
```

## Lisans kontrolu

OpenCart tarafinda musteri email/sifre ile dogrulanir. Sonra musterinin ayarlarda belirtilen UniZip Pro urun ID listesinde aktif siparisi olup olmadigi kontrol edilir.

## Cihaz limiti

`oc_unizip_license_device` tablosunda token hash ve cihaz bilgisi tutulur. Baslangic ayari 1 cihazdir.
