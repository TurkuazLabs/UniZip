/*
# Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/tools/ChecksumSidecarTest.java
# Amac: Explorer SHA-256 output ve no-overwrite contract testleri
# Modul - Java JUnit 5
# Version: 0.3.1
# Aciklama: Create-new SHA-256 sidecar, name collision, path with spaces and input validation
# Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ChecksumSidecarTest {
    @TempDir
    Path temp;

    @Test
    void createsCorrectSha256SidecarForFileWithSpaces() throws Exception {
        Path input = temp.resolve("Türkçe dosya.txt");
        Files.writeString(input, "abc", StandardCharsets.UTF_8);
        Path sidecar = new ChecksumTool().writeSha256Sidecar(input);
        assertEquals(input.getFileName() + ".sha256", sidecar.getFileName().toString());
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
                        + "  Türkçe dosya.txt", Files.readString(sidecar).trim());
    }

    @Test
    void neverOverwritesAnExistingSidecar() throws Exception {
        Path input = temp.resolve("source.bin");
        Files.writeString(input, "hello");
        Path existing = temp.resolve("source.bin.sha256");
        Files.writeString(existing, "leave this alone");
        Path next = new ChecksumTool().writeSha256Sidecar(input);
        assertEquals("source.bin (2).sha256", next.getFileName().toString());
        assertEquals("leave this alone", Files.readString(existing));
        assertTrue(Files.readString(next).contains("source.bin"));
    }

    @Test
    void rejectsDirectoriesAndMissingFiles() throws Exception {
        ChecksumTool tool = new ChecksumTool();
        assertThrows(java.io.IOException.class, () -> tool.writeSha256Sidecar(temp));
        assertThrows(java.io.IOException.class, () -> tool.writeSha256Sidecar(temp.resolve("missing.zip")));
    }
}
