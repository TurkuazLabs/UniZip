# 📄 Dosya Yolu: docs/v0.1.55/search-clear-button.md
# 📌 Amac: UniZip arama kutusu temizleme butonu entegrasyonunu belgelemek
# 📌 Modul - FileType
# Version: 0.1.56
# Aciklama: arama aktifken gorunen X butonu ve mevcut klasor gorunumune donus akisi
Bagimli Oldugu Katman: View | Language

## Degisiklik

Arama kutusuna kucuk bir X temizleme butonu eklendi.

## Davranis

- Arama bosken X butonu gizlidir.
- Arama aktifken X butonu gorunur.
- X tiklaninca arama metni temizlenir.
- Arama temizlenince mevcut klasor gorunumu geri gelir.
- Arama kutusu focus durumunu korur.

## Etkilenen dosyalar

- apps/desktop/src/main/java/com/unizip/desktop/views/MainFrame.java
- apps/desktop/src/main/resources/language/tr.yml
- apps/desktop/src/main/resources/language/en.yml
