/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/tools/ArchiveEngineCoreTest.java
# 📌 Amac: ArchiveEngine Core sisteminin otomatik testlerini calistirmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Format detector, registry ve capability state davranisini test eder

Bagimli Oldugu Katman: Tool | Service | Repo/Model
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveCapabilities;
import com.unizip.desktop.models.ArchiveFormat;
import com.unizip.desktop.models.ArchiveSessionState;
import com.unizip.desktop.models.ExtractOverwriteMode;
import com.unizip.desktop.services.ArchiveCommandGuard;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ArchiveEngineCoreTest {
    @Test
    void detectorReadsZipMagicHeader() throws Exception {
        Path zip = Files.createTempFile("unizip-detector-", ".bin");
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zip))) {
            outputStream.putNextEntry(new ZipEntry("note.txt"));
            outputStream.write("hello".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            outputStream.closeEntry();
        }

        ArchiveFormatDetector detector = new ArchiveFormatDetector();
        assertEquals(ArchiveFormat.ZIP, detector.detect(zip));
        Files.deleteIfExists(zip);
    }

    @Test
    void registryResolvesZipEngine() throws Exception {
        Path zip = Files.createTempFile("unizip-registry-", ".zip");
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(zip))) {
            outputStream.putNextEntry(new ZipEntry("a.txt"));
            outputStream.write("a".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            outputStream.closeEntry();
        }

        ArchiveEngineRegistry registry = ArchiveEngineRegistry.createDefault();
        ArchiveEngine engine = registry.resolve(zip);
        assertEquals(ArchiveFormat.ZIP, engine.format());
        assertTrue(engine.capabilities().add());
        assertEquals(1, engine.extract(zip, Files.createTempDirectory("unizip-extract-"), List.of(), ExtractOverwriteMode.OVERWRITE));
        Files.deleteIfExists(zip);
    }

    @Test
    void sessionStateCombinesCapabilityAndSelection() {
        ArchiveSessionState zipNoSelection = new ArchiveSessionState(
                Path.of("sample.zip"),
                ArchiveFormat.ZIP,
                "unizip.engine.zip.java",
                "UniZip ZIP Engine",
                ArchiveCapabilities.zipFull(),
                true,
                false,
                false,
                false,
                false
        );

        assertTrue(zipNoSelection.canAdd());
        assertFalse(zipNoSelection.canRename());
        assertFalse(zipNoSelection.canExtractSelected());

        ArchiveSessionState zipWithSelection = zipNoSelection.withSelection(true, true, false);
        assertTrue(zipWithSelection.canRename());
        assertTrue(zipWithSelection.canEditSaveBack());
        assertTrue(zipWithSelection.canExtractSelected());
    }

    @Test
    void commandGuardRejectsUnsupportedAdd() {
        ArchiveCommandGuard guard = new ArchiveCommandGuard();
        assertThrows(UnsupportedOperationException.class, () -> guard.requireAdd(ArchiveCapabilities.none()));
    }
}
