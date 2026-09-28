# 📄 Dosya Yolu: docs/v0.1.53/add-refresh-selection-fix.md
# 📌 Amac: Add sonrasi liste seciminin kilitli gorunmesi sorununu aciklamak
# 📌 Modul - FileType
# Version: 0.1.53
# Aciklama: tam satir sec ayari ve liste refresh secim modeli bugfix notlari

Bagimli Oldugu Katman: View | Controller

v0.1.53, ZIP icine dosya eklendikten sonra liste seciminin kilitli veya secilemez gorunmesine neden olan gorunum katmani sorununu duzeltir.

Duzeltilen davranislar:

- Detay tablosunda satir secimi artik her zaman aktif kalir.
- `Tam satir sec` ayari artik secim modelini kapatmaz; sadece secili satirin boyanma seklini etkiler.
- Liste yenilenirken olusan ara `null` secim eventleri bastirilir.
- Yenileme bittikten sonra aktif satir tekrar controller/preview tarafina bildirilir.
- Add, overwrite, rename ve editor save-back sonrasi secim modeli tutarli kalir.
