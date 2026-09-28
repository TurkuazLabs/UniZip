/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/SettingsRepository.java
# 📌 Amac: Kullanici ayarlarini dosya sisteminde saklamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: tema, dil, editorler, klasor davranisi, sistem secenekleri ve UniZip ayar tercihlerini userdata/settings.yml icinde okur ve yazar

Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.repositories;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class SettingsRepository {
    private static final String DEFAULT_THEME_ID = "Turkuaz";
    private static final String DEFAULT_LANGUAGE_ID = "tr";
    private static final boolean DEFAULT_OPEN_TEXT_ON_DOUBLE_CLICK = true;
    private static final String DEFAULT_TEXT_EDITOR_COMMAND = "notepad.exe";
    private static final String DEFAULT_EDITABLE_TEXT_EXTENSIONS = "txt,md,csv,log,ini,conf,cfg,yml,yaml,json,xml,html,css,js,java,py,sql";
    private static final boolean DEFAULT_OPEN_IMAGE_ON_DOUBLE_CLICK = true;
    private static final String DEFAULT_IMAGE_EDITOR_COMMAND = "mspaint.exe";
    private static final String DEFAULT_EDITABLE_IMAGE_EXTENSIONS = "png,jpg,jpeg,bmp,gif,tif,tiff,webp,jfif";
    private static final String DEFAULT_ARCHIVE_FILE_EXTENSIONS = "zip,7z,rar,001,cab,iso,xz,txz,lzma,tar,gz,gzip,tgz,tpz,zst,tzst,bz2,bzip2,tbz,tbz2,tbz2,gz";
    private static final boolean DEFAULT_CONTEXT_MENU_ENABLED = false;
    private static final boolean DEFAULT_GROUP_CONTEXT_MENU = true;
    private static final boolean DEFAULT_BLOCK_ROOT_FOLDER_DUPLICATION = true;
    private static final String DEFAULT_WORKING_FOLDER_MODE = "system_temp";
    private static final String DEFAULT_WORKING_FOLDER_CUSTOM_PATH = "";
    private static final String DEFAULT_VIEWER_COMMAND = "";
    private static final String DEFAULT_DIFF_COMMAND = "";
    private static final boolean DEFAULT_SHOW_PARENT_FOLDER_ITEM = true;
    private static final boolean DEFAULT_SHOW_EXACT_SIZES = false;
    private static final boolean DEFAULT_SELECT_FULL_ROW = true;
    private static final boolean DEFAULT_SHOW_SYSTEM_MENU = false;
    private static final boolean DEFAULT_USE_LARGE_MEMORY_PAGES = false;

    private final Path settingsPath;

    public SettingsRepository() {
        this.settingsPath = Path.of("userdata", "settings.yml");
    }

    public Path settingsPath() {
        return settingsPath;
    }

    public Optional<String> readActiveThemeId() { return readScalarValue("theme"); }
    public Optional<String> readActiveLanguageId() { return readScalarValue("language"); }
    public boolean readOpenTextOnDoubleClick() { return readBoolean("open_text_on_double_click", DEFAULT_OPEN_TEXT_ON_DOUBLE_CLICK); }
    public String readTextEditorCommand() { return readText("text_editor_command", DEFAULT_TEXT_EDITOR_COMMAND); }
    public String readEditableTextExtensions() { return readText("editable_text_extensions", DEFAULT_EDITABLE_TEXT_EXTENSIONS); }
    public boolean readOpenImageOnDoubleClick() { return readBoolean("open_image_on_double_click", DEFAULT_OPEN_IMAGE_ON_DOUBLE_CLICK); }
    public String readImageEditorCommand() { return readText("image_editor_command", DEFAULT_IMAGE_EDITOR_COMMAND); }
    public String readEditableImageExtensions() { return readText("editable_image_extensions", DEFAULT_EDITABLE_IMAGE_EXTENSIONS); }
    public String readArchiveFileExtensions() { return readText("archive_file_extensions", DEFAULT_ARCHIVE_FILE_EXTENSIONS); }
    public boolean readContextMenuEnabled() { return readBoolean("context_menu_enabled", DEFAULT_CONTEXT_MENU_ENABLED); }
    public boolean readGroupContextMenu() { return readBoolean("group_context_menu", DEFAULT_GROUP_CONTEXT_MENU); }
    public boolean readBlockRootFolderDuplication() { return readBoolean("block_root_folder_duplication", DEFAULT_BLOCK_ROOT_FOLDER_DUPLICATION); }
    public String readWorkingFolderMode() { return readText("working_folder_mode", DEFAULT_WORKING_FOLDER_MODE); }
    public String readWorkingFolderCustomPath() { return readScalarValue("working_folder_custom_path").orElse(DEFAULT_WORKING_FOLDER_CUSTOM_PATH); }
    public String readViewerCommand() { return readScalarValue("viewer_command").orElse(DEFAULT_VIEWER_COMMAND); }
    public String readDiffCommand() { return readScalarValue("diff_command").orElse(DEFAULT_DIFF_COMMAND); }
    public boolean readShowParentFolderItem() { return readBoolean("show_parent_folder_item", DEFAULT_SHOW_PARENT_FOLDER_ITEM); }
    public boolean readShowExactSizes() { return readBoolean("show_exact_sizes", DEFAULT_SHOW_EXACT_SIZES); }
    public boolean readSelectFullRow() { return readBoolean("select_full_row", DEFAULT_SELECT_FULL_ROW); }
    public boolean readShowSystemMenu() { return readBoolean("show_system_menu", DEFAULT_SHOW_SYSTEM_MENU); }
    public boolean readUseLargeMemoryPages() { return readBoolean("use_large_memory_pages", DEFAULT_USE_LARGE_MEMORY_PAGES); }

    public void saveUiSettings(String themeId, String languageId) throws IOException {
        saveSettings(
                themeId,
                languageId,
                readOpenTextOnDoubleClick(),
                readTextEditorCommand(),
                readEditableTextExtensions(),
                readOpenImageOnDoubleClick(),
                readImageEditorCommand(),
                readEditableImageExtensions(),
                readArchiveFileExtensions(),
                readContextMenuEnabled(),
                readGroupContextMenu(),
                readBlockRootFolderDuplication(),
                readWorkingFolderMode(),
                readWorkingFolderCustomPath(),
                readViewerCommand(),
                readDiffCommand(),
                readShowParentFolderItem(),
                readShowExactSizes(),
                readSelectFullRow(),
                readShowSystemMenu(),
                readUseLargeMemoryPages()
        );
    }

    public void saveSettings(
            String themeId,
            String languageId,
            boolean openTextOnDoubleClick,
            String textEditorCommand,
            String editableTextExtensions,
            boolean openImageOnDoubleClick,
            String imageEditorCommand,
            String editableImageExtensions,
            String archiveFileExtensions,
            boolean contextMenuEnabled,
            boolean groupContextMenu,
            boolean blockRootFolderDuplication,
            String workingFolderMode,
            String workingFolderCustomPath,
            String viewerCommand,
            String diffCommand,
            boolean showParentFolderItem,
            boolean showExactSizes,
            boolean selectFullRow,
            boolean showSystemMenu,
            boolean useLargeMemoryPages
    ) throws IOException {
        Files.createDirectories(settingsPath.getParent());
        String content = "# 📄 Dosya Yolu: userdata/settings.yml" + System.lineSeparator()
                + "# 📌 Amac: UniZip kullanici ayarlarini saklamak" + System.lineSeparator()
                + "# 📌 Modul - FileType" + System.lineSeparator()
                + "# Version: 0.1.57" + System.lineSeparator()
                + "# Aciklama: aktif tema, dil, editorler, klasor davranisi, sistem secenekleri ve UniZip ayarlari" + System.lineSeparator()
                + System.lineSeparator()
                + "# Bagimli Oldugu Katman: Config" + System.lineSeparator()
                + System.lineSeparator()
                + "ui:" + System.lineSeparator()
                + "  theme: \"" + escapeYaml(safeValue(themeId, DEFAULT_THEME_ID)) + "\"" + System.lineSeparator()
                + "  language: \"" + escapeYaml(safeValue(languageId, DEFAULT_LANGUAGE_ID)) + "\"" + System.lineSeparator()
                + "  show_parent_folder_item: " + showParentFolderItem + System.lineSeparator()
                + "  show_exact_sizes: " + showExactSizes + System.lineSeparator()
                + "  select_full_row: " + selectFullRow + System.lineSeparator()
                + "  show_system_menu: " + showSystemMenu + System.lineSeparator()
                + System.lineSeparator()
                + "edit:" + System.lineSeparator()
                + "  open_text_on_double_click: " + openTextOnDoubleClick + System.lineSeparator()
                + "  text_editor_command: \"" + escapeYaml(safeCommand(textEditorCommand, DEFAULT_TEXT_EDITOR_COMMAND)) + "\"" + System.lineSeparator()
                + "  editable_text_extensions: \"" + escapeYaml(safeExtensions(editableTextExtensions, DEFAULT_EDITABLE_TEXT_EXTENSIONS)) + "\"" + System.lineSeparator()
                + "  open_image_on_double_click: " + openImageOnDoubleClick + System.lineSeparator()
                + "  image_editor_command: \"" + escapeYaml(safeCommand(imageEditorCommand, DEFAULT_IMAGE_EDITOR_COMMAND)) + "\"" + System.lineSeparator()
                + "  editable_image_extensions: \"" + escapeYaml(safeExtensions(editableImageExtensions, DEFAULT_EDITABLE_IMAGE_EXTENSIONS)) + "\"" + System.lineSeparator()
                + "  viewer_command: \"" + escapeYaml(safeCommand(viewerCommand, DEFAULT_VIEWER_COMMAND)) + "\"" + System.lineSeparator()
                + "  diff_command: \"" + escapeYaml(safeCommand(diffCommand, DEFAULT_DIFF_COMMAND)) + "\"" + System.lineSeparator()
                + System.lineSeparator()
                + "system:" + System.lineSeparator()
                + "  archive_file_extensions: \"" + escapeYaml(safeExtensions(archiveFileExtensions, DEFAULT_ARCHIVE_FILE_EXTENSIONS)) + "\"" + System.lineSeparator()
                + "  context_menu_enabled: " + contextMenuEnabled + System.lineSeparator()
                + "  group_context_menu: " + groupContextMenu + System.lineSeparator()
                + "  block_root_folder_duplication: " + blockRootFolderDuplication + System.lineSeparator()
                + "  use_large_memory_pages: " + useLargeMemoryPages + System.lineSeparator()
                + System.lineSeparator()
                + "folders:" + System.lineSeparator()
                + "  working_folder_mode: \"" + escapeYaml(safeWorkingFolderMode(workingFolderMode)) + "\"" + System.lineSeparator()
                + "  working_folder_custom_path: \"" + escapeYaml(safeCommand(workingFolderCustomPath, DEFAULT_WORKING_FOLDER_CUSTOM_PATH)) + "\"" + System.lineSeparator();
        Files.writeString(settingsPath, content, StandardCharsets.UTF_8);
    }

    public void saveActiveThemeId(String themeId) throws IOException { saveUiSettings(themeId, readActiveLanguageId().orElse(DEFAULT_LANGUAGE_ID)); }
    public void saveActiveLanguageId(String languageId) throws IOException { saveUiSettings(readActiveThemeId().orElse(DEFAULT_THEME_ID), languageId); }

    public void ensureDefaults() throws IOException {
        if (!Files.exists(settingsPath)) {
            saveSettings(
                    DEFAULT_THEME_ID,
                    DEFAULT_LANGUAGE_ID,
                    DEFAULT_OPEN_TEXT_ON_DOUBLE_CLICK,
                    DEFAULT_TEXT_EDITOR_COMMAND,
                    DEFAULT_EDITABLE_TEXT_EXTENSIONS,
                    DEFAULT_OPEN_IMAGE_ON_DOUBLE_CLICK,
                    DEFAULT_IMAGE_EDITOR_COMMAND,
                    DEFAULT_EDITABLE_IMAGE_EXTENSIONS,
                    DEFAULT_ARCHIVE_FILE_EXTENSIONS,
                    DEFAULT_CONTEXT_MENU_ENABLED,
                    DEFAULT_GROUP_CONTEXT_MENU,
                    DEFAULT_BLOCK_ROOT_FOLDER_DUPLICATION,
                    DEFAULT_WORKING_FOLDER_MODE,
                    DEFAULT_WORKING_FOLDER_CUSTOM_PATH,
                    DEFAULT_VIEWER_COMMAND,
                    DEFAULT_DIFF_COMMAND,
                    DEFAULT_SHOW_PARENT_FOLDER_ITEM,
                    DEFAULT_SHOW_EXACT_SIZES,
                    DEFAULT_SELECT_FULL_ROW,
                    DEFAULT_SHOW_SYSTEM_MENU,
                    DEFAULT_USE_LARGE_MEMORY_PAGES
            );
        }
    }

    private boolean readBoolean(String key, boolean fallback) {
        return readScalarValue(key).map(Boolean::parseBoolean).orElse(fallback);
    }

    private String readText(String key, String fallback) {
        return readScalarValue(key).filter(value -> !value.isBlank()).orElse(fallback);
    }

    private Optional<String> readScalarValue(String key) {
        if (!Files.exists(settingsPath)) { return Optional.empty(); }
        String prefix = key + ":";
        try {
            return Files.readAllLines(settingsPath, StandardCharsets.UTF_8)
                    .stream()
                    .map(String::trim)
                    .filter(line -> line.startsWith(prefix))
                    .map(line -> line.substring(prefix.length()).trim())
                    .map(this::cleanValue)
                    .findFirst();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private String safeValue(String value, String fallback) {
        if (value == null || value.isBlank()) { return fallback; }
        return value.trim().replace("\"", "").replace("'", "");
    }

    private String safeCommand(String value, String fallback) {
        if (value == null) { return fallback; }
        return value.trim();
    }

    private String safeExtensions(String value, String fallback) {
        if (value == null || value.isBlank()) { return fallback; }
        return value.trim();
    }

    private String safeWorkingFolderMode(String value) {
        if (value == null || value.isBlank()) { return DEFAULT_WORKING_FOLDER_MODE; }
        String normalized = value.trim();
        return switch (normalized) {
            case "system_temp", "current_folder", "custom_folder" -> normalized;
            default -> DEFAULT_WORKING_FOLDER_MODE;
        };
    }

    private String cleanValue(String value) {
        String cleaned = value.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return unescapeYaml(cleaned);
    }

    private String escapeYaml(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }

    private String unescapeYaml(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (escaping) { builder.append(current); escaping = false; continue; }
            if (current == '\\') { escaping = true; continue; }
            builder.append(current);
        }
        if (escaping) { builder.append('\\'); }
        return builder.toString();
    }
}
