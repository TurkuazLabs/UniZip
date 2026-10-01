/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/UpdateConfigRepository.java
# 📌 Amac: UniZip otomatik guncelleme ayarlarini app.yml dosyasindan okumak
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Kanal, exact manifest, kanal template ve environment/property override ayarlarini yukler
# Bagimli Oldugu Katman: Repo/Model | Config | Tool
*/
package com.unizip.desktop.repositories;

import com.unizip.desktop.models.UpdateConfig;
import com.unizip.desktop.tools.SimpleYamlTool;

import java.io.InputStream;
import java.util.Map;

public final class UpdateConfigRepository {
    private static final String APP_CONFIG_RESOURCE = "/config/app.yml";

    private static final String KEY_ENABLED = "update.enabled";
    private static final String KEY_CHECK_ON_STARTUP = "update.check_on_startup";
    private static final String KEY_AUTO_DOWNLOAD = "update.auto_download";
    private static final String KEY_CHANNEL = "update.channel";
    private static final String KEY_MANIFEST_URL = "update.manifest_url";
    private static final String KEY_MANIFEST_URL_TEMPLATE =
            "update.manifest_url_template";

    private static final String ENV_MANIFEST_URL =
            "UNIZIP_UPDATE_MANIFEST_URL";
    private static final String ENV_MANIFEST_URL_TEMPLATE =
            "UNIZIP_UPDATE_MANIFEST_URL_TEMPLATE";
    private static final String PROPERTY_MANIFEST_URL =
            "unizip.update.manifestUrl";
    private static final String PROPERTY_MANIFEST_URL_TEMPLATE =
            "unizip.update.manifestUrlTemplate";

    private final SimpleYamlTool yamlTool;

    public UpdateConfigRepository(SimpleYamlTool yamlTool) {
        this.yamlTool = yamlTool;
    }

    public UpdateConfig load() {
        try (InputStream inputStream =
                     getClass().getResourceAsStream(APP_CONFIG_RESOURCE)) {
            Map<String, String> values = inputStream == null
                    ? Map.of()
                    : yamlTool.readFlattened(inputStream);

            String configuredUrl = values.getOrDefault(
                    KEY_MANIFEST_URL,
                    UpdateConfig.DEFAULT_MANIFEST_URL);
            String configuredTemplate = values.getOrDefault(
                    KEY_MANIFEST_URL_TEMPLATE,
                    UpdateConfig.DEFAULT_MANIFEST_URL_TEMPLATE);

            String overrideUrl = readOverride(
                    PROPERTY_MANIFEST_URL,
                    ENV_MANIFEST_URL);
            String overrideTemplate = readOverride(
                    PROPERTY_MANIFEST_URL_TEMPLATE,
                    ENV_MANIFEST_URL_TEMPLATE);

            return new UpdateConfig(
                    parseBoolean(values.get(KEY_ENABLED), true),
                    parseBoolean(values.get(KEY_CHECK_ON_STARTUP), true),
                    parseBoolean(values.get(KEY_AUTO_DOWNLOAD), false),
                    values.getOrDefault(
                            KEY_CHANNEL,
                            UpdateConfig.DEFAULT_CHANNEL),
                    useOverride(overrideUrl, configuredUrl),
                    useOverride(
                            overrideTemplate,
                            configuredTemplate));
        } catch (Exception exception) {
            return new UpdateConfig(
                    true,
                    true,
                    false,
                    UpdateConfig.DEFAULT_CHANNEL,
                    UpdateConfig.DEFAULT_MANIFEST_URL,
                    UpdateConfig.DEFAULT_MANIFEST_URL_TEMPLATE);
        }
    }

    private String readOverride(
            String propertyName,
            String environmentName) {
        return System.getProperty(
                propertyName,
                System.getenv(environmentName));
    }

    private String useOverride(String override, String configured) {
        return override == null || override.isBlank()
                ? configured
                : override.trim();
    }

    private boolean parseBoolean(String value, boolean fallback) {
        return value == null || value.isBlank()
                ? fallback
                : Boolean.parseBoolean(value.trim());
    }
}
