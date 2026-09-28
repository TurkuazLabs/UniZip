# 📄 Dosya Yolu: apps/desktop/README.md
# 📌 Amac: UniZip Community projesinin kurulum, test, lisans ve guncelleme bilgisini vermek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Java 21 Maven tabanli ZIP yoneticisi community gelistirme paketi
Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language | Config

# UniZip Community v0.1.61

## Bu Surumde

- Acilista token varsa lisans online dogrulanir.
- Baglanti yoksa son basarili dogrulama ve offline grace suresi kullanilir.
- Feature gate altyapisi Community/Pro ozelliklerini merkezi yonetir.
- Stable kanal guncellemeleri acilista arka planda kontrol edilir.
- Yeni surum varsa indirme onayi gosterilir.
- Indirilen paket boyut ve SHA-256 ile dogrulanir.
- Dogrulanan paket `userdata/updates` klasorune kaydedilir.
- `Yardim > Guncellemeleri Denetle` ile manuel kontrol yapilir.

## Update Sunucusu

Varsayilan manifest:

```text
https://unizip.turkuaz.com/releases/stable/update.yml
```

Ornek manifest:

```text
releases/stable/update.yml.example
```

Gercek manifest olusturma:

```bash
python scripts/generate-update-manifest.py \
  --file dist/UniZip_v0.1.61_portable.zip \
  --version 0.1.61 \
  --download-url https://unizip.turkuaz.com/releases/stable/UniZip_v0.1.61_portable.zip \
  --output dist/update.yml
```

NetBeans testinde farkli manifest kullanmak icin:

```text
-Dunizip.update.manifestUrl=http://localhost:8080/update.yml
```

veya:

```text
UNIZIP_UPDATE_MANIFEST_URL=http://localhost:8080/update.yml
```

HTTP yalnizca `localhost` testinde kabul edilir. Gercek guncelleme adresi HTTPS olmak zorundadir.

## Guvenli Kurulum Siniri

v0.1.61 paketi otomatik kontrol ve guvenli indirme yapar; calisan uygulama dosyalarini kendi uzerine yazmaz. Inno Setup/jpackage tamamlandiginda dogrulanan installer uygulama kapatilarak baslatilacaktir.

## Calistirma

```bash
cd apps/desktop
mvn exec:java
```

## Test ve Paketleme

```bash
cd apps/desktop
mvn clean verify
```

Windows local CI:

```bat
scripts\run-ci-local.bat
```

## Lisans

UniZip, OpenCart 3.x Turkuaz Entitlement API ile email/sifre girisi yapar. Sifre saklanmaz; token ve cihaz bilgisi `userdata/license.yml` icinde tutulur.

## Format Destek Durumu

| Format | Listele | Cikar | Test | Ekle | Sil | Rename | Edit Save-Back |
| --- | --- | --- | --- | --- | --- | --- | --- |
| ZIP | Var | Var | Var | Var | Var | Var | Var |
| 7Z | Engine bekleniyor | Engine bekleniyor | Engine bekleniyor | Yok | Yok | Yok | Yok |
| RAR | Engine bekleniyor | Engine bekleniyor | Engine bekleniyor | Yok | Yok | Yok | Yok |
| TAR/GZ | Engine bekleniyor | Engine bekleniyor | Engine bekleniyor | Yok | Yok | Yok | Yok |
