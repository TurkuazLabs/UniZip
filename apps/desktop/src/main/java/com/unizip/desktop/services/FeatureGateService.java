/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/FeatureGateService.java
# 📌 Amac: UniZip ozelliklerine Community ve Pro lisans erisimi uygulamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Ozelliklerin minimum edition bilgisini merkezi olarak dogrular

Bagimli Oldugu Katman: Service | Repo/Model
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.AppFeature;
import com.unizip.desktop.models.LicenseEdition;

public final class FeatureGateService {
    private final LicenseService licenseService;

    public FeatureGateService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    public boolean isAllowed(AppFeature feature) {
        if (feature == null || feature.minimumEdition() == LicenseEdition.COMMUNITY) {
            return true;
        }
        return licenseService.isProUsable();
    }

    public void require(AppFeature feature) {
        if (!isAllowed(feature)) {
            throw new IllegalStateException("Bu ozellik UniZip Pro lisansi gerektirir");
        }
    }
}
