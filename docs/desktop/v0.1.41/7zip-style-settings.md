# 📄 Dosya Yolu: docs/v0.1.42/7zip-style-settings.md
# 📌 Amac: Gorsellerden alinan 7-Zip ayar mantigini UniZip icine aktarma notlari
# 📌 Modul - FileType
# Version: 0.1.42
# Aciklama: Options ekranindaki Sistem, UniZip, Klasorler, Duzenleyici, Ayarlar, Dil ve Hakkinda sekmelerinin entegrasyon ozeti

Bagimli Oldugu Katman: View | Service | Repo/Model | Config | Language

## Analiz

Gorsellerdeki 7-Zip Options penceresi su ana fikirleri veriyor:

- Dosya iliskilendirme listesi ayri bir Sistem sekmesinde tutuluyor.
- Sag tik menu davranislari ayri bir uygulama/7-Zip sekmesinde tutuluyor.
- Gecici calisma klasoru ayri bir Klasorler sekmesinde seciliyor.
- Goruntuleyici, duzenleyici ve fark araclari ayri Duzenleyici sekmesinde tanimlaniyor.
- Gorunum ve genel tercih bayraklari Ayarlar sekmesinde tutuluyor.
- Dil secimi ve katkilar ayri sekmede gosteriliyor.
- Hakkinda penceresi uygulama versiyonunu ayri gosteriyor.

## UniZip v0.1.42 uygulamasi

- Ayarlar penceresi JTabbedPane ile sekmeli hale getirildi.
- Dosya uzantilari config icinden degistirilebilir oldu.
- Windows dosya iliskisi icin registry yazilmadi; bunun yerine Windows Varsayilan Uygulamalar ekrani aciliyor.
- Calisma klasoru politikasi gercek editor ve drag-out gecici dosya akisine baglandi.
- `..` oge gorunurlugu ve tam satir secimi uygulama davranisina baglandi.
- Diger gorunum/sistem bayraklari config icine alindi ve sonraki surumler icin hazirlandi.
