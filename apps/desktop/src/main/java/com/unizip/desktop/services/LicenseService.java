/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/LicenseService.java
# 📌 Amac: UniZip lisans girisi, dogrulama ve cikis is kurallarini yonetmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: OpenCart API token, cihaz aktivasyonu, offline grace ve feature gate lisans durumu servisidir

Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import com.unizip.desktop.config.AppVersion;
import com.unizip.desktop.models.LicenseApiResponse;
import com.unizip.desktop.models.LicenseEdition;
import com.unizip.desktop.models.LicenseState;
import com.unizip.desktop.models.LicenseStatus;
import com.unizip.desktop.repositories.LicenseRepository;
import com.unizip.desktop.tools.DeviceFingerprintTool;
import com.unizip.desktop.tools.LicenseApiTool;

import java.time.Instant;

public final class LicenseService {
    private final LicenseRepository licenseRepository;
    private final LicenseApiTool licenseApiTool;
    private final DeviceFingerprintTool deviceFingerprintTool;
    private LicenseState currentState;

    public LicenseService(LicenseRepository licenseRepository, LicenseApiTool licenseApiTool, DeviceFingerprintTool deviceFingerprintTool) {
        this.licenseRepository = licenseRepository;
        this.licenseApiTool = licenseApiTool;
        this.deviceFingerprintTool = deviceFingerprintTool;
        this.currentState = licenseRepository.load(deviceFingerprintTool.deviceId(), deviceFingerprintTool.deviceName());
    }

    public LicenseState currentState() {
        if (currentState == null) {
            currentState = licenseRepository.load(deviceFingerprintTool.deviceId(), deviceFingerprintTool.deviceName());
        }
        return currentState;
    }

    public LicenseState saveApiBaseUrl(String apiBaseUrl) throws Exception {
        currentState = currentState().withApiBaseUrl(apiBaseUrl);
        licenseRepository.save(currentState);
        return currentState;
    }

    public LicenseState login(String apiBaseUrl, String email, char[] password) throws Exception {
        validateLoginInput(apiBaseUrl, email, password);
        String deviceId = deviceFingerprintTool.deviceId();
        String deviceName = deviceFingerprintTool.deviceName();
        LicenseApiResponse response = licenseApiTool.login(apiBaseUrl, email.trim(), new String(password), deviceId, deviceName, AppVersion.CURRENT);
        if (!response.success()) {
            throw new IllegalStateException(response.message().isBlank() ? "Lisans girisi basarisiz" : response.message());
        }
        currentState = new LicenseState(
                apiBaseUrl,
                response.customerEmail().isBlank() ? email.trim() : response.customerEmail(),
                response.accessToken(),
                response.refreshToken(),
                deviceId,
                deviceName,
                response.edition(),
                response.status(),
                Instant.now(),
                response.expiresAt(),
                response.offlineGraceDays(),
                response.message()
        );
        licenseRepository.save(currentState);
        return currentState;
    }

    public LicenseState validateOnline() throws Exception {
        LicenseState state = currentState();
        if (!state.hasToken()) {
            throw new IllegalStateException("Kayitli lisans token bulunamadi");
        }
        LicenseApiResponse response = licenseApiTool.validate(state.apiBaseUrl(), state.accessToken(), state.deviceId(), AppVersion.CURRENT);
        if (!response.success()) {
            currentState = new LicenseState(
                    state.apiBaseUrl(),
                    state.customerEmail(),
                    "",
                    "",
                    state.deviceId(),
                    state.deviceName(),
                    LicenseEdition.COMMUNITY,
                    LicenseStatus.INVALID,
                    Instant.now(),
                    null,
                    state.offlineGraceDays(),
                    response.message()
            );
            licenseRepository.save(currentState);
            throw new IllegalStateException(response.message().isBlank() ? "Lisans dogrulanamadi" : response.message());
        }
        currentState = new LicenseState(
                state.apiBaseUrl(),
                response.customerEmail().isBlank() ? state.customerEmail() : response.customerEmail(),
                state.accessToken(),
                response.refreshToken().isBlank() ? state.refreshToken() : response.refreshToken(),
                state.deviceId(),
                state.deviceName(),
                response.edition(),
                response.status(),
                Instant.now(),
                response.expiresAt(),
                response.offlineGraceDays(),
                response.message()
        );
        licenseRepository.save(currentState);
        return currentState;
    }

    public LicenseState logout() throws Exception {
        LicenseState state = currentState();
        if (state.hasToken()) {
            try {
                licenseApiTool.logout(state.apiBaseUrl(), state.accessToken(), state.deviceId());
            } catch (Exception ignored) {
                // Offline cikista yerel token yine temizlenir.
            }
        }
        licenseRepository.clearButKeepEndpoint(state.apiBaseUrl(), deviceFingerprintTool.deviceId(), deviceFingerprintTool.deviceName());
        currentState = licenseRepository.load(deviceFingerprintTool.deviceId(), deviceFingerprintTool.deviceName());
        return currentState;
    }

    public LicenseState applyOfflineState(String message) {
        LicenseState state = currentState();
        LicenseStatus status = state.isOfflineGraceValid(Instant.now())
                ? LicenseStatus.OFFLINE
                : LicenseStatus.EXPIRED;
        currentState = state.withRuntimeStatus(status, message == null ? "Offline" : message);
        try {
            licenseRepository.save(currentState);
        } catch (Exception ignored) {
            // Runtime durumu bellekte korunur.
        }
        return currentState;
    }

    public boolean isProActive() {
        return currentState().isProActive();
    }

    public boolean isProUsable() {
        LicenseState state = currentState();
        return state.edition() == LicenseEdition.PRO
                && (state.status() == LicenseStatus.ACTIVE || state.status() == LicenseStatus.OFFLINE);
    }

    public String statusText() {
        LicenseState state = currentState();
        return state.edition().displayName() + " / " + state.status().name();
    }

    private void validateLoginInput(String apiBaseUrl, String email, char[] password) {
        if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
            throw new IllegalArgumentException("Lisans API adresi bos olamaz");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Gecerli email girilmedi");
        }
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Sifre bos olamaz");
        }
    }
}
