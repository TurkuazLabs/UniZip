/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/models/UpdateConfigTest.java
# 📌 Amac: UpdateConfig kanal ve manifest template cozumleme davranisini regression olarak dogrulamak
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Configured stable kaynak, alternatif kanal template cozumleme ve gecersiz template fail-closed senaryolarini test eder
# Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class UpdateConfigTest {
    private static final String CHANNEL_PRIORITY = "priority";
    private static final String STABLE_URL = "https://updates.example.invalid/stable/update.yml";
    private static final String TEMPLATE_URL = "https://updates.example.invalid/{channel}/update.yml";
    private static final String PRIORITY_URL = "https://updates.example.invalid/priority/update.yml";
    private static final String INVALID_TEMPLATE_URL = "https://updates.example.invalid/update.yml";

    @Test
    void configuredChannelUsesExactManifestUrl() {
        UpdateConfig config = new UpdateConfig(
                true,
                true,
                false,
                UpdateConfig.DEFAULT_CHANNEL,
                STABLE_URL,
                TEMPLATE_URL);

        UpdateSource source = config.sourceFor(UpdateConfig.DEFAULT_CHANNEL);

        assertEquals(UpdateConfig.DEFAULT_CHANNEL, source.channel());
        assertEquals(STABLE_URL, source.manifestUrl());
    }

    @Test
    void alternateChannelUsesManifestTemplate() {
        UpdateConfig config = new UpdateConfig(
                true,
                true,
                false,
                UpdateConfig.DEFAULT_CHANNEL,
                STABLE_URL,
                TEMPLATE_URL);

        UpdateSource source = config.sourceFor(CHANNEL_PRIORITY);

        assertEquals(CHANNEL_PRIORITY, source.channel());
        assertEquals(PRIORITY_URL, source.manifestUrl());
    }

    @Test
    void alternateChannelWithoutTemplateTokenIsRejected() {
        UpdateConfig config = new UpdateConfig(
                true,
                true,
                false,
                UpdateConfig.DEFAULT_CHANNEL,
                STABLE_URL,
                INVALID_TEMPLATE_URL);

        assertThrows(
                IllegalStateException.class,
                () -> config.sourceFor(CHANNEL_PRIORITY));
    }
}
