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
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
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

    /**
     * Writes a portable SHA-256 sidecar without replacing an existing user's file.
     * A second invocation writes a numbered sibling; the original is preserved.
     */
    public Path writeSha256Sidecar(Path input) throws IOException {
        if (input == null || !Files.isRegularFile(input)) {
            throw new IOException("SHA-256 yalniz dosyalar icin desteklenir");
        }
        Path file = input.toAbsolutePath().normalize();
        String hash = sha256(file);
        String cleanName = file.getFileName().toString()
                .replace("\r", "_")
                .replace("\n", "_");
        String content = hash + "  " + cleanName + System.lineSeparator();
        Path parent = file.getParent();
        String targetName = file.getFileName() + ".sha256";
        for (int index = 1; index <= 1000; index++) {
            Path target = parent.resolve(index == 1
                    ? targetName
                    : file.getFileName() + " (" + index + ").sha256");
            try {
                Files.writeString(target, content, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                return target;
            } catch (java.nio.file.FileAlreadyExistsException ignored) {
                // Never replace a previously generated file.
            }
        }
        throw new IOException("SHA-256 hedef dosya adi icin bos bir ad bulunamadi");
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
