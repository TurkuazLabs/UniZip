/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/UpdateSourcePolicyService.java
# 📌 Amac: UpdateService icin edition-bagimsiz guncelleme kaynagi karar portunu tanimlamak
# 📌 Modul - Java
# Version: 0.2.0
# Aciklama: Community build'in Pro implementasyonuna bagimli olmadan kanal ve manifest kaynagi enjekte etmesini saglar
# Bagimli Oldugu Katman: Service | Repo/Model
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.UpdateConfig;
import com.unizip.desktop.models.UpdateSource;

@FunctionalInterface
public interface UpdateSourcePolicyService {
    UpdateSource resolve(UpdateConfig config);
}
