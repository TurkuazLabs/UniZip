/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/UpdateConfig.java
# 📌 Amac: UniZip otomatik guncelleme ayarlarini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Acilista kontrol, kanal, manifest adresi ve otomatik indirme tercihlerini tutar

Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.models;

public record UpdateConfig(
        boolean enabled,
        boolean checkOnStartup,
        boolean autoDownload,
        String channel,
        String manifestUrl
) {
    public static final String DEFAULT_CHANNEL = "stable";
    public static final String DEFAULT_MANIFEST_URL = "https://unizip.turkuaz.com/releases/stable/update.yml";

    public UpdateConfig {
        channel = channel == null || channel.isBlank() ? DEFAULT_CHANNEL : channel.trim().toLowerCase(java.util.Locale.ROOT);
        manifestUrl = manifestUrl == null || manifestUrl.isBlank() ? DEFAULT_MANIFEST_URL : manifestUrl.trim();
    }
}
