# 📄 Dosya Yolu: docs/repository-layout.md
# 📌 Amac: UniZip monorepo klasor sinirlarini ve katkida bulunma kurallarini tanimlamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Desktop, OpenCart, dokuman, script ve release katmanlarini ayirir
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

# Repository Yapisi

## `apps/desktop`

Java 21 ve Maven tabanli UniZip masaustu uygulamasidir. Controller, Service, Repo/Model, Tool, View ve Language sinirlari bu modul icinde korunur.

## `extensions/opencart/turkuaz-entitlement-api`

OpenCart 3.x OCMOD kaynak paketidir. `upload` klasoru OpenCart kokune kopyalanacak yapidadir. `install.xml` OCMOD tanimidir.

## `docs/desktop`

UniZip Desktop surum dokumanlari ve gecmis teknik kararlaridir.

## `scripts`

Build, test, OCMOD paketleme, update manifest ve remote ayarlama araclaridir.

## `dist`

Build sonucunda uretilir ve Git'e alinmaz.

## Surumleme

Desktop ve OpenCart eklentisi bagimsiz surumlenir:

```text
Desktop: apps/desktop/pom.xml + version.txt
OpenCart: extensions/opencart/turkuaz-entitlement-api/version.txt + install.xml
```
