/*
# 📄 Dosya Yolu: /apps/desktop/src/test/java/com/unizip/desktop/tools/ArchiveExtractionSecurityTest.java
# 📌 Amac: ZIP traversal, symlink ve resource limit regression testlerini dogrulamak
# 📌 Modul - Java
# Version: 0.3.1
# Aciklama: Cikarma sinirlarinda mevcut dosyayi degistirmeden fail-closed davranisi test eder
Bagimli Oldugu Katman: Tool | Config
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.config.ArchiveExtractionLimits;
import com.unizip.desktop.models.ExtractOverwriteMode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

final class ArchiveExtractionSecurityTest {
    @TempDir
    Path temp;

    @Test
    void rejectsParentTraversal() throws Exception {
        Path archive = zipWithEntry("../outside.txt", "out");
        Path output = temp.resolve("out");
        assertThrows(IOException.class, () -> new JavaZipTool(new SafeExtractTool())
                .extract(archive, output, List.of(), ExtractOverwriteMode.OVERWRITE));
        assertFalse(Files.exists(temp.resolve("outside.txt")));
    }

    @Test
    void rejectsExistingSymlinkDirectory() throws Exception {
        Path outside = Files.createDirectory(temp.resolve("outside"));
        Path output = Files.createDirectory(temp.resolve("out"));
        try {
            Files.createSymbolicLink(output.resolve("linked"), outside);
        } catch (IOException | UnsupportedOperationException | SecurityException exception) {
            assumeTrue(false, "OS cannot create test symlink");
        }
        Path archive = zipWithEntry("linked/file.txt", "data");
        assertThrows(IOException.class, () -> new JavaZipTool(new SafeExtractTool())
                .extract(archive, output, List.of(), ExtractOverwriteMode.OVERWRITE));
        assertFalse(Files.exists(outside.resolve("file.txt")));
    }

    @Test
    void extractionLimitPreservesExistingFile() throws Exception {
        Path archive = zipWithEntry("existing.txt", "content exceeds tiny extraction limit");
        Path output = Files.createDirectory(temp.resolve("out"));
        Files.writeString(output.resolve("existing.txt"), "original", StandardCharsets.UTF_8);
        JavaZipTool tool = new JavaZipTool(
                new SafeExtractTool(), new ArchiveExtractionLimits(10, 16, 32));
        assertThrows(IOException.class, () ->
                tool.extract(archive, output, List.of(), ExtractOverwriteMode.OVERWRITE));
        assertEquals("original", Files.readString(output.resolve("existing.txt")));
    }

    @Test
    void extractionLimitAppliesToDirectoryExport() throws Exception {
        Path archive = zipWithEntry("folder/file.txt", "content exceeds tiny extraction limit");
        Path output = Files.createDirectory(temp.resolve("out"));
        JavaZipTool tool = new JavaZipTool(
                new SafeExtractTool(), new ArchiveExtractionLimits(10, 16, 32));
        assertThrows(IOException.class, () ->
                tool.exportEntriesToDirectory(archive, List.of("folder"), output));
        assertFalse(Files.exists(output.resolve("folder/file.txt")));
    }

    private Path zipWithEntry(String name, String content) throws IOException {
        Path archive = Files.createTempFile(temp, "unizip-security-", ".zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
            output.putNextEntry(new ZipEntry(name));
            output.write(content.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return archive;
    }
}
