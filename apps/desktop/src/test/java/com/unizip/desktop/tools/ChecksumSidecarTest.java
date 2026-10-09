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
    void sha512AndCrc32ProduceKnownHashesAndNeverOverwrite() throws Exception {
        Path input = temp.resolve("Türkçe file.bin");
        Files.writeString(input, "abc", StandardCharsets.UTF_8);
        ChecksumTool tool = new ChecksumTool();

        Path sha = tool.writeSha512Sidecar(input);
        Path crc = tool.writeCrc32Sidecar(input);
        String expectedSha = "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a"
                + "2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f";
        assertEquals(expectedSha + "  Türkçe file.bin", Files.readString(sha).trim());
        assertEquals("352441c2  Türkçe file.bin", Files.readString(crc).trim());

        Path second = tool.writeSha512Sidecar(input);
        assertEquals("Türkçe file.bin (2).sha512", second.getFileName().toString());
        assertEquals(expectedSha + "  Türkçe file.bin", Files.readString(sha).trim());
        assertEquals(expectedSha + "  Türkçe file.bin", Files.readString(second).trim());
    }

    @Test
    void hashRejectsSymlinkEvenWhenItPointsToRegularFile() throws Exception {
        Path input = temp.resolve("real.txt");
        Files.writeString(input, "abc");
        Path link = temp.resolve("link.txt");
        try {
            Files.createSymbolicLink(link, input);
        } catch (java.io.IOException | UnsupportedOperationException | SecurityException error) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "No symlink permission on test host");
        }
        assertThrows(java.io.IOException.class, () -> new ChecksumTool().writeSha512Sidecar(link));
    }

    @Test
    void rejectsDirectoriesAndMissingFiles() throws Exception {
        ChecksumTool tool = new ChecksumTool();
        assertThrows(java.io.IOException.class, () -> tool.writeSha256Sidecar(temp));
        assertThrows(java.io.IOException.class, () -> tool.writeSha256Sidecar(temp.resolve("missing.zip")));
    }
}
