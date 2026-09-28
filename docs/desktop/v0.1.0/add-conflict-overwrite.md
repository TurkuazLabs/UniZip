# 📄 Dosya Yolu: docs/v0.1.0/add-conflict-overwrite.md
# 📌 Amac: ZIP icine ekleme sirasinda ayni isim cakismasi davranisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.29
# Aciklama: otomatik yeniden adlandirma yerine uzerine yazma onayi alma akislarini aciklar

Bagimli Oldugu Katman: View | Controller | Service | Tool | Language

## Karar

v0.1.29 ile ayni isimli dosya/klasor eklenirken otomatik `dosya (1).txt` adlandirmasi yerine kullaniciya onay sorulur.

## Akis

```text
ArchiveController.addPathsToArchive()
ArchiveService.findAddConflicts()
JavaZipTool.findAddConflicts()
JOptionPane YES/NO
JavaZipTool.addToZip(overwriteExisting=true)
```

## Davranis

```text
Evet: ZIP icindeki ayni isimli eski girdi atlanir, yeni girdi ayni adla yazilir.
Hayir: ekleme iptal edilir.
```
