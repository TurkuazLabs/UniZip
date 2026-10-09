/*
# Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/ExplorerShellCommandService.java
# Amac: Explorer komutlarini mevcut ArchiveService ve ChecksumTool uzerinden tek noktada yonetmek
# Modul - Java Service
# Version: 0.3.1
# Aciklama: ZIP islevlerinde tekli secim, no-clobber hedef ve komut dogrulama
# Bagimli Oldugu Katman: Service | Tool | Model
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.ArchiveOperationResult;
import com.unizip.desktop.models.ExtractOverwriteMode;
import com.unizip.desktop.tools.ChecksumTool;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class ExplorerShellCommandService {
    private static final Set<String> COMMANDS = Set.of(
            "--extract-here", "--extract-to-folder", "--test",
            "--hash-sha256", "--hash-sha512", "--hash-crc32", "--add-to-archive");
    private final ArchiveService archiveService;
    private final ChecksumTool checksumTool;

    public ExplorerShellCommandService(ArchiveService archiveService, ChecksumTool checksumTool) {
        this.archiveService = Objects.requireNonNull(archiveService, "archiveService");
        this.checksumTool = Objects.requireNonNull(checksumTool, "checksumTool");
    }

    public static boolean supports(String command) {
        return command != null && COMMANDS.contains(command.trim().toLowerCase(Locale.ROOT));
    }

    public ArchiveOperationResult execute(String[] args) throws Exception {
        if (args == null || args.length != 2 || !supports(args[0])
                || args[1] == null || args[1].isBlank()) {
            throw new IllegalArgumentException("Explorer islemi tek bir gecerli dosya veya klasor bekliyor");
        }
        String command = args[0].trim().toLowerCase(Locale.ROOT);
        Path inputPath = Path.of(args[1]).toAbsolutePath().normalize();
        if (!Files.exists(inputPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("Secili dosya veya klasor bulunamadi: " + inputPath);
        }
        return switch (command) {
            case "--extract-here" -> archiveService.extractZip(
                    inputPath, shellOutputDirectory(inputPath), false,
                    List.of(), ExtractOverwriteMode.RENAME);
            case "--extract-to-folder" -> archiveService.extractZip(
                    inputPath, shellOutputDirectory(inputPath), true,
                    List.of(), ExtractOverwriteMode.RENAME);
            case "--test" -> archiveService.testZip(inputPath);
            case "--hash-sha256" -> new ArchiveOperationResult(
                    true, "SHA-256 dosyasi olusturuldu: "
                    + checksumTool.writeSha256Sidecar(inputPath), 1);
            case "--hash-sha512" -> new ArchiveOperationResult(
                    true, "SHA-512 dosyasi olusturuldu: "
                    + checksumTool.writeSha512Sidecar(inputPath), 1);
            case "--hash-crc32" -> new ArchiveOperationResult(
                    true, "CRC-32 dosyasi olusturuldu: "
                    + checksumTool.writeCrc32Sidecar(inputPath), 1);
            case "--add-to-archive" -> archiveService.createZip(
                    inputPath, uniqueArchivePath(inputPath));
            default -> throw new IllegalArgumentException("Desteklenmeyen Explorer islemi");
        };
    }

    private static Path shellOutputDirectory(Path archivePath) {
        Path parent = archivePath.getParent();
        return parent == null ? Path.of(System.getProperty("user.dir", ".")) : parent;
    }

    static Path uniqueArchivePath(Path inputPath) throws java.io.IOException {
        Path parent = inputPath.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            throw new java.io.IOException("Arsiv hedef klasoru bulunamadi");
        }
        String name = inputPath.getFileName() == null
                ? "archive" : inputPath.getFileName().toString();
        // For directories, retain the complete folder name including dots.
        String baseName = Files.isDirectory(inputPath) ? name : stripExtension(name);
        Path candidate = parent.resolve(baseName + ".zip");
        for (int index = 2; Files.exists(candidate, LinkOption.NOFOLLOW_LINKS); index++) {
            if (index > 10000) {
                throw new java.io.IOException("ZIP hedefi icin bos dosya adi bulunamadi");
            }
            candidate = parent.resolve(baseName + " (" + index + ").zip");
        }
        return candidate;
    }

    private static String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }
}
