# 📄 Dosya Yolu: docs/v0.1.46/windows-registry-file-association.md
# 📌 Amac: UniZip Windows Registry dosya iliskilendirme akisinin teknik kaydini tutmak
# 📌 Modul - FileType
# Version: 0.1.46
# Aciklama: Sistem sekmesindeki UniZip + butonlarinin HKCU/HKLM Registry yazim davranisini aciklar

Bagimli Oldugu Katman: View | Service | Tool | Config | Language

## Ozet

v0.1.46 ile Sistem sekmesindeki iliskilendirme tablosu sadece gorunum degil, gercek Registry yazimi yapar hale getirildi.

## Eklenen katmanlar

- `FileAssociationScope`
- `FileAssociationStatusModel`
- `FileAssociationService`
- `WindowsRegistryTool`

## Yazilan Registry alanlari

Gecerli kullanici icin:

- `HKCU\Software\Classes\.ext`
- `HKCU\Software\Classes\UniZip.Archive`
- `HKCU\Software\UniZip\Capabilities`
- `HKCU\Software\RegisteredApplications`

Tum kullanicilar icin:

- `HKLM\Software\Classes\.ext`
- `HKLM\Software\Classes\UniZip.Archive`
- `HKLM\Software\UniZip\Capabilities`
- `HKLM\Software\RegisteredApplications`

## Davranis

- Secili satir varsa sadece secili uzantilar islenir.
- Secim yoksa tum arsiv uzantilari islenir.
- Gecerli kullanici butonu HKCU yazar.
- Tum kullanicilar butonu HKLM yazar ve yonetici yetkisi gerektirir.
- Iliskilendirme sonrasi tablo Registry durumunu tekrar okur.
- Windows iliskilendirmesiyle gelen `%1` argumani `MainApp` tarafindan okunur ve ZIP otomatik acilir.
