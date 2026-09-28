# 📄 Dosya Yolu: EDITIONS.md
# 📌 Amac: UniZip Community ve Pro edition kaynak, lisans ve dagitim sinirlarini tanimlamak
# 📌 Modul - FileType
# Version: 1.0.0
# Aciklama: Public Community kodu ile ayri dagitilan ticari Pro bileşenlerini birbirinden ayirir
Bagimli Oldugu Katman: Config

# UniZip Community ve Pro Edition Politikasi

## Community Edition

Bu public repository UniZip Community Edition kaynak agacidir.

- TurkuazLabs tarafindan bu repository icinde yayinlanan kaynak kod ve dokumantasyon, dosya veya ucuncu taraf bileşeni uzerinde farkli bir lisans acikca belirtilmedikce Apache License 2.0 altindadir.
- Community kodu Apache License 2.0 kosullarina uygun olarak kullanilabilir, degistirilebilir ve dagitilabilir.
- Community kodunun ticari bir urunun parcasi olarak kullanilmasi Apache License 2.0 tarafindan yasaklanmaz.
- Ucuncu taraf kutuphane, veri modeli, font, ikon veya diger harici bileşenler kendi lisans kosullarini korur.

## Pro Edition

UniZip Pro Edition Community repository'sinin icinde yer alan ikinci bir lisans modu degildir.

- Pro kaynak kodu public Community repository'sinde tutulmaz.
- Pro moduller, ticari servisler, ozel entegrasyonlar, lisans/entitlement kurallari ve Pro'ya ozel dagitimlar ayri private repository, package veya servisler uzerinden saglanabilir.
- Pro bileşenleri ayri ticari lisans, EULA, abonelik veya sozlesme kosullarina tabidir.
- Apache License 2.0 ile verilen Community haklari, ayri dagitilan Pro kaynak kodu, Pro paketleri veya Pro servisleri icin lisans hakki vermez.
- Pro bileşenlerinin Community koduna baglanmasi veya Community API'lerini kullanmasi, Pro bileşenlerinin lisansini otomatik olarak Apache-2.0 yapmaz.

## Repository Siniri

Public Community repository'sine su icerikler eklenmez:

- Pro kaynak kodu
- Private API anahtarlari ve signing key'leri
- Musteriye ozel gizli entegrasyonlar
- Ticari lisans dosyalari veya private entitlement policy'leri
- Secret, token, credential veya production private endpoint bilgileri

Community ile Pro arasindaki ortak entegrasyon noktalarinda yalnizca public interface, contract veya adaptor tanimlari bulunabilir.

## Marka ve Urun Adlari

Apache License 2.0, TurkuazLabs veya UniZip adlari, logolari ve diger marka/urun kimlikleri icin genel bir marka lisansi vermez. Kaynagin nereden geldigini aciklamak icin gereken makul kullanimlar Apache License 2.0 kosullarina tabidir.

## Lisans Dosyalari

- Community: `LICENSE`
- Attribution: `NOTICE`
- Edition siniri: `EDITIONS.md`
- Pro: Pro dagitimi ile birlikte verilen ayri ticari lisans veya sozlesme
