/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ArchiveFormatDetector.java
# 📌 Amac: Arsiv dosyasinin formatini uzanti ve magic signature ile tespit etmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Engine seciminde uzantiya ek olarak dosya imzasini kontrol eder

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class ArchiveFormatDetector {
    public ArchiveFormat detect(Path archivePath) {
        if (archivePath == null || !Files.isRegularFile(archivePath)) {
            return ArchiveFormat.UNKNOWN;
        }
        ArchiveFormat signatureFormat = detectBySignature(archivePath);
        if (signatureFormat != ArchiveFormat.UNKNOWN) {
            return signatureFormat;
        }
        return ArchiveFormat.fromExtension(extensionOf(archivePath));
    }

    public ArchiveFormat detectByExtension(Path archivePath) {
        return ArchiveFormat.fromExtension(extensionOf(archivePath));
    }

    private ArchiveFormat detectBySignature(Path archivePath) {
        byte[] header = new byte[512];
        int read;
        try (InputStream inputStream = Files.newInputStream(archivePath)) {
            read = inputStream.read(header);
        } catch (IOException exception) {
            return ArchiveFormat.UNKNOWN;
        }
        if (read < 2) {
            return ArchiveFormat.UNKNOWN;
        }
        if (hasPrefix(header, read, 0x50, 0x4B, 0x03, 0x04)
                || hasPrefix(header, read, 0x50, 0x4B, 0x05, 0x06)
                || hasPrefix(header, read, 0x50, 0x4B, 0x07, 0x08)) {
            return ArchiveFormat.ZIP;
        }
        if (hasPrefix(header, read, 0x37, 0x7A, 0xBC, 0xAF, 0x27, 0x1C)) {
            return ArchiveFormat.SEVEN_Z;
        }
        if (hasPrefix(header, read, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
                || hasPrefix(header, read, 0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00)) {
            return ArchiveFormat.RAR;
        }
        if (hasPrefix(header, read, 0x1F, 0x8B)) {
            return ArchiveFormat.GZIP;
        }
        if (hasPrefix(header, read, 0x42, 0x5A, 0x68)) {
            return ArchiveFormat.BZIP2;
        }
        if (hasPrefix(header, read, 0xFD, 0x37, 0x7A, 0x58, 0x5A, 0x00)) {
            return ArchiveFormat.XZ;
        }
        if (read > 262
                && header[257] == 'u'
                && header[258] == 's'
                && header[259] == 't'
                && header[260] == 'a'
                && header[261] == 'r') {
            return ArchiveFormat.TAR;
        }
        return ArchiveFormat.UNKNOWN;
    }

    private boolean hasPrefix(byte[] data, int read, int... values) {
        if (read < values.length) {
            return false;
        }
        for (int index = 0; index < values.length; index++) {
            if ((data[index] & 0xFF) != values[index]) {
                return false;
            }
        }
        return true;
    }

    private String extensionOf(Path archivePath) {
        if (archivePath == null || archivePath.getFileName() == null) {
            return "";
        }
        String fileName = archivePath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (fileName.endsWith(".tar.gz") || fileName.endsWith(".tgz")) {
            return "tgz";
        }
        if (fileName.endsWith(".tar.bz2") || fileName.endsWith(".tbz") || fileName.endsWith(".tbz2")) {
            return "tbz2";
        }
        if (fileName.endsWith(".tar.xz") || fileName.endsWith(".txz")) {
            return "txz";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }
}
