/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/UpdateRepository.java
# 📌 Amac: UniZip guncelleme kontrol durumunu userdata altinda saklamak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Son kontrol zamani ve son gorulen surumu update-state.yml dosyasina yazar

Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.repositories;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public final class UpdateRepository {
    private final Path statePath;
    private final Path downloadsDirectory;

    public UpdateRepository() {
        this.statePath = Path.of("userdata", "update-state.yml");
        this.downloadsDirectory = Path.of("userdata", "updates");
    }

    public Path downloadsDirectory() {
        return downloadsDirectory;
    }

    public void saveCheck(Instant checkedAt, String availableVersion) throws Exception {
        Files.createDirectories(statePath.getParent());
        String content = "# 📄 Dosya Yolu: userdata/update-state.yml" + System.lineSeparator()
                + "# 📌 Amac: UniZip guncelleme kontrol durumunu saklamak" + System.lineSeparator()
                + "# 📌 Modul - FileType" + System.lineSeparator()
                + "# Version: 0.1.61" + System.lineSeparator()
                + "# Aciklama: Son kontrol zamani ve yayin sunucusunda gorulen surum" + System.lineSeparator()
                + System.lineSeparator()
                + "# Bagimli Oldugu Katman: Config" + System.lineSeparator()
                + System.lineSeparator()
                + "update:" + System.lineSeparator()
                + "  last_checked_at: \"" + (checkedAt == null ? "" : checkedAt) + "\"" + System.lineSeparator()
                + "  available_version: \"" + escape(availableVersion) + "\"" + System.lineSeparator();
        Files.writeString(statePath, content, StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
