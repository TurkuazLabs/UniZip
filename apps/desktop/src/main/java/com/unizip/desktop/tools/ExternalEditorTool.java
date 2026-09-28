/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ExternalEditorTool.java
# 📌 Amac: Gecici dosyayi harici editor ile acip editor kapanana kadar beklemek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ayarlanabilir editor komutu ile metin ve resim dosyasi duzenleme akisina dis dunya adaptoru saglar

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ExternalEditorTool {
    public void openAndWait(Path filePath, String editorCommand) throws IOException, InterruptedException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IOException("Duzenlenecek gecici dosya bulunamadi: " + filePath);
        }

        List<String> command = parseCommand(editorCommand);
        if (command.isEmpty()) {
            command = defaultEditorCommand();
        }
        command.add(filePath.toAbsolutePath().toString());

        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Editor hata kodu ile kapandi: " + exitCode);
        }
    }

    private List<String> defaultEditorCommand() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return new ArrayList<>(List.of("notepad.exe"));
        }
        if (osName.contains("mac")) {
            return new ArrayList<>(List.of("open", "-W", "-t"));
        }
        String envEditor = System.getenv("EDITOR");
        if (envEditor != null && !envEditor.isBlank()) {
            return parseCommand(envEditor);
        }
        return new ArrayList<>(List.of("xdg-open"));
    }

    private List<String> parseCommand(String commandText) {
        List<String> parts = new ArrayList<>();
        if (commandText == null || commandText.isBlank()) {
            return parts;
        }

        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int index = 0; index < commandText.length(); index++) {
            char value = commandText.charAt(index);
            if (value == '"') {
                inQuote = !inQuote;
                continue;
            }
            if (Character.isWhitespace(value) && !inQuote) {
                addPart(parts, current);
                continue;
            }
            current.append(value);
        }
        addPart(parts, current);
        return parts;
    }

    private void addPart(List<String> parts, StringBuilder current) {
        if (current.length() == 0) {
            return;
        }
        parts.add(current.toString());
        current.setLength(0);
    }
}
