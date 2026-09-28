/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/FileSystemTool.java
# 📌 Amac: Dosya sistemi islemlerini Tool katmaninda toplamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: klasor olusturma ve varlik kontrol yardimcisi

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileSystemTool {
    public boolean exists(Path path) {
        return path != null && Files.exists(path);
    }

    public void createDirectories(Path directory) throws IOException {
        if (directory != null) {
            Files.createDirectories(directory);
        }
    }

    public void createParentDirectories(Path file) throws IOException {
        if (file != null && file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
    }
}
