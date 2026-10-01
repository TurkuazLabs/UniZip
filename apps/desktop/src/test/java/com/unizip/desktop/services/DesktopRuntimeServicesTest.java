/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/DesktopRuntimeServicesTest.java
# 📌 Amac: Community default ve custom edition runtime service injection contractini regression olarak dogrulamak
# 📌 Modul - Java
# Version: 0.3.0
# Aciklama: Community update source policy default'unu, custom policy preservation'i ve null fail-fast davranisini test eder

Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class DesktopRuntimeServicesTest {

    @Test
    void communityFactoryUsesCommunityPolicy() {
        DesktopRuntimeServices runtime =
                DesktopRuntimeServices.community();

        assertInstanceOf(
                CommunityUpdateSourcePolicyService.class,
                runtime.updateSourcePolicyService());
    }

    @Test
    void customPolicyIsPreserved() {
        UpdateSourcePolicyService policy =
                config -> config.sourceFor(
                        config.channel());

        DesktopRuntimeServices runtime =
                new DesktopRuntimeServices(policy);

        assertSame(
                policy,
                runtime.updateSourcePolicyService());
    }

    @Test
    void nullPolicyIsRejected() {
        assertThrows(
                NullPointerException.class,
                () -> new DesktopRuntimeServices(
                        null));
    }
}
