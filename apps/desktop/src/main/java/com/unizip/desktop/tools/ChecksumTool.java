/*
# Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ChecksumTool.java
# Amac: Tek ortak dosya hash motoru ve veri-koruyan sidecar yazimi
# Modul - Tool
# Version: 0.3.1
# Aciklama: SHA-256, SHA-512, CRC-32 ve CREATE_NEW ile no-overwrite checksum dosyalari
# Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.zip.CRC32;

public final class ChecksumTool {
    @FunctionalInterface
    private interface HashFunction {
        String calculate(Path file) throws IOException;
    }

    public String sha256(Path file) throws IOException {
        return shaDigest(file, "SHA-256");
    }

    public String sha512(Path file) throws IOException {
        return shaDigest(file, "SHA-512");
    }

    public String crc32(Path file) throws IOException {
        CRC32 crc = new CRC32();
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = in.read(buffer)) != -1) {
                crc.update(buffer, 0, count);
            }
        }
        return String.format(Locale.ROOT, "%08x", crc.getValue());
    }

    private String shaDigest(Path file, String algorithm) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(file))) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, count);
                }
            }
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Hash algoritmasi desteklenmiyor: " + algorithm, exception);
        }
    }

    public Path writeSha256Sidecar(Path input) throws IOException {
        return writeSidecar(input, ".sha256", this::sha256);
    }

    public Path writeSha512Sidecar(Path input) throws IOException {
        return writeSidecar(input, ".sha512", this::sha512);
    }

    public Path writeCrc32Sidecar(Path input) throws IOException {
        return writeSidecar(input, ".crc32", this::crc32);
    }

    private Path writeSidecar(Path input, String suffix, HashFunction calculator) throws IOException {
        if (input == null) {
            throw new IOException("Hash yalniz normal dosyalar icin desteklenir");
        }
        Path file = input.toAbsolutePath().normalize();
        if (Files.isSymbolicLink(file) || !Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("Hash yalniz normal dosyalar icin desteklenir");
        }
        String hash = calculator.calculate(file);
        String filename = file.getFileName().toString()
                .replace("\r", "_")
                .replace("\n", "_");
        String content = hash + "  " + filename + System.lineSeparator();
        Path parent = file.getParent();
        if (parent == null) {
            throw new IOException("Hash hedef klasoru bulunamadi");
        }
        for (int index = 1; index <= 1000; index++) {
            String outName = index == 1 ? filename + suffix
                    : filename + " (" + index + ")" + suffix;
            Path target = parent.resolve(outName);
            try {
                Files.writeString(target, content, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                return target;
            } catch (java.nio.file.FileAlreadyExistsException ignored) {
                // Do not overwrite any existing checksum, including symlinks.
            }
        }
        throw new IOException("Hash hedefi icin bos dosya adi bulunamadi");
    }
}
