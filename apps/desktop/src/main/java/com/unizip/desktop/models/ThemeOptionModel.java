/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ThemeOptionModel.java
# 📌 Amac: Ayarlar ekraninda secilebilir tema bilgisini tasimak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: tema id, gorunen ad ve mod bilgisini iceren model

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public record ThemeOptionModel(String id, String name, String mode) {
    @Override
    public String toString() {
        return name + " (" + mode + ")";
    }
}
