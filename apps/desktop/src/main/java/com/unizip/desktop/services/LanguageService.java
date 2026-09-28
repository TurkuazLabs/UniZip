/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/LanguageService.java
# 📌 Amac: Dil dosyasindan metin tokenlarini yuklemek ve aktif dili yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: settings repository uzerinden aktif dili okur, ayarlar ekranina dil listesi sunar

Bagimli Oldugu Katman: Service | Repo/Model | Language | Config
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.LanguageOptionModel;
import com.unizip.desktop.repositories.SettingsRepository;
import com.unizip.desktop.tools.SimpleYamlTool;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class LanguageService {
    private static final String DEFAULT_LANGUAGE_ID = "tr";
    private static final String LANGUAGE_RESOURCE_DIR = "/language/";
    private static final String LANGUAGE_EXTENSION = ".yml";

    private final SimpleYamlTool yamlTool;
    private Map<String, String> messages;
    private String activeLanguageId;

    public LanguageService(SimpleYamlTool yamlTool, SettingsRepository settingsRepository) {
        this.yamlTool = yamlTool;
        this.activeLanguageId = settingsRepository.readActiveLanguageId().orElse(DEFAULT_LANGUAGE_ID);
        this.messages = loadMessages(activeLanguageId);
        if (messages.isEmpty()) {
            this.activeLanguageId = DEFAULT_LANGUAGE_ID;
            this.messages = loadMessages(DEFAULT_LANGUAGE_ID);
        }
        if (messages.isEmpty()) {
            this.messages = new HashMap<>();
        }
    }

    public String text(String key) {
        return messages.getOrDefault(key, key);
    }

    public String activeLanguageId() {
        return activeLanguageId;
    }

    public List<LanguageOptionModel> availableLanguages() {
        return List.of(
                new LanguageOptionModel("tr", "Türkçe"),
                new LanguageOptionModel("en", "English")
        );
    }

    public boolean activateLanguage(String languageId) {
        String safeLanguageId = Optional.ofNullable(languageId)
                .filter(id -> !id.isBlank())
                .orElse(DEFAULT_LANGUAGE_ID);
        Map<String, String> loadedMessages = loadMessages(safeLanguageId);
        if (loadedMessages.isEmpty()) {
            return false;
        }
        activeLanguageId = safeLanguageId;
        messages = loadedMessages;
        return true;
    }

    private Map<String, String> loadMessages(String languageId) {
        String safeLanguageId = Optional.ofNullable(languageId)
                .filter(id -> !id.isBlank())
                .orElse(DEFAULT_LANGUAGE_ID);
        String resourcePath = LANGUAGE_RESOURCE_DIR + safeLanguageId + LANGUAGE_EXTENSION;
        try (InputStream inputStream = LanguageService.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return new HashMap<>();
            }
            return yamlTool.readFlattened(inputStream);
        } catch (Exception exception) {
            return new HashMap<>();
        }
    }
}
