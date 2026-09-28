/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/ArchiveCommandGuard.java
# 📌 Amac: Desteklenmeyen arsiv islemlerini service seviyesinde engellemek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: UI disindan gelen komutlarda capability dogrulamasi yapar

Bagimli Oldugu Katman: Service
*/
package com.unizip.desktop.services;

import com.unizip.desktop.models.ArchiveCapabilities;

public final class ArchiveCommandGuard {
    public void requireList(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.list()) {
            throw unsupported("list");
        }
    }

    public void requireExtract(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.extract()) {
            throw unsupported("extract");
        }
    }

    public void requireTest(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.test()) {
            throw unsupported("test");
        }
    }

    public void requireAdd(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.add()) {
            throw unsupported("add");
        }
    }

    public void requireDelete(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.delete()) {
            throw unsupported("delete");
        }
    }

    public void requireRename(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.rename()) {
            throw unsupported("rename");
        }
    }

    public void requireEditSaveBack(ArchiveCapabilities capabilities) {
        if (capabilities == null || !capabilities.editSaveBack()) {
            throw unsupported("edit save-back");
        }
    }

    private UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException("Bu arsiv formati bu islemi desteklemiyor: " + operation);
    }
}
