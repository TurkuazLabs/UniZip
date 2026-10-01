/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/UpdateSource.java
# 📌 Amac: Guncelleme kanali ve manifest adresini tek public contract modeli olarak tasimak
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Community ve harici edition policy implementasyonlarinin UpdateService'e standart kaynak karari vermesini saglar
# Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.util.Locale;

public record UpdateSource(
        String channel,
        String manifestUrl
) {
    public UpdateSource {
        channel = normalizeChannel(channel);
        manifestUrl = manifestUrl == null ? "" : manifestUrl.trim();
        if (manifestUrl.isBlank()) {
            throw new IllegalArgumentException("Guncelleme manifest adresi bos olamaz");
        }
    }

    private static String normalizeChannel(String value) {
        return value == null || value.isBlank()
                ? UpdateConfig.DEFAULT_CHANNEL
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
