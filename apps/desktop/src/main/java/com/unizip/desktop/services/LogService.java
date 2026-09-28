/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/LogService.java
# 📌 Amac: Uygulama loglarini merkezi olarak yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: log kaydi, okuma ve temizleme servisi

Bagimli Oldugu Katman: Service | Repo
*/
package com.unizip.desktop.services;

import com.unizip.desktop.repositories.HistoryRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class LogService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final HistoryRepository historyRepository;

    public LogService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void info(String message) {
        write("INFO", message);
    }

    public void error(String message) {
        write("ERROR", message);
    }

    public List<String> readAll() {
        return historyRepository.readAll();
    }

    public void clear() {
        historyRepository.clear();
    }

    private void write(String level, String message) {
        String line = FORMATTER.format(LocalDateTime.now()) + " [" + level + "] " + message;
        historyRepository.append(line);
    }
}
