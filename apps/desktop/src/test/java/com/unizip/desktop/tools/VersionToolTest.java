/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/tools/VersionToolTest.java
# 📌 Amac: UniZip semantik surum karsilastirma kurallarini otomatik test etmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Yeni, esit ve on surum karsilastirmalarini JUnit ile dogrular

Bagimli Oldugu Katman: Tool | CI
*/
package com.unizip.desktop.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VersionToolTest {
    private final VersionTool versionTool = new VersionTool();

    @Test
    void newerPatchVersionIsDetected() {
        assertTrue(versionTool.compare("0.1.62", "0.1.61") > 0);
    }

    @Test
    void equalVersionsAreEqual() {
        assertEquals(0, versionTool.compare("v0.1.61", "0.1.61"));
    }

    @Test
    void stableVersionIsNewerThanPreRelease() {
        assertTrue(versionTool.compare("0.1.61", "0.1.61-beta") > 0);
    }
}
