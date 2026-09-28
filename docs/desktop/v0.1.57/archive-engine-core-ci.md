# 📄 Dosya Yolu: docs/v0.1.57/archive-engine-core-ci.md
# 📌 Amac: UniZip ArchiveEngine Core ve CI test sistemini aciklamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Format engine, capability, service guard ve GitHub Actions test tasarimi

Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Config

## Hedef

v0.1.57 surumunde ZIP disi formatlara gecmeden once motor mimarisi kuruldu.

Program artik arsiv formatini tespit edip uygun engine secimi yapabilecek sekilde tasarlandi.

## Yeni ana kavramlar

- ArchiveFormat
- ArchiveCapabilities
- ArchiveSessionState
- ArchiveEngine
- ArchiveEngineProvider
- ArchiveEngineRegistry
- ArchiveFormatDetector
- ArchiveCommandGuard
- ZipArchiveEngine
- UnsupportedArchiveEngine

## Neden gerekli

ZIP tam destekli olabilir ama 7z veya RAR sadece listeleme/cikarma destekleyebilir.

Bu yuzden butonlar format adina gore degil, engine yeteneklerine gore aktif/pasif olmali.

## Akis

```text
Kullanici arsiv acar
  -> ArchiveFormatDetector imza ve uzanti kontrolu yapar
  -> ArchiveEngineRegistry uygun engine bulur
  -> Engine capability verir
  -> ArchiveSessionState secim/read-only durumu ile birlestirir
  -> MainFrame butonlari aktif/pasif eder
  -> ArchiveCommandGuard service seviyesinde son kontrolu yapar
```

## GitHub katkisi modeli

Yeni format destegi icin katkida bulunan kisi sadece kod yazmamalidir.

Eklenmesi gerekenler:

- Engine sinifi
- Engine provider kaydi
- Capability tanimi
- Format detector imza testi
- List/extract testi
- Desteklenmeyen islemler icin guard testi
- README format destek tablosu guncellemesi

## CI

`.github/workflows/ci.yml` eklendi.

Pipeline sunlari yapar:

- Java 21 kurar
- Maven dependency cache kullanir
- `mvn clean test` calistirir
- `mvn package` calistirir
- Olusan jar dosyasini artifact olarak yukler

## Local test

Windows:

```bat
scripts\run-ci-local.bat
```

Linux/macOS:

```sh
scripts/run-ci-local.sh
```
