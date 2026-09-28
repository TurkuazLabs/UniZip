/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/UnsupportedArchiveEngine.java
# 📌 Amac: Desteklenmeyen arsiv formatlari icin guvenli fallback engine saglamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Yanlis buton aktifligini engeller ve net desteklenmiyor hatasi verir

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveCapabilities;
import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveFormat;
import com.unizip.desktop.models.ArchiveTestResultModel;
import com.unizip.desktop.models.ExtractOverwriteMode;
import java.nio.file.Path;
import java.util.List;

public final class UnsupportedArchiveEngine implements ArchiveEngine {
    private final ArchiveFormat format;

    public UnsupportedArchiveEngine(ArchiveFormat format) {
        this.format = format == null ? ArchiveFormat.UNKNOWN : format;
    }

    @Override
    public String engineId() {
        return "unizip.engine.unsupported." + format.displayName();
    }

    @Override
    public String displayName() {
        return "Unsupported " + format.displayName() + " Engine";
    }

    @Override
    public ArchiveFormat format() {
        return format;
    }

    @Override
    public ArchiveCapabilities capabilities() {
        return ArchiveCapabilities.none();
    }

    @Override
    public boolean canOpen(Path archivePath) {
        return false;
    }

    @Override
    public List<ArchiveEntryModel> list(Path archivePath) throws Exception {
        throw unsupported();
    }

    @Override
    public int extract(Path archivePath, Path outputDirectory, List<String> selectedEntries, ExtractOverwriteMode overwriteMode) throws Exception {
        throw unsupported();
    }

    @Override
    public ArchiveTestResultModel test(Path archivePath) throws Exception {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Bu arsiv formati henuz desteklenmiyor: " + format.displayName());
    }
}
