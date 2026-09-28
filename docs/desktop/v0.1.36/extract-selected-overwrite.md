# 📄 Dosya Yolu: docs/v0.1.36/extract-selected-overwrite.md
# 📌 Amac: UniZip v0.1.36 secili/tum cikarma ve dosya cakisma politikasini belgelemek
# 📌 Modul - FileType
# Version: 0.1.36
# Aciklama: Extract All, Extract Selected, overwrite, skip ve rename davranislarini aciklar
Bagimli Oldugu Katman: Controller | Service | Tool | View | Language

## Hedef

v0.1.36 surumunde cikarma akisi daha guvenli hale getirildi.

## Eklenenler

- Extract butonu tum arsivi cikartir.
- Extract Selected butonu secili dosya veya klasoru cikartir.
- Cikarma dialogunda ayni dosya varsa secenegi eklendi:
  - Uzerine yaz
  - Atla
  - Yeniden adlandir
- ZIP Slip/path traversal kontrolu korunur.
- Klasor seciliyse altindaki dosyalar birlikte cikartilir.

## Katman Notu

Controller sadece secili entry bilgisini alir ve Service'e gonderir.
Service hedef klasor ve cikarma kapsam kararini yonetir.
JavaZipTool dis dunya ZIP okuma/yazma operasyonunu yapar.
View sadece kullanicidan cikarma seceneklerini alir.
