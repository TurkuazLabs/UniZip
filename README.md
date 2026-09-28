# 📄 Dosya Yolu: README.md
# 📌 Amac: UniZip monorepo yapisini, modulleri ve gelistirme komutlarini aciklamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Desktop uygulamasi ve Turkuaz Entitlement API kaynaklarini tek repoda toplar
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

# UniZip Platform Monorepo

Bu repository iki ana urunu tek kaynak agacinda tutar:

- `apps/desktop`: UniZip Java 21 masaustu uygulamasi
- `extensions/opencart/turkuaz-entitlement-api`: OpenCart 3.x lisans ve entitlement eklentisi

## Klasor Yapisi

```text
apps/desktop/
  UniZip Java masaustu uygulamasi

extensions/opencart/turkuaz-entitlement-api/
  OpenCart 3.x OCMOD kaynaklari

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

- UniZip Desktop: `0.1.61`
- Turkuaz Entitlement API: `0.2.0`
