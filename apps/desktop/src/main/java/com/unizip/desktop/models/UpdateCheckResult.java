/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/UpdateCheckResult.java
# 📌 Amac: UniZip guncelleme kontrol sonucunu temsil etmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Mevcut surum, guncel durum ve varsa yeni manifest bilgisini tasir

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public record UpdateCheckResult(
        String currentVersion,
        boolean updateAvailable,
        UpdateManifest manifest
) {
    public static UpdateCheckResult current(String currentVersion, UpdateManifest manifest) {
        return new UpdateCheckResult(currentVersion, false, manifest);
    }

    public static UpdateCheckResult available(String currentVersion, UpdateManifest manifest) {
        return new UpdateCheckResult(currentVersion, true, manifest);
    }
}
