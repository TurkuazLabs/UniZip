/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/LicenseState.java
# 📌 Amac: UniZip lisans oturum durumunu saklamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: API adresi, token, cihaz, edition ve offline grace durumunu tek modelde toplar

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.time.Instant;

public final class LicenseState {
    public static final String DEFAULT_API_BASE_URL = "https://unizip.turkuaz.com/index.php?route=extension/module/turkuaz_entitlement_api";

    private final String apiBaseUrl;
    private final String customerEmail;
    private final String accessToken;
    private final String refreshToken;
    private final String deviceId;
    private final String deviceName;
    private final LicenseEdition edition;
    private final LicenseStatus status;
    private final Instant lastCheckedAt;
    private final Instant expiresAt;
    private final int offlineGraceDays;
    private final String message;

    public LicenseState(
            String apiBaseUrl,
            String customerEmail,
            String accessToken,
            String refreshToken,
            String deviceId,
            String deviceName,
            LicenseEdition edition,
            LicenseStatus status,
            Instant lastCheckedAt,
            Instant expiresAt,
            int offlineGraceDays,
            String message
    ) {
        this.apiBaseUrl = safe(apiBaseUrl, DEFAULT_API_BASE_URL);
        this.customerEmail = safe(customerEmail, "");
        this.accessToken = safe(accessToken, "");
        this.refreshToken = safe(refreshToken, "");
        this.deviceId = safe(deviceId, "");
        this.deviceName = safe(deviceName, "");
        this.edition = edition == null ? LicenseEdition.COMMUNITY : edition;
        this.status = status == null ? LicenseStatus.OFFLINE : status;
        this.lastCheckedAt = lastCheckedAt;
        this.expiresAt = expiresAt;
        this.offlineGraceDays = offlineGraceDays <= 0 ? 7 : offlineGraceDays;
        this.message = safe(message, "");
    }

    public static LicenseState community(String apiBaseUrl, String deviceId, String deviceName) {
        return new LicenseState(
                apiBaseUrl,
                "",
                "",
                "",
                deviceId,
                deviceName,
                LicenseEdition.COMMUNITY,
                LicenseStatus.OFFLINE,
                null,
                null,
                7,
                "Community"
        );
    }

    public String apiBaseUrl() { return apiBaseUrl; }
    public String customerEmail() { return customerEmail; }
    public String accessToken() { return accessToken; }
    public String refreshToken() { return refreshToken; }
    public String deviceId() { return deviceId; }
    public String deviceName() { return deviceName; }
    public LicenseEdition edition() { return edition; }
    public LicenseStatus status() { return status; }
    public Instant lastCheckedAt() { return lastCheckedAt; }
    public Instant expiresAt() { return expiresAt; }
    public int offlineGraceDays() { return offlineGraceDays; }
    public String message() { return message; }

    public boolean isProActive() {
        return edition == LicenseEdition.PRO && status == LicenseStatus.ACTIVE;
    }

    public boolean hasToken() {
        return !accessToken.isBlank();
    }

    public LicenseState withApiBaseUrl(String newApiBaseUrl) {
        return new LicenseState(newApiBaseUrl, customerEmail, accessToken, refreshToken, deviceId, deviceName, edition, status, lastCheckedAt, expiresAt, offlineGraceDays, message);
    }

    public LicenseState withMessage(String newMessage) {
        return new LicenseState(apiBaseUrl, customerEmail, accessToken, refreshToken, deviceId, deviceName, edition, status, lastCheckedAt, expiresAt, offlineGraceDays, newMessage);
    }

    public LicenseState withRuntimeStatus(LicenseStatus newStatus, String newMessage) {
        return new LicenseState(apiBaseUrl, customerEmail, accessToken, refreshToken, deviceId, deviceName, edition, newStatus, lastCheckedAt, expiresAt, offlineGraceDays, newMessage);
    }

    public boolean isOfflineGraceValid(Instant now) {
        if (!hasToken() || edition != LicenseEdition.PRO || lastCheckedAt == null) {
            return false;
        }
        Instant safeNow = now == null ? Instant.now() : now;
        return !safeNow.isAfter(lastCheckedAt.plusSeconds((long) offlineGraceDays * 24L * 60L * 60L));
    }

    private static String safe(String value, String fallback) {
        return value == null ? fallback : value.trim();
    }
}
