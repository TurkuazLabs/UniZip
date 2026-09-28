/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/LanguageOptionModel.java
# 📌 Amac: Ayarlar ekraninda secilebilir dil bilgisini tasimak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: dil id ve gorunen ad bilgisini iceren model

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public record LanguageOptionModel(String id, String name) {
    @Override
    public String toString() {
        return name;
    }
}
