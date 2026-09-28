/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveSessionState.java
# 📌 Amac: Aci lan arsivin runtime durumuna gore komut uygunlugunu hesaplamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Engine capability, secim ve read-only durumunu tek UI state modelinde birlestirir

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.nio.file.Path;

public record ArchiveSessionState(
        Path archivePath,
        ArchiveFormat format,
        String engineId,
        String engineName,
        ArchiveCapabilities capabilities,
        boolean archiveOpened,
        boolean readOnly,
        boolean hasSelection,
        boolean hasEditableSelection,
        boolean folderSelection
) {
    public static ArchiveSessionState empty() {
        return new ArchiveSessionState(
                null,
                ArchiveFormat.UNKNOWN,
                "unizip.engine.none",
                "No archive engine",
                ArchiveCapabilities.none(),
                false,
                true,
                false,
                false,
                false
        );
    }

    public ArchiveSessionState withSelection(boolean selected, boolean editable, boolean folder) {
        return new ArchiveSessionState(
                archivePath,
                format,
                engineId,
                engineName,
                capabilities,
                archiveOpened,
                readOnly,
                selected,
                editable,
                folder
        );
    }

    public boolean canList() {
        return archiveOpened && capabilities.list();
    }

    public boolean canAdd() {
        return archiveOpened && !readOnly && capabilities.add();
    }

    public boolean canDelete() {
        return archiveOpened && !readOnly && hasSelection && capabilities.delete();
    }

    public boolean canRename() {
        return archiveOpened && !readOnly && hasSelection && capabilities.rename();
    }

    public boolean canEditSaveBack() {
        return archiveOpened && !readOnly && hasEditableSelection && capabilities.editSaveBack();
    }

    public boolean canExtract() {
        return archiveOpened && capabilities.extract();
    }

    public boolean canExtractSelected() {
        return archiveOpened && hasSelection && capabilities.extract();
    }

    public boolean canTest() {
        return archiveOpened && capabilities.test();
    }

    public boolean canCreate() {
        return capabilities.create();
    }
}
