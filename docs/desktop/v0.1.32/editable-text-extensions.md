# 📄 Dosya Yolu: docs/v0.1.32/editable-text-extensions.md
# 📌 Amac: ZIP icindeki metin dosyasi duzenleme uzantilarini ayar dosyasindan yonetmek
# 📌 Modul - FileType
# Version: 0.1.32
# Aciklama: editable_text_extensions ayari ile desteklenen metin dosya turlerini genisletme akisi

Bagimli Oldugu Katman: Config | Repo/Model | Service | View

## Amac

v0.1.31 icinde metin editoru destegi sabit Java listesi ile calisiyordu. v0.1.32 ile bu liste `userdata/settings.yml` icindeki `edit.editable_text_extensions` ayarina tasindi.

## Ayar

```yml
# 📄 Dosya Yolu: userdata/settings.yml
# 📌 Amac: UniZip kullanici ayarlarini saklamak
# 📌 Modul - FileType
# Version: 0.1.32
# Aciklama: aktif tema, dil, metin editoru ve duzenlenebilir metin uzantilari

# Bagimli Oldugu Katman: Config

edit:
  open_text_on_double_click: true
  text_editor_command: "notepad.exe"
  editable_text_extensions: "txt,md,csv,log,ini,conf,cfg,yml,yaml,json,xml,html,css,js,java,py,sql,php"
```

## Kural

- Uzantilar virgul, bosluk veya noktali virgul ile ayrilabilir.
- Basa nokta koymak zorunlu degildir.
- `.php` ve `php` ayni kabul edilir.
- `blade.php` gibi cok parcali uzantilar desteklenir.
- Bos liste kaydedilemez.

## Katman karari

- `SettingsRepository` ayari okur ve yazar.
- `SettingsService` uzantilari normalize eder.
- `ArchiveService` dosyanin duzenlenebilir olup olmadigini `SettingsService` uzerinden kontrol eder.
- `SettingsDialog` kullaniciya listeyi ister UI'dan ister dosyadan degistirme imkani verir.
