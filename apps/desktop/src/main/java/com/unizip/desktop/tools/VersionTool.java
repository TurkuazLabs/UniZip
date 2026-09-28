/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/VersionTool.java
# 📌 Amac: UniZip surum numaralarini guvenli sekilde karsilastirmak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Noktali sayisal surumleri ve opsiyonel on surum eklerini karsilastirir

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

public final class VersionTool {
    public int compare(String left, String right) {
        ParsedVersion leftVersion = parse(left);
        ParsedVersion rightVersion = parse(right);
        int max = Math.max(leftVersion.numbers.length, rightVersion.numbers.length);
        for (int index = 0; index < max; index++) {
            int leftNumber = index < leftVersion.numbers.length ? leftVersion.numbers[index] : 0;
            int rightNumber = index < rightVersion.numbers.length ? rightVersion.numbers[index] : 0;
            int comparison = Integer.compare(leftNumber, rightNumber);
            if (comparison != 0) {
                return comparison;
            }
        }
        if (leftVersion.qualifier.isBlank() && !rightVersion.qualifier.isBlank()) {
            return 1;
        }
        if (!leftVersion.qualifier.isBlank() && rightVersion.qualifier.isBlank()) {
            return -1;
        }
        return leftVersion.qualifier.compareToIgnoreCase(rightVersion.qualifier);
    }

    private ParsedVersion parse(String value) {
        String normalized = value == null ? "0" : value.trim().replaceFirst("^[vV]", "");
        String[] mainAndQualifier = normalized.split("-", 2);
        String[] parts = mainAndQualifier[0].split("\\.");
        int[] numbers = new int[parts.length];
        for (int index = 0; index < parts.length; index++) {
            numbers[index] = parseNumber(parts[index]);
        }
        return new ParsedVersion(numbers, mainAndQualifier.length > 1 ? mainAndQualifier[1] : "");
    }

    private int parseNumber(String value) {
        try {
            return Integer.parseInt(value.replaceAll("[^0-9]", ""));
        } catch (Exception exception) {
            return 0;
        }
    }

    private record ParsedVersion(int[] numbers, String qualifier) {
    }
}
