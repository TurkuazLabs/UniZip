# 📄 Dosya Yolu: docs/v0.1.0/drag-drop-add.md
# 📌 Amac: Surukle-birak ile ZIP icine dosya/klasor ekleme akislarini belgelemek
# 📌 Modul - FileType
# Version: 0.1.25
# Aciklama: TransferHandler uzerinden javaFileListFlavor ile dosya alma ve Add altyapisina baglama

Bagimli Oldugu Katman: View | Controller | Service | Tool | Language

## Akis

v0.1.25 ile ana pencereye surukle-birak destegi eklendi.

Desteklenen alanlar:

```text
Ana panel
Arsiv liste paneli
Sol arsiv agaci
Sag onizleme paneli
```

Teknik akis:

```text
MainFrame.ArchiveDropTransferHandler
ArchiveController.addPathsToArchive()
ArchiveService.addToZip()
JavaZipTool.addToZip()
```
