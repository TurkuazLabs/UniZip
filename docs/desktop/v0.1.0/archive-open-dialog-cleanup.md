# 📄 Dosya Yolu: docs/v0.1.0/archive-open-dialog-cleanup.md
# 📌 Amac: ZIP dosyasi secme ekraninin sadeleştirilmesini belgelemek
# 📌 Modul - FileType
# Version: 0.1.21
# Aciklama: ozel sol panelin kaldirilmasini ve Indirilenler baslangic klasorunun korunmasini aciklar

Bagimli Oldugu Katman: Controller | Tool | View | Language

## Karar

v0.1.20'de JFileChooser yanina eklenen ozel sol panel kaldirildi.

Sebep:

```text
JFileChooser zaten kendi native/Look&Feel sol kisayol panelini olusturuyor.
Ek panel ikinci bir sol alan olusturdugu icin arayuz daginik gorundu.
```

## Yeni davranis

```text
ZIP dosyasi sec ekrani standart JFileChooser ile acilir.
Sol panel JFileChooser'in kendi panelidir.
Ilk acilis klasoru Indirilenler/Downloads olur.
```

## Katman

```text
View
  ArchiveOpenDialog

Tool
  UserDirectoryTool
```
