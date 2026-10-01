# 📄 Dosya Yolu: CHANGELOG.md
# 📌 Amac: UniZip monorepo seviyesindeki yapisal degisiklikleri kaydetmek
# 📌 Modul - FileType
# Version: 0.3.0
# Aciklama: Desktop public runtime bootstrap ve monorepo yapisal degisikliklerini kaydeder
Bagimli Oldugu Katman: Config | Tool

# Changelog

## 0.3.0-desktop-runtime-bootstrap

- `DesktopRuntimeServices` public composition modeli eklendi.
- `MainApp.launch(args, runtimeServices)` public edition-neutral bootstrap hook eklendi.
- Community `main()` ayni default `CommunityUpdateSourcePolicyService` davranisini korur.
- Private/harici edition dagitimlari public update source policy portunu runtime'da enjekte edebilir.
- Community repository Pro kaynak koduna bagimli hale gelmedi.
- Desktop release version 0.3.0'a cikarildi.

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
