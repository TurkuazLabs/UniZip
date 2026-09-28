/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/repositories/RecentArchiveRepository.java
# 📌 Amac: Son acilan ZIP dosyalarini userdata/recent-archives.yml icinde saklamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: recent archive listesini okur, yazar, tekillestirir ve limitler

Bagimli Oldugu Katman: Repo/Model | Config
*/
package com.unizip.desktop.repositories;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RecentArchiveRepository {
    private static final int MAX_RECENT_ARCHIVES = 10;
    private final Path recentPath;

    public RecentArchiveRepository() {
        this.recentPath = Path.of("userdata", "recent-archives.yml");
    }

    public Path recentPath() {
        return recentPath;
    }

    public List<Path> readRecentArchives() {
        if (!Files.exists(recentPath)) {
            return new ArrayList<>();
        }
        List<Path> result = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(recentPath, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (!trimmed.startsWith("- ")) {
                    continue;
                }
                String value = cleanValue(trimmed.substring(2).trim());
                if (!value.isBlank()) {
                    result.add(Path.of(value));
                }
            }
        } catch (IOException exception) {
            return new ArrayList<>();
        }
        return result;
    }

    public void addRecentArchive(Path archivePath) throws IOException {
        if (archivePath == null) {
            return;
        }
        Path absolutePath = archivePath.toAbsolutePath().normalize();
        Set<String> uniquePaths = new LinkedHashSet<>();
        uniquePaths.add(absolutePath.toString());
        for (Path recentArchive : readRecentArchives()) {
            if (recentArchive != null) {
                uniquePaths.add(recentArchive.toAbsolutePath().normalize().toString());
            }
        }
        writeRecentArchives(uniquePaths.stream().limit(MAX_RECENT_ARCHIVES).map(Path::of).toList());
    }

    public void clearRecentArchives() throws IOException {
        writeRecentArchives(List.of());
    }

    public void pruneMissingArchives() throws IOException {
        List<Path> existing = readRecentArchives().stream()
                .filter(Files::exists)
                .limit(MAX_RECENT_ARCHIVES)
                .toList();
        writeRecentArchives(existing);
    }

    private void writeRecentArchives(List<Path> archives) throws IOException {
        Files.createDirectories(recentPath.getParent());
        StringBuilder builder = new StringBuilder();
        builder.append("# 📄 Dosya Yolu: userdata/recent-archives.yml").append(System.lineSeparator());
        builder.append("# 📌 Amac: UniZip son acilan arsiv listesini saklamak").append(System.lineSeparator());
        builder.append("# 📌 Modul - FileType").append(System.lineSeparator());
        builder.append("# Version: 0.1.57").append(System.lineSeparator());
        builder.append("# Aciklama: File menusu recent archives listesi").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("# Bagimli Oldugu Katman: Config").append(System.lineSeparator());
        builder.append(System.lineSeparator());
        builder.append("recent_archives:").append(System.lineSeparator());
        for (Path archive : archives) {
            builder.append("  - \"").append(escapeYaml(archive.toAbsolutePath().normalize().toString())).append("\"").append(System.lineSeparator());
        }
        Files.writeString(recentPath, builder.toString(), StandardCharsets.UTF_8);
    }

    private String cleanValue(String value) {
        String cleaned = value.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        return unescapeYaml(cleaned);
    }

    private String escapeYaml(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeYaml(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaping = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (escaping) {
                builder.append(current);
                escaping = false;
                continue;
            }
            if (current == '\\') {
                escaping = true;
                continue;
            }
            builder.append(current);
        }
        if (escaping) {
            builder.append('\\');
        }
        return builder.toString();
    }
}
