# 📄 Dosya Yolu: docs/v0.1.35/location-preserve-after-edit.md
# 📌 Amac: Harici editor sonrasi ZIP liste konumunun korunmasini belgelemek
# 📌 Modul - FileType
# Version: 0.1.35
# Aciklama: cift tik editor akisi sonrasi mevcut klasor ve secili entry bilgisinin korunmasi

Bagimli Oldugu Katman: Controller | View | Service

## Problem

ZIP icindeki bir metin veya resim dosyasina cift tiklandiginda dosya harici editor ile aciliyordu. Editor kapaninca ZIP guncelleniyor ve liste yeniden yukleniyordu. Yenileme sonrasi `currentFolder` kok dizine sifirlandigi icin kullanici tekrar ilgili klasore girmek zorunda kaliyordu.

## Cozum

`ArchiveController` operasyon baslamadan once mevcut klasoru ve secili entry adini alir. Operasyon bittikten sonra ZIP tekrar listelenirken bu bilgiler `MainFrame` katmanina geri verilir.

`MainFrame.setArchiveEntriesKeepingLocation()` su islemleri yapar:

- Arsiv entry listesini gunceller.
- Eski klasor hala varsa ayni klasoru aktif yapar.
- Klasor yoksa en yakin ust klasore duser.
- Arsiv agacinda ilgili klasoru tekrar secer.
- Liste icinde eski entry varsa onu tekrar secili hale getirir.

## Etki

- Metin editoru kapandiktan sonra konum korunur.
- Paint veya baska resim editoru kapandiktan sonra konum korunur.
- Add, delete, test ve diger operasyonlarda da yenileme daha az rahatsiz edici hale gelir.

## Sinir

Secili dosya silinmisse veya filtre disinda kalmissa liste ilk gorunur ogeye duser.
