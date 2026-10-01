/*
# 📄 Dosya Yolu: apps/desktop/src/test/java/com/unizip/desktop/services/CommunityUpdateSourcePolicyServiceTest.java
# 📌 Amac: Community update source policy'nin configured kanali korudugunu regression olarak dogrulamak
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Public update source portunun Community varsayilan davranisini test eder
# Bagimli Oldugu Katman: Service | Repo/Model | Config
*/
package com.unizip.desktop.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unizip.desktop.models.UpdateConfig;
import com.unizip.desktop.models.UpdateSource;
import org.junit.jupiter.api.Test;

final class CommunityUpdateSourcePolicyServiceTest {
    private static final String STABLE_URL = "https://updates.example.invalid/stable/update.yml";
    private static final String TEMPLATE_URL = "https://updates.example.invalid/{channel}/update.yml";

    @Test
    void communityUsesConfiguredSource() {
        UpdateConfig config = new UpdateConfig(
                true,
                true,
                false,
                UpdateConfig.DEFAULT_CHANNEL,
                STABLE_URL,
                TEMPLATE_URL);
        CommunityUpdateSourcePolicyService service =
                new CommunityUpdateSourcePolicyService();

        UpdateSource source = service.resolve(config);

        assertEquals(UpdateConfig.DEFAULT_CHANNEL, source.channel());
        assertEquals(STABLE_URL, source.manifestUrl());
    }
}
