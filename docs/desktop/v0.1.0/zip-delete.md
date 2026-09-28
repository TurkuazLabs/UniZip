# 📄 Dosya Yolu: docs/v0.1.0/zip-delete.md
# 📌 Amac: ZIP icinden secili girdi silme akislarini belgelemek
# 📌 Modul - FileType
# Version: 0.1.26
# Aciklama: Delete butonunun gecici ZIP uzerinden guvenli yeniden yazma mantigini aciklar

Bagimli Oldugu Katman: View | Controller | Service | Tool | Language

## Akis

```text
MainFrame Delete butonu
ArchiveController.deleteSelectedEntry()
ArchiveService.deleteZipEntries()
JavaZipTool.deleteEntries()
```

## Not

Add ve Delete islemlerinden sonra popup mesaj gosterilmez. Durum cubugu ve log guncellenir.
