/*
# Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/ExplorerShellCommandServiceTest.java
# Amac: Explorer komut secimi ve ZIP cikti adi icin veri-koruyan kontrat testleri
# Modul - Java JUnit
# Version: 0.3.1
# Aciklama: Single-command whitelist, collision, dotted folder names and missing names
# Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class ExplorerShellCommandServiceTest {
    @TempDir Path temp;

    @Test
    void acceptsOnlyImplementedExplorerVerbs() {
        for (String verb : new String[] {
                "--extract-here", "--extract-to-folder", "--test",
                "--hash-sha256", "--add-to-archive"}) {
            assertTrue(ExplorerShellCommandService.supports(verb), verb);
        }
        assertFalse(ExplorerShellCommandService.supports("--erase"));
        assertFalse(ExplorerShellCommandService.supports("--extract-rar"));
        assertFalse(ExplorerShellCommandService.supports(null));
    }

    @Test
    void choosesNewArchiveNameWithoutOverwritingExistingOne() throws Exception {
        Path input = temp.resolve("one.txt");
        Files.writeString(input, "data");
        Path existing = temp.resolve("one.zip");
        Files.writeString(existing, "original");
        Path other = temp.resolve("one (2).zip");
        Files.writeString(other, "also original");

        assertEquals(temp.resolve("one (3).zip"),
                ExplorerShellCommandService.uniqueArchivePath(input));
        assertEquals("original", Files.readString(existing));
        assertEquals("also original", Files.readString(other));
    }

    @Test
    void preservesCompleteFolderNameContainingDots() throws Exception {
        Path folder = Files.createDirectory(temp.resolve("photos.2026"));
        assertEquals(temp.resolve("photos.2026.zip"),
                ExplorerShellCommandService.uniqueArchivePath(folder));
    }

    @Test
    void protectsExistingArchiveWithSameNameAsSelectedZip() throws Exception {
        Path input = temp.resolve("already.zip");
        Files.writeString(input, "dummy");
        assertEquals(temp.resolve("already (2).zip"),
                ExplorerShellCommandService.uniqueArchivePath(input));
    }
}
