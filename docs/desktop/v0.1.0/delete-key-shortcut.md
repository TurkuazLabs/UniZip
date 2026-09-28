# 📄 Dosya Yolu: docs/v0.1.0/delete-key-shortcut.md
# 📌 Amac: Del tusu ile secili ZIP girdisini silme kisayolunu belgelemek
# 📌 Modul - FileType
# Version: 0.1.27
# Aciklama: Swing InputMap/ActionMap ile Delete aksiyonunun baglanmasini aciklar

Bagimli Oldugu Katman: View | Controller

## Akis

v0.1.27 ile `Del` tusu secili arsiv girdisini silme aksiyonuna baglandi.

Baglanan alanlar:

```text
Ana panel
Arsiv liste paneli
Sol arsiv agaci
Sag onizleme paneli
RootPane
```

## Teknik

```java
KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)
runDeleteAction()
ArchiveController.deleteSelectedEntry()
```
