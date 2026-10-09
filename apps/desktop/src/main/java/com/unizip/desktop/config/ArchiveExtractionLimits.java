/*
# 📄 Dosya Yolu: /apps/desktop/src/main/java/com/unizip/desktop/config/ArchiveExtractionLimits.java
# 📌 Amac: ZIP cikarma kaynak tuketim sinirlarini merkezi olarak tanimlamak
# 📌 Modul - Java
# Version: 0.3.1
# Aciklama: Varsayilan guvenlik limitleri ve test icin kucuk budget destegi
Bagimli Oldugu Katman: Config
*/
package com.unizip.desktop.config;

public record ArchiveExtractionLimits(int maxEntries, long maxEntryBytes, long maxTotalBytes) {
    private static final int DEFAULT_MAX_ENTRIES = 100_000;
    private static final long DEFAULT_MAX_ENTRY_BYTES = 1024L * 1024L * 1024L;
    private static final long DEFAULT_MAX_TOTAL_BYTES = 4L * 1024L * 1024L * 1024L;

    public ArchiveExtractionLimits {
        if (maxEntries <= 0 || maxEntryBytes <= 0 || maxTotalBytes <= 0
                || maxEntryBytes > maxTotalBytes) {
            throw new IllegalArgumentException("ZIP extraction limitleri gecersiz");
        }
    }

    public static ArchiveExtractionLimits defaults() {
        return new ArchiveExtractionLimits(
                DEFAULT_MAX_ENTRIES,
                DEFAULT_MAX_ENTRY_BYTES,
                DEFAULT_MAX_TOTAL_BYTES);
    }
}
