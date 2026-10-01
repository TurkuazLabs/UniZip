/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/UpdateConfig.java
# 📌 Amac: UniZip otomatik guncelleme ayarlarini ve kanal bazli manifest cozumlemesini temsil etmek
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Exact Community manifest adresini korur ve alternatif edition kanallari icin public manifest URL template contract'i saglar
# Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.models;

import java.util.Locale;

public record UpdateConfig(
        boolean enabled,
        boolean checkOnStartup,
        boolean autoDownload,
        String channel,
        String manifestUrl,
        String manifestUrlTemplate
) {
    public static final String DEFAULT_CHANNEL = "stable";
    public static final String MANIFEST_CHANNEL_TOKEN = "{channel}";
    public static final String DEFAULT_MANIFEST_URL =
            "https://unizip.turkuaz.com/releases/stable/update.yml";
    public static final String DEFAULT_MANIFEST_URL_TEMPLATE =
            "https://unizip.turkuaz.com/releases/{channel}/update.yml";

    public UpdateConfig {
        channel = normalizeChannel(channel);
        manifestUrl = safeUrl(manifestUrl, DEFAULT_MANIFEST_URL);
        manifestUrlTemplate = safeUrl(
                manifestUrlTemplate,
                DEFAULT_MANIFEST_URL_TEMPLATE);
    }

    public UpdateConfig(
            boolean enabled,
            boolean checkOnStartup,
            boolean autoDownload,
            String channel,
            String manifestUrl
    ) {
        this(
                enabled,
                checkOnStartup,
                autoDownload,
                channel,
                manifestUrl,
                DEFAULT_MANIFEST_URL_TEMPLATE);
    }

    public UpdateSource sourceFor(String requestedChannel) {
        String resolvedChannel = normalizeChannel(requestedChannel);
        return new UpdateSource(
                resolvedChannel,
                manifestUrlFor(resolvedChannel));
    }

    public String manifestUrlFor(String requestedChannel) {
        String resolvedChannel = normalizeChannel(requestedChannel);
        if (resolvedChannel.equals(channel)) {
            return manifestUrl;
        }
        if (!manifestUrlTemplate.contains(MANIFEST_CHANNEL_TOKEN)) {
            throw new IllegalStateException(
                    "Alternatif guncelleme kanali icin manifest template token bulunamadi");
        }
        return manifestUrlTemplate.replace(
                MANIFEST_CHANNEL_TOKEN,
                resolvedChannel);
    }

    private static String normalizeChannel(String value) {
        return value == null || value.isBlank()
                ? DEFAULT_CHANNEL
                : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String safeUrl(String value, String fallback) {
        return value == null || value.isBlank()
                ? fallback
                : value.trim();
    }
}
