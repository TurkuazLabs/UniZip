# 📄 Dosya Yolu: docs/v0.1.61/license-guard-auto-update.md
# 📌 Amac: UniZip lisans guard ve otomatik guncelleme mimarisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Acilis validate, offline grace, feature gate, manifest ve SHA-256 indirme akisi
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Config

## Acilis akisi

1. Uygulama UI threadini bloklamadan lisans tokenini online dogrular.
2. Lisans sunucusuna ulasilamazsa son basarili kontrol ve offline grace suresi uygulanir.
3. Otomatik guncelleme aciksa stable manifest HTTPS uzerinden okunur.
4. Yeni surum varsa kullaniciya indirme secenegi gosterilir.
5. Paket indirildikten sonra boyut ve SHA-256 dogrulamasi yapilir.
6. Dogrulanan paket `userdata/updates` altina alinir.

## Manifest yayin adresi

`https://unizip.turkuaz.com/releases/stable/update.yml`

Gercek `update.yml`, `scripts/generate-update-manifest.py` ile release paketinden uretilmelidir.

## Kurulum siniri

v0.1.61 calisan uygulamayi kendisi uzerine yazmaz. Guvenli paketi indirir ve klasoru acar. Inno Setup/jpackage kurulum sistemi tamamlandiginda dogrulanan installer kontrollu sekilde baslatilacaktir.

## Feature gate

Mevcut Community ozellikleri kapatilmadi. `FeatureGateService` gelecekte Pro ozelliklerinin tek merkezden acilip kapatilmasi icin eklendi. Priority update kanali Pro adayi olarak tanimlandi; stable otomatik guncellemeler Community icin aciktir.
