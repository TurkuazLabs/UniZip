/*
# Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/FileAssociationContextMenuTest.java
# Amac: Varsayilan programi degistirmeyen UniZip Explorer baglamsal menu ve jpackage EXE command parity testleri
# Modul - Java JUnit 5
# Version: 0.3.1
# Aciklama: Registry kurmadan TR/EN menu scripting, EXE launcher ve ZIP shell command kontratini dogrular
# Bagimli Oldugu Katman: Service | Tool | Config
*/
package com.unizip.desktop.services;

import com.unizip.desktop.tools.WindowsRegistryTool;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileAssociationContextMenuTest {

    private final FileAssociationService service = new FileAssociationService(new WindowsRegistryTool());

    @Test
    void contextMenuIsPerUserAndDoesNotChangeDefaultZipHandler() throws Exception {
        String script = service.buildContextMenuScript();
        assertTrue(script.startsWith("Windows Registry Editor Version 5.00"));
        assertTrue(script.contains("HKEY_CURRENT_USER\\Software\\Classes\\SystemFileAssociations\\.zip\\shell\\UniZip"));
        assertTrue(script.contains("HKEY_CURRENT_USER\\Software\\Classes\\*\\shell\\UniZip.Compress"));
        assertTrue(script.contains("HKEY_CURRENT_USER\\Software\\Classes\\Directory\\shell\\UniZip.Compress"));
        assertFalse(script.contains("HKEY_LOCAL_MACHINE"));
        assertFalse(script.contains("[HKEY_CURRENT_USER\\Software\\Classes\\.zip]"));
        assertFalse(script.contains("UserChoice"));
        assertFalse(script.contains("RegisteredApplications"));
    }

    @Test
    void zipSelectionUsesOnlyArchiveMenuAndGenericFilesStillHaveCompression() throws Exception {
        String script = service.buildContextMenuScript();
        assertTrue(script.contains(
                "\"AppliesTo\"=\"NOT System.FileExtension:=.zip"
                + " AND NOT System.FileExtension:=.sha256"
                + " AND NOT System.FileExtension:=.sha512"
                + " AND NOT System.FileExtension:=.crc32\""));
        assertTrue(script.contains(
                "Software\\Classes\\SystemFileAssociations\\.zip\\shell\\UniZip"));
        assertTrue(script.contains(
                "Software\\Classes\\*\\shell\\UniZip.Compress"));
    }

    @Test
    void fileAssociationsCannotReintroduceLegacyMenusOrDeleteNewOnes() throws Exception {
        String script = service.buildRegistryScript(
                WindowsRegistryTool.ROOT_CURRENT_USER,
                java.util.List.of("zip", "7z"),
                "\"C:\\Apps\\UniZip Pro.exe\" \"%1\"",
                "C:\\Apps\\UniZip Pro.exe,0", true, true);
        assertFalse(script.contains("shell\\UniZip.Compress"));
        assertFalse(script.contains("shell\\UniZip]"));
        assertFalse(script.contains("[-HKEY_CURRENT_USER"));
        assertFalse(script.contains("\"AppliesTo\""));
        assertFalse(script.contains("SystemFileAssociations"));
        assertTrue(script.contains("HKEY_CURRENT_USER\\Software\\Classes\\.zip\\OpenWithProgids"));
        assertFalse(script.contains("[HKEY_CURRENT_USER\\Software\\Classes\\.zip]"));
        assertFalse(script.contains("\\UserChoice"));
        assertFalse(script.contains("[-HKEY_CURRENT_USER"));
    }

    @Test
    void potentialHandlerRegistrationNeverSetsOrDeletesTheDefaultApp() throws Exception {
        String script = service.buildRegistryScript(
                WindowsRegistryTool.ROOT_CURRENT_USER, java.util.List.of("zip"),
                "\"UniZip.exe\" \"%1\"", "UniZip.exe,0", false, false);
        assertTrue(script.contains("Software\\\\RegisteredApplications"));
        assertTrue(script.contains("Software\\\\Classes\\\\.zip\\\\OpenWithProgids"));
        assertTrue(script.contains("Software\\\\UniZip\\\\Capabilities\\\\FileAssociations"));
        assertFalse(script.contains("[HKEY_CURRENT_USER\\\\Software\\\\Classes\\\\.zip]"));
        assertFalse(script.contains("UserChoice"));
        assertFalse(script.contains("[-"));
        assertFalse(script.contains("UniZip.Compress"));
        assertFalse(script.contains("SystemFileAssociations"));
    }

    @Test
    void packagedProApplicationRegistersItsActualExecutableName() throws Exception {
        String old = System.getProperty("jpackage.app-path");
        Path launcher = Files.createTempFile("UniZip Pro ", ".exe");
        try {
            System.setProperty("jpackage.app-path", launcher.toString());
            String script = service.buildRegistryScript(
                    WindowsRegistryTool.ROOT_CURRENT_USER, java.util.List.of("zip"),
                    "\"C:\\\\Apps\\\\UniZip Pro.exe\" \"%1\"",
                    "C:\\\\Apps\\\\UniZip Pro.exe,0", false, false);
            assertTrue(script.contains("Applications\\\\"
                    + launcher.getFileName() + "]"));
            assertFalse(script.contains("Applications\\\\UniZip.exe]"));
        } finally {
            if (old == null) System.clearProperty("jpackage.app-path");
            else System.setProperty("jpackage.app-path", old);
            Files.deleteIfExists(launcher);
        }
    }

    @Test
    void installationMigratesOwnedLegacyMenusWithoutChangingUserDefaults() throws Exception {
        String script = service.buildContextMenuScript();
        assertTrue(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\*\\shell\\UniZip]"));
        assertTrue(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\Directory\\shell\\UniZip]"));
        assertTrue(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\UniZip.Archive\\shell\\UniZip]"));
        assertFalse(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\.zip]"));
        assertFalse(script.contains("\\UserChoice"));
        assertFalse(script.contains("[-HKEY_LOCAL_MACHINE"));
    }

    @Test
    void uninstallOnlyDeletesUnizipOwnedVerbsAndNeverTouchesAssociations() {
        String script = service.buildContextMenuRemovalScript();
        assertTrue(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\SystemFileAssociations\\.zip\\shell\\UniZip]"));
        assertTrue(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\*\\shell\\UniZip.Compress]"));
        assertTrue(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\Directory\\shell\\UniZip.Compress]"));
        assertFalse(script.contains("[-HKEY_CURRENT_USER\\Software\\Classes\\.zip]"));
        assertFalse(script.contains("\\UserChoice"));
        assertFalse(script.contains("[-HKEY_LOCAL_MACHINE"));
    }

    @Test
    void menuOnlyOffersCommandsImplementedByCurrentZipEngine() throws Exception {
        String script = service.buildContextMenuScript();
        assertTrue(script.contains("--extract-here"));
        assertTrue(script.contains("--extract-to-folder"));
        assertTrue(script.contains("--test"));
        assertTrue(script.contains("--hash-sha256"));
        assertTrue(script.contains("--hash-sha512"));
        assertTrue(script.contains("--hash-crc32"));
        assertTrue(script.contains("--verify-checksum"));
        assertTrue(script.contains("--add-to-archive"));
        assertTrue(script.contains("\"MUIVerb\"=\"UniZip\""));
        assertFalse(script.contains("--encrypt"));
        assertFalse(script.contains("--extract-rar"));
        assertFalse(script.contains("--hash-md5"));
    }

    @Test
    void everyExplorerVerbExplicitlyRefusesMultipleSelections() throws Exception {
        String script = service.buildContextMenuScript();
        assertTrue(script.contains("\"MultiSelectModel\"=\"Single\""));
        String[] sections = script.split("(?=\\[HKEY_CURRENT_USER\\\\)");
        int commands = 0;
        for (String section : sections) {
            if (section.contains("]") && section.contains("command]")) {
                continue;
            }
            if (section.contains("MultiSelectModel") && section.contains("UniZip")) {
                assertTrue(section.contains("Single"), "Every UniZip verb must be single selection");
                commands++;
            }
        }
        assertTrue(commands >= 7, "Expected both root menus and all subcommands");
    }

    @Test
    void packagedProLauncherIsPreservedInsteadOfStartingCommunityMainApp() throws Exception {
        String original = System.getProperty("jpackage.app-path");
        Path launcher = Files.createTempFile("UniZip Pro launcher ", ".exe");
        try {
            System.setProperty("jpackage.app-path", launcher.toString());
            String script = service.buildContextMenuScript();
            String executable = launcher.toAbsolutePath().normalize().toString();
            assertTrue(script.contains(executable.replace("\\", "\\\\")));
            assertFalse(script.contains("com.unizip.desktop.MainApp"));
            assertFalse(script.contains("javaw.exe"));
        } finally {
            if (original == null) {
                System.clearProperty("jpackage.app-path");
            } else {
                System.setProperty("jpackage.app-path", original);
            }
            Files.deleteIfExists(launcher);
        }
    }

    @Test
    void absentPackagedLauncherFallsBackToJavaWithoutInjectingShellArguments() throws Exception {
        String original = System.getProperty("jpackage.app-path");
        try {
            System.setProperty("jpackage.app-path", "nonexistent-UniZip.exe");
            String script = service.buildContextMenuScript();
            assertTrue(script.contains("com.unizip.desktop.MainApp") || script.contains("-jar"));
            assertTrue(script.contains("%1"));
        } finally {
            if (original == null) {
                System.clearProperty("jpackage.app-path");
            } else {
                System.setProperty("jpackage.app-path", original);
            }
        }
    }
}
