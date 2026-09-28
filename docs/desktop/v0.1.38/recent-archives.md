# 📄 Dosya Yolu: docs/v0.1.38/recent-archives.md
# 📌 Amac: UniZip son acilan arsivler ozelligini belgelemek
# 📌 Modul - FileType
# Version: 0.1.38
# Aciklama: File menusu recent archive listesi ve userdata/recent-archives.yml kaydi

Bagimli Oldugu Katman: Controller | Service | Repo/Model | View | Language | Config

# UniZip v0.1.38 - Recent Archives

Bu surumde son acilan ZIP dosyalari `File > Son Acilan Arsivler` menusunden tekrar acilabilir hale getirildi.

## Davranis

- Arsiv basariyla acilinca listeye en uste eklenir.
- Ayni arsiv tekrar acilirsa kopya olusmaz, en uste tasinir.
- Liste en fazla 10 arsiv tutar.
- Artik bulunamayan arsivler menuye gosterilmez.
- Kullanici menu uzerinden son listeyi temizleyebilir.

## Veri dosyasi

```text
userdata/recent-archives.yml
```

Bu dosya kullanici tarafinda olusur ve proje icindeki kaynak kodla karistirilmaz.
