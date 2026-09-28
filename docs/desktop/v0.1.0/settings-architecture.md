# 📄 Dosya Yolu: docs/v0.1.0/settings-architecture.md
# 📌 Amac: UniZip ayar, dil ve tema mimarisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.19
# Aciklama: SettingsDialog, SettingsService ve SettingsRepository akislarini aciklar

Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

## Ayar akis karari

v0.1.18 ile yeni ozelliklere gecmeden once ayar altyapisi toparlandi.

## Katmanlar

```text
View
  SettingsDialog

Service
  SettingsService
  ThemeService
  LanguageService

Repo/Model
  SettingsRepository
  ThemeOptionModel
  LanguageOptionModel

Config
  userdata/settings.yml
  apps/desktop/src/main/resources/config/default-settings.yml

Language
  apps/desktop/src/main/resources/language/tr.yml
  apps/desktop/src/main/resources/language/en.yml

Theme
  apps/desktop/src/main/resources/themes/Turkuaz.yml
```

## Kullanici ayar dosyasi

Uygulama calisma klasorunde su dosya kullanilir:

```text
userdata/settings.yml
```

Icerik:

```yaml
ui:
  theme: "Turkuaz"
  language: "tr"
```

## Not

Tema ve dil degisikligi kaydedilir. Arayuzun tum parcalarinda tam uygulanmasi icin uygulamayi yeniden baslatmak gerekir.
