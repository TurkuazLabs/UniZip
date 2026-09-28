/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/AppConfig.java
# 📌 Amac: Uygulama konfigurasyon modelini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: app, tema ve dil bilgisi modeli

Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.models;

public record AppConfig(String appName, String version, String theme, String language) {
}
