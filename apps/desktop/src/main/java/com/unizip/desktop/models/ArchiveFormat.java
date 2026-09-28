/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveFormat.java
# 📌 Amac: UniZip tarafindan taninan arsiv formatlarini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Engine secimi, format etiketi ve destek tablosu icin enum

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.util.Locale;

public enum ArchiveFormat {
    ZIP("zip", true),
    SEVEN_Z("7z", false),
    RAR("rar", false),
    TAR("tar", false),
    GZIP("gz", false),
    BZIP2("bz2", false),
    XZ("xz", false),
    ISO("iso", false),
    UNKNOWN("unknown", false);

    private final String displayName;
    private final boolean fullySupported;

    ArchiveFormat(String displayName, boolean fullySupported) {
        this.displayName = displayName;
        this.fullySupported = fullySupported;
    }

    public String displayName() {
        return displayName;
    }

    public boolean fullySupported() {
        return fullySupported;
    }

    public static ArchiveFormat fromExtension(String extension) {
        String normalized = extension == null ? "" : extension.trim().toLowerCase(Locale.ROOT);
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return switch (normalized) {
            case "zip", "jar", "war", "ear" -> ZIP;
            case "7z" -> SEVEN_Z;
            case "rar" -> RAR;
            case "tar" -> TAR;
            case "gz", "gzip", "tgz" -> GZIP;
            case "bz2", "tbz", "tbz2" -> BZIP2;
            case "xz", "txz" -> XZ;
            case "iso" -> ISO;
            default -> UNKNOWN;
        };
    }
}
