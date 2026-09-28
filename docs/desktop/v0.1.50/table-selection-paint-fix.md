# 📄 Dosya Yolu: docs/v0.1.53/table-selection-paint-fix.md
# 📌 Amac: Add sonrasi detay tablosunda secili satirin renksiz gorunmesini duzeltmek
# 📌 Modul - FileType
# Version: 0.1.53
# Aciklama: JTable selection renderer ve row selection repaint bugfix notu

Bagimli Oldugu Katman: View

v0.1.53, ZIP icine dosya eklendikten sonra secili oge mantiken secili olsa bile detay tablosunda renkli gorunmemesi sorununu duzeltir.

Sebep:

- Detay tablo renderer'i secim rengini `detailsSelectFullRow` ayarina bagli yorumluyordu.
- Bu nedenle secim modeli dogru olsa bile bazi kolonlar veya tiklanan alan renksiz gorunebiliyordu.
- Add sonrasi refresh ile secim geri yuklense bile gorunur boya net garanti edilmiyordu.

Duzeltme:

- Renderer satir secimini JTable selection modelinden okur.
- Secili satir tum kolonlarda tema selection rengiyle boyanir.
- Tablo her zaman row selection modunda kalir.
- Programatik secim sonrasi repaint cagirilir.
