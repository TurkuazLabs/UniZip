/*
# Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/tools/ZipCreationSafetyTest.java
# Amac: Explorer ZIP olusturma sirasinda hedefi koruma, partial ZIP temizligi ve symlink dislama
# Modul - Java JUnit
# Version: 0.3.1
# Aciklama: existing destination, valid roundtrip, symlink and ZIP-in-source fail-closed regression tests
# Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

final class ZipCreationSafetyTest {
    @TempDir Path temp;
    private final JavaZipTool zip = new JavaZipTool(new SafeExtractTool());

    @Test
    void existingDestinationIsNeverTruncated() throws Exception {
        Path source = temp.resolve("data.txt");
        Path target = temp.resolve("data.zip");
        Files.writeString(source, "new source");
        Files.writeString(target, "existing important archive");
        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> zip.createZip(source, target));
        assertEquals("existing important archive", Files.readString(target));
        assertFalse(hasStagingFiles());
    }

    @Test
    void successfulCreationContainsSourceAndLeavesNoStagingFiles() throws Exception {
        Path source = temp.resolve("Turkce ornek.txt");
        Path target = temp.resolve("output.zip");
        Files.writeString(source, "hello", StandardCharsets.UTF_8);
        assertEquals(1, zip.createZip(source, target));
        try (ZipFile read = new ZipFile(target.toFile())) {
            assertNotNull(read.getEntry("Turkce ornek.txt"));
            try (var stream = read.getInputStream(read.getEntry("Turkce ornek.txt"))) {
                assertEquals("hello", new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        assertFalse(hasStagingFiles());
    }

    @Test
    void doesNotRecursivelyZipItsOwnOutput() throws Exception {
        Path sourceDir = Files.createDirectory(temp.resolve("data"));
        Files.writeString(sourceDir.resolve("item.txt"), "ok");
        Path target = sourceDir.resolve("output.zip");
        assertThrows(IOException.class, () -> zip.createZip(sourceDir, target));
        assertFalse(Files.exists(target));
        assertFalse(hasStagingFiles());
    }

    @Test
    void rejectsSymlinkInputsAndDoesNotPublishPartialArchives() throws Exception {
        Path sourceDir = Files.createDirectory(temp.resolve("originals"));
        Path outside = temp.resolve("private.txt");
        Files.writeString(outside, "secret");
        try {
            Files.createSymbolicLink(sourceDir.resolve("link.txt"), outside);
        } catch (IOException | UnsupportedOperationException | SecurityException exception) {
            assumeTrue(false, "Symlink permissions unavailable for test");
        }
        Path target = temp.resolve("originals.zip");
        assertThrows(IOException.class, () -> zip.createZip(sourceDir, target));
        assertFalse(Files.exists(target));
        assertFalse(hasStagingFiles());
    }

    private boolean hasStagingFiles() throws IOException {
        try (var files = Files.list(temp)) {
            return files.anyMatch(path -> path.getFileName().toString().startsWith(".unizip-create-"));
        }
    }
}
