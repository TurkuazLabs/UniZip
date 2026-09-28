# 📄 Dosya Yolu: docs/v0.1.42/bugfix-structure-check.md
# 📌 Amac: v0.1.38 genel kontrolunde bulunan mantik hatalarini belgelemek
# 📌 Modul - FileType
# Version: 0.1.42
# Aciklama: recent menu, dil anahtari ve konum koruma temizlik notlari

Bagimli Oldugu Katman: Controller | View | Language | Config

## Duzeltilenler

- Ingilizce dil dosyasinda eksik olan tablo, ayar ve navigasyon anahtarlari eklendi.
- MainFrame icindeki tekrar eden selectTreePath(currentFolder) satiri temizlendi.
- Yeni ZIP olusturma akisi duzeltildi; islem basarili olunca yeni ZIP aktif arsiv olur, liste yuklenir ve recent menu o anda yenilenir.
- Genel islemler sonrasi recent menu refresh gecikmesi azaltildi.

## Kontrol

- Java 21 compile kontrolu gecmelidir.
- Dil anahtari eksik kontrolu bos donmelidir.
- ZIP paket testi gecmelidir.
