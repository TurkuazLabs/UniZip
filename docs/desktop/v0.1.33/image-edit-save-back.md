# 📄 Dosya Yolu: docs/v0.1.33/image-edit-save-back.md
# 📌 Amac: ZIP icindeki resim dosyasini harici editor ile acip kayit sonrasi ZIP icine geri yazma akisina not dusmek
# 📌 Modul - FileType
# Version: 0.1.33
# Aciklama: Paint ve ayarlanabilir resim editoru ile save-back mantigi

Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

## Ozet

v0.1.33 ile ZIP icindeki desteklenen resim dosyalari cift tikla harici editor ile acilabilir. Windows icin varsayilan komut `mspaint.exe` olarak gelir.

## Ayar dosyasi

```yml
# 📄 Dosya Yolu: userdata/settings.yml
# 📌 Amac: UniZip kullanici ayarlarini saklamak
# 📌 Modul - FileType
# Version: 0.1.33
# Aciklama: metin ve resim editoru ayarlari

edit:
  open_image_on_double_click: true
  image_editor_command: "mspaint.exe"
  editable_image_extensions: "png,jpg,jpeg,bmp,gif,tif,tiff,webp,jfif"
```

## Akis

1. Kullanici ZIP icindeki resim entry uzerine cift tiklar.
2. Entry gecici klasore guvenli dosya adi ile cikarilir.
3. SHA-256 ilk deger alinir.
4. `image_editor_command` ile editor acilir ve kapanmasi beklenir.
5. SHA-256 tekrar hesaplanir.
6. Degisiklik varsa `JavaZipTool.replaceEntryFromFile()` ile ayni entry ZIP icinde guncellenir.
7. Degisiklik yoksa ZIP yeniden yazilmaz.

## Not

`Desktop.open()` kullanilmadi, cunku editor kapanisini guvenilir sekilde beklemek zor. Ayarlanabilir komut + `Process.waitFor()` akisi kullanildi.
