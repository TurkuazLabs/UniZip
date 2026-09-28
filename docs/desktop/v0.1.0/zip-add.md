# 📄 Dosya Yolu: docs/v0.1.0/zip-add.md
# 📌 Amac: ZIP icine dosya veya klasor ekleme akislarini belgelemek
# 📌 Modul - FileType
# Version: 0.1.23
# Aciklama: Add butonunun gecici ZIP uzerinden guvenli yeniden yazma mantigini aciklar

Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language

## Add akisi

v0.1.23 ile `Add` butonu gercek calisir hale getirildi.

Akis:

```text
MainFrame Add butonu
ArchiveController.addToArchive()
ArchiveService.addToZip()
JavaZipTool.addToZip()
```

## Neden ZIP yeniden yaziliyor?

`java.util.zip` mevcut ZIP dosyasina guvenli append yapmaz.

Bu nedenle UniZip:

```text
1. Mevcut ZIP'i gecici ZIP'e kopyalar
2. Yeni dosya/klasorleri ekler
3. Basarili olursa eski ZIP'i gecici ZIP ile degistirir
4. Hata olursa gecici ZIP silinir, eski ZIP korunur
```

## Cakisan dosya adlari

Ayni entry adi varsa yeni dosya otomatik su sekilde adlandirilir:

```text
dosya.txt
dosya (1).txt
dosya (2).txt
```
