# 📄 Dosya Yolu: docs/v0.1.0/theme-id-migration.md
# 📌 Amac: UniZip tema id adlandirma degisikligini belgelemek
# 📌 Modul - FileType
# Version: 0.1.19
# Aciklama: bandizip-classic teknik id'sinin Turkuaz olarak sadeleştirilmesini aciklar

Bagimli Oldugu Katman: Service | Repo/Model | Config | View | Language

## Karar

Eski teknik tema id'si:

```yaml
theme: "bandizip-classic"
```

yerine sade isim kullanilir:

```yaml
theme: "Turkuaz"
```

## Geriye uyumluluk

Eski `userdata/settings.yml` icinde `bandizip-classic` varsa `ThemeService` bunu otomatik olarak `Turkuaz` temasina yonlendirir.

## Tema dosyalari

Ana tema dosyasi:

```text
apps/desktop/src/main/resources/themes/Turkuaz.yml
```

Geriye uyumluluk icin eski dosya korunur:

```text
apps/desktop/src/main/resources/themes/bandizip-classic.yml
```
