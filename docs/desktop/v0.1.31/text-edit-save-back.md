# 📄 Dosya Yolu: docs/v0.1.31/text-edit-save-back.md
# 📌 Amac: ZIP icindeki metin dosyasini editor ile duzenleyip geri kaydetme akisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.31
# Aciklama: cift tikla editor acma, degisiklik algilama ve ZIP guncelleme tasarimi

Bagimli Oldugu Katman: Controller | Service | Tool | View | Language | Config

## Hedef
ZIP icindeki desteklenen metin dosyasina cift tiklaninca dosya gecici klasore cikarilir, ayarlanan editor ile acilir, editor kapaninca degisiklik varsa ayni ZIP girdisi guncellenir.

## Varsayilan
- `edit.open_text_on_double_click: true`
- `edit.text_editor_command: "notepad.exe"`

## Guvenlik
- Entry yolu `../` ve absolute path icermemelidir.
- ZIP dogrudan yerinde degistirilmez; gecici ZIP uzerinden yeniden yazilir.
- Degisiklik SHA-256 ile kontrol edilir.
- Degisiklik yoksa ZIP tekrar yazilmaz.

## Desteklenen baslangic uzantilari
`txt`, `md`, `csv`, `log`, `ini`, `conf`, `cfg`, `yml`, `yaml`, `json`, `xml`, `html`, `css`, `js`, `java`, `py`, `sql`.
