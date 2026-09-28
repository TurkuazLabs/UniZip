/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/UserDirectoryTool.java
# 📌 Amac: Kullanici klasorlerini platformdan bagimsiz hesaplamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: Desktop, Documents ve Downloads klasorlerini dosya secme ekranlari icin saglar

Bagimli Oldugu Katman: Tool | View | Controller
*/
package com.unizip.desktop.tools;

import java.nio.file.Files;
import java.nio.file.Path;

public final class UserDirectoryTool {
    private final Path homeDirectory;

    public UserDirectoryTool() {
        this.homeDirectory = Path.of(System.getProperty("user.home", "."));
    }

    public Path homeDirectory() {
        return homeDirectory;
    }

    public Path desktopDirectory() {
        return resolvePreferred("Desktop", "Masaüstü");
    }

    public Path documentsDirectory() {
        return resolvePreferred("Documents", "Belgeler");
    }

    public Path downloadsDirectory() {
        return resolvePreferred("Downloads", "İndirilenler", "Indirilenler");
    }

    private Path resolvePreferred(String... names) {
        for (String name : names) {
            Path candidate = homeDirectory.resolve(name);
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        return homeDirectory.resolve(names[0]);
    }
}
