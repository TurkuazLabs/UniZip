# 📄 Dosya Yolu: docs/v0.1.38/archive-integrity-test.md
# 📌 Amac: UniZip detayli ZIP test ve risk raporu akisinin kapsamını belgelemek
# 📌 Modul - FileType
# Version: 0.1.38
# Aciklama: Test Archive ozelliginin okuma, boyut, tekrar eden girdi ve guvenli yol kontrollerini tanimlar
Bagimli Oldugu Katman: Controller | Service | Tool | View | Language

## Kapsam

v0.1.38 ile Test butonu sadece temel okuma yapmaz; arsiv icin daha detayli bir rapor uretir.

Kontroller:

- Tum ZIP girdileri sirayla okunur.
- Dosya girdilerinin byte icerigi okunarak CRC/stream hatalari yakalanir.
- Klasor ve dosya sayisi ayrilir.
- Toplam okunan byte hesaplanir.
- Tekrar eden entry isimleri uyarilir.
- Zip Slip / path traversal riski olan entry isimleri uyarilir.
- ZIP entry boyutu ile okunan boyut uyusmazsa uyarilir.

## Davranis

- Uyari yoksa sonuc `ZIP test basarili` olarak gosterilir.
- Uyari varsa test tamamlanir ama sonuc mesajinda uyarilar listelenir.
- Kritik okunamayan veya bozuk stream hatalari exception olarak ekrana ve log paneline duser.

## Not

Bu surum ZIP uzerinde degisiklik yapmaz. Sadece okuma ve dogrulama yapar.
