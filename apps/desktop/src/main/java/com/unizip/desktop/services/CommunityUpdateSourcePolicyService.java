/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/CommunityUpdateSourcePolicyService.java
# 📌 Amac: Community edition icin varsayilan guncelleme kanal ve manifest kaynagini belirlemek
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Community config kanalini public UpdateSourcePolicyService portu uzerinden UpdateService'e aktarir
# Bagimli Oldugu Katman: Service | Repo/Model
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.UpdateConfig;
import com.unizip.desktop.models.UpdateSource;

public final class CommunityUpdateSourcePolicyService implements UpdateSourcePolicyService {
    @Override
    public UpdateSource resolve(UpdateConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Guncelleme config bulunamadi");
        }
        return config.sourceFor(config.channel());
    }
}
