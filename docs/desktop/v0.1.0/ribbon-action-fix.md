# 📄 Dosya Yolu: docs/v0.1.0/ribbon-action-fix.md
# 📌 Amac: Ribbon Add ve Test buton baglanti duzeltmesini belgelemek
# 📌 Modul - FileType
# Version: 0.1.24
# Aciklama: ust menu calisirken ribbon butonlarinin eski Coming Soon aksiyonunda kalmasini aciklar

Bagimli Oldugu Katman: View | Controller

## Sorun

v0.1.23'te `Edit > Add` menusu `runAddAction()` ile calisiyordu.

Fakat ribbon'daki buyuk `Add` butonu hala:

```java
this::showNotReadyMessage
```

aksiyonuna bagliydi.

Ayni sorun `Test` ribbon butonunda da vardi.

## Duzeltme

```java
buttonPanel.add(createRibbonButton("add", "button.add", this::runAddAction));
buttonPanel.add(createRibbonButton("test", "button.test", this::runTestAction));
```
