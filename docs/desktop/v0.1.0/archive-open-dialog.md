# 📄 Dosya Yolu: docs/v0.1.0/archive-open-dialog.md
# 📌 Amac: ZIP dosyasi secme ekranindaki hizli erisim davranisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.20
# Aciklama: sol kisayol paneline Indirilenler eklenmesini ve baslangic klasorunu aciklar

Bagimli Oldugu Katman: Controller | Tool | View | Language

## ZIP dosyasi secme ekrani

v0.1.20 ile `ArchiveOpenDialog` eklendi.

Sol kisayollar:

```text
Masaustu
Belgeler
Indirilenler
```

Ilk acilis klasoru:

```text
Kullanici/Downloads
```

Turkce Windows klasor adi bulunursa:

```text
Kullanici/İndirilenler
```

veya:

```text
Kullanici/Indirilenler
```

kullanilir.

## Katman

```text
Controller
  ArchiveController.openArchive()

View
  ArchiveOpenDialog

Tool
  UserDirectoryTool

Model
  QuickAccessLocationModel
```
