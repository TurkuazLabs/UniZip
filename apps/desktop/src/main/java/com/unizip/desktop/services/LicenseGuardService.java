/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/LicenseGuardService.java
# 📌 Amac: Uygulama acilisinda lisansi online veya offline tolerans ile dogrulamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Token varsa online validate yapar, baglanti yoksa offline grace durumunu uygular

Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.LicenseState;
import com.unizip.desktop.tools.LicenseApiConnectionException;

public final class LicenseGuardService {
    private final LicenseService licenseService;

    public LicenseGuardService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public LicenseState refreshAtStartup() {
        LicenseState state = licenseService.currentState();
        if (!state.hasToken()) {
            return state;
        }
        try {
            return licenseService.validateOnline();
        } catch (LicenseApiConnectionException exception) {
            return licenseService.applyOfflineState(exception.getMessage());
        } catch (Exception exception) {
            return licenseService.currentState();
        }
    }
}
