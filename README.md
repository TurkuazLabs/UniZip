# 📄 Dosya Yolu: README.md
# 📌 Amac: UniZip Community monorepo yapisini, edition sinirlarini ve gelistirme komutlarini aciklamak
# 📌 Modul - FileType
# Version: 0.3.0
# Aciklama: Desktop public runtime bootstrap, OpenCart entegrasyonu, Community lisansi ve Pro ayrimini tanimlar
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

# UniZip Community Monorepo

Bu public repository **UniZip Community Edition** kaynak kodunu barindirir.

- `apps/desktop`: UniZip Java 21 masaustu uygulamasi
- `extensions/opencart/turkuaz-entitlement-api`: OpenCart 3.x entitlement entegrasyonunun public Community kaynaklari
- `docs`: mimari, surum ve gelistirme dokumanlari
- `scripts`: build, CI, paketleme ve release araclari
- Public update source policy portu Community/Pro kanal ayrimini compile-time contract ile destekler
- `DesktopRuntimeServices` ve `MainApp.launch(...)` harici edition dagitimlarina public bootstrap hook saglar

## Community ve Pro

**Community Edition**
- Bu public repoda bulunan TurkuazLabs kaynaklari Apache License 2.0 altindadir.
- Kaynak kod kullanilabilir, degistirilebilir ve Apache-2.0 kosullarina uygun olarak dagitilabilir.
- Bu repoda bulunan OpenCart entitlement entegrasyonu da Community kaynak agacinin parcasidir.

**Pro Edition**
- Pro kaynak kodu bu public repoda bulunmaz.
- Pro moduller, ticari servisler, ozel entegrasyonlar ve Pro'ya ozel dagitimlar ayri/private kaynaklar veya paketler uzerinden saglanir.
- Pro urunlerine erisim ayri ticari lisans veya abonelik kosullarina tabidir.
- Community lisansi, ayri dagitilan Pro kaynak kodu veya Pro servisleri icin otomatik lisans hakki vermez.

Ayrintili edition sinirlari icin `EDITIONS.md`, Community lisansi icin `LICENSE` dosyasina bakin.

## Klasor Yapisi

```text
apps/desktop/
  UniZip Java masaustu uygulamasi

extensions/opencart/turkuaz-entitlement-api/
  OpenCart 3.x Community entitlement entegrasyonu

docs/desktop/
  UniZip surum ve mimari dokumanlari

docs/repository-layout.md
  Monorepo kurallari

scripts/
  Local CI, release manifest ve OCMOD paketleme araclari

releases/
  Guncelleme manifest ornekleri
```

## Desktop Build

```bash
mvn clean verify
```

veya:

```bash
mvn -f apps/desktop/pom.xml clean verify
```

## OpenCart OCMOD Paketi

```bash
python scripts/build-opencart-extension.py
```

Olusan paket:

```text
dist/turkuaz_entitlement_api_v0.2.0_oc3.ocmod.zip
```

## Tum Kontroller

Windows:

```bat
scripts\build-all.bat
```

Linux/macOS:

```bash
scripts/build-all.sh
```

## GitHub ve Gitea

Ayni local repository icin iki remote kullanilabilir:

```bash
git remote add github GITHUB_REPOSITORY_URL
git remote add gitea GITEA_REPOSITORY_URL
git push github main
git push gitea main
```

Hazir yardimci scriptler:

```text
scripts/configure-dual-remotes.bat
scripts/configure-dual-remotes.sh
```

## Surumler

- UniZip Desktop: `0.3.0`
- Turkuaz Entitlement API: `0.2.0`
