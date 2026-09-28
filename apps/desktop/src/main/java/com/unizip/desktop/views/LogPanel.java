/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/views/LogPanel.java
# 📌 Amac: UniZip islem loglarini alt panelde gostermek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Bandizip Light tema ile uyumlu sade log paneli

Bagimli Oldugu Katman: View
*/
package com.unizip.desktop.views;

import com.unizip.desktop.services.LanguageService;
import com.unizip.desktop.services.LogService;
import com.unizip.desktop.services.ThemeService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

public final class LogPanel extends JPanel {
    private final ThemeService themeService;
    private final LanguageService languageService;
    private final LogService logService;
    private final JTextArea logArea;

    public LogPanel(ThemeService themeService, LanguageService languageService, LogService logService) {
        super(new BorderLayout(themeService.spacing("sm"), themeService.spacing("sm")));
        this.themeService = themeService;
        this.languageService = languageService;
        this.logService = logService;
        this.logArea = new JTextArea(5, 20);
        build();
        refresh();
    }

    public void refresh() {
        logArea.setText(String.join(System.lineSeparator(), logService.readAll()));
    }

    private void build() {
        setBackground(themeService.color("panel.background"));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeService.color("panel.border")),
                BorderFactory.createEmptyBorder(themeService.spacing("md"), themeService.spacing("md"), themeService.spacing("md"), themeService.spacing("md"))
        ));

        JLabel title = new JLabel(languageService.text("label.logs"));
        title.setFont(themeService.font("title").deriveFont(18f));
        title.setForeground(themeService.color("text.primary"));

        logArea.setEditable(false);
        logArea.setFont(themeService.font("mono"));
        logArea.setBackground(themeService.color("input.background"));
        logArea.setForeground(themeService.color("text.secondary"));
        logArea.setBorder(BorderFactory.createEmptyBorder(themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm"), themeService.spacing("sm")));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(themeService.color("panel.border")));
        scrollPane.getViewport().setBackground(themeService.color("input.background"));

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}
