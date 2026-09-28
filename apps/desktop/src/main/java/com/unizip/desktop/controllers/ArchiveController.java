/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/controllers/ArchiveController.java
# 📌 Amac: Arsiv UI olaylarini alip ArchiveService katmanina yonlendirmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ZIP acma, detayli test, ekleme, silme, tum/secili cikarma, editor ve rename akislarini yonlendirir

Bagimli Oldugu Katman: Controller
*/
package com.unizip.desktop.controllers;

import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveOperationResult;
import com.unizip.desktop.models.ArchiveSessionState;
import com.unizip.desktop.services.ArchiveService;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.LogService;
import com.unizip.desktop.services.RecentArchiveService;
import com.unizip.desktop.services.SettingsService;
import com.unizip.desktop.tools.UserDirectoryTool;
import com.unizip.desktop.views.ArchiveOpenDialog;
import com.unizip.desktop.views.ExtractOptionsDialog;
import com.unizip.desktop.views.MainFrame;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ArchiveController {
    private final ArchiveService archiveService;
    private final MainFrame mainFrame;
    private final LanguageService languageService;
    private final LogService logService;
    private final SettingsService settingsService;
    private final RecentArchiveService recentArchiveService;
    private final UserDirectoryTool userDirectoryTool;
    private Path selectedArchive;

    public ArchiveController(
            ArchiveService archiveService,
            MainFrame mainFrame,
            LanguageService languageService,
            LogService logService,
            SettingsService settingsService,
            RecentArchiveService recentArchiveService,
            UserDirectoryTool userDirectoryTool
    ) {
        this.archiveService = archiveService;
        this.mainFrame = mainFrame;
        this.languageService = languageService;
        this.logService = logService;
        this.settingsService = settingsService;
        this.recentArchiveService = recentArchiveService;
        this.userDirectoryTool = userDirectoryTool;
    }

    public void openArchive() {
        Path archivePath = ArchiveOpenDialog.showDialog(mainFrame, languageService, mainFrame.themeService(), userDirectoryTool);
        if (archivePath == null) {
            return;
        }

        openArchivePath(archivePath);
    }

    public void openRecentArchive(Path archivePath) {
        if (archivePath == null) {
            return;
        }
        if (!java.nio.file.Files.exists(archivePath)) {
            mainFrame.showError(languageService.text("error.recent_archive_missing"));
            recentArchiveService.pruneMissingArchives();
            mainFrame.refreshRecentArchivesMenu();
            return;
        }
        openArchivePath(archivePath);
    }

    public List<Path> recentArchives() {
        return recentArchiveService.existingRecentArchives();
    }

    public void clearRecentArchives() {
        try {
            recentArchiveService.clearRecentArchives();
            mainFrame.refreshRecentArchivesMenu();
            mainFrame.setStatus(languageService.text("message.recent_archives_cleared"));
        } catch (Exception exception) {
            showOperationError(exception);
        }
    }

    public void addToArchive() {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(languageService.text("message.select_add_input"));
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);

        Path downloadsDirectory = userDirectoryTool.downloadsDirectory();
        if (java.nio.file.Files.isDirectory(downloadsDirectory)) {
            chooser.setCurrentDirectory(downloadsDirectory.toFile());
        }

        int result = chooser.showOpenDialog(mainFrame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        List<Path> inputPaths = Arrays.stream(chooser.getSelectedFiles())
                .map(File::toPath)
                .toList();

        addPathsToArchive(inputPaths);
    }

    public void addPathsToArchive(List<Path> inputPaths) {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }
        if (inputPaths == null || inputPaths.isEmpty()) {
            return;
        }

        try {
            List<String> conflicts = archiveService.findAddConflicts(selectedArchive, inputPaths);
            boolean overwriteExisting = false;
            if (!conflicts.isEmpty()) {
                int result = JOptionPane.showConfirmDialog(
                        mainFrame,
                        languageService.text("message.add_conflict_overwrite") + System.lineSeparator()
                                + String.join(System.lineSeparator(), conflicts),
                        languageService.text("dialog.add_conflict_title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (result != JOptionPane.YES_OPTION) {
                    mainFrame.setStatus(languageService.text("message.add_cancelled"));
                    return;
                }
                overwriteExisting = true;
            }

            boolean finalOverwriteExisting = overwriteExisting;
            runBackgroundOperation(() -> archiveService.addToZip(selectedArchive, inputPaths, finalOverwriteExisting), false);
        } catch (Exception exception) {
            showOperationError(exception);
        }
    }

    public List<File> prepareDragExport() {
        if (selectedArchive == null) {
            return Collections.emptyList();
        }

        ArchiveEntryModel selectedEntry = mainFrame.selectedArchiveEntry();
        if (selectedEntry == null || selectedEntry.name().equals("..")) {
            return Collections.emptyList();
        }

        try {
            return archiveService.exportZipEntriesToTemp(selectedArchive, List.of(selectedEntry.name()));
        } catch (Exception exception) {
            showOperationError(exception);
            return Collections.emptyList();
        }
    }

    public void deleteSelectedEntry() {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }

        ArchiveEntryModel selectedEntry = mainFrame.selectedArchiveEntry();
        if (selectedEntry == null || selectedEntry.name().equals("..")) {
            mainFrame.showError(languageService.text("error.entry_required"));
            return;
        }

        runBackgroundOperation(() -> archiveService.deleteZipEntries(selectedArchive, List.of(selectedEntry.name())), false);
    }


    public void renameSelectedEntry() {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }

        ArchiveEntryModel selectedEntry = mainFrame.selectedArchiveEntry();
        if (selectedEntry == null || selectedEntry.name().equals("..")) {
            mainFrame.showError(languageService.text("error.entry_required"));
            return;
        }

        String currentBaseName = baseName(selectedEntry.name());
        String newBaseName = JOptionPane.showInputDialog(
                mainFrame,
                languageService.text("message.rename_prompt"),
                currentBaseName
        );
        if (newBaseName == null) {
            mainFrame.setStatus(languageService.text("message.rename_cancelled"));
            return;
        }
        newBaseName = newBaseName.trim();
        if (newBaseName.isBlank() || newBaseName.equals(currentBaseName)) {
            mainFrame.setStatus(languageService.text("message.rename_cancelled"));
            return;
        }

        String restoreFolder = mainFrame.currentFolderPath();
        String restoreEntryName = renamedEntryName(selectedEntry.name(), newBaseName);
        String finalNewBaseName = newBaseName;
        runBackgroundOperation(
                () -> archiveService.renameZipEntry(selectedArchive, selectedEntry.name(), finalNewBaseName),
                false,
                restoreFolder,
                restoreEntryName
        );
    }

    public void testArchive() {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }

        runBackgroundOperation(() -> archiveService.testZip(selectedArchive));
    }

    public void extractArchive() {
        extractEntries(List.of());
    }

    public void extractSelectedEntry() {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }

        ArchiveEntryModel selectedEntry = mainFrame.selectedArchiveEntry();
        if (selectedEntry == null || selectedEntry.name().equals("..")) {
            mainFrame.showError(languageService.text("error.entry_required"));
            return;
        }

        extractEntries(List.of(selectedEntry.name()));
    }

    private void extractEntries(List<String> selectedEntryNames) {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }

        ExtractOptionsDialog.Result result = ExtractOptionsDialog.showDialog(mainFrame, languageService, mainFrame.themeService());
        if (!result.approved()) {
            return;
        }

        runBackgroundOperation(() -> archiveService.extractZip(
                selectedArchive,
                result.outputDirectory(),
                result.createArchiveFolder(),
                selectedEntryNames,
                result.overwriteMode()
        ));
    }

    public void createZip() {
        JFileChooser inputChooser = new JFileChooser();
        inputChooser.setDialogTitle(languageService.text("message.select_input_path"));
        inputChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        int inputResult = inputChooser.showOpenDialog(mainFrame);
        if (inputResult != JFileChooser.APPROVE_OPTION) {
            return;
        }

        JFileChooser outputChooser = new JFileChooser();
        outputChooser.setDialogTitle(languageService.text("message.select_zip_output"));
        outputChooser.setSelectedFile(new File("archive.zip"));

        int outputResult = outputChooser.showSaveDialog(mainFrame);
        if (outputResult != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path inputPath = inputChooser.getSelectedFile().toPath();
        Path outputZip = outputChooser.getSelectedFile().toPath();
        mainFrame.setBusy(true);
        new SwingWorker<ArchiveOperationResult, Void>() {
            @Override
            protected ArchiveOperationResult doInBackground() throws Exception {
                return archiveService.createZip(inputPath, outputZip);
            }

            @Override
            protected void done() {
                mainFrame.setBusy(false);
                try {
                    ArchiveOperationResult result = get();
                    selectedArchive = outputZip;
                    mainFrame.setCurrentArchive(selectedArchive);
                    recentArchiveService.rememberArchive(selectedArchive);
                    mainFrame.refreshRecentArchivesMenu();
                    mainFrame.setStatus(result.message());
                    mainFrame.refreshLogs();
                    loadArchiveEntries(selectedArchive);
                    JOptionPane.showMessageDialog(mainFrame, result.message());
                } catch (Exception exception) {
                    showOperationError(exception);
                }
            }
        }.execute();
    }

    public void previewEntry(ArchiveEntryModel entry) {
        refreshArchiveState(entry);
        if (entry == null) {
            return;
        }
        mainFrame.showPreviewEntry(entry);
        if (selectedArchive == null || entry.directory() || entry.name().equals("..") || !isImageEntry(entry.name())) {
            return;
        }

        new SwingWorker<byte[], Void>() {
            @Override
            protected byte[] doInBackground() throws Exception {
                return archiveService.readEntryPreviewBytes(selectedArchive, entry.name());
            }

            @Override
            protected void done() {
                try {
                    mainFrame.showPreviewImage(entry, get());
                } catch (Exception exception) {
                    mainFrame.showPreviewEntry(entry);
                }
            }
        }.execute();
    }

    public void editEntryWithExternalEditor(ArchiveEntryModel entry) {
        if (selectedArchive == null) {
            mainFrame.showError(languageService.text("error.archive_required"));
            return;
        }
        if (entry == null || entry.directory() || entry.name().equals("..")) {
            mainFrame.showError(languageService.text("error.entry_required"));
            return;
        }

        if (archiveService.isEditableTextEntry(entry.name())) {
            if (!settingsService.openTextOnDoubleClick()) {
                mainFrame.setStatus(languageService.text("message.edit_text_on_double_click_disabled"));
                return;
            }
            runBackgroundOperation(
                    () -> archiveService.editTextEntry(selectedArchive, entry.name(), settingsService.textEditorCommand()),
                    false
            );
            return;
        }

        if (archiveService.isEditableImageEntry(entry.name())) {
            if (!settingsService.openImageOnDoubleClick()) {
                mainFrame.setStatus(languageService.text("message.edit_image_on_double_click_disabled"));
                return;
            }
            runBackgroundOperation(
                    () -> archiveService.editImageEntry(selectedArchive, entry.name(), settingsService.imageEditorCommand()),
                    false
            );
            return;
        }

        mainFrame.setStatus(languageService.text("message.edit_supported_only"));
    }

    private boolean isImageEntry(String name) {
        String lower = name == null ? "" : name.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp");
    }

    private void openArchivePath(Path archivePath) {
        selectedArchive = archivePath;
        mainFrame.setCurrentArchive(selectedArchive);
        recentArchiveService.rememberArchive(selectedArchive);
        mainFrame.refreshRecentArchivesMenu();
        loadArchiveEntries(selectedArchive);
    }

    private void loadArchiveEntries(Path archivePath) {
        runBackgroundLoad(() -> archiveService.listZipEntries(archivePath), null, null);
    }

    private void loadArchiveEntries(Path archivePath, String restoreFolder, String restoreEntryName) {
        runBackgroundLoad(() -> archiveService.listZipEntries(archivePath), restoreFolder, restoreEntryName);
    }

    private void runBackgroundLoad(ArchiveLoadTask task, String restoreFolder, String restoreEntryName) {
        mainFrame.setBusy(true);
        new SwingWorker<List<ArchiveEntryModel>, Void>() {
            @Override
            protected List<ArchiveEntryModel> doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                mainFrame.setBusy(false);
                try {
                    List<ArchiveEntryModel> entries = get();
                    if (restoreFolder == null && restoreEntryName == null) {
                        mainFrame.setArchiveEntries(entries);
                    } else {
                        mainFrame.setArchiveEntriesKeepingLocation(entries, restoreFolder, restoreEntryName);
                    }
                    mainFrame.setStatus(languageService.text("message.archive_loaded"));
                    refreshArchiveState(mainFrame.selectedArchiveEntry());
                } catch (Exception exception) {
                    showOperationError(exception);
                }
            }
        }.execute();
    }

    private void runBackgroundOperation(ArchiveOperationTask task) {
        runBackgroundOperation(task, true);
    }

    private void runBackgroundOperation(ArchiveOperationTask task, boolean showSuccessDialog) {
        runBackgroundOperation(task, showSuccessDialog, mainFrame.currentFolderPath(), mainFrame.selectedArchiveEntryName());
    }

    private void runBackgroundOperation(ArchiveOperationTask task, boolean showSuccessDialog, String restoreFolder, String restoreEntryName) {
        mainFrame.setBusy(true);
        new SwingWorker<ArchiveOperationResult, Void>() {
            @Override
            protected ArchiveOperationResult doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                mainFrame.setBusy(false);
                try {
                    ArchiveOperationResult result = get();
                    mainFrame.setStatus(result.message());
                    mainFrame.refreshLogs();
                    mainFrame.refreshRecentArchivesMenu();
                    if (selectedArchive != null) {
                        mainFrame.setCurrentArchive(selectedArchive);
                        loadArchiveEntries(selectedArchive, restoreFolder, restoreEntryName);
                    }
                    if (showSuccessDialog) {
                        JOptionPane.showMessageDialog(mainFrame, result.message());
                    }
                } catch (Exception exception) {
                    showOperationError(exception);
                }
            }
        }.execute();
    }


    private String renamedEntryName(String oldEntryName, String newBaseName) {
        String normalized = oldEntryName == null ? "" : oldEntryName.replace('\\', '/');
        boolean directory = normalized.endsWith("/");
        String withoutSlash = directory ? normalized.substring(0, normalized.length() - 1) : normalized;
        int slashIndex = withoutSlash.lastIndexOf('/');
        String parent = slashIndex >= 0 ? withoutSlash.substring(0, slashIndex + 1) : "";
        return parent + newBaseName + (directory && !newBaseName.endsWith("/") ? "/" : "");
    }

    private String baseName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        String normalized = path.replace('\\', '/');
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        int index = normalized.lastIndexOf('/');
        return index >= 0 ? normalized.substring(index + 1) : normalized;
    }


    private void refreshArchiveState(ArchiveEntryModel selectedEntry) {
        try {
            boolean hasSelection = selectedEntry != null && !selectedEntry.name().equals("..");
            boolean editableSelection = hasSelection
                    && !selectedEntry.directory()
                    && (archiveService.isEditableTextEntry(selectedEntry.name()) || archiveService.isEditableImageEntry(selectedEntry.name()));
            boolean folderSelection = hasSelection && selectedEntry.directory();
            ArchiveSessionState state = archiveService.buildSessionState(
                    selectedArchive,
                    hasSelection,
                    editableSelection,
                    folderSelection
            );
            mainFrame.applyArchiveState(state);
        } catch (Exception exception) {
            mainFrame.applyArchiveState(ArchiveSessionState.empty());
        }
    }

    private void showOperationError(Exception exception) {
        logService.error(exception.getMessage());
        mainFrame.refreshLogs();
        mainFrame.showError(languageService.text("error.operation_failed") + ": " + exception.getMessage());
    }

    private interface ArchiveLoadTask {
        List<ArchiveEntryModel> run() throws Exception;
    }

    private interface ArchiveOperationTask {
        ArchiveOperationResult run() throws Exception;
    }
}
