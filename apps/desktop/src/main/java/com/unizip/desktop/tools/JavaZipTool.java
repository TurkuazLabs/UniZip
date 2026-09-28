/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/JavaZipTool.java
# 📌 Amac: Java built-in ZIP motoru ile ZIP listeleme, cikarma ve olusturma yapmak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: java.util.zip tabanli Community ZIP araci, detayli test raporu ve entry islemleri araci

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveTestResultModel;
import com.unizip.desktop.models.ExtractOverwriteMode;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class JavaZipTool {
    private final SafeExtractTool safeExtractTool;

    public JavaZipTool(SafeExtractTool safeExtractTool) {
        this.safeExtractTool = safeExtractTool;
    }

    public List<ArchiveEntryModel> listEntries(Path zipFile) throws IOException {
        List<ArchiveEntryModel> entries = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entries.add(new ArchiveEntryModel(entry.getName(), entry.getSize(), entry.isDirectory()));
                zipInputStream.closeEntry();
            }
        }
        return entries;
    }

    public byte[] readEntryBytes(Path zipFile, String entryName, int maxBytes) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.getName().equals(entryName)) {
                    zipInputStream.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    return new byte[0];
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int total = 0;
                int read;
                while ((read = zipInputStream.read(buffer)) >= 0) {
                    total += read;
                    if (total > maxBytes) {
                        throw new IOException("Onizleme dosyasi cok buyuk: " + entryName);
                    }
                    outputStream.write(buffer, 0, read);
                }
                zipInputStream.closeEntry();
                return outputStream.toByteArray();
            }
        }
        throw new IOException("ZIP girdisi bulunamadi: " + entryName);
    }

    public ArchiveTestResultModel testZip(Path zipFile) throws IOException {
        int totalEntries = 0;
        int fileEntries = 0;
        int directoryEntries = 0;
        long testedBytes = 0L;
        List<String> warnings = new ArrayList<>();
        Set<String> seenEntryNames = new HashSet<>();

        Path safetyRoot = Files.createTempDirectory("unizip-test-safe-root-");
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zipInputStream.getNextEntry()) != null) {
                totalEntries++;
                String entryName = normalizeEntryName(entry.getName());
                if (entryName.isBlank()) {
                    warnings.add("Bos isimli ZIP girdisi bulundu");
                }
                if (!seenEntryNames.add(entryName)) {
                    warnings.add("Ayni isimli tekrar eden girdi: " + entryName);
                }
                try {
                    safeExtractTool.resolveSafeTarget(safetyRoot, entryName);
                } catch (IOException exception) {
                    warnings.add(exception.getMessage());
                }

                if (entry.isDirectory()) {
                    directoryEntries++;
                    zipInputStream.closeEntry();
                    continue;
                }

                fileEntries++;
                long entryBytes = 0L;
                int read;
                while ((read = zipInputStream.read(buffer)) >= 0) {
                    entryBytes += read;
                    testedBytes += read;
                }
                if (entry.getSize() >= 0 && entry.getSize() != entryBytes) {
                    warnings.add("Boyut uyusmazligi: " + entryName + " beklenen=" + entry.getSize() + " okunan=" + entryBytes);
                }
                zipInputStream.closeEntry();
            }
        } finally {
            try {
                Files.deleteIfExists(safetyRoot);
            } catch (IOException exception) {
                warnings.add("Gecici test klasoru temizlenemedi: " + safetyRoot);
            }
        }

        return new ArchiveTestResultModel(totalEntries, fileEntries, directoryEntries, testedBytes, warnings);
    }

    public int extract(Path zipFile, Path outputDirectory) throws IOException {
        return extract(zipFile, outputDirectory, List.of(), ExtractOverwriteMode.OVERWRITE);
    }

    public int extract(Path zipFile, Path outputDirectory, List<String> selectedEntryNames, ExtractOverwriteMode overwriteMode) throws IOException {
        Set<String> selectedNames = normalizedSelection(selectedEntryNames);
        ExtractOverwriteMode mode = overwriteMode == null ? ExtractOverwriteMode.OVERWRITE : overwriteMode;
        int count = 0;
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = normalizeEntryName(entry.getName());
                if (!selectedNames.isEmpty() && !shouldDeleteEntry(entryName, selectedNames)) {
                    zipInputStream.closeEntry();
                    continue;
                }

                Path targetPath = safeExtractTool.resolveSafeTarget(outputDirectory, entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Path finalTargetPath = resolveExtractTarget(targetPath, mode);
                    if (finalTargetPath == null) {
                        zipInputStream.closeEntry();
                        continue;
                    }
                    Path parent = finalTargetPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(finalTargetPath))) {
                        zipInputStream.transferTo(outputStream);
                    }
                    count++;
                }
                zipInputStream.closeEntry();
            }
        }
        return count;
    }

    private Path resolveExtractTarget(Path targetPath, ExtractOverwriteMode overwriteMode) throws IOException {
        if (!Files.exists(targetPath)) {
            return targetPath;
        }
        if (overwriteMode == ExtractOverwriteMode.SKIP) {
            return null;
        }
        if (overwriteMode == ExtractOverwriteMode.RENAME) {
            return uniqueExtractTarget(targetPath);
        }
        return targetPath;
    }

    private Path uniqueExtractTarget(Path targetPath) throws IOException {
        Path parent = targetPath.getParent();
        if (parent == null) {
            parent = Path.of(".");
        }
        String fileName = targetPath.getFileName() == null ? "file" : targetPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex > 0 ? fileName.substring(dotIndex) : "";
        int index = 1;
        Path candidate;
        do {
            candidate = parent.resolve(baseName + " (" + index + ")" + extension);
            index++;
        } while (Files.exists(candidate));
        return candidate;
    }

    private Set<String> normalizedSelection(List<String> selectedEntryNames) {
        Set<String> selectedNames = new HashSet<>();
        if (selectedEntryNames == null) {
            return selectedNames;
        }
        for (String entryName : selectedEntryNames) {
            if (entryName == null || entryName.isBlank() || entryName.equals("..")) {
                continue;
            }
            String normalizedName = normalizeEntryName(entryName);
            selectedNames.add(normalizedName);
            if (!normalizedName.endsWith("/")) {
                selectedNames.add(normalizedName + "/");
            }
        }
        return selectedNames;
    }

    public List<String> findAddConflicts(Path zipFile, List<Path> inputPaths) throws IOException {
        Set<String> requestedNames = requestedRootEntryNames(inputPaths);
        if (requestedNames.isEmpty()) {
            return List.of();
        }

        Set<String> conflicts = new HashSet<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = normalizeEntryName(entry.getName());
                for (String requestedName : requestedNames) {
                    if (entryName.equals(requestedName) || entryName.startsWith(requestedName + "/")) {
                        conflicts.add(requestedName);
                    }
                }
                zipInputStream.closeEntry();
            }
        }
        return conflicts.stream().sorted().toList();
    }

    public int addToZip(Path zipFile, List<Path> inputPaths, boolean overwriteExisting) throws IOException {
        if (inputPaths == null || inputPaths.isEmpty()) {
            return 0;
        }

        Path parentDirectory = zipFile.getParent();
        if (parentDirectory == null) {
            parentDirectory = Path.of(".");
        }
        Path temporaryZip = Files.createTempFile(parentDirectory, "unizip-add-", ".zip");
        Set<String> usedEntryNames = new HashSet<>();
        Set<String> requestedRootNames = requestedRootEntryNames(inputPaths);
        int addedCount = 0;

        try {
            try (
                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)));
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(temporaryZip)))
            ) {
                copyExistingEntries(zipInputStream, zipOutputStream, usedEntryNames, requestedRootNames, overwriteExisting);

                for (Path inputPath : inputPaths) {
                    if (inputPath == null || !Files.exists(inputPath)) {
                        continue;
                    }
                    if (Files.isDirectory(inputPath)) {
                        addedCount += addDirectoryWithUniqueNames(zipOutputStream, inputPath, inputPath.getFileName(), usedEntryNames, overwriteExisting);
                    } else if (Files.isRegularFile(inputPath)) {
                        addFileWithRequestedName(zipOutputStream, inputPath, inputPath.getFileName(), usedEntryNames, overwriteExisting);
                        addedCount++;
                    }
                }
            }

            Files.move(temporaryZip, zipFile, StandardCopyOption.REPLACE_EXISTING);
            return addedCount;
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(temporaryZip);
            throw exception;
        }
    }

    private void copyExistingEntries(
            ZipInputStream zipInputStream,
            ZipOutputStream zipOutputStream,
            Set<String> usedEntryNames,
            Set<String> requestedRootNames,
            boolean overwriteExisting
    ) throws IOException {
        ZipEntry entry;
        byte[] buffer = new byte[8192];

        while ((entry = zipInputStream.getNextEntry()) != null) {
            String entryName = normalizeEntryName(entry.getName());
            safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), entryName);

            if (overwriteExisting && conflictsWithRequestedRoot(entryName, requestedRootNames)) {
                zipInputStream.closeEntry();
                continue;
            }

            if (usedEntryNames.contains(entryName)) {
                zipInputStream.closeEntry();
                continue;
            }

            ZipEntry copiedEntry = new ZipEntry(entryName);
            copiedEntry.setTime(entry.getTime());
            zipOutputStream.putNextEntry(copiedEntry);

            int read;
            while ((read = zipInputStream.read(buffer)) >= 0) {
                zipOutputStream.write(buffer, 0, read);
            }

            zipOutputStream.closeEntry();
            zipInputStream.closeEntry();
            usedEntryNames.add(entryName);
        }
    }

    private int addDirectoryWithUniqueNames(
            ZipOutputStream zipOutputStream,
            Path sourceDirectory,
            Path zipRoot,
            Set<String> usedEntryNames,
            boolean overwriteExisting
    ) throws IOException {
        int[] count = {0};
        try (var stream = Files.walk(sourceDirectory)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                try {
                    Path relative = sourceDirectory.relativize(path);
                    Path entryName = zipRoot.resolve(relative);
                    addFileWithRequestedName(zipOutputStream, path, entryName, usedEntryNames, overwriteExisting);
                    count[0]++;
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
        } catch (IllegalStateException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
        return count[0];
    }

    private void addFileWithRequestedName(
            ZipOutputStream zipOutputStream,
            Path sourceFile,
            Path requestedEntryName,
            Set<String> usedEntryNames,
            boolean overwriteExisting
    ) throws IOException {
        String normalizedName = normalizeEntryName(requestedEntryName.toString());
        String finalName = overwriteExisting ? normalizedName : uniqueEntryName(normalizedName, usedEntryNames);
        ZipEntry zipEntry = new ZipEntry(finalName);
        zipOutputStream.putNextEntry(zipEntry);

        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(sourceFile))) {
            inputStream.transferTo(zipOutputStream);
        }

        zipOutputStream.closeEntry();
        usedEntryNames.add(finalName);
    }

    private Set<String> requestedRootEntryNames(List<Path> inputPaths) {
        Set<String> requestedNames = new HashSet<>();
        if (inputPaths == null) {
            return requestedNames;
        }
        for (Path inputPath : inputPaths) {
            if (inputPath == null || inputPath.getFileName() == null) {
                continue;
            }
            requestedNames.add(normalizeEntryName(inputPath.getFileName().toString()));
        }
        return requestedNames;
    }

    private boolean conflictsWithRequestedRoot(String entryName, Set<String> requestedRootNames) {
        for (String requestedRootName : requestedRootNames) {
            if (entryName.equals(requestedRootName) || entryName.startsWith(requestedRootName + "/")) {
                return true;
            }
        }
        return false;
    }

    private String normalizeEntryName(String entryName) {
        return entryName.replace('\\', '/');
    }

    private String uniqueEntryName(String requestedName, Set<String> usedEntryNames) {
        if (!usedEntryNames.contains(requestedName)) {
            return requestedName;
        }

        int slashIndex = requestedName.lastIndexOf('/');
        String directory = slashIndex >= 0 ? requestedName.substring(0, slashIndex + 1) : "";
        String fileName = slashIndex >= 0 ? requestedName.substring(slashIndex + 1) : requestedName;

        int dotIndex = fileName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String extension = dotIndex > 0 ? fileName.substring(dotIndex) : "";

        int copyIndex = 1;
        String candidate;
        do {
            candidate = directory + baseName + " (" + copyIndex + ")" + extension;
            copyIndex++;
        } while (usedEntryNames.contains(candidate));

        return candidate;
    }

    public int exportEntriesToDirectory(Path zipFile, List<String> entryNames, Path outputDirectory) throws IOException {
        if (entryNames == null || entryNames.isEmpty()) {
            return 0;
        }

        Set<String> normalizedExportNames = new HashSet<>();
        for (String entryName : entryNames) {
            if (entryName == null || entryName.isBlank()) {
                continue;
            }
            String normalizedName = normalizeEntryName(entryName);
            normalizedExportNames.add(normalizedName);
            if (!normalizedName.endsWith("/")) {
                normalizedExportNames.add(normalizedName + "/");
            }
        }

        int exportedCount = 0;
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String entryName = normalizeEntryName(entry.getName());
                if (!shouldDeleteEntry(entryName, normalizedExportNames)) {
                    zipInputStream.closeEntry();
                    continue;
                }

                Path targetPath = safeExtractTool.resolveSafeTarget(outputDirectory, entryName);
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Path parent = targetPath.getParent();
                    if (parent != null) {
                        Files.createDirectories(parent);
                    }
                    try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(targetPath))) {
                        zipInputStream.transferTo(outputStream);
                    }
                    exportedCount++;
                }
                zipInputStream.closeEntry();
            }
        }
        return exportedCount;
    }

    public void exportSingleEntryToFile(Path zipFile, String entryName, Path outputFile) throws IOException {
        String normalizedTargetName = normalizeEntryName(entryName);
        safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), normalizedTargetName);

        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String currentName = normalizeEntryName(entry.getName());
                if (!currentName.equals(normalizedTargetName)) {
                    zipInputStream.closeEntry();
                    continue;
                }
                if (entry.isDirectory()) {
                    throw new IOException("Klasor harici editor ile acilamaz: " + entryName);
                }

                Path parent = outputFile.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFile))) {
                    zipInputStream.transferTo(outputStream);
                }
                zipInputStream.closeEntry();
                return;
            }
        }
        throw new IOException("ZIP girdisi bulunamadi: " + entryName);
    }

    public int replaceEntryFromFile(Path zipFile, String entryName, Path sourceFile) throws IOException {
        String normalizedTargetName = normalizeEntryName(entryName);
        safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), normalizedTargetName);

        Path parentDirectory = zipFile.getParent();
        if (parentDirectory == null) {
            parentDirectory = Path.of(".");
        }
        Path temporaryZip = Files.createTempFile(parentDirectory, "unizip-edit-", ".zip");
        int replacedCount = 0;

        try {
            try (
                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)));
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(temporaryZip)))
            ) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String currentName = normalizeEntryName(entry.getName());
                    safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), currentName);

                    if (currentName.equals(normalizedTargetName)) {
                        ZipEntry updatedEntry = new ZipEntry(normalizedTargetName);
                        updatedEntry.setTime(System.currentTimeMillis());
                        zipOutputStream.putNextEntry(updatedEntry);
                        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(sourceFile))) {
                            inputStream.transferTo(zipOutputStream);
                        }
                        zipOutputStream.closeEntry();
                        zipInputStream.closeEntry();
                        replacedCount++;
                        continue;
                    }

                    ZipEntry copiedEntry = new ZipEntry(currentName);
                    copiedEntry.setTime(entry.getTime());
                    zipOutputStream.putNextEntry(copiedEntry);

                    int read;
                    while ((read = zipInputStream.read(buffer)) >= 0) {
                        zipOutputStream.write(buffer, 0, read);
                    }

                    zipOutputStream.closeEntry();
                    zipInputStream.closeEntry();
                }
            }

            if (replacedCount == 0) {
                throw new IOException("Guncellenecek ZIP girdisi bulunamadi: " + entryName);
            }

            Files.move(temporaryZip, zipFile, StandardCopyOption.REPLACE_EXISTING);
            return replacedCount;
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(temporaryZip);
            throw exception;
        }
    }


    public int renameEntry(Path zipFile, String oldEntryName, String newEntryName) throws IOException {
        String normalizedOldName = normalizeEntryName(oldEntryName);
        String normalizedNewName = normalizeEntryName(newEntryName);
        safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), normalizedOldName);
        safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), normalizedNewName);

        if (normalizedOldName.equals(normalizedNewName)) {
            return 0;
        }
        if (entryExists(zipFile, normalizedNewName)) {
            throw new IOException("Hedef isim ZIP icinde zaten var: " + normalizedNewName);
        }

        Path parentDirectory = zipFile.getParent();
        if (parentDirectory == null) {
            parentDirectory = Path.of(".");
        }
        Path temporaryZip = Files.createTempFile(parentDirectory, "unizip-rename-", ".zip");
        int renamedCount = 0;

        try {
            try (
                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)));
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(temporaryZip)))
            ) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String currentName = normalizeEntryName(entry.getName());
                    safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), currentName);

                    String outputName = renameEntryName(currentName, normalizedOldName, normalizedNewName);
                    ZipEntry copiedEntry = new ZipEntry(outputName);
                    copiedEntry.setTime(entry.getTime());
                    zipOutputStream.putNextEntry(copiedEntry);

                    int read;
                    while ((read = zipInputStream.read(buffer)) >= 0) {
                        zipOutputStream.write(buffer, 0, read);
                    }

                    zipOutputStream.closeEntry();
                    zipInputStream.closeEntry();
                    if (!outputName.equals(currentName)) {
                        renamedCount++;
                    }
                }
            }

            if (renamedCount == 0) {
                throw new IOException("Yeniden adlandirilacak ZIP girdisi bulunamadi: " + oldEntryName);
            }

            Files.move(temporaryZip, zipFile, StandardCopyOption.REPLACE_EXISTING);
            return renamedCount;
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(temporaryZip);
            throw exception;
        }
    }

    private boolean entryExists(Path zipFile, String targetEntryName) throws IOException {
        String normalizedTarget = normalizeEntryName(targetEntryName);
        String normalizedTargetFolder = normalizedTarget.endsWith("/") ? normalizedTarget : normalizedTarget + "/";
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String currentName = normalizeEntryName(entry.getName());
                zipInputStream.closeEntry();
                if (currentName.equals(normalizedTarget)) {
                    return true;
                }
                if (targetEntryName.endsWith("/") && currentName.startsWith(normalizedTargetFolder)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String renameEntryName(String currentName, String oldName, String newName) {
        if (currentName.equals(oldName)) {
            return newName;
        }
        String oldFolder = oldName.endsWith("/") ? oldName : oldName + "/";
        String newFolder = newName.endsWith("/") ? newName : newName + "/";
        if (currentName.startsWith(oldFolder)) {
            return newFolder + currentName.substring(oldFolder.length());
        }
        return currentName;
    }

    public int deleteEntries(Path zipFile, List<String> entryNames) throws IOException {
        if (entryNames == null || entryNames.isEmpty()) {
            return 0;
        }

        Set<String> normalizedDeleteNames = new HashSet<>();
        for (String entryName : entryNames) {
            if (entryName == null || entryName.isBlank()) {
                continue;
            }
            String normalizedName = normalizeEntryName(entryName);
            normalizedDeleteNames.add(normalizedName);
            if (!normalizedName.endsWith("/")) {
                normalizedDeleteNames.add(normalizedName + "/");
            }
        }

        Path parentDirectory = zipFile.getParent();
        if (parentDirectory == null) {
            parentDirectory = Path.of(".");
        }
        Path temporaryZip = Files.createTempFile(parentDirectory, "unizip-delete-", ".zip");
        int deletedCount = 0;

        try {
            try (
                    ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)));
                    ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(temporaryZip)))
            ) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    String entryName = normalizeEntryName(entry.getName());
                    safeExtractTool.resolveSafeTarget(Path.of(".").toAbsolutePath().normalize(), entryName);

                    if (shouldDeleteEntry(entryName, normalizedDeleteNames)) {
                        deletedCount++;
                        zipInputStream.closeEntry();
                        continue;
                    }

                    ZipEntry copiedEntry = new ZipEntry(entryName);
                    copiedEntry.setTime(entry.getTime());
                    zipOutputStream.putNextEntry(copiedEntry);

                    int read;
                    while ((read = zipInputStream.read(buffer)) >= 0) {
                        zipOutputStream.write(buffer, 0, read);
                    }

                    zipOutputStream.closeEntry();
                    zipInputStream.closeEntry();
                }
            }

            Files.move(temporaryZip, zipFile, StandardCopyOption.REPLACE_EXISTING);
            return deletedCount;
        } catch (IOException | RuntimeException exception) {
            Files.deleteIfExists(temporaryZip);
            throw exception;
        }
    }

    private boolean shouldDeleteEntry(String entryName, Set<String> normalizedDeleteNames) {
        for (String deleteName : normalizedDeleteNames) {
            if (entryName.equals(deleteName)) {
                return true;
            }
            if (deleteName.endsWith("/") && entryName.startsWith(deleteName)) {
                return true;
            }
        }
        return false;
    }

    public int createZip(Path inputPath, Path outputZip) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(outputZip)))) {
            if (Files.isDirectory(inputPath)) {
                return addDirectory(zipOutputStream, inputPath, inputPath.getFileName());
            }
            addFile(zipOutputStream, inputPath, inputPath.getFileName());
            return 1;
        }
    }

    private int addDirectory(ZipOutputStream zipOutputStream, Path sourceDirectory, Path zipRoot) throws IOException {
        int[] count = {0};
        try (var stream = Files.walk(sourceDirectory)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                try {
                    Path relative = sourceDirectory.relativize(path);
                    Path entryName = zipRoot.resolve(relative);
                    addFile(zipOutputStream, path, entryName);
                    count[0]++;
                } catch (IOException exception) {
                    throw new IllegalStateException(exception);
                }
            });
        } catch (IllegalStateException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
        return count[0];
    }

    private void addFile(ZipOutputStream zipOutputStream, Path sourceFile, Path entryName) throws IOException {
        String normalizedName = entryName.toString().replace('\\', '/');
        ZipEntry zipEntry = new ZipEntry(normalizedName);
        zipOutputStream.putNextEntry(zipEntry);
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(sourceFile))) {
            inputStream.transferTo(zipOutputStream);
        }
        zipOutputStream.closeEntry();
    }
}
