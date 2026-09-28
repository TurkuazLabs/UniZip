/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/UpdateManifest.java
# 📌 Amac: UniZip yayin sunucusundan okunan guncelleme manifestini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Surum, kanal, indirme adresi, SHA-256 ve yayin notlarini tek modelde tutar

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.net.URI;

public record UpdateManifest(
        String product,
        String version,
        String channel,
        boolean mandatory,
        String minimumVersion,
        String downloadUrl,
        String sha256,
        long sizeBytes,
        String publishedAt,
        String notes
) {
    public UpdateManifest {
        product = safe(product, "UNIZIP");
        version = safe(version, "");
        channel = safe(channel, UpdateConfig.DEFAULT_CHANNEL).toLowerCase(java.util.Locale.ROOT);
        minimumVersion = safe(minimumVersion, "0.0.0");
        downloadUrl = safe(downloadUrl, "");
        sha256 = safe(sha256, "").toLowerCase(java.util.Locale.ROOT);
        publishedAt = safe(publishedAt, "");
        notes = safe(notes, "");
    }

    public void validate() {
        if (!"UNIZIP".equalsIgnoreCase(product)) {
            throw new IllegalArgumentException("Guncelleme manifest urun kodu gecersiz");
        }
        if (version.isBlank()) {
            throw new IllegalArgumentException("Guncelleme manifest surumu bos");
        }
        if (downloadUrl.isBlank()) {
            throw new IllegalArgumentException("Guncelleme indirme adresi bos");
        }
        if (!sha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("Guncelleme SHA-256 degeri gecersiz");
        }
    }

    public String fileName() {
        try {
            String path = URI.create(downloadUrl).getPath();
            if (path != null && !path.isBlank()) {
                String candidate = path.substring(path.lastIndexOf('/') + 1);
                if (!candidate.isBlank()) {
                    return candidate.replaceAll("[^A-Za-z0-9._()-]", "_");
                }
            }
        } catch (Exception ignored) {
            // Guvenli varsayilan ad kullanilir.
        }
        return "UniZip_Update_" + version + ".bin";
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
