# 📄 Dosya Yolu: docs/v0.1.54/search-whole-archive.md
# 📌 Amac: UniZip arama kutusunun tum ZIP icerigini taramasini aciklamak
# 📌 Modul - FileType
# Version: 0.1.56
# Aciklama: mevcut klasorle sinirli search davranisini global ZIP search davranisina cevirir

Bagimli Oldugu Katman: View

## Ozet

v0.1.54 ile arama kutusu sadece acik klasordeki gorunen satirlari filtrelemez. Arama metni girildiginde tum ZIP entry listesi taranir.

## Davranis

- Arama bos ise normal klasor gorunumu kullanilir.
- Arama dolu ise tum ZIP entry adlari icinde arama yapilir.
- Arama sonucunda tam ZIP yolu gosterilir.
- Dosya turu filtresi arama sonucuna da uygulanir.
- Arama temizlenince aktif klasor gorunumu geri gelir.
