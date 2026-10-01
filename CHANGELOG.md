# 📄 Dosya Yolu: CHANGELOG.md
# 📌 Amac: UniZip monorepo seviyesindeki yapisal degisiklikleri kaydetmek
# 📌 Modul - FileType
# Version: 0.2.0
# Aciklama: Desktop ve OpenCart paketlerinin tek repository altinda birlestirilmesi
Bagimli Oldugu Katman: Config | Tool

# Changelog

## 0.2.0-update-source-port

- Desktop update kanal karari `UpdateSourcePolicyService` public portuna ayrildi.
- Community varsayilan implementasyonu `CommunityUpdateSourcePolicyService` olarak eklendi.
- Kanal bazli manifest URL template contract'i eklendi.
- Community build Pro kaynagina bagimli olmadan harici edition implementasyonlarini destekler.
- SHA-256, boyut ve HTTPS guvenlik kontrolleri Community `UpdateService` icinde kalir.

## 0.1.61-monorepo

- UniZip Desktop `apps/desktop` altina tasindi.
- Turkuaz Entitlement API `extensions/opencart/turkuaz-entitlement-api` altina eklendi.
- Root Maven aggregator eklendi.
- GitHub ve Gitea CI yapilari eklendi.
- OpenCart OCMOD paketleme scripti eklendi.
- Iki remote icin kurulum scriptleri eklendi.
- Runtime `userdata` icerigi repository disina alindi; ornek config altina tasindi.

Modul bazli degisiklikler:

- `apps/desktop/CHANGELOG.md`
- `extensions/opencart/turkuaz-entitlement-api/CHANGELOG.md`
