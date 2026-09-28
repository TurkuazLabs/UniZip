/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/UpdateConfigRepository.java
# 📌 Amac: UniZip otomatik guncelleme ayarlarini app.yml dosyasindan okumak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Guncelleme kanalini, manifest adresini ve acilis kontrol tercihini yukler

Bagimli Oldugu Katman: Repo/Model | Config | Tool
*/
package com.unizip.desktop.repositories;

import com.unizip.desktop.models.UpdateConfig;
import com.unizip.desktop.tools.SimpleYamlTool;

import java.io.InputStream;
import java.util.Map;

public final class UpdateConfigRepository {
    private static final String APP_CONFIG_RESOURCE = "/config/app.yml";
    private static final String ENV_MANIFEST_URL = "UNIZIP_UPDATE_MANIFEST_URL";
    private static final String PROPERTY_MANIFEST_URL = "unizip.update.manifestUrl";

    private final SimpleYamlTool yamlTool;

    public UpdateConfigRepository(SimpleYamlTool yamlTool) {
        this.yamlTool = yamlTool;
    }

    public UpdateConfig load() {
        try (InputStream inputStream = getClass().getResourceAsStream(APP_CONFIG_RESOURCE)) {
            Map<String, String> values = inputStream == null ? Map.of() : yamlTool.readFlattened(inputStream);
            String configuredUrl = values.getOrDefault("update.manifest_url", UpdateConfig.DEFAULT_MANIFEST_URL);
            String overrideUrl = System.getProperty(PROPERTY_MANIFEST_URL, System.getenv(ENV_MANIFEST_URL));
            return new UpdateConfig(
                    parseBoolean(values.get("update.enabled"), true),
                    parseBoolean(values.get("update.check_on_startup"), true),
                    parseBoolean(values.get("update.auto_download"), false),
                    values.getOrDefault("update.channel", UpdateConfig.DEFAULT_CHANNEL),
                    overrideUrl == null || overrideUrl.isBlank() ? configuredUrl : overrideUrl
            );
        } catch (Exception exception) {
            return new UpdateConfig(true, true, false, UpdateConfig.DEFAULT_CHANNEL, UpdateConfig.DEFAULT_MANIFEST_URL);
        }
    }

    private boolean parseBoolean(String value, boolean fallback) {
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value.trim());
    }
}
