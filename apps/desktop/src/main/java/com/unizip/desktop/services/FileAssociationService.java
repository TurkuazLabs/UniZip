/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/FileAssociationService.java
# 📌 Amac: UniZip arsiv uzantilarini ve Windows sag tik menulerini Registry uzerinden yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: UniZip ProgID, Explorer context menu, HKCU/HKLM .reg import ve CLI komut baglantilarini yazar

Bagimli Oldugu Katman: Service | Tool | Repo/Model
*/
package com.unizip.desktop.services;

import com.unizip.desktop.MainApp;
import com.unizip.desktop.models.FileAssociationScope;
import com.unizip.desktop.models.FileAssociationStatusModel;
import com.unizip.desktop.tools.WindowsRegistryTool;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class FileAssociationService {
    private static final String APP_NAME = "UniZip";
    private static final String APP_EXE_NAME = "UniZip.exe";
    private static final String PROG_ID = "UniZip.Archive";
    private static final String PROG_ID_DESCRIPTION = "UniZip Archive";
    private static final String APPLICATIONS_KEY = "Software\\Classes\\Applications\\" + APP_EXE_NAME;
    private static final String CAPABILITIES_KEY = "Software\\UniZip\\Capabilities";
    private static final String REGISTERED_APPLICATIONS_KEY = "Software\\RegisteredApplications";
    private static final String FILE_EXTS_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts";

    private final WindowsRegistryTool registryTool;

    public FileAssociationService(WindowsRegistryTool registryTool) {
        this.registryTool = registryTool;
    }

    public boolean isWindows() {
        return registryTool.isWindows();
    }

    public List<FileAssociationStatusModel> associationStatuses(List<String> extensions) {
        List<FileAssociationStatusModel> statuses = new ArrayList<>();
        for (String extension : normalizeExtensions(extensions)) {
            statuses.add(new FileAssociationStatusModel(
                    extension,
                    displayName(currentUserAssociationValue(extension)),
                    displayName(allUsersAssociationValue(extension))
            ));
        }
        return statuses;
    }

    public void associateExtensions(
            FileAssociationScope scope,
            List<String> extensions,
            boolean contextMenuEnabled,
            boolean groupContextMenu
    ) throws Exception {
        if (!registryTool.isWindows()) {
            throw new IllegalStateException("Windows Registry sadece Windows uzerinde desteklenir");
        }
        List<String> normalizedExtensions = normalizeExtensions(extensions);
        if (normalizedExtensions.isEmpty()) {
            throw new IllegalArgumentException("Iliskilendirilecek uzanti bulunamadi");
        }
        String root = scope == FileAssociationScope.ALL_USERS
                ? WindowsRegistryTool.ROOT_LOCAL_MACHINE
                : WindowsRegistryTool.ROOT_CURRENT_USER;
        String openCommand = buildOpenCommand();
        String defaultIcon = buildDefaultIcon();
        String registryScript = buildRegistryScript(
                root,
                normalizedExtensions,
                openCommand,
                defaultIcon,
                contextMenuEnabled,
                groupContextMenu
        );
        registryTool.importRegistryScript(registryScript);
        if (scope == FileAssociationScope.CURRENT_USER) {
            for (String extension : normalizedExtensions) {
                clearCurrentUserChoice(extension);
            }
        }
    }

    private String buildRegistryScript(
            String root,
            List<String> extensions,
            String openCommand,
            String defaultIcon,
            boolean contextMenuEnabled,
            boolean groupContextMenu
    ) throws Exception {
        String rootName = registryRootName(root);
        StringBuilder script = new StringBuilder();
        script.append("Windows Registry Editor Version 5.00\r\n\r\n");

        addDefault(script, rootName, APPLICATIONS_KEY, APP_NAME);
        addDefault(script, rootName, APPLICATIONS_KEY + "\\DefaultIcon", defaultIcon);
        addDefault(script, rootName, APPLICATIONS_KEY + "\\shell", "open");
        addDefault(script, rootName, APPLICATIONS_KEY + "\\shell\\open", "UniZip ile ac");
        addDefault(script, rootName, APPLICATIONS_KEY + "\\shell\\open\\command", openCommand);

        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID, PROG_ID_DESCRIPTION);
        addString(script, rootName, "Software\\Classes\\" + PROG_ID, "FriendlyTypeName", PROG_ID_DESCRIPTION);
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\DefaultIcon", defaultIcon);
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell", "open");
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\open", "UniZip ile ac");
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\open\\command", openCommand);

        addString(script, rootName, CAPABILITIES_KEY, "ApplicationName", APP_NAME);
        addString(script, rootName, CAPABILITIES_KEY, "ApplicationDescription", "UniZip Community archive manager");
        addString(script, rootName, REGISTERED_APPLICATIONS_KEY, APP_NAME, CAPABILITIES_KEY);

        for (String extension : extensions) {
            String dottedExtension = dotExtension(extension);
            addDefault(script, rootName, "Software\\Classes\\" + dottedExtension, PROG_ID);
            addString(script, rootName, CAPABILITIES_KEY + "\\FileAssociations", dottedExtension, PROG_ID);
            addString(script, rootName, APPLICATIONS_KEY + "\\SupportedTypes", dottedExtension, "");
            addOpenWithProgId(script, rootName, "Software\\Classes\\" + dottedExtension + "\\OpenWithProgids", PROG_ID);
        }

        clearOldContextMenus(script, rootName);
        if (contextMenuEnabled) {
            addArchiveContextMenu(script, rootName, defaultIcon, groupContextMenu);
            addInputContextMenu(script, rootName, defaultIcon, groupContextMenu);
        }
        return script.toString();
    }

    private void clearOldContextMenus(StringBuilder script, String rootName) {
        deleteKey(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip");
        deleteKey(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.Open");
        deleteKey(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.ExtractHere");
        deleteKey(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.ExtractToFolder");
        deleteKey(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.Test");
        deleteKey(script, rootName, "Software\\Classes\\*\\shell\\UniZip");
        deleteKey(script, rootName, "Software\\Classes\\*\\shell\\UniZip.AddToArchive");
        deleteKey(script, rootName, "Software\\Classes\\Directory\\shell\\UniZip");
        deleteKey(script, rootName, "Software\\Classes\\Directory\\shell\\UniZip.AddToArchive");
    }

    private void addArchiveContextMenu(StringBuilder script, String rootName, String defaultIcon, boolean grouped) throws Exception {
        String openCommand = buildOpenCommand();
        String extractHereCommand = buildCommand("--extract-here", "%1");
        String extractToFolderCommand = buildCommand("--extract-to-folder", "%1");
        String testCommand = buildCommand("--test", "%1");
        if (grouped) {
            String menuKey = "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip";
            addString(script, rootName, menuKey, "MUIVerb", "UniZip");
            addString(script, rootName, menuKey, "Icon", defaultIcon);
            addString(script, rootName, menuKey, "SubCommands", "");
            addSubCommand(script, rootName, menuKey, "open", "UniZip ile ac", defaultIcon, openCommand);
            addSubCommand(script, rootName, menuKey, "extract_here", "Buraya cikar", defaultIcon, extractHereCommand);
            addSubCommand(script, rootName, menuKey, "extract_to_folder", "Klasore cikar", defaultIcon, extractToFolderCommand);
            addSubCommand(script, rootName, menuKey, "test", "Arsivi sina", defaultIcon, testCommand);
            return;
        }
        addShellCommand(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.Open", "UniZip ile ac", defaultIcon, openCommand);
        addShellCommand(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.ExtractHere", "Buraya cikar", defaultIcon, extractHereCommand);
        addShellCommand(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.ExtractToFolder", "Klasore cikar", defaultIcon, extractToFolderCommand);
        addShellCommand(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\UniZip.Test", "Arsivi sina", defaultIcon, testCommand);
    }

    private void addInputContextMenu(StringBuilder script, String rootName, String defaultIcon, boolean grouped) throws Exception {
        String addCommand = buildCommand("--add-to-archive", "%1");
        if (grouped) {
            addGroupedInputMenu(script, rootName, "Software\\Classes\\*\\shell\\UniZip", defaultIcon, addCommand);
            addGroupedInputMenu(script, rootName, "Software\\Classes\\Directory\\shell\\UniZip", defaultIcon, addCommand);
            return;
        }
        addShellCommand(script, rootName, "Software\\Classes\\*\\shell\\UniZip.AddToArchive", "UniZip arsive ekle", defaultIcon, addCommand);
        addShellCommand(script, rootName, "Software\\Classes\\Directory\\shell\\UniZip.AddToArchive", "UniZip arsive ekle", defaultIcon, addCommand);
    }

    private void addGroupedInputMenu(StringBuilder script, String rootName, String menuKey, String defaultIcon, String addCommand) {
        addString(script, rootName, menuKey, "MUIVerb", "UniZip");
        addString(script, rootName, menuKey, "Icon", defaultIcon);
        addString(script, rootName, menuKey, "SubCommands", "");
        addSubCommand(script, rootName, menuKey, "add_to_archive", "Arsive ekle", defaultIcon, addCommand);
    }

    private void addSubCommand(StringBuilder script, String rootName, String parentKey, String commandKey, String label, String icon, String command) {
        addShellCommand(script, rootName, parentKey + "\\shell\\" + commandKey, label, icon, command);
    }

    private void addShellCommand(StringBuilder script, String rootName, String key, String label, String icon, String command) {
        addDefault(script, rootName, key, label);
        addString(script, rootName, key, "Icon", icon);
        addDefault(script, rootName, key + "\\command", command);
    }

    private void addDefault(StringBuilder script, String rootName, String key, String value) {
        addKeyHeader(script, rootName, key);
        script.append("@=\"").append(regEscape(value)).append("\"\r\n\r\n");
    }

    private void addString(StringBuilder script, String rootName, String key, String valueName, String value) {
        addKeyHeader(script, rootName, key);
        script.append("\"").append(regEscape(valueName)).append("\"=\"").append(regEscape(value)).append("\"\r\n\r\n");
    }

    private void addOpenWithProgId(StringBuilder script, String rootName, String key, String valueName) {
        addKeyHeader(script, rootName, key);
        script.append("\"").append(regEscape(valueName)).append("\"=hex(0):\r\n\r\n");
    }

    private void addKeyHeader(StringBuilder script, String rootName, String key) {
        script.append("[").append(rootName).append("\\").append(key).append("]\r\n");
    }

    private void deleteKey(StringBuilder script, String rootName, String key) {
        script.append("[-").append(rootName).append("\\").append(key).append("]\r\n\r\n");
    }

    private String registryRootName(String root) {
        if (WindowsRegistryTool.ROOT_LOCAL_MACHINE.equals(root)) {
            return "HKEY_LOCAL_MACHINE";
        }
        return "HKEY_CURRENT_USER";
    }

    private String regEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Optional<String> currentUserAssociationValue(String extension) {
        String extensionKey = "Software\\Classes\\" + dotExtension(extension);
        Optional<String> userChoiceValue = registryTool.queryValue(
                WindowsRegistryTool.ROOT_CURRENT_USER,
                FILE_EXTS_KEY + "\\" + dotExtension(extension) + "\\UserChoice",
                "ProgId"
        );
        if (userChoiceValue.isPresent()) {
            return userChoiceValue;
        }
        return registryTool.queryDefaultValue(WindowsRegistryTool.ROOT_CURRENT_USER, extensionKey)
                .or(() -> registryTool.queryDefaultValue(WindowsRegistryTool.ROOT_LOCAL_MACHINE, extensionKey));
    }

    private Optional<String> allUsersAssociationValue(String extension) {
        String extensionKey = "Software\\Classes\\" + dotExtension(extension);
        return registryTool.queryDefaultValue(WindowsRegistryTool.ROOT_LOCAL_MACHINE, extensionKey);
    }

    private void clearCurrentUserChoice(String extension) {
        String userChoiceKey = FILE_EXTS_KEY + "\\" + dotExtension(extension) + "\\UserChoice";
        registryTool.deleteTreeIfExists(WindowsRegistryTool.ROOT_CURRENT_USER, userChoiceKey);
    }

    private List<String> normalizeExtensions(List<String> extensions) {
        Set<String> normalized = new LinkedHashSet<>();
        if (extensions == null) {
            return new ArrayList<>();
        }
        for (String extension : extensions) {
            String value = normalizeExtension(extension);
            if (!value.isBlank()) {
                normalized.add(value);
            }
        }
        return new ArrayList<>(normalized);
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        String normalized = extension.trim().toLowerCase(Locale.ROOT);
        while (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()
                || normalized.contains("/")
                || normalized.contains("\\")
                || normalized.contains(":")
                || normalized.contains("*")
                || normalized.contains("?")
                || normalized.contains("\"")
                || normalized.contains("<")
                || normalized.contains(">")
                || normalized.contains("|")) {
            return "";
        }
        return normalized;
    }

    private String displayName(Optional<String> rawValue) {
        String value = rawValue.orElse("").trim();
        if (value.isBlank()) {
            return "";
        }
        if (PROG_ID.equalsIgnoreCase(value) || value.toLowerCase(Locale.ROOT).contains("unizip")) {
            return APP_NAME;
        }
        return "";
    }

    private String dotExtension(String extension) {
        return "." + normalizeExtension(extension);
    }

    private String buildOpenCommand() throws Exception {
        return buildCommand(null, "%1");
    }

    private String buildCommand(String option, String argumentExpression) throws Exception {
        String javawPath = javaLauncherPath();
        URI codeSourceUri = MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path codeSourcePath = Path.of(codeSourceUri).toAbsolutePath().normalize();
        StringBuilder builder = new StringBuilder();
        builder.append(quote(javawPath)).append(' ');
        if (Files.isRegularFile(codeSourcePath) && codeSourcePath.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
            builder.append("-jar ").append(quote(codeSourcePath.toString()));
        } else {
            String classPath = System.getProperty("java.class.path", ".");
            builder.append("-cp ").append(quote(classPath)).append(" com.unizip.desktop.MainApp");
        }
        if (option != null && !option.isBlank()) {
            builder.append(' ').append(option);
        }
        builder.append(' ').append(quote(argumentExpression));
        return builder.toString();
    }

    private String buildDefaultIcon() throws Exception {
        URI codeSourceUri = MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path codeSourcePath = Path.of(codeSourceUri).toAbsolutePath().normalize();
        if (Files.isRegularFile(codeSourcePath)) {
            return codeSourcePath.toString() + ",0";
        }
        String javawPath = javaLauncherPath();
        return javawPath + ",0";
    }

    private String javaLauncherPath() {
        Path javawPath = Path.of(System.getProperty("java.home"), "bin", "javaw.exe");
        if (Files.exists(javawPath)) {
            return javawPath.toString();
        }
        return Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    }

    private String quote(String value) {
        return "\"" + value.replace("\"", "") + "\"";
    }
}
