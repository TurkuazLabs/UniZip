/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/LicenseStatus.java
# 📌 Amac: UniZip lisans durumlarini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: Offline, active, expired ve invalid lisans durumlarini UI ve service icin ayirir

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public enum LicenseStatus {
    OFFLINE,
    ACTIVE,
    EXPIRED,
    INVALID;

    public static LicenseStatus fromText(String value) {
        if (value == null || value.isBlank()) {
            return OFFLINE;
        }
        for (LicenseStatus status : values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return INVALID;
    }
}
