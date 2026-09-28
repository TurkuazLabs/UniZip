/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/HistoryRepository.java
# 📌 Amac: Log ve islem gecmisi kayitlarini yerel dosyada saklamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: basit dosya tabanli history repo

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.repositories;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class HistoryRepository {
    private final Path historyFile;

    public HistoryRepository() {
        this.historyFile = Path.of("userdata", "history.log");
    }

    public void append(String line) {
        try {
            Files.createDirectories(historyFile.getParent());
            Files.writeString(
                    historyFile,
                    line + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // Log writing failure must not stop archive operations.
        }
    }

    public List<String> readAll() {
        try {
            if (!Files.exists(historyFile)) {
                return new ArrayList<>();
            }
            return Files.readAllLines(historyFile, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return new ArrayList<>();
        }
    }

    public void clear() {
        try {
            Files.createDirectories(historyFile.getParent());
            Files.writeString(historyFile, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {
            // Clear failure is non-critical for Community skeleton.
        }
    }
}
