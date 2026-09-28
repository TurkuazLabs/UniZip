# 📄 Dosya Yolu: docs/v0.1.0/netbeans-import-zip.md
# 📌 Amac: NetBeans Import Project from ZIP uyumlu paketleme kuralini belgelemek
# 📌 Modul - FileType
# Version: 0.1.13
# Aciklama: ZIP icindeki klasor girdilerinin korunmasi gerektigini aciklar

Bagimli Oldugu Katman: Tool | Config

## Karar

NetBeans `Import Project from ZIP` akisi v0.1.8 paketinde calisiyordu. v0.1.12 paketinde kod yapisi dogru olmasina ragmen ZIP olusturma yontemi farkliydi.

v0.1.13 ile paket tekrar klasor girdilerini icerecek sekilde olusturuldu.

## Beklenen proje yolu

```text
UniZip_v0.1.13_community/apps/desktop/pom.xml
```

## Paketleme kuralı

```bash
cd /mnt/data
zip -qr UniZip_v0.1.13_community.zip UniZip_v0.1.13_community
```

Bu yontem klasor girdilerini korur ve NetBeans import taramasina daha uyumludur.
