# 📄 Dosya Yolu: apps/desktop/CHANGELOG.md
# 📌 Amac: UniZip Community surum degisikliklerini listelemek
# 📌 Modul - FileType
# Version: 0.3.1
# Aciklama: Public runtime bootstrap, lisans guard, feature gate, otomatik guncelleme ve release CI degisiklikleri
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

## v0.3.1

- ZIP extraction symlink ve unzip budget dogrulamasi eklendi.
- Cikarma sirasinda gecici dosyaya yazip basariliysa hedefe tasima uygulandi.

## v0.3.0

- `DesktopRuntimeServices` public runtime service bundle eklendi.
- `MainApp.launch(...)` ile edition-neutral runtime injection eklendi.
- Community main entrypoint default Community policy ile geriye uyumlu korundu.
- Pro/private dagitimlar public contract uzerinden kendi update source policy implementasyonunu enjekte edebilir.
- Runtime bootstrap contract regression testleri eklendi.

## v0.2.0

- `UpdateSourcePolicyService` public extension portu eklendi.
- Community update source policy implementasyonu eklendi.
- `UpdateSource` public contract modeli eklendi.
- Kanal bazli manifest URL template destegi eklendi.
- Exact stable manifest davranisi geriye uyumlu korundu.
- Alternatif kanal icin eksik template token fail-closed reddedilir.
- Update download boyut ve SHA-256 dogrulama akisi degistirilmeden korundu.
- Public port ve template regression testleri eklendi.

## v0.1.61

- Acilista arka planda lisans validate akisi eklendi.
- Lisans sunucusuna ulasilamazsa offline grace suresi uygulanir.
- Community/Pro ozellikleri icin `FeatureGateService` eklendi.
- Mevcut Community ozellikleri kilitlenmedi.
- Acilista stable kanal otomatik guncelleme kontrolu eklendi.
- `Yardim > Guncellemeleri Denetle` manuel kontrolu eklendi.
- Update manifest HTTPS zorunlulugu eklendi.
- Paket indirme sonrasi dosya boyutu ve SHA-256 dogrulamasi eklendi.
- Dogrulanan paketler `userdata/updates` altina kaydedilir.
- Surum karsilastirma ve manifest dogrulama JUnit testleri eklendi.
- CI workflow `mvn clean verify` ve test raporu artifact akisina guncellendi.
- Tag/workflow ile portable ZIP ve `update.yml` ureten release pipeline eklendi.
- `scripts/generate-update-manifest.py` eklendi.
- `docs/v0.1.61/license-guard-auto-update.md` eklendi.

## v0.1.60

- Ust bara Community/Pro ve lisans durumu rozeti eklendi.
- Turkuaz Entitlement API istemcisi ve OpenCart token akisi tamamlandi.
- ArchiveEngine Core, capability sistemi ve GitHub Actions CI altyapisi eklendi.
