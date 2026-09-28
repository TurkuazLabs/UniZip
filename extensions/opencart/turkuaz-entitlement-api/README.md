# 📄 Dosya Yolu: extensions/opencart/turkuaz-entitlement-api/README.md
# 📌 Amac: Turkuaz Entitlement API OpenCart 3.x eklentisini aciklamak
# 📌 Modul - FileType
# Version: 0.2.0
# Aciklama: Multi-store UniZip lisans ve Ragnar item satis altyapisinin kurulum notlari
Bagimli Oldugu Katman: Config | Controller | Service | Repo/Model

# Turkuaz Entitlement API v0.2.0

Bu eklenti OpenCart 3.x icin genel hak/entitlement API saglar.

Desteklenen senaryolar:

- UniZip yazilim lisansi
- JHoster/GGuard gibi ilerideki yazilim lisanslari
- Ragnar item / premium / coin satislari
- Multi-store ayrimi
- Project code ayrimi

Ornek subdomain yapisi:

```text
unizip.turkuaz.com -> store_id 1 -> project_code UNIZIP
ragnar.turkuaz.com -> store_id 2 -> project_code RAGNAR
```

## Kurulum

1. OpenCart admin paneline gir.
2. Extensions > Installer ile `turkuaz_entitlement_api_v0.2.0_oc3.ocmod.zip` yukle.
3. Extensions > Modifications > Refresh yap.
4. Extensions > Extensions > Modules > Turkuaz Entitlement API kurulumu yap.
5. Modul ayarlarindan durumu aktif et.
6. Product map alanina urun haklarini ekle.

## Product map formati

```text
product_id|project_code|entitlement_type|entitlement_code|device_limit|quantity|duration_days
```

Ornek UniZip lisans:

```text
12|UNIZIP|SOFTWARE_LICENSE|UNIZIP_PRO|1|1|365
```

Ornek Ragnar item:

```text
34|RAGNAR|GAME_ITEM|GOLD_1000|0|1000|0
```

Ornek Ragnar premium:

```text
35|RAGNAR|GAME_PREMIUM|PREMIUM_30D|0|1|30
```

## Endpointler

```text
/index.php?route=extension/module/turkuaz_entitlement_api/login
/index.php?route=extension/module/turkuaz_entitlement_api/validate
/index.php?route=extension/module/turkuaz_entitlement_api/logout
/index.php?route=extension/module/turkuaz_entitlement_api/pending
/index.php?route=extension/module/turkuaz_entitlement_api/deliver
```

UniZip desktop login isteginde `project_code=UNIZIP` gonderir.

Ragnar sunucusu pending/deliver endpointlerini `X-Turkuaz-Api-Secret` ile kullanir.
