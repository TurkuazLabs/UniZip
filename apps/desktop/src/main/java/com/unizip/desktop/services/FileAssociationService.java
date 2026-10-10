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
import com.unizip.desktop.models.ArchiveFormat;
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
    private static final String APP_EXE_NAME = "UniZip.exe"; // developer fallback only
    private static final String PROG_ID = "UniZip.Archive";
    private static final String PROG_ID_DESCRIPTION = "UniZip Archive";
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
        List<String> normalizedExtensions = supportedAssociationExtensions(extensions);
        if (normalizedExtensions.isEmpty()) {
            throw new IllegalArgumentException("Bu surumde iliskilendirme icin desteklenen arsiv uzantisi bulunamadi (ZIP)");
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
        // Register as an available handler only: Windows 10/11 protects default
        // selection and UserChoice. Menu install has its own explicit Settings action.
        registryTool.importRegistryScript(registryScript);
    }

    // Explorer verbs can be installed for HKCU without changing the user's default ZIP application.
    public void installContextMenuCurrentUser() throws Exception {
        if (!registryTool.isWindows()) {
            throw new IllegalStateException("Explorer menu is available only on Windows");
        }
        registryTool.importRegistryScript(buildContextMenuScript());
    }

    public void removeContextMenuCurrentUser() throws Exception {
        if (!registryTool.isWindows()) {
            throw new IllegalStateException("Explorer menu is available only on Windows");
        }
        registryTool.importRegistryScript(buildContextMenuRemovalScript());
    }

    // Package-private for side-effect-free registry ownership tests.
    String buildContextMenuRemovalScript() {
        String root = registryRootName(WindowsRegistryTool.ROOT_CURRENT_USER);
        StringBuilder script = new StringBuilder("Windows Registry Editor Version 5.00\r\n\r\n");
        clearOldContextMenus(script, root);
        for (String key : contextMenuRegistryKeys()) {
            deleteKey(script, root, key);
        }
        return script.toString();
    }

    public enum ExplorerMenuStatus {
        NOT_INSTALLED, REPAIR_REQUIRED, READY
    }

    public boolean contextMenuInstalledForCurrentUser() {
        return contextMenuStatusForCurrentUser() == ExplorerMenuStatus.READY;
    }

    public ExplorerMenuStatus contextMenuStatusForCurrentUser() {
        return contextMenuStatus("Software\\Classes");
    }

    // Allow Windows integration tests to check an isolated HKCU registry tree.
    ExplorerMenuStatus contextMenuStatus(String classesPrefix) {
        if (!registryTool.isWindows()) {
            return ExplorerMenuStatus.NOT_INSTALLED;
        }
        int present = 0;
        for (String key : contextMenuRegistryKeys()) {
            String resolvedKey = classesPrefix + key.substring("Software\\Classes".length());
            if (registryTool.queryValue(
                    WindowsRegistryTool.ROOT_CURRENT_USER, resolvedKey, "MUIVerb"
            ).filter("UniZip"::equals).isPresent()) {
                present++;
            }
        }
        if (present == 0) {
            return ExplorerMenuStatus.NOT_INSTALLED;
        }
        if (present != contextMenuRegistryKeys().size()) {
            return ExplorerMenuStatus.REPAIR_REQUIRED;
        }
        try {
            boolean healthy = commandMatches(classesPrefix,
                    "SystemFileAssociations\\.zip\\shell\\UniZip\\shell\\open\\command",
                    buildOpenCommand())
                    && commandMatches(classesPrefix,
                    "SystemFileAssociations\\.zip\\shell\\UniZip\\shell\\test\\command",
                    buildCommand("--test", "%1"))
                    && commandMatches(classesPrefix,
                    "*\\shell\\UniZip.Compress\\shell\\add_to_archive\\command",
                    buildCommand("--add-to-archive", "%1"))
                    && commandMatches(classesPrefix,
                    "SystemFileAssociations\\.sha256\\shell\\UniZip.Verify\\command",
                    buildCommand("--verify-checksum", "%1"));
            return healthy ? ExplorerMenuStatus.READY : ExplorerMenuStatus.REPAIR_REQUIRED;
        } catch (Exception exception) {
            return ExplorerMenuStatus.REPAIR_REQUIRED;
        }
    }

    private boolean commandMatches(String classesPrefix, String relativePath, String expected) {
        return registryTool.queryDefaultValue(WindowsRegistryTool.ROOT_CURRENT_USER,
                classesPrefix + "\\" + relativePath).filter(expected::equals).isPresent();
    }

    // Package-private so contract tests can inspect the exact, import-ready registry script.
    String buildContextMenuScript() throws Exception {
        String root = registryRootName(WindowsRegistryTool.ROOT_CURRENT_USER);
        StringBuilder script = new StringBuilder("Windows Registry Editor Version 5.00\r\n\r\n");
        // Retire UniZip-owned legacy context verbs without altering default app associations.
        clearOldContextMenus(script, root);
        String icon = buildDefaultIcon();
        String archiveKey = "Software\\Classes\\SystemFileAssociations\\.zip\\shell\\UniZip";
        // Using SystemFileAssociations keeps verbs available if another ZIP program is default.
        addString(script, root, archiveKey, "MUIVerb", "UniZip");
        addString(script, root, archiveKey, "Icon", icon);
        addString(script, root, archiveKey, "SubCommands", "");
        addString(script, root, archiveKey, "MultiSelectModel", "Single");
        addSubCommand(script, root, archiveKey, "open", "UniZip ile ac",
                icon, buildOpenCommand());
        addSubCommand(script, root, archiveKey, "extract_here", "Buraya cikar",
                icon, buildCommand("--extract-here", "%1"));
        addSubCommand(script, root, archiveKey, "extract_folder", "Klasore cikar",
                icon, buildCommand("--extract-to-folder", "%1"));
        addSubCommand(script, root, archiveKey, "test", "Arsivi sina",
                icon, buildCommand("--test", "%1"));
        addSubCommand(script, root, archiveKey, "hash_sha256", "SHA-256 olustur",
                icon, buildCommand("--hash-sha256", "%1"));
        addSubCommand(script, root, archiveKey, "hash_sha512", "SHA-512 olustur",
                icon, buildCommand("--hash-sha512", "%1"));
        addSubCommand(script, root, archiveKey, "hash_crc32", "CRC-32 olustur",
                icon, buildCommand("--hash-crc32", "%1"));

        // Dedicated checksum-file verbs avoid treating untrusted manifests as archives.
        for (String extension : List.of(".sha256", ".sha512", ".crc32")) {
            String verifyKey = "Software\\Classes\\SystemFileAssociations\\" + extension
                    + "\\shell\\UniZip.Verify";
            addString(script, root, verifyKey, "MUIVerb", "UniZip");
            addString(script, root, verifyKey, "Icon", icon);
            addString(script, root, verifyKey, "MultiSelectModel", "Single");
            addDefault(script, root, verifyKey + "\\command",
                    buildCommand("--verify-checksum", "%1"));
        }

        // Static Windows verbs receive one selected path via %1. Multi-select requires a
        // separate IExplorerCommand implementation; do not claim multi-select support.
        String addCommand = buildCommand("--add-to-archive", "%1");
        addGroupedInputMenu(script, root,
                "Software\\Classes\\*\\shell\\UniZip.Compress", icon, addCommand);
        // Show the archive-specific menu on .zip rather than a second generic
        // file menu. Uses Windows Shell's documented fast-property AQS filter.
        addString(script, root,
                "Software\\Classes\\*\\shell\\UniZip.Compress",
                "AppliesTo", "NOT System.FileExtension:=.zip"
                        + " AND NOT System.FileExtension:=.sha256"
                        + " AND NOT System.FileExtension:=.sha512"
                        + " AND NOT System.FileExtension:=.crc32");
        addSubCommand(script, root, "Software\\Classes\\*\\shell\\UniZip.Compress",
                "hash_sha256", "SHA-256 olustur", icon,
                buildCommand("--hash-sha256", "%1"));
        addSubCommand(script, root, "Software\\Classes\\*\\shell\\UniZip.Compress",
                "hash_sha512", "SHA-512 olustur", icon,
                buildCommand("--hash-sha512", "%1"));
        addSubCommand(script, root, "Software\\Classes\\*\\shell\\UniZip.Compress",
                "hash_crc32", "CRC-32 olustur", icon,
                buildCommand("--hash-crc32", "%1"));
        addGroupedInputMenu(script, root,
                "Software\\Classes\\Directory\\shell\\UniZip.Compress", icon, addCommand);
        return script.toString();
    }

    private List<String> contextMenuRegistryKeys() {
        return List.of(
                "Software\\Classes\\SystemFileAssociations\\.zip\\shell\\UniZip",
                "Software\\Classes\\*\\shell\\UniZip.Compress",
                "Software\\Classes\\Directory\\shell\\UniZip.Compress",
                "Software\\Classes\\SystemFileAssociations\\.sha256\\shell\\UniZip.Verify",
                "Software\\Classes\\SystemFileAssociations\\.sha512\\shell\\UniZip.Verify",
                "Software\\Classes\\SystemFileAssociations\\.crc32\\shell\\UniZip.Verify"
        );
    }

    // Package-private for isolated association/menu non-duplication regression tests.
    String buildRegistryScript(
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

        String applicationsKey = "Software\\Classes\\Applications\\" + applicationExecutableName();
        addDefault(script, rootName, applicationsKey, APP_NAME);
        addDefault(script, rootName, applicationsKey + "\\DefaultIcon", defaultIcon);
        addDefault(script, rootName, applicationsKey + "\\shell", "open");
        addDefault(script, rootName, applicationsKey + "\\shell\\open", "UniZip ile ac");
        addDefault(script, rootName, applicationsKey + "\\shell\\open\\command", openCommand);

        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID, PROG_ID_DESCRIPTION);
        addString(script, rootName, "Software\\Classes\\" + PROG_ID, "FriendlyTypeName", PROG_ID_DESCRIPTION);
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\DefaultIcon", defaultIcon);
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell", "open");
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\open", "UniZip ile ac");
        addDefault(script, rootName, "Software\\Classes\\" + PROG_ID + "\\shell\\open\\command", openCommand);

        addString(script, rootName, CAPABILITIES_KEY, "ApplicationName", APP_NAME);
        addString(script, rootName, CAPABILITIES_KEY, "ApplicationDescription", "UniZip Community archive manager");
        addString(script, rootName, REGISTERED_APPLICATIONS_KEY, APP_NAME, CAPABILITIES_KEY);

        for (String extension : supportedAssociationExtensions(extensions)) {
            String dottedExtension = dotExtension(extension);
            // OpenWithProgids and RegisteredApplications advertise candidate support.
            // NEVER set HKCU/HKLM \\.zip default ProgID here. The user chooses in
            // Settings > Apps > Default apps.
            addString(script, rootName, CAPABILITIES_KEY + "\\FileAssociations", dottedExtension, PROG_ID);
            addString(script, rootName, applicationsKey + "\\SupportedTypes", dottedExtension, "");
            addOpenWithProgId(script, rootName, "Software\\Classes\\" + dottedExtension + "\\OpenWithProgids", PROG_ID);
        }

        // File handler registration is deliberately independent of shell menu lifecycle.
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

    private void addGroupedInputMenu(StringBuilder script, String rootName, String menuKey, String defaultIcon, String addCommand) {
        addString(script, rootName, menuKey, "MUIVerb", "UniZip");
        addString(script, rootName, menuKey, "Icon", defaultIcon);
        addString(script, rootName, menuKey, "SubCommands", "");
        addString(script, rootName, menuKey, "MultiSelectModel", "Single");
        addSubCommand(script, rootName, menuKey, "add_to_archive", "Arsive ekle", defaultIcon, addCommand);
    }

    private void addSubCommand(StringBuilder script, String rootName, String parentKey, String commandKey, String label, String icon, String command) {
        addShellCommand(script, rootName, parentKey + "\\shell\\" + commandKey, label, icon, command);
    }

    private void addShellCommand(StringBuilder script, String rootName, String key, String label, String icon, String command) {
        addDefault(script, rootName, key, label);
        addString(script, rootName, key, "Icon", icon);
        // A static Explorer verb gets exactly one %1; do not fan out destructive
        // operations for multi-selection until an explicit multi-file adapter exists.
        addString(script, rootName, key, "MultiSelectModel", "Single");
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

    // Advertise only formats that the actual archive engine can currently open.
    // E.g. JAR is a ZIP container but the desktop accepts only the .zip suffix.
    List<String> supportedAssociationExtensions(List<String> extensions) {
        return normalizeExtensions(extensions).stream()
                .filter(extension -> {
                    ArchiveFormat format = ArchiveFormat.fromExtension(extension);
                    return format.fullySupported() && format.displayName().equals(extension);
                })
                .toList();
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

    private String applicationExecutableName() {
        Path packagedApp = packagedLauncherPath();
        return packagedApp != null ? packagedApp.getFileName().toString() : APP_EXE_NAME;
    }

    private String buildOpenCommand() throws Exception {
        return buildCommand(null, "%1");
    }

    private String buildCommand(String option, String argumentExpression) throws Exception {
        Path packagedApp = packagedLauncherPath();
        if (packagedApp != null) {
            StringBuilder command = new StringBuilder(quote(packagedApp.toString()));
            if (option != null && !option.isBlank()) {
                command.append(' ').append(option);
            }
            return command.append(' ').append(quote(argumentExpression)).toString();
        }
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
        Path packagedApp = packagedLauncherPath();
        if (packagedApp != null) {
            return packagedApp + ",0";
        }
        URI codeSourceUri = MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        Path codeSourcePath = Path.of(codeSourceUri).toAbsolutePath().normalize();
        if (Files.isRegularFile(codeSourcePath)) {
            return codeSourcePath.toString() + ",0";
        }
        String javawPath = javaLauncherPath();
        return javawPath + ",0";
    }

    private Path packagedLauncherPath() {
        String launcher = System.getProperty("jpackage.app-path", "").trim();
        if (launcher.isBlank()) {
            return null;
        }
        Path path = Path.of(launcher).toAbsolutePath().normalize();
        return path.getFileName() != null
                && path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".exe")
                && Files.isRegularFile(path)
                ? path : null;
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
