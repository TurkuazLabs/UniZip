/*
# 📄 Dosya Yolu: /apps/desktop/src/main/java/com/unizip/desktop/tools/SafeExtractTool.java
# 📌 Amac: ZIP cikarma hedeflerini traversal ve symlink disina tasma riskine karsi dogrulamak
# 📌 Modul - Java
# Version: 0.3.1
# Aciklama: Canonical output root, ZIP entry path ve existing symlink component guard
Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SafeExtractTool {
    public Path resolveSafeTarget(Path outputDirectory, String entryName) throws IOException {
        if (outputDirectory == null || entryName == null || entryName.isBlank()) {
            throw new IOException("Cikarma hedefi veya arsiv girdisi bos olamaz");
        }

        Path output = outputDirectory.toAbsolutePath().normalize();
        Files.createDirectories(output);
        Path canonicalRoot = output.toRealPath();
        Path entry = Path.of(entryName.replace('\\', '/'));
        if (entry.isAbsolute()) {
            throw new IOException("Mutlak arsiv yolu reddedildi: " + entryName);
        }

        Path target = canonicalRoot.resolve(entry).normalize();
        if (target.equals(canonicalRoot) || !target.startsWith(canonicalRoot)) {
            throw new IOException("Guvenli olmayan arsiv girdisi: " + entryName);
        }

        Path cursor = canonicalRoot;
        for (Path component : canonicalRoot.relativize(target)) {
            cursor = cursor.resolve(component);
            if (Files.isSymbolicLink(cursor)) {
                throw new IOException("Sembolik baglanti hedefi reddedildi: " + entryName);
            }
        }

        return target;
    }
}
