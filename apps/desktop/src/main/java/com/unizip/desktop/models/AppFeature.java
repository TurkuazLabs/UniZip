/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/AppFeature.java
# 📌 Amac: UniZip ozelliklerini lisans kapisi icin merkezi olarak tanimlamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Community ve Pro erisim seviyelerini magic string kullanmadan temsil eder

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public enum AppFeature {
    ARCHIVE_CORE(LicenseEdition.COMMUNITY),
    SYSTEM_INTEGRATION(LicenseEdition.COMMUNITY),
    AUTO_UPDATE(LicenseEdition.COMMUNITY),
    EXTERNAL_ARCHIVE_ENGINE(LicenseEdition.COMMUNITY),
    PRIORITY_UPDATE_CHANNEL(LicenseEdition.PRO);

    private final LicenseEdition minimumEdition;

    AppFeature(LicenseEdition minimumEdition) {
        this.minimumEdition = minimumEdition;
    }

    public LicenseEdition minimumEdition() {
        return minimumEdition;
    }
}
