# 📄 Dosya Yolu: docs/v0.1.30/search-filter-status.md
# 📌 Amac: UniZip v0.1.30 arama, dosya turu filtreleme ve durum cubugu gelistirmelerini belgelemek
# 📌 Modul - FileType
# Version: 0.1.30
# Aciklama: arama kutusu, dosya turu filtresi, Ctrl+F ve gorunen/toplam sayac davranisi

Bagimli Oldugu Katman: View | Language

# UniZip v0.1.30 Search Filter Status

## Eklenenler

- Ust icerik cubuguna arama kutusu eklendi.
- Dosya turu filtresi eklendi.
- `Ctrl+F` arama kutusuna odaklanir.
- Durum cubugunda gorunen/toplam sayaci gosterilir.
- Filtre bos sonuc verdiginde eski secim temizlenir.

## Dosya Turu Filtreleri

- Tum dosyalar
- Resimler
- Belgeler
- Arsivler
- Diger

## Davranis

Arama, arsiv entry yoluna gore calisir. Klasor icinde arama yapildiginda sadece aktif klasorun gorunen girdileri filtrelenir.

Dosya turu filtresi ve arama ayni anda uygulanir.

## Bilinen Sinir

Klasorler gezinme kaybolmasin diye dosya turu filtresinde gorunmeye devam eder. Bir klasorun icindeki dosya turlerini recursive olarak hesaplama sonraki surume birakildi.
