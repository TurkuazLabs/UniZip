/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/MainFrame.java
# 📌 Amac: UniZip ana penceresini Bandizip klasik arayuzune yakin duzende olusturmak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Lisans rozeti, acilis guncelleme bildirimi ve manuel guncelleme kontrolunu sunar

Bagimli Oldugu Katman: View
*/
package com.unizip.desktop.views;

import com.unizip.desktop.controllers.ArchiveController;
import com.unizip.desktop.controllers.StartupController;
import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveSessionState;
import com.unizip.desktop.models.AppFeature;
import com.unizip.desktop.models.ArchiveFormat;
import com.unizip.desktop.models.LicenseEdition;
import com.unizip.desktop.models.LicenseState;
import com.unizip.desktop.models.LicenseStatus;
import com.unizip.desktop.models.UpdateManifest;
import com.unizip.desktop.services.FeatureGateService;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.LogService;
import com.unizip.desktop.services.LicenseService;
import com.unizip.desktop.services.SettingsService;
import com.unizip.desktop.services.ThemeService;
import com.unizip.desktop.tools.FlatLafTool;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Path;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MainFrame extends JFrame {
    private final ThemeService themeService;
    private final LanguageService languageService;
    private final SettingsService settingsService;
    private final LogService logService;
    private final FlatLafTool flatLafTool;
    private final LicenseService licenseService;
    private final FeatureGateService featureGateService;
    private ArchiveController archiveController;
    private StartupController startupController;
    private ArchiveListPanel archiveListPanel;
    private JLabel leftStatusLabel;
    private JLabel rightStatusLabel;
    private JLabel archiveNameLabel;
    private JProgressBar progressBar;
    private JMenu recentArchivesMenu;
    private JMenuItem menuAddItem;
    private JMenuItem menuDeleteItem;
    private JMenuItem menuRenameItem;
    private JMenuItem menuExtractItem;
    private JMenuItem menuExtractSelectedItem;
    private JMenuItem menuTestItem;
    private JButton ribbonAddButton;
    private JButton ribbonDeleteButton;
    private JButton ribbonRenameButton;
    private JButton ribbonExtractButton;
    private JButton ribbonExtractSelectedButton;
    private JButton ribbonTestButton;
    private JButton detailsViewButton;
    private JButton gridViewButton;
    private JTree archiveTree;
    private PreviewPanel previewPanel;
    private JTextField searchField;
    private JButton clearSearchButton;
    private JComboBox<FileTypeFilter> fileTypeFilterCombo;
    private JLabel licenseBadgeLabel;
    private List<ArchiveEntryModel> currentEntries;
    private String currentArchiveName;
    private String currentFolder;
    private String searchText;
    private FileTypeFilter activeFileTypeFilter;
    private boolean treeChangeFromCode;
    private ArchiveSessionState currentArchiveSessionState;

    public MainFrame(ThemeService themeService, LanguageService languageService, SettingsService settingsService, LogService logService, FlatLafTool flatLafTool, LicenseService licenseService, FeatureGateService featureGateService) {
        super(languageService.text("app.title"));
        this.themeService = themeService;
        this.languageService = languageService;
        this.settingsService = settingsService;
        this.logService = logService;
        this.flatLafTool = flatLafTool;
        this.licenseService = licenseService;
        this.featureGateService = featureGateService;
        this.currentEntries = new ArrayList<>();
        this.currentArchiveName = languageService.text("label.default_archive_name");
        this.currentFolder = "";
        this.searchText = "";
        this.activeFileTypeFilter = FileTypeFilter.ALL;
        this.currentArchiveSessionState = ArchiveSessionState.empty();
        configureFrame();
        applyFixedWindowTitle();
        applyApplicationIcon();
        buildLayout();
    }

    public void setArchiveController(ArchiveController archiveController) {
        this.archiveController = archiveController;
        refreshRecentArchivesMenu();
    }

    public void setStartupController(StartupController startupController) {
        this.startupController = startupController;
    }

    public ThemeService themeService() {
        return themeService;
    }

    public ArchiveEntryModel selectedArchiveEntry() {
        return archiveListPanel == null ? null : archiveListPanel.selectedEntry();
    }

    public String selectedArchiveEntryName() {
        return archiveListPanel == null ? null : archiveListPanel.selectedEntryName();
    }

    public String currentFolderPath() {
        return normalizeFolder(currentFolder);
    }

    public void applyArchiveState(ArchiveSessionState state) {
        ArchiveSessionState safeState = state == null ? ArchiveSessionState.empty() : state;
        currentArchiveSessionState = safeState;
        AppFeature archiveFeature = safeState.format() == ArchiveFormat.ZIP
                ? AppFeature.ARCHIVE_CORE
                : AppFeature.EXTERNAL_ARCHIVE_ENGINE;
        boolean featureAllowed = featureGateService == null || featureGateService.isAllowed(archiveFeature);
        setEnabledIfPresent(ribbonAddButton, featureAllowed && safeState.canAdd());
        setEnabledIfPresent(ribbonDeleteButton, featureAllowed && safeState.canDelete());
        setEnabledIfPresent(ribbonRenameButton, featureAllowed && safeState.canRename());
        setEnabledIfPresent(ribbonExtractButton, featureAllowed && safeState.canExtract());
        setEnabledIfPresent(ribbonExtractSelectedButton, featureAllowed && safeState.canExtractSelected());
        setEnabledIfPresent(ribbonTestButton, featureAllowed && safeState.canTest());
        setEnabledIfPresent(menuAddItem, featureAllowed && safeState.canAdd());
        setEnabledIfPresent(menuDeleteItem, featureAllowed && safeState.canDelete());
        setEnabledIfPresent(menuRenameItem, featureAllowed && safeState.canRename());
        setEnabledIfPresent(menuExtractItem, featureAllowed && safeState.canExtract());
        setEnabledIfPresent(menuExtractSelectedItem, featureAllowed && safeState.canExtractSelected());
        setEnabledIfPresent(menuTestItem, featureAllowed && safeState.canTest());
        if (rightStatusLabel != null && safeState.archiveOpened()) {
            rightStatusLabel.setText(safeState.format().displayName() + " | " + safeState.engineName());
        }
    }

    private void setEnabledIfPresent(JComponent component, boolean enabled) {
        if (component != null) {
            component.setEnabled(enabled);
        }
    }


    public void showFrame() {
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setCurrentArchive(Path archivePath) {
        if (archivePath != null && archivePath.getFileName() != null) {
            currentArchiveName = archivePath.getFileName().toString();
            currentFolder = "";
            applyFixedWindowTitle();
            if (archiveNameLabel != null) {
                archiveNameLabel.setText(currentArchiveName);
            }
            if (previewPanel != null) {
                previewPanel.showArchive(currentArchiveName);
            }
        }
    }

    public void setArchiveEntries(List<ArchiveEntryModel> entries) {
        currentEntries = new ArrayList<>(entries);
        currentFolder = "";
        refreshArchiveTree();
        refreshVisibleEntries();
        updateFooter();
    }

    public void setArchiveEntriesKeepingLocation(List<ArchiveEntryModel> entries, String folderPath, String entryName) {
        currentEntries = new ArrayList<>(entries);
        currentFolder = safeExistingFolder(folderPath);
        refreshArchiveTree();
        selectTreePath(currentFolder);
        refreshVisibleEntries(entryName);
        updateFooter();
    }

    public void setStatus(String status) {
        leftStatusLabel.setText(status);
    }

    public void setBusy(boolean busy) {
        progressBar.setIndeterminate(busy);
    }

    public void refreshLogs() {
        updateFooter();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, languageService.text("error.operation_failed"), JOptionPane.ERROR_MESSAGE);
    }

    public void showPreviewEntry(ArchiveEntryModel entry) {
        previewPanel.showEntry(entry);
    }

    public void showPreviewImage(ArchiveEntryModel entry, byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                previewPanel.showEntry(entry);
                return;
            }
            previewPanel.showImage(entry, image);
        } catch (Exception exception) {
            previewPanel.showEntry(entry);
        }
    }

    private void applyFixedWindowTitle() {
        LicenseState state = licenseService.currentState();
        setTitle(languageService.text("app.window_title") + " - " + licenseStateShortText(state));
    }

    private String licenseBadgeText() {
        LicenseState state = licenseService.currentState();
        return state.edition().displayName() + " / " + licenseStateShortText(state);
    }

    private String licenseStateShortText(LicenseState state) {
        LicenseState safeState = state == null ? licenseService.currentState() : state;
        if (!safeState.hasToken()) {
            return languageService.text("license.state_unlicensed");
        }
        if (safeState.status() == LicenseStatus.ACTIVE) {
            return languageService.text("license.state_licensed");
        }
        if (safeState.status() == LicenseStatus.EXPIRED) {
            return languageService.text("license.state_expired");
        }
        if (safeState.status() == LicenseStatus.INVALID) {
            return languageService.text("license.state_invalid");
        }
        return languageService.text("license.state_offline");
    }

    private Color licenseBadgeBackground() {
        LicenseState state = licenseService.currentState();
        if (state.status() == LicenseStatus.ACTIVE && state.edition() == LicenseEdition.PRO) {
            return themeService.color("success");
        }
        if (state.hasToken() && state.status() == LicenseStatus.OFFLINE) {
            return themeService.color("warning");
        }
        if (state.status() == LicenseStatus.EXPIRED || state.status() == LicenseStatus.INVALID) {
            return themeService.color("danger");
        }
        return themeService.color("accent.secondary");
    }

    private void updateLicenseBadge() {
        applyFixedWindowTitle();
        if (licenseBadgeLabel == null) {
            return;
        }
        licenseBadgeLabel.setText(licenseBadgeText());
        licenseBadgeLabel.setBackground(licenseBadgeBackground());
        licenseBadgeLabel.setToolTipText(languageService.text("license.badge_tooltip"));
        licenseBadgeLabel.repaint();
    }

    public void refreshLicenseState() {
        updateLicenseBadge();
        applyArchiveState(currentArchiveSessionState);
    }

    public void showUpdateCheckStarted() {
        setStatus(languageService.text("update.checking"));
    }

    public void showUpdateDownloadStarted() {
        setBusy(true);
        setStatus(languageService.text("update.downloading"));
    }

    public void showUpdateAvailable(UpdateManifest manifest) {
        if (manifest == null) {
            return;
        }
        String message = languageService.text("update.available_message")
                .replace("{version}", manifest.version())
                .replace("{notes}", manifest.notes().isBlank() ? "-" : manifest.notes());
        Object[] options = {
                languageService.text("update.download"),
                languageService.text("update.later")
        };
        int result = JOptionPane.showOptionDialog(
                this,
                message,
                languageService.text("update.available_title"),
                JOptionPane.YES_NO_OPTION,
                manifest.mandatory() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (result == JOptionPane.YES_OPTION && startupController != null) {
            startupController.downloadUpdate(manifest);
        }
    }

    public void showNoUpdateAvailable(String currentVersion) {
        setStatus(languageService.text("update.current"));
        JOptionPane.showMessageDialog(
                this,
                languageService.text("update.current_message").replace("{version}", currentVersion == null ? "" : currentVersion),
                languageService.text("update.check_title"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public void showUpdateDownloaded(Path updateFile) {
        setBusy(false);
        setStatus(languageService.text("update.downloaded"));
        String fileText = updateFile == null ? "" : updateFile.toAbsolutePath().toString();
        int result = JOptionPane.showConfirmDialog(
                this,
                languageService.text("update.downloaded_message").replace("{path}", fileText),
                languageService.text("update.downloaded_title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION && updateFile != null) {
            openUpdateDirectory(updateFile);
        }
    }

    public void showUpdateError(String message) {
        setBusy(false);
        setStatus(languageService.text("update.failed"));
        JOptionPane.showMessageDialog(
                this,
                languageService.text("update.error_message").replace("{message}", message == null ? "" : message),
                languageService.text("update.check_title"),
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void applyApplicationIcon() {
        URL iconUrl = getClass().getResource("/icons/unizip-icon-32.png");
        if (iconUrl != null) {
            setIconImage(new ImageIcon(iconUrl).getImage());
        }
    }

    private void configureFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 650));
        setPreferredSize(new Dimension(1040, 680));
        getContentPane().setBackground(themeService.color("window.background"));
    }

    private void buildLayout() {
        archiveListPanel = new ArchiveListPanel(themeService, languageService);
        archiveListPanel.setDetailsSelectFullRow(settingsService.selectFullRow());
        archiveListPanel.setSelectionListener(this::onArchiveEntrySelected);
        archiveListPanel.setOpenListener(this::onArchiveEntryOpened);
        archiveListPanel.installContextMenu(
                this::runAddAction,
                this::runExtractSelectedAction,
                this::runRenameAction,
                this::runDeleteAction,
                this::copySelectedEntryPath
        );
        progressBar = new JProgressBar();
        leftStatusLabel = new JLabel(languageService.text("label.ready"));
        rightStatusLabel = new JLabel();
        archiveNameLabel = new JLabel(currentArchiveName);
        searchField = new JTextField();
        clearSearchButton = createClearSearchButton();
        fileTypeFilterCombo = new JComboBox<>(FileTypeFilter.values());
        previewPanel = new PreviewPanel(themeService, languageService);
        previewPanel.showArchive(currentArchiveName);
        archiveListPanel.setViewMode(ArchiveListPanel.ViewMode.DETAILS);

        setJMenuBar(buildMenuBar());

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(themeService.color("window.background"));
        rootPanel.add(buildRibbon(), BorderLayout.NORTH);
        rootPanel.add(buildBody(), BorderLayout.CENTER);
        rootPanel.add(buildStatusBar(), BorderLayout.SOUTH);

        setContentPane(rootPanel);
        installDragAndDropSupport(rootPanel);
        installKeyboardShortcuts(rootPanel);
        refreshArchiveTree();
        refreshVisibleEntries();
        updateFooter();
        pack();
        revalidate();
        repaint();
    }

    private void installDragAndDropSupport(JPanel rootPanel) {
        TransferHandler handler = new ArchiveDropTransferHandler();
        rootPanel.setTransferHandler(handler);
        archiveListPanel.installDragExportHandler(handler);
        archiveTree.setTransferHandler(handler);
        previewPanel.setTransferHandler(handler);
    }

    private void installKeyboardShortcuts(JPanel rootPanel) {
        bindDeleteShortcut(rootPanel);
        bindDeleteShortcut(archiveListPanel);
        bindDeleteShortcut(archiveTree);
        bindDeleteShortcut(previewPanel);
        bindDeleteShortcut(getRootPane());
        bindSearchShortcut(rootPanel);
        bindSearchShortcut(getRootPane());
        bindRenameShortcut(rootPanel);
        bindRenameShortcut(archiveListPanel);
        archiveListPanel.installRenameShortcut(this::runRenameAction);
        bindRenameShortcut(archiveTree);
        bindRenameShortcut(previewPanel);
        bindRenameShortcut(getRootPane());
    }

    private void bindDeleteShortcut(JComponent component) {
        if (component == null) {
            return;
        }

        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = component.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "unizip.delete.selected.entry");
        actionMap.put("unizip.delete.selected.entry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runDeleteAction();
            }
        });
    }

    private void bindSearchShortcut(JComponent component) {
        if (component == null) {
            return;
        }

        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = component.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK), "unizip.search.focus");
        actionMap.put("unizip.search.focus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                focusSearchField();
            }
        });
    }


    private void bindRenameShortcut(JComponent component) {
        if (component == null) {
            return;
        }

        InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = component.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "unizip.rename.selected.entry");
        actionMap.put("unizip.rename.selected.entry", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runRenameAction();
            }
        });
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(themeService.color("menu.background"));
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, themeService.color("panel.border")));
        menuBar.add(buildFileMenu());
        menuBar.add(buildEditMenu());
        menuBar.add(buildFindMenu());
        menuBar.add(buildViewMenu());
        menuBar.add(buildToolsMenu());
        menuBar.add(buildOptionsMenu());
        menuBar.add(buildHelpMenu());
        return menuBar;
    }

    private JMenu buildFileMenu() {
        JMenu menu = createRootMenu("menu.file");
        menu.add(createMenuItem("button.new_archive", () -> archiveController.createZip(), KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menu.add(createMenuItem("button.open_archive", () -> archiveController.openArchive(), KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menu.addSeparator();
        menuExtractItem = createMenuItem("button.extract_zip", this::runExtractAllAction, KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        menu.add(menuExtractItem);
        menu.addSeparator();
        recentArchivesMenu = new JMenu(languageService.text("menu.recent_archives"));
        recentArchivesMenu.setFont(themeService.font("menu"));
        menu.add(recentArchivesMenu);
        refreshRecentArchivesMenu();
        return menu;
    }

    private JMenu buildEditMenu() {
        JMenu menu = createRootMenu("menu.edit");
        menu.add(createMenuItem("context.open", this::runOpenSelectedAction, KeyEvent.VK_ENTER, 0));
        menuAddItem = createMenuItem("button.add", this::runAddAction, KeyEvent.VK_INSERT, 0);
        menuRenameItem = createMenuItem("button.rename", this::runRenameAction, KeyEvent.VK_F2, 0);
        menuDeleteItem = createMenuItem("button.delete", this::runDeleteAction, KeyEvent.VK_DELETE, 0);
        menu.add(menuAddItem);
        menu.add(menuRenameItem);
        menu.add(menuDeleteItem);
        menu.addSeparator();
        menuExtractSelectedItem = createMenuItem("button.extract_selected", this::runExtractSelectedAction, KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        menu.add(menuExtractSelectedItem);
        menu.add(createMenuItem("context.copy_path", this::copySelectedEntryPath, KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.SHIFT_DOWN_MASK));
        return menu;
    }

    private JMenu buildFindMenu() {
        JMenu menu = createRootMenu("menu.find");
        menu.add(createMenuItem("menu.search_focus", this::focusSearchField, KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menu.add(createMenuItem("button.clear_search", this::clearSearchText, KeyEvent.VK_ESCAPE, 0));
        menu.addSeparator();
        JMenu filterMenu = new JMenu(languageService.text("menu.filter_by_type"));
        filterMenu.setFont(themeService.font("menu"));
        for (FileTypeFilter filter : FileTypeFilter.values()) {
            filterMenu.add(createMenuItem(filter.labelKey(), () -> setFileTypeFilter(filter)));
        }
        menu.add(filterMenu);
        return menu;
    }

    private JMenu buildViewMenu() {
        JMenu menu = createRootMenu("menu.view");
        menu.add(createMenuItem("button.view_details", () -> setArchiveViewMode(ArchiveListPanel.ViewMode.DETAILS), KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menu.add(createMenuItem("button.view_grid", () -> setArchiveViewMode(ArchiveListPanel.ViewMode.GRID), KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        return menu;
    }

    private JMenu buildToolsMenu() {
        JMenu menu = createRootMenu("menu.tools");
        menuTestItem = createMenuItem("button.test", this::runTestAction, KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        menu.add(menuTestItem);
        return menu;
    }

    private JMenu buildOptionsMenu() {
        JMenu menu = createRootMenu("menu.options");
        menu.add(createMenuItem("button.settings", this::showSettingsDialog, KeyEvent.VK_COMMA, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        menu.add(createMenuItem("menu.license", this::showLicenseDialog, KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        return menu;
    }

    private JMenu buildHelpMenu() {
        JMenu menu = createRootMenu("menu.help");
        menu.add(createMenuItem("menu.check_updates", this::runUpdateCheckAction));
        menu.addSeparator();
        menu.add(createMenuItem("menu.about", this::showAboutMessage, KeyEvent.VK_F1, 0));
        return menu;
    }

    private JMenu createRootMenu(String titleKey) {
        JMenu menu = new JMenu(languageService.text(titleKey));
        menu.setFont(themeService.font("menu"));
        return menu;
    }

    public void refreshRecentArchivesMenu() {
        if (recentArchivesMenu == null) {
            return;
        }
        recentArchivesMenu.removeAll();
        if (archiveController == null) {
            recentArchivesMenu.add(createDisabledMenuItem("menu.recent_archives_empty"));
            return;
        }
        List<Path> recentArchives = archiveController.recentArchives();
        if (recentArchives.isEmpty()) {
            recentArchivesMenu.add(createDisabledMenuItem("menu.recent_archives_empty"));
            return;
        }
        for (Path archivePath : recentArchives) {
            JMenuItem item = new JMenuItem(shortRecentLabel(archivePath));
            item.setFont(themeService.font("menu"));
            item.setToolTipText(archivePath.toString());
            item.addActionListener(event -> archiveController.openRecentArchive(archivePath));
            recentArchivesMenu.add(item);
        }
        recentArchivesMenu.addSeparator();
        recentArchivesMenu.add(createMenuItem("menu.clear_recent_archives", () -> archiveController.clearRecentArchives()));
    }

    private JMenuItem createMenuItem(String itemKey, Runnable action) {
        return createMenuItem(itemKey, action, 0, 0);
    }

    private JMenuItem createMenuItem(String itemKey, Runnable action, int keyCode, int modifiers) {
        JMenuItem item = new JMenuItem(languageService.text(itemKey));
        item.setFont(themeService.font("menu"));
        if (keyCode > 0) {
            item.setAccelerator(KeyStroke.getKeyStroke(keyCode, modifiers));
        }
        item.addActionListener(event -> action.run());
        return item;
    }

    private JMenuItem createDisabledMenuItem(String itemKey) {
        JMenuItem item = new JMenuItem(languageService.text(itemKey));
        item.setFont(themeService.font("menu"));
        item.setEnabled(false);
        return item;
    }

    private String shortRecentLabel(Path archivePath) {
        if (archivePath == null || archivePath.getFileName() == null) {
            return "";
        }
        Path parent = archivePath.getParent();
        String parentText = parent == null ? "" : parent.toString();
        if (parentText.length() > 48) {
            parentText = "..." + parentText.substring(parentText.length() - 45);
        }
        return archivePath.getFileName() + (parentText.isBlank() ? "" : "  -  " + parentText);
    }

    private void addMenu(JMenuBar menuBar, String titleKey, String itemKey, Runnable action) {
        JMenu menu = createRootMenu(titleKey);
        menu.add(createMenuItem(itemKey, action));
        menuBar.add(menu);
    }

    private JPanel buildRibbon() {
        RibbonPanel panel = new RibbonPanel(themeService);
        panel.setLayout(new BorderLayout(0, 0));
        panel.setPreferredSize(new Dimension(100, 92));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, themeService.color("ribbon.border")));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 22, 7));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createRibbonButton("open", "button.open_archive", () -> archiveController.openArchive()));
        ribbonExtractButton = createRibbonButton("extract", "button.extract_zip", () -> archiveController.extractArchive());
        ribbonExtractSelectedButton = createRibbonButton("extract", "button.extract_selected", this::runExtractSelectedAction);
        ribbonAddButton = createRibbonButton("add", "button.add", this::runAddAction);
        ribbonDeleteButton = createRibbonButton("delete", "button.delete", this::runDeleteAction);
        ribbonRenameButton = createRibbonButton("rename", "button.rename", this::runRenameAction);
        ribbonTestButton = createRibbonButton("test", "button.test", this::runTestAction);
        buttonPanel.add(ribbonExtractButton);
        buttonPanel.add(ribbonExtractSelectedButton);
        buttonPanel.add(createRibbonButton("new", "button.new_archive", () -> archiveController.createZip()));
        buttonPanel.add(ribbonAddButton);
        buttonPanel.add(ribbonDeleteButton);
        buttonPanel.add(ribbonRenameButton);
        buttonPanel.add(ribbonTestButton);
        buttonPanel.add(createRibbonButton("scan", "button.scan", this::showNotReadyMessage));
        buttonPanel.add(createRibbonButton("columns", "button.columns", this::showNotReadyMessage));
        buttonPanel.add(createRibbonButton("codepage", "button.codepage", this::showNotReadyMessage));
        panel.add(buttonPanel, BorderLayout.CENTER);

        JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, themeService.spacing("md"), 25));
        badgePanel.setOpaque(false);
        licenseBadgeLabel = new JLabel();
        licenseBadgeLabel.setFont(themeService.font("default").deriveFont(Font.BOLD));
        licenseBadgeLabel.setOpaque(true);
        licenseBadgeLabel.setForeground(Color.WHITE);
        licenseBadgeLabel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        licenseBadgeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        licenseBadgeLabel.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        licenseBadgeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                showLicenseDialog();
            }
        });
        badgePanel.add(licenseBadgeLabel);
        panel.add(badgePanel, BorderLayout.EAST);
        updateLicenseBadge();
        return panel;
    }

    private JButton createRibbonButton(String iconType, String textKey, Runnable action) {
        JButton button = new JButton(languageService.text(textKey));
        button.setIcon(new RibbonActionIcon(iconType, themeService.color("ribbon.foreground")));
        button.setFont(themeService.font("toolbar"));
        button.setForeground(themeService.color("ribbon.foreground"));
        button.setBackground(new Color(0, 0, 0, 0));
        button.setOpaque(false);
        button.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("md"), themeService.spacing("sm"), themeService.spacing("md")));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(88, 82));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(4);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(event -> action.run());
        return button;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 0));
        body.setBackground(themeService.color("window.background"));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftPane(), buildContentPane());
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setResizeWeight(0.0);
        body.add(splitPane, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildContentPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(themeService.color("window.background"));
        panel.add(buildContentTopBar(), BorderLayout.NORTH);

        JSplitPane contentSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, archiveListPanel, previewPanel);
        contentSplit.setDividerLocation(640);
        contentSplit.setDividerSize(5);
        contentSplit.setBorder(BorderFactory.createEmptyBorder());
        contentSplit.setResizeWeight(1.0);
        panel.add(contentSplit, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildContentTopBar() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(themeService.color("panel.background"));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, themeService.color("panel.divider")));
        panel.setPreferredSize(new Dimension(100, 42));

        JLabel pathLabel = new JLabel(languageService.text("label.archive_path"));
        pathLabel.setFont(themeService.font("default"));
        pathLabel.setForeground(themeService.color("text.primary"));
        pathLabel.setBorder(BorderFactory.createEmptyBorder(0, themeService.spacing("md"), 0, themeService.spacing("sm")));
        panel.add(pathLabel, BorderLayout.WEST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, themeService.spacing("sm"), 6));
        filterPanel.setOpaque(false);

        JLabel searchLabel = new JLabel(languageService.text("label.search"));
        searchLabel.setFont(themeService.font("default"));
        searchLabel.setForeground(themeService.color("text.secondary"));
        filterPanel.add(searchLabel);

        searchField.setPreferredSize(new Dimension(220, 26));
        searchField.setFont(themeService.font("default"));
        searchField.setToolTipText(languageService.text("label.search_placeholder"));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                updateSearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                updateSearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                updateSearchFilter();
            }
        });
        JPanel searchBoxPanel = new JPanel(new BorderLayout(0, 0));
        searchBoxPanel.setOpaque(false);
        searchBoxPanel.setPreferredSize(new Dimension(250, 26));
        searchBoxPanel.add(searchField, BorderLayout.CENTER);
        searchBoxPanel.add(clearSearchButton, BorderLayout.EAST);
        filterPanel.add(searchBoxPanel);
        updateClearSearchButton();

        JLabel filterLabel = new JLabel(languageService.text("label.filter_type"));
        filterLabel.setFont(themeService.font("default"));
        filterLabel.setForeground(themeService.color("text.secondary"));
        filterPanel.add(filterLabel);

        fileTypeFilterCombo.setFont(themeService.font("default"));
        fileTypeFilterCombo.setPreferredSize(new Dimension(140, 26));
        fileTypeFilterCombo.setRenderer((list, value, index, selected, focus) -> {
            JLabel label = new JLabel(value == null ? "" : languageService.text(value.labelKey()));
            label.setOpaque(true);
            label.setFont(themeService.font("default"));
            label.setBorder(BorderFactory.createEmptyBorder(0, themeService.spacing("sm"), 0, themeService.spacing("sm")));
            label.setBackground(selected ? themeService.color("table.selection.background") : themeService.color("panel.background"));
            label.setForeground(selected ? themeService.color("table.selection.foreground") : themeService.color("text.primary"));
            return label;
        });
        fileTypeFilterCombo.addActionListener(event -> updateTypeFilter());
        filterPanel.add(fileTypeFilterCombo);
        panel.add(filterPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, themeService.spacing("xs"), 8));
        rightPanel.setOpaque(false);
        detailsViewButton = createViewToggleButton("details", languageService.text("button.view_details"), ArchiveListPanel.ViewMode.DETAILS);
        gridViewButton = createViewToggleButton("grid", languageService.text("button.view_grid"), ArchiveListPanel.ViewMode.GRID);
        rightPanel.add(detailsViewButton);
        rightPanel.add(gridViewButton);
        panel.add(rightPanel, BorderLayout.EAST);
        updateViewModeButtons();
        return panel;
    }

    private JPanel buildLeftPane() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setPreferredSize(new Dimension(250, 100));
        panel.setBackground(themeService.color("sidebar.background"));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, themeService.color("panel.divider")));

        JLabel titleLabel = new JLabel(languageService.text("nav.archives"));
        titleLabel.setFont(themeService.font("default").deriveFont(Font.BOLD));
        titleLabel.setForeground(themeService.color("text.primary"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("md"), themeService.spacing("sm"), themeService.spacing("md")));
        panel.add(titleLabel, BorderLayout.NORTH);

        archiveTree = new JTree(new DefaultMutableTreeNode(currentArchiveName));
        archiveTree.setFont(themeService.font("default"));
        archiveTree.setBackground(themeService.color("tree.background"));
        archiveTree.setForeground(themeService.color("text.primary"));
        archiveTree.setRootVisible(true);
        archiveTree.setShowsRootHandles(true);
        archiveTree.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm")));
        archiveTree.addTreeSelectionListener(this::onTreeSelectionChanged);

        JScrollPane treeScroll = new JScrollPane(archiveTree);
        treeScroll.setBorder(BorderFactory.createEmptyBorder());
        treeScroll.getViewport().setBackground(themeService.color("tree.background"));
        panel.add(treeScroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setPreferredSize(new Dimension(100, 34));
        panel.setBackground(themeService.color("status.background"));
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, themeService.color("status.border")));

        leftStatusLabel.setFont(themeService.font("default"));
        leftStatusLabel.setForeground(themeService.color("text.primary"));
        leftStatusLabel.setBorder(BorderFactory.createEmptyBorder(0, themeService.spacing("md"), 0, 0));

        progressBar.setPreferredSize(new Dimension(120, 10));
        progressBar.setStringPainted(false);
        progressBar.setBackground(themeService.color("progress.background"));
        progressBar.setForeground(themeService.color("progress.foreground"));

        rightStatusLabel.setFont(themeService.font("default"));
        rightStatusLabel.setForeground(themeService.color("text.primary"));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, themeService.spacing("md"), 7));
        rightPanel.setOpaque(false);
        rightPanel.add(progressBar);
        rightPanel.add(rightStatusLabel);
        panel.add(leftStatusLabel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    private JButton createClearSearchButton() {
        JButton button = new JButton("x");
        button.setPreferredSize(new Dimension(26, 26));
        button.setMargin(new Insets(0, 0, 1, 0));
        button.setFont(themeService.font("default").deriveFont(Font.BOLD));
        button.setToolTipText(languageService.text("button.clear_search"));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(themeService.color("panel.background"));
        button.setForeground(themeService.color("text.secondary"));
        button.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 1, themeService.color("panel.border")));
        button.addActionListener(event -> clearSearchText());
        return button;
    }

    private void clearSearchText() {
        if (searchField == null) {
            return;
        }
        if (!searchField.getText().isBlank()) {
            searchField.setText("");
        }
        searchField.requestFocusInWindow();
        updateClearSearchButton();
        setStatus(languageService.text("message.search_cleared"));
    }

    private void updateClearSearchButton() {
        if (clearSearchButton == null) {
            return;
        }
        boolean active = isSearchActive();
        clearSearchButton.setEnabled(active);
        clearSearchButton.setVisible(active);
    }

    private JButton createViewToggleButton(String iconType, String tooltip, ArchiveListPanel.ViewMode viewMode) {
        JButton button = new JButton(new StatusViewIcon(iconType, themeService.color("text.secondary")));
        button.setPreferredSize(new Dimension(28, 24));
        button.setOpaque(true);
        button.setBackground(themeService.color("panel.background"));
        button.setBorder(BorderFactory.createLineBorder(themeService.color("panel.border")));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setToolTipText(tooltip);
        button.addActionListener(event -> {
            archiveListPanel.setViewMode(viewMode);
            updateViewModeButtons();
        });
        return button;
    }

    private void updateViewModeButtons() {
        if (detailsViewButton == null || gridViewButton == null || archiveListPanel == null) {
            return;
        }
        boolean details = archiveListPanel.viewMode() == ArchiveListPanel.ViewMode.DETAILS;
        Color activeBackground = UIManager.getColor("Table.selectionBackground");
        if (activeBackground == null) {
            activeBackground = themeService.color("grid.selection.background");
        }
        Color activeForeground = UIManager.getColor("Table.selectionForeground");
        if (activeForeground == null) {
            activeForeground = Color.WHITE;
        }
        applyViewButtonState(detailsViewButton, details, activeBackground, activeForeground, "details");
        applyViewButtonState(gridViewButton, !details, activeBackground, activeForeground, "grid");
    }

    private void applyViewButtonState(JButton button, boolean active, Color activeBackground, Color activeForeground, String iconType) {
        button.setBackground(active ? activeBackground : themeService.color("panel.background"));
        button.setBorder(BorderFactory.createLineBorder(active ? activeBackground.darker() : themeService.color("panel.border")));
        button.setIcon(new StatusViewIcon(iconType, active ? activeForeground : themeService.color("text.secondary")));
    }

    private void refreshArchiveTree() {
        if (archiveTree == null) {
            return;
        }
        treeChangeFromCode = true;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TreeItem(currentArchiveName, ""));
        Map<String, DefaultMutableTreeNode> nodes = new LinkedHashMap<>();
        nodes.put("", root);
        for (ArchiveEntryModel entry : currentEntries) {
            String path = entry.name().replace('\\', '/');
            String[] parts = path.split("/");
            String prefix = "";
            for (int index = 0; index < parts.length; index++) {
                String part = parts[index];
                if (part.isBlank()) {
                    continue;
                }
                boolean last = index == parts.length - 1;
                if (last && !entry.directory()) {
                    continue;
                }
                String key = prefix + part + "/";
                DefaultMutableTreeNode parent = nodes.get(prefix);
                if (!nodes.containsKey(key) && parent != null) {
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(new TreeItem(part, key));
                    parent.add(child);
                    nodes.put(key, child);
                }
                prefix = key;
            }
        }
        archiveTree.setModel(new DefaultTreeModel(root));
        for (int row = 0; row < archiveTree.getRowCount(); row++) {
            archiveTree.expandRow(row);
        }
        archiveTree.setSelectionRow(0);
        treeChangeFromCode = false;
    }

    private void refreshVisibleEntries() {
        refreshVisibleEntries(null);
    }

    private void refreshVisibleEntries(String preferredEntryName) {
        if (archiveListPanel == null) {
            return;
        }
        archiveListPanel.setShowFullEntryPath(isSearchActive());
        archiveListPanel.setEntries(visibleEntries(currentFolder), preferredEntryName);
        updateFooter();
    }

    private List<ArchiveEntryModel> visibleEntries(String folder) {
        if (isSearchActive()) {
            return searchEntriesAcrossArchive();
        }
        String normalizedFolder = normalizeFolder(folder);
        List<ArchiveEntryModel> visible = new ArrayList<>();
        Set<String> addedDirectories = new LinkedHashSet<>();
        if (!normalizedFolder.isBlank() && settingsService.showParentFolderItem()) {
            visible.add(new ArchiveEntryModel("..", 0L, true));
        }
        for (ArchiveEntryModel entry : currentEntries) {
            String path = entry.name().replace('\\', '/');
            if (!path.startsWith(normalizedFolder) || path.equals(normalizedFolder)) {
                continue;
            }
            String remaining = path.substring(normalizedFolder.length());
            int slashIndex = remaining.indexOf('/');
            if (slashIndex >= 0) {
                String directoryName = remaining.substring(0, slashIndex + 1);
                String directoryPath = normalizedFolder + directoryName;
                if (addedDirectories.add(directoryPath) && matchesFolderViewFilter(new ArchiveEntryModel(directoryPath, 0L, true))) {
                    visible.add(new ArchiveEntryModel(directoryPath, 0L, true));
                }
                continue;
            }
            if (!entry.directory() && matchesFolderViewFilter(entry)) {
                visible.add(entry);
            }
        }
        visible.sort(Comparator
                .comparing((ArchiveEntryModel entry) -> !entry.directory())
                .thenComparing(entry -> entry.name().equals("..") ? "" : entry.name().toLowerCase(java.util.Locale.ROOT)));
        return visible;
    }

    private List<ArchiveEntryModel> searchEntriesAcrossArchive() {
        String normalizedSearch = normalizeSearchText(searchText);
        List<ArchiveEntryModel> visible = new ArrayList<>();
        Set<String> addedEntries = new LinkedHashSet<>();
        for (ArchiveEntryModel entry : currentEntries) {
            if (entry == null || entry.name() == null || entry.name().isBlank() || entry.name().equals("..")) {
                continue;
            }
            String path = entry.name().replace('\\', '/');
            String normalizedName = path.toLowerCase(java.util.Locale.ROOT);
            if (!normalizedName.contains(normalizedSearch)) {
                continue;
            }
            if (!matchesSearchTypeFilter(entry)) {
                continue;
            }
            if (addedEntries.add(path)) {
                visible.add(entry);
            }
        }
        visible.sort(Comparator
                .comparing((ArchiveEntryModel entry) -> !entry.directory())
                .thenComparing(entry -> entry.name().toLowerCase(java.util.Locale.ROOT)));
        return visible;
    }

    private boolean matchesFolderViewFilter(ArchiveEntryModel entry) {
        if (entry == null || entry.name().equals("..")) {
            return true;
        }
        if (entry.directory()) {
            return true;
        }
        FileTypeFilter filter = activeFileTypeFilter == null ? FileTypeFilter.ALL : activeFileTypeFilter;
        return filter.matches(entry);
    }

    private boolean matchesSearchTypeFilter(ArchiveEntryModel entry) {
        if (entry == null || entry.name().equals("..")) {
            return false;
        }
        FileTypeFilter filter = activeFileTypeFilter == null ? FileTypeFilter.ALL : activeFileTypeFilter;
        if (entry.directory()) {
            return filter == FileTypeFilter.ALL;
        }
        return filter.matches(entry);
    }

    private boolean isSearchActive() {
        return !normalizeSearchText(searchText).isBlank();
    }

    private String normalizeSearchText(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private void updateSearchFilter() {
        searchText = searchField == null ? "" : searchField.getText();
        updateClearSearchButton();
        refreshVisibleEntries();
    }

    private void updateTypeFilter() {
        Object selected = fileTypeFilterCombo == null ? null : fileTypeFilterCombo.getSelectedItem();
        activeFileTypeFilter = selected instanceof FileTypeFilter filter ? filter : FileTypeFilter.ALL;
        refreshVisibleEntries();
        setStatus(languageService.text("message.filter_changed"));
    }

    private void onTreeSelectionChanged(TreeSelectionEvent event) {
        if (treeChangeFromCode) {
            return;
        }
        TreePath path = event.getPath();
        Object last = path.getLastPathComponent();
        if (!(last instanceof DefaultMutableTreeNode node)) {
            return;
        }
        Object value = node.getUserObject();
        if (value instanceof TreeItem item) {
            currentFolder = normalizeFolder(item.path());
            refreshVisibleEntries();
            if (previewPanel != null) {
                previewPanel.showArchive(displayFolder(currentFolder));
            }
            setStatus(languageService.text("message.folder_changed") + ": " + displayFolder(currentFolder));
        }
    }

    private void onArchiveEntrySelected(ArchiveEntryModel entry) {
        if (entry == null) {
            updateFooter();
            return;
        }
        updateFooter();
        if (archiveController != null) {
            archiveController.previewEntry(entry);
        } else {
            previewPanel.showEntry(entry);
        }
    }

    private void onArchiveEntryOpened(ArchiveEntryModel entry) {
        if (entry == null) {
            return;
        }
        if (entry.name().equals("..")) {
            navigateUp();
            return;
        }
        if (entry.directory()) {
            currentFolder = normalizeFolder(entry.name());
            selectTreePath(currentFolder);
            refreshVisibleEntries();
            previewPanel.showArchive(displayFolder(currentFolder));
            setStatus(languageService.text("message.folder_changed") + ": " + displayFolder(currentFolder));
            return;
        }
        setStatus(languageService.text("message.file_selected") + ": " + baseName(entry.name()));
        if (archiveController != null) {
            archiveController.editEntryWithExternalEditor(entry);
        }
    }

    private void navigateUp() {
        String folder = normalizeFolder(currentFolder);
        if (folder.isBlank()) {
            return;
        }
        String trimmed = folder.substring(0, folder.length() - 1);
        int index = trimmed.lastIndexOf('/');
        currentFolder = index < 0 ? "" : trimmed.substring(0, index + 1);
        selectTreePath(currentFolder);
        refreshVisibleEntries();
        previewPanel.showArchive(displayFolder(currentFolder));
        setStatus(languageService.text("message.folder_changed") + ": " + displayFolder(currentFolder));
    }

    private void selectTreePath(String folder) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) archiveTree.getModel().getRoot();
        TreePath found = findTreePath(new TreePath(root), normalizeFolder(folder));
        if (found != null) {
            treeChangeFromCode = true;
            archiveTree.setSelectionPath(found);
            archiveTree.scrollPathToVisible(found);
            treeChangeFromCode = false;
        }
    }

    private TreePath findTreePath(TreePath parentPath, String folder) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
        Object value = node.getUserObject();
        if (value instanceof TreeItem item && item.path().equals(folder)) {
            return parentPath;
        }
        for (int index = 0; index < node.getChildCount(); index++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(index);
            TreePath found = findTreePath(parentPath.pathByAddingChild(child), folder);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private void updateFooter() {
        ArchiveEntryModel selectedEntry = archiveListPanel == null ? null : archiveListPanel.selectedEntry();
        int selected = selectedEntry == null ? 0 : 1;
        int fileCount = archiveListPanel == null ? 0 : archiveListPanel.fileCount();
        int directoryCount = archiveListPanel == null ? 0 : archiveListPanel.directoryCount();
        long totalSize = archiveListPanel == null ? 0 : archiveListPanel.totalSize();
        leftStatusLabel.setText(selected + " " + languageService.text("label.files_selected") + ", " + formatSize(selectedEntry == null ? 0 : selectedEntry.size()));
        int visibleCount = archiveListPanel == null ? 0 : archiveListPanel.entryCount();
        int totalCount = currentEntries == null ? 0 : currentEntries.size();
        String locationText = isSearchActive()
                ? languageService.text("label.search") + ": " + searchText.trim()
                : languageService.text("label.folder") + ": " + displayFolder(currentFolder);
        rightStatusLabel.setText(locationText
                + "   |   " + languageService.text("label.filtered") + ": " + visibleCount + "/" + totalCount
                + "   |   " + languageService.text("label.file") + ": " + fileCount
                + ", " + languageService.text("label.folder") + ": " + directoryCount
                + ", " + languageService.text("label.archive_size") + ": " + formatSize(totalSize));
    }

    private String safeExistingFolder(String folder) {
        String normalized = normalizeFolder(folder);
        while (!normalized.isBlank() && !folderExists(normalized)) {
            String trimmed = normalized.substring(0, normalized.length() - 1);
            int index = trimmed.lastIndexOf('/');
            normalized = index < 0 ? "" : trimmed.substring(0, index + 1);
        }
        return normalized;
    }

    private boolean folderExists(String folder) {
        String normalized = normalizeFolder(folder);
        if (normalized.isBlank()) {
            return true;
        }
        for (ArchiveEntryModel entry : currentEntries) {
            String path = entry.name().replace('\\', '/');
            if (path.startsWith(normalized)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank() || folder.equals("..")) {
            return "";
        }
        String normalized = folder.replace('\\', '/');
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        return normalized;
    }

    private String displayFolder(String folder) {
        String normalized = normalizeFolder(folder);
        return normalized.isBlank() ? "/" : normalized;
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

    private String formatSize(long bytes) {
        if (bytes < 0) {
            return "-";
        }
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.0f KB", kb);
        }
        double mb = kb / 1024.0;
        return String.format("%.1f MB", mb);
    }

    private void showSettingsDialog() {
        SettingsDialog dialog = new SettingsDialog(this, languageService, themeService, settingsService);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showLicenseDialog() {
        LicenseDialog dialog = new LicenseDialog(this, languageService, themeService, licenseService);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        updateLicenseBadge();
    }

    private void runUpdateCheckAction() {
        if (startupController == null) {
            showUpdateError(languageService.text("update.not_ready"));
            return;
        }
        startupController.checkForUpdatesManually();
    }

    private void openUpdateDirectory(Path updateFile) {
        try {
            Path directory = updateFile.getParent();
            if (directory == null) {
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(directory.toFile());
                return;
            }
            if (System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).contains("windows")) {
                new ProcessBuilder("explorer.exe", directory.toAbsolutePath().toString()).start();
            }
        } catch (Exception exception) {
            showUpdateError(exception.getMessage());
        }
    }

    private void showNotReadyMessage() {
        setStatus(languageService.text("message.feature_coming_soon"));
    }

    private void focusSearchField() {
        if (searchField == null) {
            return;
        }
        searchField.requestFocusInWindow();
        searchField.selectAll();
        setStatus(languageService.text("message.search_focused"));
    }

    private void setFileTypeFilter(FileTypeFilter filter) {
        FileTypeFilter safeFilter = filter == null ? FileTypeFilter.ALL : filter;
        activeFileTypeFilter = safeFilter;
        if (fileTypeFilterCombo != null) {
            fileTypeFilterCombo.setSelectedItem(safeFilter);
        } else {
            refreshVisibleEntries();
        }
        setStatus(languageService.text("message.filter_changed"));
    }

    private void setArchiveViewMode(ArchiveListPanel.ViewMode viewMode) {
        if (archiveListPanel == null || viewMode == null) {
            return;
        }
        archiveListPanel.setViewMode(viewMode);
        updateViewModeButtons();
    }

    private void runOpenSelectedAction() {
        ArchiveEntryModel selectedEntry = selectedArchiveEntry();
        if (selectedEntry == null) {
            showNotReadyMessage();
            return;
        }
        onArchiveEntryOpened(selectedEntry);
    }

    private void runExtractAllAction() {
        if (archiveController == null) {
            showNotReadyMessage();
            return;
        }
        archiveController.extractArchive();
    }

    private void runDeleteAction() {
        if (archiveController == null) {
            showNotReadyMessage();
            return;
        }
        archiveController.deleteSelectedEntry();
    }

    private void runAddAction() {
        if (archiveController == null) {
            showNotReadyMessage();
            return;
        }
        archiveController.addToArchive();
    }

    private void runExtractSelectedAction() {
        if (archiveController == null) {
            showNotReadyMessage();
            return;
        }
        archiveController.extractSelectedEntry();
    }


    private void runRenameAction() {
        if (archiveController == null) {
            showNotReadyMessage();
            return;
        }
        archiveController.renameSelectedEntry();
    }

    private void copySelectedEntryPath() {
        String entryName = selectedArchiveEntryName();
        if (entryName == null || entryName.isBlank() || entryName.equals("..")) {
            showNotReadyMessage();
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(entryName), null);
        setStatus(languageService.text("message.path_copied") + ": " + entryName);
    }

    private void runTestAction() {
        if (archiveController == null) {
            showNotReadyMessage();
            return;
        }
        archiveController.testArchive();
    }

    private void showTestMessage() {
        setStatus(languageService.text("message.test_ready"));
    }

    private void showAboutMessage() {
        JOptionPane.showMessageDialog(this, languageService.text("message.about_text"), languageService.text("menu.about"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void rebuildAfterThemeChange() {
        SwingUtilities.invokeLater(() -> {
            flatLafTool.install(themeService.activeThemeMode());
            UIManager.put("RootPane.background", themeService.color("window.background"));
            getContentPane().removeAll();
            getContentPane().setBackground(themeService.color("window.background"));
            buildLayout();
            archiveListPanel.setViewMode(ArchiveListPanel.ViewMode.DETAILS);
            updateViewModeButtons();
            SwingUtilities.updateComponentTreeUI(this);
        });
    }

    private record TreeItem(String name, String path) {
        @Override
        public String toString() {
            return name;
        }
    }

    private final class ArchiveDropTransferHandler extends TransferHandler {
        @Override
        public int getSourceActions(JComponent component) {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent component) {
            if (archiveController == null) {
                return null;
            }

            List<File> exportedFiles = archiveController.prepareDragExport();
            if (exportedFiles.isEmpty()) {
                return null;
            }

            setStatus(languageService.text("message.drag_out_started"));
            return new FileListTransferable(exportedFiles);
        }

        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            try {
                Transferable transferable = support.getTransferable();
                Object transferData = transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if (!(transferData instanceof List<?> droppedItems)) {
                    return false;
                }

                List<Path> droppedPaths = droppedItems.stream()
                        .filter(File.class::isInstance)
                        .map(File.class::cast)
                        .map(File::toPath)
                        .toList();

                if (droppedPaths.isEmpty()) {
                    return false;
                }

                if (archiveController == null) {
                    showNotReadyMessage();
                    return false;
                }

                archiveController.addPathsToArchive(droppedPaths);
                setStatus(languageService.text("message.drop_add_started"));
                return true;
            } catch (Exception exception) {
                showError(exception.getMessage());
                return false;
            }
        }
    }

    private static final class FileListTransferable implements Transferable {
        private final List<File> files;

        private FileListTransferable(List<File> files) {
            this.files = files;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.javaFileListFlavor};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            return files;
        }
    }

    private enum FileTypeFilter {
        ALL("filter.all") {
            @Override
            boolean matches(ArchiveEntryModel entry) {
                return true;
            }
        },
        IMAGES("filter.images") {
            @Override
            boolean matches(ArchiveEntryModel entry) {
                return isExtension(entry, "jpg", "jpeg", "png", "gif", "webp", "bmp");
            }
        },
        DOCUMENTS("filter.documents") {
            @Override
            boolean matches(ArchiveEntryModel entry) {
                return isExtension(entry, "txt", "md", "pdf", "doc", "docx", "xls", "xlsx", "csv", "ppt", "pptx");
            }
        },
        ARCHIVES("filter.archives") {
            @Override
            boolean matches(ArchiveEntryModel entry) {
                return isExtension(entry, "zip", "rar", "7z", "tar", "gz", "bz2");
            }
        },
        OTHER("filter.other") {
            @Override
            boolean matches(ArchiveEntryModel entry) {
                return !IMAGES.matches(entry) && !DOCUMENTS.matches(entry) && !ARCHIVES.matches(entry) && !entry.directory();
            }
        };

        private final String labelKey;

        FileTypeFilter(String labelKey) {
            this.labelKey = labelKey;
        }

        String labelKey() {
            return labelKey;
        }

        abstract boolean matches(ArchiveEntryModel entry);

        private static boolean isExtension(ArchiveEntryModel entry, String... extensions) {
            if (entry == null || entry.directory()) {
                return false;
            }
            String name = entry.name();
            int index = name == null ? -1 : name.lastIndexOf('.');
            if (index < 0 || index == name.length() - 1) {
                return false;
            }
            String extension = name.substring(index + 1).toLowerCase(java.util.Locale.ROOT);
            for (String allowed : extensions) {
                if (extension.equals(allowed)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class RibbonPanel extends JPanel {
        private final ThemeService themeService;

        private RibbonPanel(ThemeService themeService) {
            this.themeService = themeService;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setPaint(new GradientPaint(0, 0, themeService.color("ribbon.top"), 0, getHeight(), themeService.color("ribbon.bottom")));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }
    }

    private static final class RibbonActionIcon implements Icon {
        private static final int SIZE = 42;
        private final String type;
        private final Color color;

        private RibbonActionIcon(String type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(color);
            switch (type) {
                case "open" -> paintOpen(g);
                case "extract" -> paintExtract(g);
                case "new" -> paintNew(g);
                case "add" -> paintAdd(g);
                case "delete" -> paintDelete(g);
                case "rename" -> paintRename(g);
                case "test" -> paintTest(g);
                case "scan" -> paintScan(g);
                case "columns" -> paintColumns(g);
                case "codepage" -> paintCodepage(g);
                default -> paintFile(g);
            }
            g.dispose();
        }

        private void paintOpen(Graphics2D g) {
            g.drawRoundRect(7, 13, 27, 20, 3, 3);
            g.drawLine(8, 13, 16, 7);
            g.drawLine(16, 7, 25, 7);
            g.drawLine(34, 22, 24, 22);
            g.drawLine(34, 22, 29, 17);
            g.drawLine(34, 22, 29, 27);
        }

        private void paintExtract(Graphics2D g) {
            g.drawRoundRect(8, 6, 18, 28, 2, 2);
            g.drawRoundRect(16, 10, 18, 24, 2, 2);
            g.drawLine(25, 18, 25, 29);
            g.drawLine(25, 29, 20, 24);
            g.drawLine(25, 29, 30, 24);
        }

        private void paintNew(Graphics2D g) {
            paintFile(g);
            g.drawLine(21, 16, 21, 28);
            g.drawLine(15, 22, 27, 22);
        }

        private void paintAdd(Graphics2D g) {
            paintFile(g);
            g.drawLine(23, 18, 23, 30);
            g.drawLine(17, 24, 29, 24);
        }

        private void paintDelete(Graphics2D g) {
            paintFile(g);
            g.drawLine(15, 24, 29, 24);
        }

        private void paintRename(Graphics2D g) {
            paintFile(g);
            g.drawLine(15, 30, 29, 16);
            g.drawLine(25, 16, 29, 16);
            g.drawLine(29, 16, 29, 20);
        }

        private void paintTest(Graphics2D g) {
            g.drawLine(24, 4, 12, 22);
            g.drawLine(12, 22, 23, 21);
            g.drawLine(23, 21, 17, 38);
            g.drawLine(17, 38, 31, 17);
            g.drawLine(31, 17, 21, 18);
        }

        private void paintScan(Graphics2D g) {
            g.drawRoundRect(9, 8, 24, 27, 7, 7);
            g.drawLine(15, 21, 21, 27);
            g.drawLine(21, 27, 30, 16);
        }

        private void paintColumns(Graphics2D g) {
            for (int row = 0; row < 4; row++) {
                int y = 10 + row * 7;
                g.fillOval(8, y, 3, 3);
                g.drawLine(16, y + 1, 34, y + 1);
            }
        }

        private void paintCodepage(Graphics2D g) {
            g.drawOval(8, 8, 26, 26);
            g.drawLine(8, 21, 34, 21);
            g.drawLine(21, 8, 21, 34);
            g.drawArc(13, 8, 16, 26, 90, 180);
            g.drawArc(13, 8, 16, 26, -90, 180);
        }

        private void paintFile(Graphics2D g) {
            g.drawRoundRect(11, 6, 22, 30, 2, 2);
            g.drawLine(25, 6, 33, 14);
            g.drawLine(25, 6, 25, 14);
            g.drawLine(25, 14, 33, 14);
        }
    }

    private static final class StatusViewIcon implements Icon {
        private static final int SIZE = 14;
        private final String type;
        private final Color color;

        private StatusViewIcon(String type, Color color) {
            this.type = type;
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return SIZE;
        }

        @Override
        public int getIconHeight() {
            return SIZE;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.translate(x, y);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            if ("grid".equals(type)) {
                for (int row = 0; row < 2; row++) {
                    for (int column = 0; column < 2; column++) {
                        g.drawRect(column * 7 + 1, row * 7 + 1, 5, 5);
                    }
                }
            } else {
                for (int row = 0; row < 3; row++) {
                    int lineY = 2 + row * 4;
                    g.fillRect(1, lineY, 3, 3);
                    g.drawLine(6, lineY + 1, 13, lineY + 1);
                }
            }
            g.dispose();
        }
    }

    private static final class PreviewPanel extends JPanel {
        private final ThemeService themeService;
        private final LanguageService languageService;
        private BufferedImage image;
        private ArchiveEntryModel entry;
        private String archiveName;

        private PreviewPanel(ThemeService themeService, LanguageService languageService) {
            this.themeService = themeService;
            this.languageService = languageService;
            setPreferredSize(new Dimension(220, 150));
            setBackground(themeService.color("preview.background"));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, themeService.color("panel.divider")),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
            ));
        }

        private void showArchive(String archiveName) {
            this.archiveName = archiveName;
            this.entry = null;
            this.image = null;
            repaint();
        }

        private void showEntry(ArchiveEntryModel entry) {
            this.entry = entry;
            this.image = null;
            repaint();
        }

        private void showImage(ArchiveEntryModel entry, BufferedImage image) {
            this.entry = entry;
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            if (image != null) {
                paintImagePreview(g);
            } else {
                paintPlaceholder(g);
            }
            g.dispose();
        }

        private void paintImagePreview(Graphics2D g) {
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            g.setColor(themeService.color("preview.background"));
            g.fillRect(0, 0, panelWidth, panelHeight);
            double scale = Math.min((panelWidth - 12) / (double) image.getWidth(), (panelHeight - 28) / (double) image.getHeight());
            int width = Math.max(1, (int) (image.getWidth() * scale));
            int height = Math.max(1, (int) (image.getHeight() * scale));
            int x = (panelWidth - width) / 2;
            int y = 6;
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            g.drawImage(scaled, x, y, null);
            g.setColor(themeService.color("text.primary"));
            g.setFont(themeService.font("default"));
            g.drawString(shortName(entry.name()), themeService.spacing("sm"), panelHeight - themeService.spacing("sm"));
        }

        private void paintPlaceholder(Graphics2D g) {
            int width = getWidth();
            int height = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(132, 180, 238), 0, height, new Color(36, 95, 160)));
            g.fillRect(0, 0, width, height);
            g.setColor(new Color(220, 238, 255, 180));
            g.fillOval(width / 3, height / 2, width, height);
            g.setColor(new Color(24, 62, 116, 130));
            g.setStroke(new BasicStroke(3f));
            g.drawArc(width / 5, height / 3, width / 2, height / 2, 20, 180);
            g.setColor(Color.WHITE);
            g.setFont(themeService.font("default").deriveFont(Font.BOLD));
            String firstLine = entry == null ? archiveName : shortName(entry.name());
            String secondLine = entry == null ? languageService.text("label.preview") : formatEntryType(entry);
            g.drawString(firstLine == null ? "UniZip" : firstLine, themeService.spacing("md"), height - 34);
            g.setFont(themeService.font("default"));
            g.drawString(secondLine, themeService.spacing("md"), height - themeService.spacing("md"));
        }

        private String formatEntryType(ArchiveEntryModel entry) {
            if (entry.name().equals("..")) {
                return languageService.text("label.parent_folder");
            }
            if (entry.directory()) {
                return languageService.text("label.folder");
            }
            return languageService.text("label.file") + " - " + formatSize(entry.size());
        }

        private String shortName(String path) {
            if (path == null || path.isBlank()) {
                return "";
            }
            if (path.equals("..")) {
                return path;
            }
            String normalized = path.replace('\\', '/');
            if (normalized.endsWith("/")) {
                normalized = normalized.substring(0, normalized.length() - 1);
            }
            int index = normalized.lastIndexOf('/');
            return index >= 0 ? normalized.substring(index + 1) : normalized;
        }

        private String formatSize(long bytes) {
            if (bytes < 0) {
                return "-";
            }
            if (bytes < 1024) {
                return bytes + " B";
            }
            double kb = bytes / 1024.0;
            if (kb < 1024) {
                return String.format("%.0f KB", kb);
            }
            double mb = kb / 1024.0;
            return String.format("%.1f MB", mb);
        }
    }
}
