# 📄 Dosya Yolu: docs/v0.1.0/architecture.md
# 📌 Amac: UniZip uygulama mimarisini katmanlara ayirmak
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: Controller, Service, Repo, Tool, View ve Language ayrimi

Bagimli Oldugu Katman: Controller | Service | Repo | Tool | View | Language

# Architecture

UniZip mimarisi sade ve genisletilebilir kalacak sekilde ayrilir.

## Controller

UI olaylarini alir ve Service katmanini cagirir. Is kurali yazmaz.

## Service

Is kurallari burada bulunur. Arsiv islemi, guvenli cikarma, format secimi, log yonetimi ve tema/dil akisi burada koordine edilir.

## Repo/Model

Ayarlar, gecmis kayitlari, operasyon modelleri ve basit kalici veri bu katmandadir.

## Tool

Dis dunya ve teknik adaptorler bu katmandadir.

Ornekler:

- JavaZipTool
- SafeExtractTool
- ThemeYamlTool
- LanguageYamlTool
- ChecksumTool

## View

Swing ekranlari ve UI componentleri bu katmandadir. View icinde is kurali yoktur.

## Language

Dil dosyalari ve ceviri anahtarlari bu katmandadir.

## Kural

```text
Controller -> Service -> Repo/Model -> Tool -> View -> Language
```

Ters bagimlilik yapilmaz. Tool, View'i bilmez. Repo, View'i bilmez. Controller sadece Service cagirir.
