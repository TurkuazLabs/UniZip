/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ArchiveEngine.java
# 📌 Amac: Tum arsiv motorlari icin ortak islem sozlesmesi saglamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ZIP, 7Z, RAR, TAR gibi motorlari tek API altinda toplar

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import com.unizip.desktop.models.ArchiveCapabilities;
import com.unizip.desktop.models.ArchiveEntryModel;
import com.unizip.desktop.models.ArchiveEngineDescriptor;
import com.unizip.desktop.models.ArchiveFormat;
import com.unizip.desktop.models.ArchiveTestResultModel;
import com.unizip.desktop.models.ExtractOverwriteMode;
import java.nio.file.Path;
import java.util.List;

public interface ArchiveEngine {
    String engineId();

    String displayName();

    ArchiveFormat format();

    ArchiveCapabilities capabilities();

    boolean canOpen(Path archivePath);

    List<ArchiveEntryModel> list(Path archivePath) throws Exception;

    int extract(Path archivePath, Path outputDirectory, List<String> selectedEntries, ExtractOverwriteMode overwriteMode) throws Exception;

    ArchiveTestResultModel test(Path archivePath) throws Exception;

    default ArchiveEngineDescriptor descriptor() {
        return new ArchiveEngineDescriptor(
                engineId(),
                displayName(),
                format(),
                List.of(format().displayName()),
                List.of(),
                capabilities()
        );
    }

    default List<String> findAddConflicts(Path archivePath, List<Path> inputPaths) throws Exception {
        throw unsupported("add conflict check");
    }

    default int add(Path archivePath, List<Path> inputPaths, boolean overwriteExisting) throws Exception {
        throw unsupported("add");
    }

    default int delete(Path archivePath, List<String> entryNames) throws Exception {
        throw unsupported("delete");
    }

    default int rename(Path archivePath, String oldEntryName, String newEntryName) throws Exception {
        throw unsupported("rename");
    }

    default void exportSingleEntryToFile(Path archivePath, String entryName, Path outputFile) throws Exception {
        throw unsupported("export single entry");
    }

    default int exportEntriesToDirectory(Path archivePath, List<String> entryNames, Path outputDirectory) throws Exception {
        throw unsupported("export entries");
    }

    default int replace(Path archivePath, String entryName, Path sourceFile) throws Exception {
        throw unsupported("replace");
    }

    default byte[] readEntryBytes(Path archivePath, String entryName, int maxBytes) throws Exception {
        throw unsupported("read entry bytes");
    }

    private UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException(displayName() + " does not support " + operation + ".");
    }
}
