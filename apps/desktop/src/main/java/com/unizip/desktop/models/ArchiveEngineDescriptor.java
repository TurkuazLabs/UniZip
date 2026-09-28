/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveEngineDescriptor.java
# 📌 Amac: Bir arsiv motorunun format, uzanti ve yetenek bilgisini tasimak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: GitHub commit veya harici provider ile gelen engine destek bilgisini standartlastirir

Bagimli Oldugu Katman: Repo/Model | Tool
*/
package com.unizip.desktop.models;

import java.util.List;

public record ArchiveEngineDescriptor(
        String engineId,
        String displayName,
        ArchiveFormat format,
        List<String> extensions,
        List<String> signatures,
        ArchiveCapabilities capabilities
) {
    public ArchiveEngineDescriptor {
        extensions = extensions == null ? List.of() : List.copyOf(extensions);
        signatures = signatures == null ? List.of() : List.copyOf(signatures);
        capabilities = capabilities == null ? ArchiveCapabilities.none() : capabilities;
    }
}
