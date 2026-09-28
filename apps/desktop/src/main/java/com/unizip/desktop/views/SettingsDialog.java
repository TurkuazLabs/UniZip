/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/SettingsDialog.java
# 📌 Amac: UniZip sekmeli ayarlarini duzenlemek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Sistem, UniZip, Gorunum, Dil ve Windows varsayilan uygulama gecisini yonetir

Bagimli Oldugu Katman: View | Service | Language
*/
package com.unizip.desktop.views;

import com.unizip.desktop.models.FileAssociationScope;
import com.unizip.desktop.models.FileAssociationStatusModel;
import com.unizip.desktop.models.LanguageOptionModel;
import com.unizip.desktop.models.ThemeOptionModel;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.SettingsService;
import com.unizip.desktop.services.ThemeService;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public final class SettingsDialog extends JDialog {
    private static final String WORKING_FOLDER_SYSTEM_TEMP = "system_temp";
    private static final String WORKING_FOLDER_CURRENT_FOLDER = "current_folder";
    private static final String WORKING_FOLDER_CUSTOM_FOLDER = "custom_folder";

    private final LanguageService languageService;
    private final ThemeService themeService;
    private final SettingsService settingsService;

    private JComboBox<ThemeOptionModel> themeComboBox;
    private JComboBox<LanguageOptionModel> languageComboBox;
    private JTextField archiveFileExtensionsField;
    private JCheckBox contextMenuEnabledCheckBox;
    private JCheckBox groupContextMenuCheckBox;
    private JCheckBox blockRootFolderDuplicationCheckBox;
    private JRadioButton systemTempRadioButton;
    private JRadioButton currentFolderRadioButton;
    private JRadioButton customFolderRadioButton;
    private JTextField customWorkingFolderField;
    private JCheckBox openTextOnDoubleClickCheckBox;
    private JTextField textEditorCommandField;
    private JTextField editableTextExtensionsField;
    private JCheckBox openImageOnDoubleClickCheckBox;
    private JTextField imageEditorCommandField;
    private JTextField editableImageExtensionsField;
    private JTextField viewerCommandField;
    private JTextField diffCommandField;
    private JCheckBox showParentFolderCheckBox;
    private JCheckBox showExactSizesCheckBox;
    private JCheckBox selectFullRowCheckBox;
    private JCheckBox showSystemMenuCheckBox;
    private JCheckBox useLargeMemoryPagesCheckBox;
    private JTable associationTable;

    public SettingsDialog(
            JFrame owner,
            LanguageService languageService,
            ThemeService themeService,
            SettingsService settingsService
    ) {
        super(owner, languageService.text("dialog.settings_title"), true);
        this.languageService = languageService;
        this.themeService = themeService;
        this.settingsService = settingsService;
        buildLayout();
    }

    private void buildLayout() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 580);
        setResizable(false);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(themeService.color("panel.background"));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md")
        ));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(themeService.font("default"));
        tabbedPane.addTab(languageService.text("settings.tab.system"), buildSystemTab());
        tabbedPane.addTab(languageService.text("settings.tab.unizip"), buildUniZipTab());
        tabbedPane.addTab(languageService.text("settings.tab.folders"), buildFoldersTab());
        tabbedPane.addTab(languageService.text("settings.tab.editors"), buildEditorsTab());
        tabbedPane.addTab(languageService.text("settings.tab.view"), buildViewTab());
        tabbedPane.addTab(languageService.text("settings.tab.language"), buildLanguageTab());

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        rootPanel.add(buildButtonPanel(), BorderLayout.SOUTH);
        setContentPane(rootPanel);
    }

    private JPanel buildSystemTab() {
        JPanel panel = baseFormPanel();
        contextMenuEnabledCheckBox = checkBox("settings.context_menu_enabled", settingsService.contextMenuEnabled());
        groupContextMenuCheckBox = checkBox("settings.group_context_menu", settingsService.groupContextMenu());
        blockRootFolderDuplicationCheckBox = checkBox("settings.block_root_folder_duplication", settingsService.blockRootFolderDuplication());
        showSystemMenuCheckBox = checkBox("settings.show_system_menu", settingsService.showSystemMenu());
        useLargeMemoryPagesCheckBox = checkBox("settings.use_large_memory_pages", settingsService.useLargeMemoryPages());

        addFullWidthRow(panel, 0, contextMenuEnabledCheckBox);
        addFullWidthRow(panel, 1, groupContextMenuCheckBox);
        addFullWidthRow(panel, 2, blockRootFolderDuplicationCheckBox);
        addFullWidthRow(panel, 3, showSystemMenuCheckBox);
        addFullWidthRow(panel, 4, useLargeMemoryPagesCheckBox);
        addFullWidthRow(panel, 5, mutedLabel(languageService.text("settings.context_menu_note")));
        return panel;
    }

    private JPanel buildUniZipTab() {
        JPanel panel = new JPanel(new BorderLayout(0, themeService.spacing("sm")));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md")
        ));

        JLabel associateLabel = mutedLabel(languageService.text("settings.associate_with_unizip"));
        panel.add(associateLabel, BorderLayout.NORTH);

        associationTable = new JTable(buildAssociationTableModel());
        associationTable.setFont(themeService.font("default"));
        associationTable.getTableHeader().setFont(themeService.font("default"));
        associationTable.setRowHeight(22);
        associationTable.setFillsViewportHeight(true);
        associationTable.setShowGrid(false);
        associationTable.setIntercellSpacing(new java.awt.Dimension(0, 0));
        associationTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        associationTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        associationTable.getColumnModel().getColumn(0).setCellRenderer(new ExtensionTypeRenderer());
        TableColumnModel columnModel = associationTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120);
        columnModel.getColumn(1).setPreferredWidth(230);
        columnModel.getColumn(2).setPreferredWidth(230);

        JPanel tablePanel = new JPanel(new BorderLayout(0, themeService.spacing("xs")));
        tablePanel.setOpaque(false);
        tablePanel.add(buildAssociationButtonHeader(), BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(associationTable), BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);

        archiveFileExtensionsField = new JTextField(settingsService.archiveFileExtensionsRaw());
        JPanel extensionPanel = new JPanel(new GridBagLayout());
        extensionPanel.setOpaque(false);
        addRow(extensionPanel, 0, languageService.text("settings.archive_file_extensions"), archiveFileExtensionsField);
        addFullWidthRow(extensionPanel, 1, mutedLabel(languageService.text("settings.association_note")));
        JButton defaultAppsButton = new JButton(languageService.text("button.open_default_apps"));
        defaultAppsButton.setFont(themeService.font("default"));
        defaultAppsButton.addActionListener(event -> openWindowsDefaultApps());
        addFullWidthRow(extensionPanel, 2, defaultAppsButton);
        panel.add(extensionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAssociationButtonHeader() {
        JPanel buttonHeader = new JPanel(new GridBagLayout());
        buttonHeader.setOpaque(false);

        GridBagConstraints leftSpacer = new GridBagConstraints();
        leftSpacer.gridx = 0;
        leftSpacer.gridy = 0;
        leftSpacer.weightx = 0.0;
        leftSpacer.fill = GridBagConstraints.HORIZONTAL;
        leftSpacer.insets = new Insets(0, 0, 0, 0);
        JLabel spacer = new JLabel(" ");
        spacer.setPreferredSize(new java.awt.Dimension(120, 28));
        buttonHeader.add(spacer, leftSpacer);

        JButton currentUserButton = associationButton("settings.associate_current_user", "button.associate_current_user");
        GridBagConstraints currentUserConstraints = new GridBagConstraints();
        currentUserConstraints.gridx = 1;
        currentUserConstraints.gridy = 0;
        currentUserConstraints.weightx = 1.0;
        currentUserConstraints.fill = GridBagConstraints.HORIZONTAL;
        currentUserConstraints.insets = new Insets(0, 0, 0, themeService.spacing("xl"));
        buttonHeader.add(currentUserButton, currentUserConstraints);

        JButton allUsersButton = associationButton("settings.associate_all_users", "button.associate_all_users");
        GridBagConstraints allUsersConstraints = new GridBagConstraints();
        allUsersConstraints.gridx = 2;
        allUsersConstraints.gridy = 0;
        allUsersConstraints.weightx = 1.0;
        allUsersConstraints.fill = GridBagConstraints.HORIZONTAL;
        allUsersConstraints.insets = new Insets(0, 0, 0, 0);
        buttonHeader.add(allUsersButton, allUsersConstraints);

        return buttonHeader;
    }

    private JButton associationButton(String tooltipKey, String textKey) {
        FileAssociationScope scope = tooltipKey.equals("settings.associate_all_users")
                ? FileAssociationScope.ALL_USERS
                : FileAssociationScope.CURRENT_USER;
        JButton button = new JButton(languageService.text(textKey));
        button.setFont(themeService.font("default"));
        button.setFocusPainted(false);
        button.setToolTipText(languageService.text(tooltipKey));
        button.addActionListener(event -> applyFileAssociation(scope));
        return button;
    }

    private DefaultTableModel buildAssociationTableModel() {
        String currentUser = System.getProperty("user.name", languageService.text("settings.current_user"));
        String[] columns = {
                languageService.text("settings.association_type"),
                currentUser,
                languageService.text("settings.association_all_users")
        };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (FileAssociationStatusModel status : settingsService.fileAssociationStatuses()) {
            model.addRow(new Object[]{
                    status.extension().toLowerCase(Locale.ROOT),
                    status.currentUserAssociation(),
                    status.allUsersAssociation()
            });
        }
        return model;
    }

    private final class ExtensionTypeRenderer extends DefaultTableCellRenderer {
        private final FileSystemView fileSystemView = FileSystemView.getFileSystemView();

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String extension = String.valueOf(value);
            label.setText(extension);
            label.setIcon(archiveIcon(extension));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setIconTextGap(themeService.spacing("sm"));
            return label;
        }

        private Icon archiveIcon(String extension) {
            try {
                return fileSystemView.getSystemIcon(new File("unizip-dummy." + extension));
            } catch (Exception exception) {
                return null;
            }
        }
    }

    private JPanel buildFoldersTab() {
        JPanel panel = baseFormPanel();
        systemTempRadioButton = radioButton("settings.working_folder_system_temp");
        currentFolderRadioButton = radioButton("settings.working_folder_current_folder");
        customFolderRadioButton = radioButton("settings.working_folder_custom_folder");
        ButtonGroup group = new ButtonGroup();
        group.add(systemTempRadioButton);
        group.add(currentFolderRadioButton);
        group.add(customFolderRadioButton);
        selectWorkingFolderMode(settingsService.workingFolderMode());

        customWorkingFolderField = new JTextField(settingsService.workingFolderCustomPath());
        customWorkingFolderField.setFont(themeService.font("default"));
        JButton browseButton = new JButton(languageService.text("button.browse"));
        browseButton.setFont(themeService.font("default"));
        browseButton.addActionListener(event -> browseWorkingFolder());

        JPanel customPanel = new JPanel(new BorderLayout(themeService.spacing("sm"), 0));
        customPanel.setOpaque(false);
        customPanel.add(customWorkingFolderField, BorderLayout.CENTER);
        customPanel.add(browseButton, BorderLayout.EAST);

        addFullWidthRow(panel, 0, systemTempRadioButton);
        addFullWidthRow(panel, 1, currentFolderRadioButton);
        addFullWidthRow(panel, 2, customFolderRadioButton);
        addRow(panel, 3, languageService.text("settings.working_folder_custom_path"), customPanel);
        addFullWidthRow(panel, 4, mutedLabel(languageService.text("settings.working_folder_note")));
        return panel;
    }

    private JPanel buildEditorsTab() {
        JPanel panel = baseFormPanel();
        textEditorCommandField = new JTextField(settingsService.textEditorCommand());
        editableTextExtensionsField = new JTextField(settingsService.editableTextExtensionsRaw());
        openTextOnDoubleClickCheckBox = checkBox("settings.open_text_on_double_click", settingsService.openTextOnDoubleClick());
        imageEditorCommandField = new JTextField(settingsService.imageEditorCommand());
        editableImageExtensionsField = new JTextField(settingsService.editableImageExtensionsRaw());
        openImageOnDoubleClickCheckBox = checkBox("settings.open_image_on_double_click", settingsService.openImageOnDoubleClick());
        viewerCommandField = new JTextField(settingsService.viewerCommand());
        diffCommandField = new JTextField(settingsService.diffCommand());

        addRow(panel, 0, languageService.text("settings.text_editor_command"), textEditorCommandField);
        addRow(panel, 1, languageService.text("settings.editable_text_extensions"), editableTextExtensionsField);
        addFullWidthRow(panel, 2, openTextOnDoubleClickCheckBox);
        addRow(panel, 3, languageService.text("settings.image_editor_command"), imageEditorCommandField);
        addRow(panel, 4, languageService.text("settings.editable_image_extensions"), editableImageExtensionsField);
        addFullWidthRow(panel, 5, openImageOnDoubleClickCheckBox);
        addRow(panel, 6, languageService.text("settings.viewer_command"), viewerCommandField);
        addRow(panel, 7, languageService.text("settings.diff_command"), diffCommandField);
        addFullWidthRow(panel, 8, mutedLabel(languageService.text("settings.editors_note")));
        addFullWidthRow(panel, 9, mutedLabel(languageService.text("settings.editable_extensions_note")));
        return panel;
    }

    private JPanel buildViewTab() {
        JPanel panel = baseFormPanel();
        themeComboBox = new JComboBox<>(settingsService.availableThemes().toArray(new ThemeOptionModel[0]));
        selectTheme(settingsService.activeThemeId());
        showParentFolderCheckBox = checkBox("settings.show_parent_folder_item", settingsService.showParentFolderItem());
        showExactSizesCheckBox = checkBox("settings.show_exact_sizes", settingsService.showExactSizes());
        selectFullRowCheckBox = checkBox("settings.select_full_row", settingsService.selectFullRow());

        addRow(panel, 0, languageService.text("settings.theme"), themeComboBox);
        addFullWidthRow(panel, 1, showParentFolderCheckBox);
        addFullWidthRow(panel, 2, showExactSizesCheckBox);
        addFullWidthRow(panel, 3, selectFullRowCheckBox);
        addFullWidthRow(panel, 4, mutedLabel(languageService.text("settings.restart_note")));
        return panel;
    }

    private JPanel buildLanguageTab() {
        JPanel panel = baseFormPanel();
        languageComboBox = new JComboBox<>(settingsService.availableLanguages().toArray(new LanguageOptionModel[0]));
        selectLanguage(settingsService.activeLanguageId());
        addRow(panel, 0, languageService.text("settings.language"), languageComboBox);
        addFullWidthRow(panel, 1, mutedLabel(languageService.text("settings.language_contributors")));
        addFullWidthRow(panel, 2, mutedLabel("tr : Burak Ozsoy"));
        addFullWidthRow(panel, 3, mutedLabel("en : UniZip Community"));
        addFullWidthRow(panel, 4, mutedLabel(languageService.text("settings.path") + ": " + settingsService.settingsPath()));
        return panel;
    }

    private JPanel baseFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md"),
                themeService.spacing("md")
        ));
        return panel;
    }

    private JCheckBox checkBox(String key, boolean selected) {
        JCheckBox checkBox = new JCheckBox(languageService.text(key));
        checkBox.setOpaque(false);
        checkBox.setFont(themeService.font("default"));
        checkBox.setForeground(themeService.color("text.primary"));
        checkBox.setSelected(selected);
        return checkBox;
    }

    private JRadioButton radioButton(String key) {
        JRadioButton radioButton = new JRadioButton(languageService.text(key));
        radioButton.setOpaque(false);
        radioButton.setFont(themeService.font("default"));
        radioButton.setForeground(themeService.color("text.primary"));
        return radioButton;
    }

    private JLabel mutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(themeService.font("default"));
        label.setForeground(themeService.color("text.muted"));
        return label;
    }

    private void addRow(JPanel formPanel, int row, String labelText, java.awt.Component component) {
        JLabel label = new JLabel(labelText);
        label.setFont(themeService.font("default"));
        label.setForeground(themeService.color("text.primary"));
        component.setFont(themeService.font("default"));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.weightx = 0.0;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.insets = new Insets(0, 0, themeService.spacing("sm"), themeService.spacing("md"));
        formPanel.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, themeService.spacing("sm"), 0);
        formPanel.add(component, fieldConstraints);
    }

    private void addFullWidthRow(JPanel formPanel, int row, java.awt.Component component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(themeService.spacing("sm"), 0, themeService.spacing("sm"), 0);
        formPanel.add(component, constraints);
    }

    private JPanel buildButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, themeService.spacing("sm"), 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("lg"), 0, 0, 0));

        JButton closeButton = new JButton(languageService.text("button.close"));
        closeButton.setFont(themeService.font("default"));
        closeButton.addActionListener(event -> dispose());

        JButton saveButton = new JButton(languageService.text("button.save_settings"));
        saveButton.setFont(themeService.font("default"));
        saveButton.addActionListener(event -> saveSettings());

        buttonPanel.add(closeButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private void saveSettings() {
        ThemeOptionModel selectedTheme = (ThemeOptionModel) themeComboBox.getSelectedItem();
        LanguageOptionModel selectedLanguage = (LanguageOptionModel) languageComboBox.getSelectedItem();
        if (selectedTheme == null || selectedLanguage == null) {
            return;
        }
        try {
            settingsService.savePreferences(
                    selectedTheme.id(),
                    selectedLanguage.id(),
                    openTextOnDoubleClickCheckBox.isSelected(),
                    textEditorCommandField.getText(),
                    editableTextExtensionsField.getText(),
                    openImageOnDoubleClickCheckBox.isSelected(),
                    imageEditorCommandField.getText(),
                    editableImageExtensionsField.getText(),
                    archiveFileExtensionsField.getText(),
                    contextMenuEnabledCheckBox.isSelected(),
                    groupContextMenuCheckBox.isSelected(),
                    blockRootFolderDuplicationCheckBox.isSelected(),
                    selectedWorkingFolderMode(),
                    customWorkingFolderField.getText(),
                    viewerCommandField.getText(),
                    diffCommandField.getText(),
                    showParentFolderCheckBox.isSelected(),
                    showExactSizesCheckBox.isSelected(),
                    selectFullRowCheckBox.isSelected(),
                    showSystemMenuCheckBox.isSelected(),
                    useLargeMemoryPagesCheckBox.isSelected()
            );
            JOptionPane.showMessageDialog(
                    this,
                    languageService.text("message.settings_saved"),
                    languageService.text("dialog.settings_title"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            dispose();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    languageService.text("error.operation_failed") + ": " + exception.getMessage(),
                    languageService.text("dialog.settings_title"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void selectTheme(String themeId) {
        for (int index = 0; index < themeComboBox.getItemCount(); index++) {
            ThemeOptionModel item = themeComboBox.getItemAt(index);
            if (item.id().equals(themeId)) {
                themeComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void selectLanguage(String languageId) {
        for (int index = 0; index < languageComboBox.getItemCount(); index++) {
            LanguageOptionModel item = languageComboBox.getItemAt(index);
            if (item.id().equals(languageId)) {
                languageComboBox.setSelectedIndex(index);
                return;
            }
        }
    }

    private void selectWorkingFolderMode(String mode) {
        if (WORKING_FOLDER_CURRENT_FOLDER.equals(mode)) {
            currentFolderRadioButton.setSelected(true);
        } else if (WORKING_FOLDER_CUSTOM_FOLDER.equals(mode)) {
            customFolderRadioButton.setSelected(true);
        } else {
            systemTempRadioButton.setSelected(true);
        }
    }

    private String selectedWorkingFolderMode() {
        if (currentFolderRadioButton.isSelected()) {
            return WORKING_FOLDER_CURRENT_FOLDER;
        }
        if (customFolderRadioButton.isSelected()) {
            return WORKING_FOLDER_CUSTOM_FOLDER;
        }
        return WORKING_FOLDER_SYSTEM_TEMP;
    }

    private void browseWorkingFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String currentValue = customWorkingFolderField.getText();
        if (currentValue != null && !currentValue.isBlank()) {
            chooser.setSelectedFile(Path.of(currentValue).toFile());
        }
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            customWorkingFolderField.setText(chooser.getSelectedFile().toPath().toString());
            customFolderRadioButton.setSelected(true);
        }
    }

    private void openWindowsDefaultApps() {
        try {
            if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows")) {
                JOptionPane.showMessageDialog(
                        this,
                        languageService.text("error.windows_only"),
                        languageService.text("dialog.settings_title"),
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            new ProcessBuilder("cmd", "/c", "start", "", "ms-settings:defaultapps").start();
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    languageService.text("error.default_apps_failed") + ": " + exception.getMessage(),
                    languageService.text("dialog.settings_title"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void applyFileAssociation(FileAssociationScope scope) {
        try {
            if (!settingsService.fileAssociationSupported()) {
                JOptionPane.showMessageDialog(
                        this,
                        languageService.text("error.windows_only"),
                        languageService.text("dialog.settings_title"),
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            java.util.List<String> extensions = selectedAssociationExtensions();
            settingsService.associateArchiveExtensions(scope, extensions);
            refreshAssociationTable();
            String messageKey = scope == FileAssociationScope.ALL_USERS
                    ? "message.association_saved_all_users"
                    : "message.association_saved_current_user";
            JOptionPane.showMessageDialog(
                    this,
                    languageService.text(messageKey),
                    languageService.text("dialog.settings_title"),
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception exception) {
            JOptionPane.showMessageDialog(
                    this,
                    languageService.text("error.association_failed") + ": " + exception.getMessage(),
                    languageService.text("dialog.settings_title"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private java.util.List<String> selectedAssociationExtensions() {
        java.util.List<String> extensions = new java.util.ArrayList<>();
        if (associationTable == null) {
            return archiveExtensionsFromField();
        }
        int[] selectedRows = associationTable.getSelectedRows();
        if (selectedRows.length == 0) {
            return archiveExtensionsFromField();
        }
        for (int selectedRow : selectedRows) {
            int modelRow = associationTable.convertRowIndexToModel(selectedRow);
            Object value = associationTable.getModel().getValueAt(modelRow, 0);
            if (value != null && !value.toString().isBlank()) {
                extensions.add(value.toString());
            }
        }
        return extensions;
    }

    private java.util.List<String> archiveExtensionsFromField() {
        java.util.LinkedHashSet<String> normalizedExtensions = new java.util.LinkedHashSet<>();
        String rawValue = archiveFileExtensionsField == null ? "" : archiveFileExtensionsField.getText();
        if (rawValue != null) {
            for (String part : rawValue.split("[,;\\s]+")) {
                String normalized = normalizeExtension(part);
                if (!normalized.isBlank()) {
                    normalizedExtensions.add(normalized);
                }
            }
        }
        if (normalizedExtensions.isEmpty()) {
            return settingsService.archiveFileExtensions();
        }
        return new java.util.ArrayList<>(normalizedExtensions);
    }

    private String normalizeExtension(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
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

    private void refreshAssociationTable() {
        if (associationTable == null) {
            return;
        }
        associationTable.setModel(buildAssociationTableModel());
        associationTable.getColumnModel().getColumn(0).setCellRenderer(new ExtensionTypeRenderer());
        TableColumnModel columnModel = associationTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(120);
        columnModel.getColumn(1).setPreferredWidth(230);
        columnModel.getColumn(2).setPreferredWidth(230);
    }
}
