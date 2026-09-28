/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/LicenseRepository.java
# 📌 Amac: UniZip lisans durumunu yerel userdata klasorunde saklamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Sifre saklamadan token, cihaz, edition ve son dogrulama bilgilerini license.yml icinde tutar

Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.repositories;

import com.unizip.desktop.models.LicenseEdition;
import com.unizip.desktop.models.LicenseState;
import com.unizip.desktop.models.LicenseStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class LicenseRepository {
    private final Path licensePath;

    public LicenseRepository() {
        this.licensePath = Path.of("userdata", "license.yml");
    }

    public Path licensePath() {
        return licensePath;
    }

    public LicenseState load(String fallbackDeviceId, String fallbackDeviceName) {
        if (!Files.exists(licensePath)) {
            return LicenseState.community(LicenseState.DEFAULT_API_BASE_URL, fallbackDeviceId, fallbackDeviceName);
        }
        try {
            Map<String, String> values = readValues();
            return new LicenseState(
                    values.getOrDefault("api_base_url", LicenseState.DEFAULT_API_BASE_URL),
                    values.getOrDefault("customer_email", ""),
                    values.getOrDefault("access_token", ""),
                    values.getOrDefault("refresh_token", ""),
                    values.getOrDefault("device_id", fallbackDeviceId),
                    values.getOrDefault("device_name", fallbackDeviceName),
                    LicenseEdition.fromText(values.get("edition")),
                    LicenseStatus.fromText(values.get("status")),
                    parseInstant(values.get("last_checked_at")),
                    parseInstant(values.get("expires_at")),
                    parseInt(values.get("offline_grace_days"), 7),
                    values.getOrDefault("message", "")
            );
        } catch (Exception exception) {
            return LicenseState.community(LicenseState.DEFAULT_API_BASE_URL, fallbackDeviceId, fallbackDeviceName)
                    .withMessage(exception.getMessage());
        }
    }

    public void save(LicenseState state) throws IOException {
        Files.createDirectories(licensePath.getParent());
        String content = "# 📄 Dosya Yolu: userdata/license.yml" + System.lineSeparator()
                + "# 📌 Amac: UniZip lisans oturum durumunu saklamak" + System.lineSeparator()
                + "# 📌 Modul - FileType" + System.lineSeparator()
                + "# Version: 0.1.61" + System.lineSeparator()
                + "# Aciklama: OpenCart lisans API token, cihaz ve edition bilgileri" + System.lineSeparator()
                + System.lineSeparator()
                + "# Bagimli Oldugu Katman: Config" + System.lineSeparator()
                + System.lineSeparator()
                + "license:" + System.lineSeparator()
                + "  api_base_url: \"" + escape(state.apiBaseUrl()) + "\"" + System.lineSeparator()
                + "  customer_email: \"" + escape(state.customerEmail()) + "\"" + System.lineSeparator()
                + "  access_token: \"" + escape(state.accessToken()) + "\"" + System.lineSeparator()
                + "  refresh_token: \"" + escape(state.refreshToken()) + "\"" + System.lineSeparator()
                + "  device_id: \"" + escape(state.deviceId()) + "\"" + System.lineSeparator()
                + "  device_name: \"" + escape(state.deviceName()) + "\"" + System.lineSeparator()
                + "  edition: \"" + state.edition().name().toLowerCase(java.util.Locale.ROOT) + "\"" + System.lineSeparator()
                + "  status: \"" + state.status().name().toLowerCase(java.util.Locale.ROOT) + "\"" + System.lineSeparator()
                + "  last_checked_at: \"" + formatInstant(state.lastCheckedAt()) + "\"" + System.lineSeparator()
                + "  expires_at: \"" + formatInstant(state.expiresAt()) + "\"" + System.lineSeparator()
                + "  offline_grace_days: " + state.offlineGraceDays() + System.lineSeparator()
                + "  message: \"" + escape(state.message()) + "\"" + System.lineSeparator();
        Files.writeString(licensePath, content, StandardCharsets.UTF_8);
    }

    public void clearButKeepEndpoint(String apiBaseUrl, String deviceId, String deviceName) throws IOException {
        save(LicenseState.community(apiBaseUrl, deviceId, deviceName));
    }

    private Map<String, String> readValues() throws IOException {
        Map<String, String> values = new HashMap<>();
        for (String line : Files.readAllLines(licensePath, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.equals("license:")) {
                continue;
            }
            int separator = trimmed.indexOf(':');
            if (separator <= 0) {
                continue;
            }
            String key = trimmed.substring(0, separator).trim();
            String value = trimmed.substring(separator + 1).trim();
            values.put(key, unquote(value));
        }
        return values;
    }

    private String unquote(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Instant parseInstant(String value) {
        try {
            if (value == null || value.isBlank()) {
                return null;
            }
            return Instant.parse(value.trim());
        } catch (Exception exception) {
            return null;
        }
    }

    private String formatInstant(Instant instant) {
        return instant == null ? "" : instant.toString();
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null || value.isBlank() ? fallback : Integer.parseInt(value.trim());
        } catch (Exception exception) {
            return fallback;
        }
    }
}
