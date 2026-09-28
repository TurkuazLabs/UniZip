# 📄 Dosya Yolu: docs/v0.1.59/turkuaz-entitlement-client.md
# 📌 Amac: UniZip Desktop lisans istemcisini Turkuaz Entitlement API yapisina baglamak
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: project_code=UNIZIP, multi-store endpoint ve token tabanli lisans uyumu
Bagimli Oldugu Katman: Service | Tool | Repo/Model | Config

# UniZip v0.1.59 Turkuaz Entitlement Client

Bu surumde UniZip lisans istemcisi dar `unizip_license_api` endpointinden genel `turkuaz_entitlement_api` endpointine tasindi.

Desktop istemci her login, validate ve logout isteginde sabit olarak su proje kodunu gonderir:

```text
project_code=UNIZIP
```

Varsayilan API adresi:

```text
https://unizip.turkuaz.com/index.php?route=extension/module/turkuaz_entitlement_api
```

OpenCart multi-store tarafinda `unizip.turkuaz.com` store context'i UniZip magazasini temsil eder. Eklenti urunu `project_code=UNIZIP` olan hak kaydiyla eslestirir.
