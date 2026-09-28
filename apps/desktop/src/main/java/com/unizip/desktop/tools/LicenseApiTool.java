/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/LicenseApiTool.java
# 📌 Amac: Turkuaz Entitlement API ile HTTPS uzerinden haberlesmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Login, validate ve logout isteklerini Java HttpClient ile gonderir; baglanti hatalarini ayri siniflandirir

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.LicenseApiResponse;
import com.unizip.desktop.models.LicenseEdition;
import com.unizip.desktop.models.LicenseStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LicenseApiTool {
    private static final String PROJECT_CODE = "UNIZIP";

    private final HttpClient httpClient;

    public LicenseApiTool() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .build();
    }

    public LicenseApiResponse login(
            String apiBaseUrl,
            String email,
            String password,
            String deviceId,
            String deviceName,
            String appVersion
    ) throws Exception {
        String payload = "{"
                + jsonPair("project_code", PROJECT_CODE) + ","
                + jsonPair("email", email) + ","
                + jsonPair("password", password) + ","
                + jsonPair("device_id", deviceId) + ","
                + jsonPair("device_name", deviceName) + ","
                + jsonPair("app_version", appVersion)
                + "}";
        return post(apiBaseUrl, "login", payload, "");
    }

    public LicenseApiResponse validate(String apiBaseUrl, String accessToken, String deviceId, String appVersion) throws Exception {
        String payload = "{"
                + jsonPair("project_code", PROJECT_CODE) + ","
                + jsonPair("token", accessToken) + ","
                + jsonPair("device_id", deviceId) + ","
                + jsonPair("app_version", appVersion)
                + "}";
        return post(apiBaseUrl, "validate", payload, accessToken);
    }

    public LicenseApiResponse logout(String apiBaseUrl, String accessToken, String deviceId) throws Exception {
        String payload = "{"
                + jsonPair("project_code", PROJECT_CODE) + ","
                + jsonPair("token", accessToken) + ","
                + jsonPair("device_id", deviceId)
                + "}";
        return post(apiBaseUrl, "logout", payload, accessToken);
    }

    private LicenseApiResponse post(String apiBaseUrl, String action, String jsonPayload, String accessToken) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(actionUrl(apiBaseUrl, action)))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload));
        if (accessToken != null && !accessToken.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + accessToken);
        }
        HttpResponse<String> response;
        try {
            response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (java.io.IOException exception) {
            throw new LicenseApiConnectionException("Lisans sunucusuna baglanilamadi", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LicenseApiConnectionException("Lisans sunucusu istegi kesildi", exception);
        }
        if (response.statusCode() >= 500) {
            throw new LicenseApiConnectionException("Lisans sunucusu gecici hata verdi: HTTP " + response.statusCode());
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("License API HTTP " + response.statusCode());
        }
        return parseResponse(response.body());
    }

    private String actionUrl(String apiBaseUrl, String action) {
        String base = apiBaseUrl == null || apiBaseUrl.isBlank()
                ? "https://unizip.turkuaz.com/index.php?route=extension/module/turkuaz_entitlement_api"
                : apiBaseUrl.trim();
        if (base.contains("route=extension/module/turkuaz_entitlement_api")) {
            if (base.endsWith("/login") || base.endsWith("/validate") || base.endsWith("/logout")) {
                return base.replaceAll("/(login|validate|logout)$", "/" + action);
            }
            return base + "/" + action;
        }
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base + action;
    }

    private LicenseApiResponse parseResponse(String json) {
        boolean success = readBoolean(json, "success");
        String statusText = readString(json, "license_status");
        if (statusText.isBlank()) {
            statusText = success ? "active" : "invalid";
        }
        return new LicenseApiResponse(
                success,
                readString(json, "code"),
                readString(json, "message"),
                readString(json, "token"),
                readString(json, "refresh_token"),
                readString(json, "customer_email"),
                LicenseEdition.fromText(readString(json, "edition")),
                LicenseStatus.fromText(statusText),
                readInstant(json, "expires_at"),
                readInt(json, "offline_grace_days", 7)
        );
    }

    private String jsonPair(String key, String value) {
        return "\"" + escapeJson(key) + "\":\"" + escapeJson(value) + "\"";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String readString(String json, String key) {
        if (json == null || key == null) {
            return "";
        }
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return "";
        }
        return unescapeJson(matcher.group(1));
    }

    private boolean readBoolean(String json, String key) {
        if (json == null || key == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(true|false|1|0)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return false;
        }
        String value = matcher.group(1).toLowerCase(Locale.ROOT);
        return "true".equals(value) || "1".equals(value);
    }

    private int readInt(String json, String key, int fallback) {
        if (json == null || key == null) {
            return fallback;
        }
        Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (Exception exception) {
            return fallback;
        }
    }

    private Instant readInstant(String json, String key) {
        try {
            String value = readString(json, key);
            return value.isBlank() ? null : Instant.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private String unescapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
