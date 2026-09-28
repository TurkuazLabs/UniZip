# 📄 Dosya Yolu: docs/v0.1.0/drag-out-extract.md
# 📌 Amac: ZIP icinden disari surukle-birak akislarini belgelemek
# 📌 Modul - FileType
# Version: 0.1.28
# Aciklama: secili ZIP girdisinin gecici klasore cikarilip Explorer'a FileListTransferable olarak verilmesini aciklar

Bagimli Oldugu Katman: View | Controller | Service | Tool | Language

## Akis

```text
ArchiveListPanel drag source
MainFrame.ArchiveDropTransferHandler.createTransferable()
ArchiveController.prepareDragExport()
ArchiveService.exportZipEntriesToTemp()
JavaZipTool.exportEntriesToDirectory()
```

## Not

Disari suruklenen dosya once sistem gecici klasorune cikarilir. Explorer drop islemi bu gecici dosyayi hedef klasore kopyalar.
