# 📄 Dosya Yolu: docs/v0.1.42/f2-rename-shortcut-fix.md
# 📌 Amac: UniZip F2 rename kisayolunun tablo focusundayken calismamasini duzeltmek
# 📌 Modul - FileType
# Version: 0.1.42
# Aciklama: JTable/JList focus durumlari icin F2 key binding duzeltmesi

Bagimli Oldugu Katman: View | Controller

## Sorun

ZIP icindeki oge seciliyken F2 tusu her zaman rename akisini baslatmiyordu.

## Neden

Rename kisayolu ana panel ve root pane uzerine bagliydi. Ancak detay gorunumundeki JTable focus aldiginda kendi key binding haritasi F2 tusunu yakalayabiliyordu.

## Cozum

ArchiveListPanel icine listeye ozel F2 binding eklendi.

Baglanan componentler:

- ArchiveListPanel
- Grid JList
- Details JTable

Bu sayede tablo veya ikon gorunumu focus altindayken F2 direkt rename action cagirir.
