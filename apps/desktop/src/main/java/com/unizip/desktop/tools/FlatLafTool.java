/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/FlatLafTool.java
# 📌 Amac: FlatLaf Light gorunumu Bandizip Classic duzeni icin baslatmak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: native pencere dekorasyonu, koseli kontroller ve klasik acik tema varsayilanlari

Bagimli Oldugu Katman: Tool | View
*/
package com.unizip.desktop.tools;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

public final class FlatLafTool {
    private static final String FLAT_LIGHT_CLASS = "com.formdev.flatlaf.FlatLightLaf";

    public boolean install(String themeMode) {
        try {
            configureDefaults();
            UIManager.setLookAndFeel(FLAT_LIGHT_CLASS);
            return true;
        } catch (Exception exception) {
            return installSystemFallback();
        }
    }

    private void configureDefaults() {
        System.setProperty("flatlaf.useWindowDecorations", "false");
        JFrame.setDefaultLookAndFeelDecorated(false);
        JDialog.setDefaultLookAndFeelDecorated(false);

        UIManager.put("Button.arc", 0);
        UIManager.put("Component.arc", 0);
        UIManager.put("TextComponent.arc", 0);
        UIManager.put("ProgressBar.arc", 0);
        UIManager.put("ScrollBar.thumbArc", 0);
        UIManager.put("ScrollBar.trackArc", 0);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.rowHeight", 28);
        UIManager.put("Tree.rowHeight", 24);
        UIManager.put("Component.focusColor", new java.awt.Color(2, 167, 184));
        UIManager.put("Component.borderColor", new java.awt.Color(209, 224, 231));
        UIManager.put("Button.default.background", new java.awt.Color(2, 167, 184));
        UIManager.put("Button.default.foreground", java.awt.Color.WHITE);
        UIManager.put("Table.selectionBackground", new java.awt.Color(2, 167, 184));
        UIManager.put("Table.selectionForeground", java.awt.Color.WHITE);
        UIManager.put("Tree.selectionBackground", new java.awt.Color(215, 244, 246));
        UIManager.put("Tree.selectionForeground", new java.awt.Color(16, 32, 51));
    }

    private boolean installSystemFallback() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            return false;
        } catch (Exception ignored) {
            return false;
        }
    }
}
