/*
# Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/FileAssociationRegistryRoundTripTest.java
# Amac: Windows reg.exe kurulum-kaldirma semantigini izole HKCU namespace'de gercekten dogrulamak
# Modul - JUnit Windows integration test
# Version: 0.3.1
# Aciklama: Temp Registry subkey; no changes to live HKCU\Software\Classes or user defaults
# Bagimli Oldugu Katman: WindowsRegistryTool | FileAssociationService
*/
package com.unizip.desktop.services;

import com.unizip.desktop.tools.WindowsRegistryTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.WINDOWS)
final class FileAssociationRegistryRoundTripTest {
    @Test
    void windowsRegistryImporterInstallsAndRemovesIsolatedExplorerVerbs() throws Exception {
        WindowsRegistryTool registry = new WindowsRegistryTool();
        FileAssociationService service = new FileAssociationService(registry);

        String subtree = "Software\\UniZip\\CI-Registry-Tests\\" +
                UUID.randomUUID().toString().replace("-", "");
        String originalRoot = "HKEY_CURRENT_USER\\Software\\Classes\\";
        String isolatedRoot = "HKEY_CURRENT_USER\\" + subtree + "\\";

        // Rewrite every HKCU Classes path, including legacy deletion entries,
        // into a fresh sandbox owned by this test. Never touch live Explorer verbs.
        String install = service.buildContextMenuScript()
                .replace(originalRoot, isolatedRoot);
        String remove = service.buildContextMenuRemovalScript()
                .replace(originalRoot, isolatedRoot);

        assertFalse(install.contains(originalRoot));
        assertFalse(remove.contains(originalRoot));
        assertFalse(install.contains("HKEY_LOCAL_MACHINE"));
        try {
            registry.importRegistryScript(install);

            String archive = subtree + "\\SystemFileAssociations\\.zip\\shell\\UniZip";
            String regular = subtree + "\\*\\shell\\UniZip.Compress";
            assertEquals("UniZip",
                    registry.queryValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                            archive, "MUIVerb").orElseThrow());
            assertEquals("NOT System.FileExtension:=.zip",
                    registry.queryValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                            regular, "AppliesTo").orElseThrow());
            String zipTestCommand = registry.queryDefaultValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER,
                    archive + "\\shell\\test\\command").orElseThrow();
            assertTrue(zipTestCommand.contains("--test"));
            assertTrue(zipTestCommand.contains("%1"));

            registry.importRegistryScript(remove);
            assertTrue(registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER,
                    archive, "MUIVerb").isEmpty());
            assertTrue(registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER,
                    regular, "MUIVerb").isEmpty());
        } finally {
            // Clean all owned sandbox state, even if assertions or import fail.
            registry.deleteTreeIfExists(WindowsRegistryTool.ROOT_CURRENT_USER, subtree);
        }
    }
}
