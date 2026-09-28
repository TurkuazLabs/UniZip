# 📄 Dosya Yolu: docs/v0.1.0/extract-options.md
# 📌 Amac: UniZip cikarma seceneklerini belgelemek
# 📌 Modul - FileType
# Version: 0.1.15
# Aciklama: arsiv adiyla alt klasore cikarma secenegini aciklar

Bagimli Oldugu Katman: Controller | Service | Tool | View | Language

## Arsiv adiyla alt klasore cikarma

Cikarma klasoru secme ekraninda su secenek eklendi:

```text
ZIP dosyasi adi ile alt klasor olustur
```

Secenek aktifse:

```text
SecilenKlasor/Belge/
```

gibi bir hedef klasor olusturulur ve arsiv icerigi bu klasore cikarilir.

Ornek:

```text
Arsiv: Belge.zip
Secilen hedef: D:/Cikarilanlar
Gercek cikis: D:/Cikarilanlar/Belge/
```

Secenek kapaliysa eski davranis korunur ve dosyalar direkt secilen hedef klasore cikarilir.
