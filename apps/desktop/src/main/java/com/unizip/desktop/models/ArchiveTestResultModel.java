/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveTestResultModel.java
# 📌 Amac: ZIP test sonucu istatistiklerini ve uyarilarini tasimak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: test edilen girdi, dosya, klasor, okunan byte ve risk uyarilarini tutar

Bagimli Oldugu Katman: Repo/Model | Service | Tool
*/
package com.unizip.desktop.models;

import java.util.List;

public record ArchiveTestResultModel(
        int totalEntries,
        int fileEntries,
        int directoryEntries,
        long testedBytes,
        List<String> warnings
) {
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public int warningCount() {
        return warnings == null ? 0 : warnings.size();
    }
}
