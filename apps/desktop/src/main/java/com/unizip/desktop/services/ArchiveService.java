/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/ArchiveService.java
# 📌 Amac: Arsiv listeleme, cikarma ve olusturma is kurallarini yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ZIP servis akisi, detayli test raporu, extract, editor ve rename entegrasyonu

Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import java.io.File;

import com.unizip.desktop.models.ArchiveCapabilities;
import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveFormat;
import com.unizip.desktop.models.ArchiveOperationResult;
import com.unizip.desktop.models.ArchiveSessionState;
import com.unizip.desktop.models.ArchiveTestResultModel;
import com.unizip.desktop.models.ExtractOverwriteMode;
import com.unizip.desktop.tools.ArchiveEngine;
import com.unizip.desktop.tools.ArchiveEngineRegistry;
import com.unizip.desktop.tools.ChecksumTool;
import com.unizip.desktop.tools.ExternalEditorTool;
import com.unizip.desktop.tools.FileSystemTool;
import com.unizip.desktop.tools.JavaZipTool;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public final class ArchiveService {
    private final JavaZipTool javaZipTool;
    private final FileSystemTool fileSystemTool;
    private final LogService logService;
    private final ChecksumTool checksumTool;
    private final ExternalEditorTool externalEditorTool;
    private final SettingsService settingsService;
    private final ArchiveEngineRegistry archiveEngineRegistry;
    private final ArchiveCommandGuard archiveCommandGuard;

    public ArchiveService(
            JavaZipTool javaZipTool,
            FileSystemTool fileSystemTool,
            LogService logService,
            ChecksumTool checksumTool,
            ExternalEditorTool externalEditorTool,
            SettingsService settingsService
    ) {
        this.javaZipTool = javaZipTool;
        this.fileSystemTool = fileSystemTool;
        this.logService = logService;
        this.checksumTool = checksumTool;
        this.externalEditorTool = externalEditorTool;
        this.settingsService = settingsService;
        this.archiveEngineRegistry = ArchiveEngineRegistry.createDefault();
        this.archiveCommandGuard = new ArchiveCommandGuard();
    }

    public List<ArchiveEntryModel> listZipEntries(Path archivePath) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireList(engine.capabilities());
        logService.info("Arsiv listeleniyor: " + archivePath + " | Engine: " + engine.displayName());
        List<ArchiveEntryModel> entries = engine.list(archivePath);
        logService.info("Girdi sayisi: " + entries.size());
        return entries;
    }

    public ArchiveOperationResult extractZip(Path archivePath, Path outputDirectory) throws Exception {
        return extractZip(archivePath, outputDirectory, false, List.of(), ExtractOverwriteMode.OVERWRITE);
    }

    public ArchiveOperationResult extractZip(Path archivePath, Path outputDirectory, boolean createArchiveFolder) throws Exception {
        return extractZip(archivePath, outputDirectory, createArchiveFolder, List.of(), ExtractOverwriteMode.OVERWRITE);
    }

    public ArchiveOperationResult extractZip(
            Path archivePath,
            Path outputDirectory,
            boolean createArchiveFolder,
            List<String> selectedEntryNames,
            ExtractOverwriteMode overwriteMode
    ) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireExtract(engine.capabilities());
        Path finalOutputDirectory = createArchiveFolder ? outputDirectory.resolve(archiveBaseName(archivePath)) : outputDirectory;
        fileSystemTool.createDirectories(finalOutputDirectory);
        logService.info("Arsiv cikarma basladi: " + archivePath + " | Engine: " + engine.displayName());
        logService.info("Hedef klasor: " + finalOutputDirectory);
        int count = engine.extract(archivePath, finalOutputDirectory, selectedEntryNames, overwriteMode);
        String scope = selectedEntryNames == null || selectedEntryNames.isEmpty() ? "tum arsiv" : "secili girdi";
        String message = "Arsiv cikarma tamamlandi. Kapsam: " + scope + ", dosya sayisi: " + count;
        logService.info(message);
        return new ArchiveOperationResult(true, message, count);
    }

    public List<String> findAddConflicts(Path archivePath, List<Path> inputPaths) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireAdd(engine.capabilities());
        return engine.findAddConflicts(archivePath, inputPaths);
    }

    public ArchiveOperationResult addToZip(Path archivePath, List<Path> inputPaths, boolean overwriteExisting) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireAdd(engine.capabilities());
        if (inputPaths == null || inputPaths.isEmpty()) {
            throw new IllegalArgumentException("Eklenecek dosya veya klasor secilmedi");
        }

        logService.info("Arsiv ekleme basladi: " + archivePath + " | Engine: " + engine.displayName());
        int count = engine.add(archivePath, inputPaths, overwriteExisting);
        String message = "Arsiv ekleme tamamlandi. Eklenen dosya sayisi: " + count;
        logService.info(message);
        return new ArchiveOperationResult(true, message, count);
    }

    public List<File> exportZipEntriesToTemp(Path archivePath, List<String> entryNames) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireExtract(engine.capabilities());
        if (entryNames == null || entryNames.isEmpty()) {
            throw new IllegalArgumentException("Disari aktarilacak arsiv girdisi secilmedi");
        }

        for (String entryName : entryNames) {
            validateEntryName(entryName);
        }

        Path tempRoot = settingsService.createWorkingDirectory("unizip-drag-out-");
        engine.exportEntriesToDirectory(archivePath, entryNames, tempRoot);

        List<File> exportedFiles = new ArrayList<>();
        for (String entryName : entryNames) {
            String normalizedName = entryName.replace('\\', '/');
            if (normalizedName.endsWith("/")) {
                normalizedName = normalizedName.substring(0, normalizedName.length() - 1);
            }
            int slashIndex = normalizedName.indexOf('/');
            String topLevelName = slashIndex >= 0 ? normalizedName.substring(0, slashIndex) : normalizedName;
            Path topLevelPath = tempRoot.resolve(topLevelName).normalize();
            if (Files.exists(topLevelPath)) {
                exportedFiles.add(topLevelPath.toFile());
            }
        }

        logService.info("ZIP disari surukleme icin gecici cikti hazirlandi: " + tempRoot);
        return exportedFiles;
    }

    public ArchiveOperationResult deleteZipEntries(Path archivePath, List<String> entryNames) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireDelete(engine.capabilities());
        if (entryNames == null || entryNames.isEmpty()) {
            throw new IllegalArgumentException("Silinecek arsiv girdisi secilmedi");
        }

        for (String entryName : entryNames) {
            validateEntryName(entryName);
        }

        logService.info("Arsiv silme basladi: " + archivePath + " | Engine: " + engine.displayName());
        int count = engine.delete(archivePath, entryNames);
        String message = "Arsiv silme tamamlandi. Silinen girdi sayisi: " + count;
        logService.info(message);
        return new ArchiveOperationResult(true, message, count);
    }


    public ArchiveOperationResult renameZipEntry(Path archivePath, String oldEntryName, String newBaseName) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireRename(engine.capabilities());
        validateEntryName(oldEntryName);
        String normalizedNewBaseName = normalizeRenameBaseName(newBaseName);
        String newEntryName = buildRenamedEntryName(oldEntryName, normalizedNewBaseName);
        validateEntryName(newEntryName);

        logService.info("Arsiv yeniden adlandirma basladi: " + oldEntryName + " -> " + newEntryName + " | Engine: " + engine.displayName());
        int count = engine.rename(archivePath, oldEntryName, newEntryName);
        String message = "Arsiv girdisi yeniden adlandirildi: " + newEntryName;
        logService.info(message);
        return new ArchiveOperationResult(true, message, count);
    }

    public ArchiveOperationResult testZip(Path archivePath) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireTest(engine.capabilities());
        logService.info("Arsiv detayli test basladi: " + archivePath + " | Engine: " + engine.displayName());
        ArchiveTestResultModel result = engine.test(archivePath);
        String message = buildArchiveTestMessage(result);
        logService.info(message.replace(System.lineSeparator(), " | "));
        return new ArchiveOperationResult(!result.hasWarnings(), message, result.totalEntries());
    }

    private String buildArchiveTestMessage(ArchiveTestResultModel result) {
        StringBuilder builder = new StringBuilder();
        builder.append(result.hasWarnings() ? "Arsiv test tamamlandi, uyari var." : "Arsiv test basarili.");
        builder.append(System.lineSeparator()).append("Girdi: ").append(result.totalEntries());
        builder.append(System.lineSeparator()).append("Dosya: ").append(result.fileEntries());
        builder.append(System.lineSeparator()).append("Klasor: ").append(result.directoryEntries());
        builder.append(System.lineSeparator()).append("Okunan: ").append(formatBytes(result.testedBytes()));
        builder.append(System.lineSeparator()).append("Uyari: ").append(result.warningCount());
        if (result.hasWarnings()) {
            builder.append(System.lineSeparator()).append(System.lineSeparator()).append("Uyarilar:");
            int index = 1;
            for (String warning : result.warnings()) {
                builder.append(System.lineSeparator()).append(index).append(". ").append(warning);
                index++;
                if (index > 10) {
                    builder.append(System.lineSeparator()).append("...");
                    break;
                }
            }
        }
        return builder.toString();
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format(java.util.Locale.ROOT, "%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format(java.util.Locale.ROOT, "%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format(java.util.Locale.ROOT, "%.2f GB", gb);
    }

    public ArchiveOperationResult createZip(Path inputPath, Path outputZip) throws Exception {
        if (!fileSystemTool.exists(inputPath)) {
            throw new IllegalArgumentException("Girdi yolu bulunamadi: " + inputPath);
        }
        fileSystemTool.createParentDirectories(outputZip);
        logService.info("ZIP olusturma basladi: " + outputZip);
        int count = javaZipTool.createZip(inputPath, outputZip);
        String message = "ZIP olusturma tamamlandi. Dosya sayisi: " + count;
        logService.info(message);
        return new ArchiveOperationResult(true, message, count);
    }

    public byte[] readEntryPreviewBytes(Path archivePath, String entryName) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireList(engine.capabilities());
        validateEntryName(entryName);
        logService.info("Onizleme okunuyor: " + entryName);
        return engine.readEntryBytes(archivePath, entryName, 5 * 1024 * 1024);
    }


    public boolean isEditableTextEntry(String entryName) {
        return settingsService.isEditableTextExtension(entryName);
    }

    public boolean isEditableImageEntry(String entryName) {
        return settingsService.isEditableImageExtension(entryName);
    }

    public ArchiveOperationResult editTextEntry(Path archivePath, String entryName, String editorCommand) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireEditSaveBack(engine.capabilities());
        validateEntryName(entryName);
        if (!isEditableTextEntry(entryName)) {
            throw new IllegalArgumentException("Bu dosya metin duzenleme icin desteklenmiyor: " + entryName);
        }

        return editEntryWithExternalTool(
                archivePath,
                entryName,
                editorCommand,
                "unizip-text-edit-",
                "Metin editoru aciliyor: ",
                "Metin dosyasi kaydedildi ve ZIP guncellendi: ",
                "Degisiklik yok. ZIP guncellenmedi: "
        );
    }

    public ArchiveOperationResult editImageEntry(Path archivePath, String entryName, String editorCommand) throws Exception {
        ArchiveEngine engine = requireEngine(archivePath);
        archiveCommandGuard.requireEditSaveBack(engine.capabilities());
        validateEntryName(entryName);
        if (!isEditableImageEntry(entryName)) {
            throw new IllegalArgumentException("Bu dosya resim duzenleme icin desteklenmiyor: " + entryName);
        }

        return editEntryWithExternalTool(
                archivePath,
                entryName,
                editorCommand,
                "unizip-image-edit-",
                "Resim editoru aciliyor: ",
                "Resim dosyasi kaydedildi ve ZIP guncellendi: ",
                "Degisiklik yok. ZIP guncellenmedi: "
        );
    }

    private ArchiveOperationResult editEntryWithExternalTool(
            Path archivePath,
            String entryName,
            String editorCommand,
            String tempPrefix,
            String openingLogPrefix,
            String updatedMessagePrefix,
            String unchangedMessagePrefix
    ) throws Exception {
        Path tempDirectory = settingsService.createWorkingDirectory(tempPrefix);
        Path tempFile = tempDirectory.resolve(safeEditFileName(entryName));
        boolean cleanupTempDirectory = false;
        try {
            ArchiveEngine engine = requireEngine(archivePath);
            archiveCommandGuard.requireEditSaveBack(engine.capabilities());
            engine.exportSingleEntryToFile(archivePath, entryName, tempFile);
            String beforeHash = checksumTool.sha256(tempFile);

            logService.info(openingLogPrefix + entryName);
            externalEditorTool.openAndWait(tempFile, editorCommand);

            if (!Files.exists(tempFile)) {
                throw new IllegalStateException("Duzenlenen gecici dosya bulunamadi: " + tempFile);
            }

            String afterHash = checksumTool.sha256(tempFile);
            if (beforeHash.equals(afterHash)) {
                cleanupTempDirectory = true;
                String message = unchangedMessagePrefix + entryName;
                logService.info(message);
                return new ArchiveOperationResult(true, message, 0);
            }

            int count = engine.replace(archivePath, entryName, tempFile);
            cleanupTempDirectory = true;
            String message = updatedMessagePrefix + entryName;
            logService.info(message);
            return new ArchiveOperationResult(true, message, count);
        } finally {
            if (cleanupTempDirectory) {
                deleteTempDirectory(tempDirectory);
            }
        }
    }


    private String normalizeRenameBaseName(String value) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException("Yeni ad bos olamaz");
        }
        String name = value.trim().replace('\\', '/');
        if (name.contains("/") || name.equals("..") || name.contains("../")) {
            throw new IllegalArgumentException("Yeni ad klasor yolu iceremez: " + value);
        }
        String[] unsafeCharacters = {":", "*", "?", "\"", "<", ">", "|"};
        for (String unsafeCharacter : unsafeCharacters) {
            if (name.contains(unsafeCharacter)) {
                throw new IllegalArgumentException("Yeni ad gecersiz karakter iceriyor: " + unsafeCharacter);
            }
        }
        return name;
    }

    private String buildRenamedEntryName(String oldEntryName, String newBaseName) {
        String normalizedOldName = oldEntryName.replace('\\', '/');
        boolean directory = normalizedOldName.endsWith("/");
        String oldWithoutSlash = directory ? normalizedOldName.substring(0, normalizedOldName.length() - 1) : normalizedOldName;
        int slashIndex = oldWithoutSlash.lastIndexOf('/');
        String parent = slashIndex >= 0 ? oldWithoutSlash.substring(0, slashIndex + 1) : "";
        String normalizedNewName = directory && !newBaseName.endsWith("/") ? newBaseName + "/" : newBaseName;
        return parent + normalizedNewName;
    }

    private String safeEditFileName(String entryName) {
        String normalized = entryName == null ? "entry.txt" : entryName.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
        String safeName = fileName.trim();
        String[] unsafeCharacters = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        for (String unsafeCharacter : unsafeCharacters) {
            safeName = safeName.replace(unsafeCharacter, "_");
        }
        return safeName.isBlank() ? "entry.txt" : safeName;
    }

    private void deleteTempDirectory(Path tempDirectory) throws Exception {
        if (tempDirectory == null || !Files.exists(tempDirectory)) {
            return;
        }
        try (var stream = Files.walk(tempDirectory)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    public ArchiveSessionState buildSessionState(Path archivePath, boolean hasSelection, boolean hasEditableSelection, boolean folderSelection) {
        if (archivePath == null || !fileSystemTool.exists(archivePath)) {
            return ArchiveSessionState.empty();
        }
        ArchiveEngine engine = archiveEngineRegistry.resolve(archivePath);
        ArchiveCapabilities capabilities = engine.capabilities();
        boolean opened = capabilities.list();
        boolean readOnly = !Files.isWritable(archivePath);
        return new ArchiveSessionState(
                archivePath,
                engine.format(),
                engine.engineId(),
                engine.displayName(),
                capabilities,
                opened,
                readOnly,
                hasSelection,
                hasEditableSelection,
                folderSelection
        );
    }

    public ArchiveFormat detectArchiveFormat(Path archivePath) {
        return archiveEngineRegistry.detect(archivePath);
    }

    private ArchiveEngine requireEngine(Path archivePath) {
        if (!fileSystemTool.exists(archivePath)) {
            throw new IllegalArgumentException("Arsiv dosyasi bulunamadi: " + archivePath);
        }
        ArchiveEngine engine = archiveEngineRegistry.resolve(archivePath);
        if (!engine.capabilities().list()) {
            throw new IllegalArgumentException("Bu arsiv formati henuz desteklenmiyor: " + engine.format().displayName());
        }
        return engine;
    }

    private String archiveBaseName(Path archivePath) {
        if (archivePath == null || archivePath.getFileName() == null) {
            return "archive";
        }
        String fileName = archivePath.getFileName().toString();
        int dotIndex = fileName.toLowerCase().lastIndexOf(".zip");
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String safeName = baseName.trim();
        String[] unsafeCharacters = {"\\", "/", ":", "*", "?", "\"", "<", ">", "|"};
        for (String unsafeCharacter : unsafeCharacters) {
            safeName = safeName.replace(unsafeCharacter, "_");
        }
        return safeName.isBlank() ? "archive" : safeName;
    }

    private void validateEntryName(String entryName) {
        if (entryName == null || entryName.isBlank()) {
            throw new IllegalArgumentException("Girdi adi bos olamaz");
        }
        String normalized = entryName.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("../") || normalized.equals("..")) {
            throw new IllegalArgumentException("Guvenli olmayan girdi yolu: " + entryName);
        }
    }

    private void validateZipFile(Path archivePath) {
        if (!fileSystemTool.exists(archivePath)) {
            throw new IllegalArgumentException("ZIP dosyasi bulunamadi: " + archivePath);
        }
        if (!archivePath.toString().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Bu surum sadece ZIP destekler: " + archivePath);
        }
    }
}
