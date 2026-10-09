# 📄 Dosya Yolu: extensions/opencart/turkuaz-entitlement-api/CHANGELOG.md
# 📌 Amac: Turkuaz Entitlement API surum degisikliklerini kaydetmek
# 📌 Modul - FileType
# Version: 0.2.1
# Aciklama: OpenCart 3.x multi-store entitlement API ilk genel surum kaydi
Bagimli Oldugu Katman: Controller | Service | Repo/Model | View | Language | Config

# Changelog

## 0.2.1

- OpenCart 3.x customer login artik standart customer service API kullanir.
- Lisans login ve token validate suresi dolan entitlement'i reddeder.
- Pending game entitlement sorgusu expired kayitlari dislar.

## 0.2.0

- Multi-store ayrimi eklendi.
- `project_code` destekli hak modeli eklendi.
- UniZip yazilim lisansi login/validate/logout endpointleri eklendi.
- Ragnar pending/deliver endpointleri eklendi.
- Product map tabanli entitlement ayari eklendi.
