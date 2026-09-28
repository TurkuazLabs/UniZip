/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/tools/UpdateManifestTest.java
# 📌 Amac: UniZip guncelleme manifest guvenlik kurallarini otomatik test etmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Urun kodu, SHA-256 ve dosya adi normalizasyonunu dogrular

Bagimli Oldugu Katman: Repo/Model | Tool | CI
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.UpdateManifest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class UpdateManifestTest {
    private static final String VALID_SHA = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    @Test
    void validManifestPassesValidation() {
        UpdateManifest manifest = new UpdateManifest(
                "UNIZIP",
                "0.1.62",
                "stable",
                false,
                "0.1.0",
                "https://unizip.turkuaz.com/releases/UniZip_Setup_v0.1.62.exe",
                VALID_SHA,
                0L,
                "2026-07-19T00:00:00Z",
                "Test"
        );
        manifest.validate();
        assertEquals("UniZip_Setup_v0.1.62.exe", manifest.fileName());
    }

    @Test
    void invalidHashIsRejected() {
        UpdateManifest manifest = new UpdateManifest(
                "UNIZIP", "0.1.62", "stable", false, "0.1.0",
                "https://unizip.turkuaz.com/releases/update.exe", "bad", 0L, "", ""
        );
        assertThrows(IllegalArgumentException.class, manifest::validate);
    }
}
