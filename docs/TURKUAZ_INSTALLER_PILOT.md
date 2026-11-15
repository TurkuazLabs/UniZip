# Dosya Yolu: REPO_ROOT/docs/TURKUAZ_INSTALLER_PILOT.md
# Amac: UniZip icin TurkuazInstaller Community pilot sinirlarini ve dogrulamasini tanimlamak
# Modul - Documentation Markdown
# Version: 0.1.4
# Aciklama: Sabit surumler, JRE dahil CI artifact'i, smoke kapsami ve production olmayan dagitim durumu
# Bagimli Oldugu Katman: Documentation | Tool | Config

# UniZip TurkuazInstaller pilotu

Bu pilot UniZip Community Desktop `0.3.1` uygulamasini Java 21 runtime
dahil self-contained `jpackage` app-image olarak hazirlar ve
TurkuazInstaller Community `0.2.23`, kaynak revizyonu
`987603a6784d16e5b2f7d1b64b248c03582d199c` ile paketler.

`.github/workflows/turkuaz-installer-pilot.yml` yalniz manuel veya PR
dogrulamasinda calisir. Mevcut portable ZIP release akisini degistirmez,
tag olusturmaz ve GitHub Release yayinlamaz.

## Otomatik dogrulama

- Java 21 Maven verify ve runtime dependency toplama.
- JRE dahil self-contained UniZip Windows app-image.
- Sabit TurkuazInstaller commit ve CLI surum kontrolu.
- `.tzpkg` ve tek dosyali pilot Setup EXE uretimi.
- Gecici dizine gercek `install-local-self` kurulumu.
- Kurulan `UniZip.exe` SHA-256 karsilastirmasi.
- `uninstall-local` ile yalniz yonetilen payload'in kaldirilmasi.
- Setup, paket ve metadata SHA-256 artifact'leri.

TurkuazInstaller `0.2.23` sihirbazi uygulama adini `UniZip.exe` giris
noktasindan `UniZip` olarak turetir. Baslat menusunde `UniZip` ve `Kaldir`
ile masaustunde `UniZip` kisayollarini zorunlu olusturur; kaldirmada kendi
kisayollarini guvenli bicimde temizler.

## Sinirlar

Artifact imzasizdir, 14 gun saklanir ve yalniz kontrollu Windows pilotu
icindir. Authenticode/SmartScreen guveni, temiz Windows 10/11 etkileşimli
wizard kabulü ve Explorer entegrasyonu production dagitimdan once ayri
tamamlanmalidir. Mevcut portable ZIP ve update manifest akisi bu pilot
tarafindan degistirilmez.
