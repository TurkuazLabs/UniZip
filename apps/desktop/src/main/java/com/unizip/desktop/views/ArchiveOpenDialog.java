/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/ArchiveOpenDialog.java
# 📌 Amac: ZIP dosyasi secme ekranini sade tutup Indirilenler klasorunden baslatmak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ozel sol paneli kaldirir, standart JFileChooser gorunumunu korur

Bagimli Oldugu Katman: View | Tool | Language
*/
package com.unizip.desktop.views;

import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.ThemeService;
import com.unizip.desktop.tools.UserDirectoryTool;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ArchiveOpenDialog extends JDialog {
    private final LanguageService languageService;
    private final UserDirectoryTool userDirectoryTool;
    private final JFileChooser fileChooser;
    private Path selectedArchive;

    public ArchiveOpenDialog(
            JFrame owner,
            LanguageService languageService,
            ThemeService themeService,
            UserDirectoryTool userDirectoryTool
    ) {
        super(owner, languageService.text("message.select_archive"), true);
        this.languageService = languageService;
        this.userDirectoryTool = userDirectoryTool;
        this.fileChooser = new JFileChooser();
        this.selectedArchive = null;
        buildLayout();
    }

    public static Path showDialog(
            JFrame owner,
            LanguageService languageService,
            ThemeService themeService,
            UserDirectoryTool userDirectoryTool
    ) {
        ArchiveOpenDialog dialog = new ArchiveOpenDialog(owner, languageService, themeService, userDirectoryTool);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        return dialog.selectedArchive;
    }

    private void buildLayout() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 520);
        setResizable(true);

        fileChooser.setDialogTitle(languageService.text("message.select_archive"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setControlButtonsAreShown(true);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.setFileFilter(new FileNameExtensionFilter(languageService.text("filter.zip_files"), "zip"));
        fileChooser.setApproveButtonText(languageService.text("button.open_archive"));

        Path downloadsDirectory = userDirectoryTool.downloadsDirectory();
        if (Files.isDirectory(downloadsDirectory)) {
            fileChooser.setCurrentDirectory(downloadsDirectory.toFile());
        }

        fileChooser.addActionListener(event -> {
            if (JFileChooser.APPROVE_SELECTION.equals(event.getActionCommand())) {
                if (fileChooser.getSelectedFile() != null) {
                    selectedArchive = fileChooser.getSelectedFile().toPath();
                }
                dispose();
            }
            if (JFileChooser.CANCEL_SELECTION.equals(event.getActionCommand())) {
                selectedArchive = null;
                dispose();
            }
        });

        setLayout(new BorderLayout(0, 0));
        add(fileChooser, BorderLayout.CENTER);
    }
}
