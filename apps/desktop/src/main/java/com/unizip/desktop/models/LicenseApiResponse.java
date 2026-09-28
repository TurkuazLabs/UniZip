/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/LicenseApiResponse.java
# 📌 Amac: UniZip lisans API cevaplarini tasimak
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: OpenCart lisans API login/validate/logout sonucunu desktop service katmanina aktarir

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.time.Instant;

public final class LicenseApiResponse {
    private final boolean success;
    private final String code;
    private final String message;
    private final String accessToken;
    private final String refreshToken;
    private final String customerEmail;
    private final LicenseEdition edition;
    private final LicenseStatus status;
    private final Instant expiresAt;
    private final int offlineGraceDays;

    public LicenseApiResponse(
            boolean success,
            String code,
            String message,
            String accessToken,
            String refreshToken,
            String customerEmail,
            LicenseEdition edition,
            LicenseStatus status,
            Instant expiresAt,
            int offlineGraceDays
    ) {
        this.success = success;
        this.code = code == null ? "" : code;
        this.message = message == null ? "" : message;
        this.accessToken = accessToken == null ? "" : accessToken;
        this.refreshToken = refreshToken == null ? "" : refreshToken;
        this.customerEmail = customerEmail == null ? "" : customerEmail;
        this.edition = edition == null ? LicenseEdition.COMMUNITY : edition;
        this.status = status == null ? LicenseStatus.INVALID : status;
        this.expiresAt = expiresAt;
        this.offlineGraceDays = offlineGraceDays <= 0 ? 7 : offlineGraceDays;
    }

    public boolean success() { return success; }
    public String code() { return code; }
    public String message() { return message; }
    public String accessToken() { return accessToken; }
    public String refreshToken() { return refreshToken; }
    public String customerEmail() { return customerEmail; }
    public LicenseEdition edition() { return edition; }
    public LicenseStatus status() { return status; }
    public Instant expiresAt() { return expiresAt; }
    public int offlineGraceDays() { return offlineGraceDays; }
}
