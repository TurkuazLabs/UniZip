/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/services/RecentArchiveService.java
# 📌 Amac: Son acilan arsivler is kurallarini yonetmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: RecentArchiveRepository uzerinden arsiv listesini tekillestirir, temizler ve gorunur hale getirir

Bagimli Oldugu Katman: Service | Repo/Model
*/
package com.unizip.desktop.services;

import com.unizip.desktop.repositories.RecentArchiveRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class RecentArchiveService {
    private final RecentArchiveRepository recentArchiveRepository;

    public RecentArchiveService(RecentArchiveRepository recentArchiveRepository) {
        this.recentArchiveRepository = recentArchiveRepository;
    }

    public List<Path> recentArchives() {
        return recentArchiveRepository.readRecentArchives();
    }

    public List<Path> existingRecentArchives() {
        return recentArchives().stream().filter(Files::exists).toList();
    }

    public void rememberArchive(Path archivePath) {
        try {
            recentArchiveRepository.addRecentArchive(archivePath);
        } catch (Exception ignored) {
            // Recent list failure must not block archive operations.
        }
    }

    public void clearRecentArchives() throws Exception {
        recentArchiveRepository.clearRecentArchives();
    }

    public void pruneMissingArchives() {
        try {
            recentArchiveRepository.pruneMissingArchives();
        } catch (Exception ignored) {
            // Cleanup failure is non-critical.
        }
    }
}
