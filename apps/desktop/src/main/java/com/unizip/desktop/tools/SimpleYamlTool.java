/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/SimpleYamlTool.java
# 📌 Amac: Basit YAML dosyalarini ek bagimlilik olmadan okumak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: UniZip tema ve dil dosyalari icin minimal YAML parser

Bagimli Oldugu Katman: Tool | Config | Language
*/
package com.unizip.desktop.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class SimpleYamlTool {
    public Map<String, String> readFlattened(InputStream inputStream) throws IOException {
        Map<String, String> result = new HashMap<>();
        Deque<YamlNode> stack = new ArrayDeque<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.trim().startsWith("#")) {
                    continue;
                }
                int indent = countLeadingSpaces(line);
                String trimmed = line.trim();
                int separatorIndex = trimmed.indexOf(':');
                if (separatorIndex <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separatorIndex).trim();
                String value = trimmed.substring(separatorIndex + 1).trim();

                while (!stack.isEmpty() && stack.peek().indent() >= indent) {
                    stack.pop();
                }

                if (value.isBlank()) {
                    String prefix = buildPrefix(stack, key);
                    stack.push(new YamlNode(indent, prefix));
                } else {
                    String fullKey = buildPrefix(stack, key);
                    result.put(fullKey, cleanValue(value));
                }
            }
        }
        return result;
    }

    private String buildPrefix(Deque<YamlNode> stack, String key) {
        if (stack.isEmpty()) {
            return key;
        }
        return stack.peek().path() + "." + key;
    }

    private String cleanValue(String value) {
        String cleaned = value.trim();
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) || (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            return cleaned.substring(1, cleaned.length() - 1);
        }
        return cleaned;
    }

    private int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private record YamlNode(int indent, String path) {
    }
}
