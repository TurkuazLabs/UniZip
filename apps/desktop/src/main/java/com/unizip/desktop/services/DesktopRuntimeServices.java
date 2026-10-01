/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/DesktopRuntimeServices.java
# 📌 Amac: UniZip Desktop edition-bagimsiz runtime service extension noktalarini tek public composition modelinde toplamak
# 📌 Modul - Java
# Version: 0.3.0
# Aciklama: Community default update source policy'sini korur ve harici edition dagitimlarinin public service portlarini enjekte etmesini saglar

Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import java.util.Objects;

public record DesktopRuntimeServices(
        UpdateSourcePolicyService updateSourcePolicyService
) {
    public DesktopRuntimeServices {
        updateSourcePolicyService = Objects.requireNonNull(
                updateSourcePolicyService,
                "updateSourcePolicyService");
    }

    public static DesktopRuntimeServices community() {
        return new DesktopRuntimeServices(
                new CommunityUpdateSourcePolicyService());
    }
}
