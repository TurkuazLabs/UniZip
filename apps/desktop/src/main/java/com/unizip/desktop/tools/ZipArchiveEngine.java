/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ZipArchiveEngine.java
# 📌 Amac: Mevcut JavaZipTool islemlerini ArchiveEngine sistemine baglamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ZIP icin tam destek bildiren engine adapter sinifi

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveCapabilities;
import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveEngineDescriptor;
import com.unizip.desktop.models.ArchiveFormat;
import com.unizip.desktop.models.ArchiveTestResultModel;
import com.unizip.desktop.models.ExtractOverwriteMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ZipArchiveEngine implements ArchiveEngine {
    private final JavaZipTool javaZipTool;

    public ZipArchiveEngine(JavaZipTool javaZipTool) {
        this.javaZipTool = javaZipTool;
    }

    @Override
    public String engineId() {
        return "unizip.engine.zip.java";
    }

    @Override
    public String displayName() {
        return "UniZip ZIP Engine";
    }

    @Override
    public ArchiveFormat format() {
        return ArchiveFormat.ZIP;
    }

    @Override
    public ArchiveCapabilities capabilities() {
        return ArchiveCapabilities.zipFull();
    }

    @Override
    public ArchiveEngineDescriptor descriptor() {
        return new ArchiveEngineDescriptor(
                engineId(),
                displayName(),
                format(),
                List.of("zip", "jar", "war", "ear"),
                List.of("50 4B 03 04", "50 4B 05 06", "50 4B 07 08"),
                capabilities()
        );
    }

    @Override
    public boolean canOpen(Path archivePath) {
        return archivePath != null && Files.isRegularFile(archivePath) && new ArchiveFormatDetector().detect(archivePath) == ArchiveFormat.ZIP;
    }

    @Override
    public List<ArchiveEntryModel> list(Path archivePath) throws Exception {
        return javaZipTool.listEntries(archivePath);
    }

    @Override
    public int extract(Path archivePath, Path outputDirectory, List<String> selectedEntries, ExtractOverwriteMode overwriteMode) throws Exception {
        return javaZipTool.extract(archivePath, outputDirectory, selectedEntries, overwriteMode);
    }

    @Override
    public ArchiveTestResultModel test(Path archivePath) throws Exception {
        return javaZipTool.testZip(archivePath);
    }

    @Override
    public List<String> findAddConflicts(Path archivePath, List<Path> inputPaths) throws Exception {
        return javaZipTool.findAddConflicts(archivePath, inputPaths);
    }

    @Override
    public int add(Path archivePath, List<Path> inputPaths, boolean overwriteExisting) throws Exception {
        return javaZipTool.addToZip(archivePath, inputPaths, overwriteExisting);
    }

    @Override
    public int delete(Path archivePath, List<String> entryNames) throws Exception {
        return javaZipTool.deleteEntries(archivePath, entryNames);
    }

    @Override
    public int rename(Path archivePath, String oldEntryName, String newEntryName) throws Exception {
        return javaZipTool.renameEntry(archivePath, oldEntryName, newEntryName);
    }

    @Override
    public void exportSingleEntryToFile(Path archivePath, String entryName, Path outputFile) throws Exception {
        javaZipTool.exportSingleEntryToFile(archivePath, entryName, outputFile);
    }

    @Override
    public int exportEntriesToDirectory(Path archivePath, List<String> entryNames, Path outputDirectory) throws Exception {
        return javaZipTool.exportEntriesToDirectory(archivePath, entryNames, outputDirectory);
    }

    @Override
    public int replace(Path archivePath, String entryName, Path sourceFile) throws Exception {
        return javaZipTool.replaceEntryFromFile(archivePath, entryName, sourceFile);
    }

    @Override
    public byte[] readEntryBytes(Path archivePath, String entryName, int maxBytes) throws Exception {
        return javaZipTool.readEntryBytes(archivePath, entryName, maxBytes);
    }
}
