# 📄 Dosya Yolu: docs/v0.1.46/association-display-unizip-only.md
# 📌 Amac: Sistem sekmesindeki iliskilendirme tablosunda UniZip disi uygulama adlarini gizlemek
# 📌 Modul - FileType
# Version: 0.1.46
# Aciklama: Registry'den gelen 7-Zip/CABFolder gibi degerlerin UniZip kolonu icinde gosterilmesini engeller

Bagimli Oldugu Katman: Service | Tool | View | Language | Config

## Duzeltme

Sistem sekmesindeki iliskilendirme tablosu artik mevcut Windows sahipligini ham metin olarak yazmaz.

Onceki hatali gorunum:

- zip -> 7-Zip
- 7z -> 7-Zip
- cab -> CABFolder
- iso -> Windows.IsoFile

Yeni davranis:

- Uzanti UniZip ile iliskiliyse kolonda `UniZip` yazar.
- Uzanti UniZip ile iliskili degilse kolon bos kalir.
- Boylece kullanici ve tum kullanicilar kolonlari sadece UniZip sahipligini gosterir.

## Registry davranisi

Gecerli kullanici icin iliskilendirme yazilirken:

- `HKCU\Software\Classes\.ext` -> `UniZip.Archive`
- `HKCU\Software\Classes\UniZip.Archive` open command/default icon
- `HKCU\Software\UniZip\Capabilities`
- `HKCU\Software\RegisteredApplications`
- `HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts\.ext\UserChoice` temizlenmeye calisilir

UserChoice anahtari Windows tarafindan korunuyorsa temizleme sessiz gecilir; ana Registry yazimi iptal edilmez.
