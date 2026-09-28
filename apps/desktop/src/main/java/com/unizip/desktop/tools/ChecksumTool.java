/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ChecksumTool.java
# 📌 Amac: Dosyalar icin checksum hesaplamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: SHA-256 hesaplama araci

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ChecksumTool {
    public String sha256(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file))) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 desteklenmiyor", exception);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
