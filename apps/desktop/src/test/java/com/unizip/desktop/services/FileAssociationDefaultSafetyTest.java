/*
# Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/FileAssociationDefaultSafetyTest.java
# Amac: UniZip default app aday kaydi mevcut ZIP handler ve UserChoice kaydini silmemeli
# Modul - Windows integration test
# Version: 0.3.1
# Aciklama: gercek reg.exe ile izole HKCU key testleri, baskalarinin ayarlarina dokunulmaz
# Bagimli Oldugu Katman: Service | WindowsRegistryTool
*/
package com.unizip.desktop.services;

import com.unizip.desktop.tools.WindowsRegistryTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.WINDOWS)
final class FileAssociationDefaultSafetyTest {
    @Test
    void registeringUnizipCandidateDoesNotReplaceDefaultHandlerOrUserChoice() throws Exception {
        WindowsRegistryTool registry = new WindowsRegistryTool();
        FileAssociationService service = new FileAssociationService(registry);
        String prefix = "Software\\UniZip\\CI-Registry-Tests\\FileDefault-"
                + UUID.randomUUID().toString().replace("-", "");
        String actualRoot = "HKEY_CURRENT_USER\\Software\\";
        String isolatedRoot = "HKEY_CURRENT_USER\\" + prefix + "\\";
        String thirdPartyDefault = prefix + "\\Classes\\.zip";
        String userChoice = prefix
                + "\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\.zip\\UserChoice";
        try {
            registry.addDefaultValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                    thirdPartyDefault, "Existing.Zip.App");
            registry.addStringValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                    userChoice, "ProgId", "Existing.Zip.App");

            String script = service.buildRegistryScript(
                    WindowsRegistryTool.ROOT_CURRENT_USER,
                    List.of("zip"),
                    "\"C:\\Tools\\UniZip Pro.exe\" \"%1\"",
                    "C:\\Tools\\UniZip Pro.exe,0",
                    false, false).replace(actualRoot, isolatedRoot);
            // The sandbox itself lives under HKCU\\Software, so assert the real
            // top-level shell and capabilities locations are never referenced.
            assertFalse(script.contains("HKEY_CURRENT_USER\\Software\\Classes\\"));
            assertFalse(script.contains("HKEY_CURRENT_USER\\Software\\RegisteredApplications]"));
            assertFalse(script.contains("HKEY_CURRENT_USER\\Software\\UniZip\\Capabilities]"));
            assertFalse(script.contains("HKEY_LOCAL_MACHINE"));
            assertFalse(script.contains("UserChoice"));
            assertFalse(script.contains("[-HKEY_CURRENT_USER"));
            registry.importRegistryScript(script);

            assertEquals("Existing.Zip.App", registry.queryDefaultValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, thirdPartyDefault).orElseThrow());
            assertEquals("Existing.Zip.App", registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, userChoice, "ProgId").orElseThrow());
            assertTrue(registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER,
                    prefix + "\\Classes\\.zip\\OpenWithProgids", "UniZip.Archive").isPresent());
            assertEquals("Software\\UniZip\\Capabilities", registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER,
                    prefix + "\\RegisteredApplications", "UniZip").orElseThrow());
        } finally {
            registry.deleteTreeIfExists(WindowsRegistryTool.ROOT_CURRENT_USER, prefix);
        }
    }
}
