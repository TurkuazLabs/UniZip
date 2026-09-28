# 📄 Dosya Yolu: docs/v0.1.35/rename-entry.md
# 📌 Amac: ZIP icindeki dosya ve klasor girdilerini yeniden adlandirma akisini belgelemek
# 📌 Modul - FileType
# Version: 0.1.35
# Aciklama: Rename butonu, F2 kisayolu ve guvenli ZIP rewrite kurallari

Bagimli Oldugu Katman: Controller | Service | Tool | View | Language

## Hedef

Secili ZIP girdisi, kullanici ayni klasorde kalirken yeniden adlandirilir.

## Akis

1. Kullanici entry secer.
2. `Rename` butonuna basar veya `F2` kullanir.
3. Controller yeni adi ister.
4. Service yeni adi dogrular.
5. Tool gecici ZIP olusturur.
6. Eski entry adi yeni entry adi ile yazilir.
7. Islem basarili olursa gecici ZIP ana ZIP dosyasinin yerine gecer.
8. View mevcut klasore doner ve yeni entry secili kalir.

## Guvenlik kurallari

- Bos ad kabul edilmez.
- Yeni ad klasor yolu iceremez.
- `..` ve guvenli olmayan path parcalari engellenir.
- Windows icin sorunlu karakterler engellenir.
- Hedef entry ZIP icinde zaten varsa islem durdurulur.

## Not

Klasor rename islemi, klasorun altindaki entry adlarini da ayni kok degisimiyle gunceller.
