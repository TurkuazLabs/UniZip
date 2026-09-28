/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/ExtractOptionsDialog.java
# 📌 Amac: ZIP cikarma hedef klasoru, alt klasor ve overwrite seceneklerini tek ekranda toplamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: secili/tum cikarma akisi icin hedef klasor ve cakisma politikasini View katmaninda alir

Bagimli Oldugu Katman: View
*/
package com.unizip.desktop.views;

import com.unizip.desktop.models.ExtractOverwriteMode;
import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.ThemeService;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;

public final class ExtractOptionsDialog extends JDialog {
    private final LanguageService languageService;
    private final ThemeService themeService;
    private final JFileChooser folderChooser;
    private final JCheckBox createArchiveFolderCheckBox;
    private final JComboBox<OverwriteOption> overwriteComboBox;
    private Result result;

    public ExtractOptionsDialog(JFrame owner, LanguageService languageService, ThemeService themeService) {
        super(owner, languageService.text("dialog.extract_title"), true);
        this.languageService = languageService;
        this.themeService = themeService;
        this.folderChooser = new JFileChooser(FileSystemView.getFileSystemView());
        this.createArchiveFolderCheckBox = new JCheckBox(languageService.text("option.extract_to_archive_folder"));
        this.overwriteComboBox = new JComboBox<>(new OverwriteOption[]{
                new OverwriteOption(ExtractOverwriteMode.OVERWRITE, languageService.text("extract.overwrite_mode.overwrite")),
                new OverwriteOption(ExtractOverwriteMode.SKIP, languageService.text("extract.overwrite_mode.skip")),
                new OverwriteOption(ExtractOverwriteMode.RENAME, languageService.text("extract.overwrite_mode.rename"))
        });
        this.result = Result.cancelled();
        buildLayout();
    }

    public static Result showDialog(JFrame owner, LanguageService languageService, ThemeService themeService) {
        ExtractOptionsDialog dialog = new ExtractOptionsDialog(owner, languageService, themeService);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return dialog.result;
    }

    private void buildLayout() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 550);
        setResizable(true);

        folderChooser.setDialogTitle(languageService.text("message.select_output_dir"));
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setControlButtonsAreShown(true);
        folderChooser.setApproveButtonText(languageService.text("button.extract_zip"));
        folderChooser.setApproveButtonToolTipText(languageService.text("button.extract_zip"));

        folderChooser.addActionListener(event -> {
            if (JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())) {
                if (folderChooser.getSelectedFile() != null) {
                    result = new Result(
                            true,
                            folderChooser.getSelectedFile().toPath(),
                            createArchiveFolderCheckBox.isSelected(),
                            selectedOverwriteMode()
                    );
                } else {
                    result = Result.cancelled();
                }
                dispose();
            }
            if (JFileChooser.CANCEL_SELECTION.equals(event.getActionCommand())) {
                result = Result.cancelled();
                dispose();
            }
        });

        createArchiveFolderCheckBox.setSelected(true);
        createArchiveFolderCheckBox.setFont(themeService.font("default"));
        createArchiveFolderCheckBox.setForeground(themeService.color("text.primary"));
        createArchiveFolderCheckBox.setOpaque(false);

        overwriteComboBox.setFont(themeService.font("default"));

        JLabel overwriteLabel = new JLabel(languageService.text("label.extract_overwrite_mode"));
        overwriteLabel.setFont(themeService.font("default"));
        overwriteLabel.setForeground(themeService.color("text.primary"));

        JPanel optionsPanel = new JPanel(new BorderLayout(0, 0));
        optionsPanel.setBackground(themeService.color("panel.background"));
        optionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, themeService.color("panel.divider")),
                BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("md"), themeService.spacing("sm"), themeService.spacing("md"))
        ));

        JPanel overwritePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, themeService.spacing("sm"), 0));
        overwritePanel.setOpaque(false);
        overwritePanel.add(overwriteLabel);
        overwritePanel.add(overwriteComboBox);

        optionsPanel.add(createArchiveFolderCheckBox, BorderLayout.WEST);
        optionsPanel.add(overwritePanel, BorderLayout.EAST);

        JPanel rootPanel = new JPanel(new BorderLayout(0, 0));
        rootPanel.setBackground(themeService.color("panel.background"));
        rootPanel.add(folderChooser, BorderLayout.CENTER);
        rootPanel.add(optionsPanel, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private ExtractOverwriteMode selectedOverwriteMode() {
        Object selected = overwriteComboBox.getSelectedItem();
        return selected instanceof OverwriteOption option ? option.mode() : ExtractOverwriteMode.OVERWRITE;
    }

    public record Result(boolean approved, Path outputDirectory, boolean createArchiveFolder, ExtractOverwriteMode overwriteMode) {
        public static Result cancelled() {
            return new Result(false, null, false, ExtractOverwriteMode.OVERWRITE);
        }
    }

    private record OverwriteOption(ExtractOverwriteMode mode, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
