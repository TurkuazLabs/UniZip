/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/LicenseEdition.java
# 📌 Amac: UniZip lisans surum tiplerini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: Community ve Pro edition bilgisini merkezi model olarak tutar

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public enum LicenseEdition {
    COMMUNITY("Community"),
    PRO("Pro");

    private final String displayName;

    LicenseEdition(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static LicenseEdition fromText(String value) {
        if (value == null || value.isBlank()) {
            return COMMUNITY;
        }
        return "pro".equalsIgnoreCase(value.trim()) ? PRO : COMMUNITY;
    }
}
