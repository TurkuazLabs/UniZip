/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/SettingsService.java
# 📌 Amac: Ayarlar ekraninin tema, dil, editor, klasor, sistem ve Registry iliskilendirme tercihlerini yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: UniZip sekmeli ayarlar ekraninin is kurallarini, working folder cozumlemesini ve dosya iliskilendirmeyi toplar

Bagimli Oldugu Katman: Service | Repo/Model | Config | Language | View
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.FileAssociationScope;
import com.unizip.desktop.models.FileAssociationStatusModel;
import com.unizip.desktop.models.LanguageOptionModel;
import com.unizip.desktop.models.ThemeOptionModel;
import com.unizip.desktop.repositories.SettingsRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SettingsService {
    private static final String WORKING_FOLDER_SYSTEM_TEMP = "system_temp";
    private static final String WORKING_FOLDER_CURRENT_FOLDER = "current_folder";
    private static final String WORKING_FOLDER_CUSTOM_FOLDER = "custom_folder";

    private final SettingsRepository settingsRepository;
    private final ThemeService themeService;
    private final LanguageService languageService;
    private final FileAssociationService fileAssociationService;

    public SettingsService(
            SettingsRepository settingsRepository,
            ThemeService themeService,
            LanguageService languageService,
            FileAssociationService fileAssociationService
    ) {
        this.settingsRepository = settingsRepository;
        this.themeService = themeService;
        this.languageService = languageService;
        this.fileAssociationService = fileAssociationService;
    }

    public String activeThemeId() { return themeService.activeThemeId(); }
    public String activeLanguageId() { return languageService.activeLanguageId(); }
    public List<ThemeOptionModel> availableThemes() { return themeService.availableThemes(); }
    public List<LanguageOptionModel> availableLanguages() { return languageService.availableLanguages(); }
    public Path settingsPath() { return settingsRepository.settingsPath(); }

    public boolean openTextOnDoubleClick() { return settingsRepository.readOpenTextOnDoubleClick(); }
    public String textEditorCommand() { return settingsRepository.readTextEditorCommand(); }
    public String editableTextExtensionsRaw() { return settingsRepository.readEditableTextExtensions(); }
    public List<String> editableTextExtensions() { return parseExtensions(editableTextExtensionsRaw()); }
    public boolean isEditableTextExtension(String entryName) { return hasConfiguredExtension(entryName, editableTextExtensions()); }

    public boolean openImageOnDoubleClick() { return settingsRepository.readOpenImageOnDoubleClick(); }
    public String imageEditorCommand() { return settingsRepository.readImageEditorCommand(); }
    public String editableImageExtensionsRaw() { return settingsRepository.readEditableImageExtensions(); }
    public List<String> editableImageExtensions() { return parseExtensions(editableImageExtensionsRaw()); }
    public boolean isEditableImageExtension(String entryName) { return hasConfiguredExtension(entryName, editableImageExtensions()); }

    public String archiveFileExtensionsRaw() { return settingsRepository.readArchiveFileExtensions(); }

    public List<FileAssociationStatusModel> fileAssociationStatuses() {
        return fileAssociationService.associationStatuses(archiveFileExtensions());
    }

    public void associateArchiveExtensions(FileAssociationScope scope, List<String> extensions) throws Exception {
        fileAssociationService.associateExtensions(scope, extensions, contextMenuEnabled(), groupContextMenu());
    }

    public void installExplorerContextMenu() throws Exception {
        fileAssociationService.installContextMenuCurrentUser();
    }

    public void removeExplorerContextMenu() throws Exception {
        fileAssociationService.removeContextMenuCurrentUser();
    }

    public boolean explorerContextMenuInstalled() {
        return fileAssociationService.contextMenuInstalledForCurrentUser();
    }

    public FileAssociationService.ExplorerMenuStatus explorerMenuStatus() {
        return fileAssociationService.contextMenuStatusForCurrentUser();
    }

    public boolean fileAssociationSupported() {
        return fileAssociationService.isWindows();
    }

    public List<String> archiveFileExtensions() {
        return parseExtensions(archiveFileExtensionsRaw());
    }
    public boolean contextMenuEnabled() { return settingsRepository.readContextMenuEnabled(); }
    public boolean groupContextMenu() { return settingsRepository.readGroupContextMenu(); }
    public boolean blockRootFolderDuplication() { return settingsRepository.readBlockRootFolderDuplication(); }
    public String workingFolderMode() { return settingsRepository.readWorkingFolderMode(); }
    public String workingFolderCustomPath() { return settingsRepository.readWorkingFolderCustomPath(); }
    public String viewerCommand() { return settingsRepository.readViewerCommand(); }
    public String diffCommand() { return settingsRepository.readDiffCommand(); }
    public boolean showParentFolderItem() { return settingsRepository.readShowParentFolderItem(); }
    public boolean showExactSizes() { return settingsRepository.readShowExactSizes(); }
    public boolean selectFullRow() { return settingsRepository.readSelectFullRow(); }
    public boolean showSystemMenu() { return settingsRepository.readShowSystemMenu(); }
    public boolean useLargeMemoryPages() { return settingsRepository.readUseLargeMemoryPages(); }

    public void saveUiPreferences(String themeId, String languageId) throws Exception {
        savePreferences(
                themeId,
                languageId,
                openTextOnDoubleClick(),
                textEditorCommand(),
                editableTextExtensionsRaw(),
                openImageOnDoubleClick(),
                imageEditorCommand(),
                editableImageExtensionsRaw(),
                archiveFileExtensionsRaw(),
                contextMenuEnabled(),
                groupContextMenu(),
                blockRootFolderDuplication(),
                workingFolderMode(),
                workingFolderCustomPath(),
                viewerCommand(),
                diffCommand(),
                showParentFolderItem(),
                showExactSizes(),
                selectFullRow(),
                showSystemMenu(),
                useLargeMemoryPages()
        );
    }

    public void savePreferences(
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
    ) throws Exception {
        String normalizedTextExtensions = String.join(",", parseAndValidateExtensions(editableTextExtensions, "error.editable_text_extensions_required"));
        String normalizedImageExtensions = String.join(",", parseAndValidateExtensions(editableImageExtensions, "error.editable_image_extensions_required"));
        String normalizedArchiveExtensions = String.join(",", parseAndValidateExtensions(archiveFileExtensions, "error.archive_file_extensions_required"));
        String normalizedWorkingFolderMode = normalizeWorkingFolderMode(workingFolderMode);
        if (WORKING_FOLDER_CUSTOM_FOLDER.equals(normalizedWorkingFolderMode) && (workingFolderCustomPath == null || workingFolderCustomPath.isBlank())) {
            throw new IllegalArgumentException(languageService.text("error.working_folder_required"));
        }
        settingsRepository.saveSettings(
                themeId,
                languageId,
                openTextOnDoubleClick,
                textEditorCommand,
                normalizedTextExtensions,
                openImageOnDoubleClick,
                imageEditorCommand,
                normalizedImageExtensions,
                normalizedArchiveExtensions,
                contextMenuEnabled,
                groupContextMenu,
                blockRootFolderDuplication,
                normalizedWorkingFolderMode,
                workingFolderCustomPath,
                viewerCommand,
                diffCommand,
                showParentFolderItem,
                showExactSizes,
                selectFullRow,
                showSystemMenu,
                useLargeMemoryPages
        );
        themeService.activateTheme(themeId);
        languageService.activateLanguage(languageId);
    }

    public Path createWorkingDirectory(String prefix) throws Exception {
        String safePrefix = prefix == null || prefix.isBlank() ? "unizip-work-" : prefix;
        String mode = normalizeWorkingFolderMode(workingFolderMode());
        Path baseDirectory;
        if (WORKING_FOLDER_CURRENT_FOLDER.equals(mode)) {
            baseDirectory = Path.of(System.getProperty("user.dir", "."));
        } else if (WORKING_FOLDER_CUSTOM_FOLDER.equals(mode)) {
            baseDirectory = Path.of(workingFolderCustomPath());
        } else {
            return Files.createTempDirectory(safePrefix);
        }
        Files.createDirectories(baseDirectory);
        return Files.createTempDirectory(baseDirectory, safePrefix);
    }

    private String normalizeWorkingFolderMode(String value) {
        if (value == null || value.isBlank()) {
            return WORKING_FOLDER_SYSTEM_TEMP;
        }
        return switch (value.trim()) {
            case WORKING_FOLDER_SYSTEM_TEMP, WORKING_FOLDER_CURRENT_FOLDER, WORKING_FOLDER_CUSTOM_FOLDER -> value.trim();
            default -> WORKING_FOLDER_SYSTEM_TEMP;
        };
    }

    private boolean hasConfiguredExtension(String entryName, List<String> extensions) {
        if (entryName == null || entryName.isBlank()) { return false; }
        String lowerName = entryName.toLowerCase(Locale.ROOT);
        for (String extension : extensions) {
            if (lowerName.endsWith("." + extension)) { return true; }
        }
        return false;
    }

    private List<String> parseExtensions(String rawExtensions) {
        Set<String> normalizedExtensions = new LinkedHashSet<>();
        if (rawExtensions != null) {
            for (String part : rawExtensions.split("[,;\\s]+")) {
                String normalized = normalizeExtension(part);
                if (!normalized.isBlank()) { normalizedExtensions.add(normalized); }
            }
        }
        return new ArrayList<>(normalizedExtensions);
    }

    private List<String> parseAndValidateExtensions(String rawExtensions, String errorKey) {
        List<String> extensions = parseExtensions(rawExtensions);
        if (extensions.isEmpty()) { throw new IllegalArgumentException(languageService.text(errorKey)); }
        return extensions;
    }

    private String normalizeExtension(String value) {
        if (value == null) { return ""; }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        while (normalized.startsWith(".")) { normalized = normalized.substring(1); }
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
}
