# 📄 Dosya Yolu: docs/v0.1.0/extract-options-dialog.md
# 📌 Amac: UniZip cikarma secenekleri ekraninin konum ve davranisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.16
# Aciklama: folder name alaninin altinda alt klasor secenegi gosterimini aciklar

Bagimli Oldugu Katman: Controller | View | Service | Language

## Cikarma secenekleri ekrani

v0.1.16 ile `JFileChooser` accessory kullanimi kaldirildi.

Yerine `ExtractOptionsDialog` eklendi.

Ekran duzeni:

```text
Folder name: [ hedef klasor yolu ] [ Gozat ]

[✓] ZIP dosyasi adi ile alt klasor olustur

[ Iptal ] [ Extract ]
```

Boylece secenek `Folder name` alaninin altinda gorunur.
