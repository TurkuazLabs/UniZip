# 📄 Dosya Yolu: docs/v0.1.0/zip-test.md
# 📌 Amac: ZIP test butonu davranisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.22
# Aciklama: Test butonunun ZIP arşivini okuyup doğrulama yapmasını aciklar

Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language

## Test akisi

v0.1.22 ile `Test` butonu gerçek çalışır hale getirildi.

Akis:

```text
MainFrame Test butonu
ArchiveController.testArchive()
ArchiveService.testZip()
JavaZipTool.testZip()
```

## Kontroller

```text
ZIP dosyasi var mi
Uzanti .zip mi
ZIP girdileri okunabiliyor mu
Her dosya entry'si son byte'a kadar okunabiliyor mu
Zip Slip gibi guvenli olmayan entry yolu var mi
```

## Sonuc

Basarili test sonucunda durum cubugunda ve logda ozet mesaj gosterilir:

```text
ZIP test basarili. Girdi: X, Dosya: Y, Klasor: Z, Okunan: N MB
```
