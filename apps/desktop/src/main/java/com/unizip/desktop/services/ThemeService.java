/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/ThemeService.java
# 📌 Amac: Tema dosyalarindan UI tokenlarini yuklemek ve aktif temayi yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: settings repository uzerinden aktif temayi okur, ayarlar ekranina tema listesi sunar

Bagimli Oldugu Katman: Service | Repo/Model | Tool | View | Config
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.ThemeOptionModel;
import com.unizip.desktop.repositories.SettingsRepository;
import com.unizip.desktop.tools.SimpleYamlTool;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ThemeService {
    private static final String DEFAULT_THEME_ID = "Turkuaz";
    private static final String THEME_RESOURCE_DIR = "/themes/";
    private static final String THEME_EXTENSION = ".yml";

    private final SimpleYamlTool yamlTool;
    private final SettingsRepository settingsRepository;
    private Map<String, String> tokens;
    private String activeThemeId;

    public ThemeService(SimpleYamlTool yamlTool, SettingsRepository settingsRepository) {
        this.yamlTool = yamlTool;
        this.settingsRepository = settingsRepository;
        this.activeThemeId = normalizeThemeId(settingsRepository.readActiveThemeId().orElse(DEFAULT_THEME_ID));
        this.tokens = loadTokens(activeThemeId);
        if (tokens.isEmpty()) {
            this.activeThemeId = DEFAULT_THEME_ID;
            this.tokens = loadTokens(DEFAULT_THEME_ID);
        }
        if (tokens.isEmpty()) {
            this.tokens = new HashMap<>();
        }
    }

    public String activeThemeId() {
        return activeThemeId;
    }

    public String activeThemeName() {
        return tokens.getOrDefault("theme.name", activeThemeId);
    }

    public String activeThemeMode() {
        return tokens.getOrDefault("theme.mode", "light");
    }

    public List<ThemeOptionModel> availableThemes() {
        return List.of(new ThemeOptionModel(
                DEFAULT_THEME_ID,
                tokens.getOrDefault("theme.name", "Turkuaz"),
                tokens.getOrDefault("theme.mode", "light")
        ));
    }

    public boolean activateTheme(String themeId) {
        String safeThemeId = normalizeThemeId(Optional.ofNullable(themeId)
                .filter(id -> !id.isBlank())
                .orElse(DEFAULT_THEME_ID));
        Map<String, String> loadedTokens = loadTokens(safeThemeId);
        if (loadedTokens.isEmpty()) {
            return false;
        }
        activeThemeId = safeThemeId;
        tokens = loadedTokens;
        return true;
    }

    public Color color(String key) {
        String value = tokens.get("colors." + key);
        if (value == null || value.isBlank()) {
            Color fallback = UIManager.getColor("Panel.background");
            return fallback == null ? new JPanel().getBackground() : fallback;
        }
        try {
            return Color.decode(value);
        } catch (NumberFormatException exception) {
            Color fallback = UIManager.getColor("Panel.background");
            return fallback == null ? new JPanel().getBackground() : fallback;
        }
    }

    public Font font(String key) {
        String family = tokens.getOrDefault("fonts." + key + ".family", tokens.get("fonts.default.family"));
        String sizeText = tokens.getOrDefault("fonts." + key + ".size", tokens.get("fonts.default.size"));
        int size = parseInt(sizeText, 13);
        return new Font(family == null ? Font.SANS_SERIF : family, Font.PLAIN, size);
    }

    public int spacing(String key) {
        return parseInt(tokens.get("spacing." + key), 8);
    }

    public int radius(String key) {
        return parseInt(tokens.get("radius." + key), 0);
    }

    public void applyPanelStyle(JComponent component) {
        component.setBackground(color("panel.background"));
        component.setForeground(color("text.primary"));
        component.setBorder(BorderFactory.createEmptyBorder(spacing("md"), spacing("md"), spacing("md"), spacing("md")));
    }

    public void applyButtonStyle(JComponent component, boolean primary) {
        if (primary) {
            component.setBackground(color("accent.primary"));
            component.setForeground(Color.WHITE);
        } else {
            component.setBackground(color("panel.background"));
            component.setForeground(color("text.primary"));
        }
        component.setFont(font("default"));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? color("accent.primary") : color("panel.border")),
                BorderFactory.createEmptyBorder(spacing("sm"), spacing("lg"), spacing("sm"), spacing("lg"))
        ));
    }

    public Insets defaultInsets() {
        int md = spacing("md");
        return new Insets(md, md, md, md);
    }

    private Map<String, String> loadTokens(String themeId) {
        String safeThemeId = normalizeThemeId(Optional.ofNullable(themeId)
                .filter(id -> !id.isBlank())
                .orElse(DEFAULT_THEME_ID));
        String resourcePath = THEME_RESOURCE_DIR + safeThemeId + THEME_EXTENSION;
        try (InputStream inputStream = ThemeService.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return new HashMap<>();
            }
            return yamlTool.readFlattened(inputStream);
        } catch (Exception exception) {
            return new HashMap<>();
        }
    }

    private String normalizeThemeId(String themeId) {
        if (themeId == null || themeId.isBlank()) {
            return DEFAULT_THEME_ID;
        }
        if ("bandizip-classic".equalsIgnoreCase(themeId.trim())) {
            return DEFAULT_THEME_ID;
        }
        return themeId.trim();
    }

    private int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }
}
