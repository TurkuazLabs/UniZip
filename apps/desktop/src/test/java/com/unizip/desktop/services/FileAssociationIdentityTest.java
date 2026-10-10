/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/FileAssociationIdentityTest.java
# 📌 Amac: Community ve Pro EXE'lerinin Windows uygulama kimliklerini birbirinin uzerine yazmadan ayirmasini test etmek
# 📌 Modul - JUnit Java
# Version: 0.3.2
# Aciklama: Pro/Community ProgID, SupportedTypes, Capabilities, RegisteredApplications ve UserChoice regresyonu

Bagimli Oldugu Katman: Service | Repo/Model | Tool
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.WindowsFileAssociationIdentity;
import com.unizip.desktop.tools.WindowsRegistryTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class FileAssociationIdentityTest {
    private final FileAssociationService service = new FileAssociationService(new WindowsRegistryTool());

    @Test
    void packagedEditionsAdvertiseIndependentFileHandlers() throws Exception {
        Path folder = Files.createTempDirectory("unizip-identity-");
        Path community = Files.createFile(folder.resolve(WindowsFileAssociationIdentity.COMMUNITY_EXECUTABLE));
        Path pro = Files.createFile(folder.resolve(WindowsFileAssociationIdentity.PRO_EXECUTABLE));
        String old = System.getProperty("jpackage.app-path");
        try {
            System.setProperty("jpackage.app-path", community.toString());
            String communityScript = buildScript();
            assertTrue(communityScript.contains("Classes\\Applications\\UniZip.exe"));
            assertTrue(communityScript.contains("Classes\\UniZip.Archive]"));
            assertTrue(communityScript.contains("Software\\UniZip\\Capabilities"));
            assertTrue(communityScript.contains("\"UniZip\"="));
            assertFalse(communityScript.contains("UniZip.Pro.Archive"));

            System.setProperty("jpackage.app-path", pro.toString());
            String proScript = buildScript();
            assertTrue(proScript.contains("Classes\\Applications\\UniZip Pro.exe"));
            assertTrue(proScript.contains("Classes\\UniZip.Pro.Archive]"));
            assertTrue(proScript.contains("Software\\UniZip\\Pro\\Capabilities"));
            assertTrue(proScript.contains("\"UniZip Pro\"="));
            assertFalse(proScript.contains("[HKEY_CURRENT_USER\\Software\\Classes\\UniZip.Archive]"));
            assertFalse(proScript.contains("\"UniZip\"=\"Software"));
            assertNotEquals(communityScript, proScript);
            for (String script : List.of(communityScript, proScript)) {
                assertFalse(script.contains("UserChoice"));
                assertFalse(script.contains("[HKEY_CURRENT_USER\\Software\\Classes\\.zip]"));
                assertFalse(script.contains("[-HKEY_CURRENT_USER"));
            }
        } finally {
            restore(old);
            Files.deleteIfExists(pro);
            Files.deleteIfExists(community);
            Files.deleteIfExists(folder);
        }
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void windowsRegistryCanRetainBothCandidatesAndExistingThirdPartyDefault() throws Exception {
        WindowsRegistryTool registry = new WindowsRegistryTool();
        String root = "Software\\UniZip\\CI-Registry-Tests\\Edition-" +
                UUID.randomUUID().toString().replace("-", "");
        String defaultKey = root + "\\Classes\\.zip";
        String currentChoice = root + "\\Explorer\\UserChoice";
        String original = System.getProperty("jpackage.app-path");
        Path folder = Files.createTempDirectory("unizip-editions-");
        Path community = Files.createFile(folder.resolve(WindowsFileAssociationIdentity.COMMUNITY_EXECUTABLE));
        Path pro = Files.createFile(folder.resolve(WindowsFileAssociationIdentity.PRO_EXECUTABLE));
        try {
            registry.addDefaultValue(WindowsRegistryTool.ROOT_CURRENT_USER, defaultKey, "Other.Zip");
            registry.addStringValue(WindowsRegistryTool.ROOT_CURRENT_USER, currentChoice, "ProgId", "Other.Zip");
            String from = "HKEY_CURRENT_USER\\Software\\";
            String to = "HKEY_CURRENT_USER\\" + root + "\\";

            System.setProperty("jpackage.app-path", community.toString());
            registry.importRegistryScript(buildScript().replace(from, to));
            System.setProperty("jpackage.app-path", pro.toString());
            registry.importRegistryScript(buildScript().replace(from, to));

            assertEquals("Other.Zip", registry.queryDefaultValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, defaultKey).orElseThrow());
            assertEquals("Other.Zip", registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, currentChoice, "ProgId").orElseThrow());
            assertTrue(registry.queryDefaultValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                    root + "\\Classes\\UniZip.Archive\\shell\\open\\command").isPresent());
            assertTrue(registry.queryDefaultValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                    root + "\\Classes\\UniZip.Pro.Archive\\shell\\open\\command").isPresent());
            assertEquals("Software\\UniZip\\Capabilities", registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, root + "\\RegisteredApplications", "UniZip").orElseThrow());
            assertEquals("Software\\UniZip\\Pro\\Capabilities", registry.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, root + "\\RegisteredApplications", "UniZip Pro").orElseThrow());
        } finally {
            restore(original);
            registry.deleteTreeIfExists(WindowsRegistryTool.ROOT_CURRENT_USER, root);
            Files.deleteIfExists(pro);
            Files.deleteIfExists(community);
            Files.deleteIfExists(folder);
        }
    }

    private String buildScript() throws Exception {
        return service.buildRegistryScript(WindowsRegistryTool.ROOT_CURRENT_USER,
                List.of("zip", "7z"), "\"C:\\Apps\\UniZip.exe\" \"%1\"",
                "C:\\Apps\\UniZip.exe,0", false, false);
    }

    private void restore(String old) {
        if (old == null) System.clearProperty("jpackage.app-path");
        else System.setProperty("jpackage.app-path", old);
    }
}
