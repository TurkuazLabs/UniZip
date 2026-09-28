/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/SafeExtractTool.java
# 📌 Amac: Arsiv cikarma sirasinda path traversal ve Zip Slip riskini engellemek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: hedef dosya yolunu guvenli klasor icinde dogrular

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SafeExtractTool {
    public Path resolveSafeTarget(Path outputDirectory, String entryName) throws IOException {
        Path normalizedOutput = outputDirectory.toAbsolutePath().normalize();
        Files.createDirectories(normalizedOutput);
        Path target = normalizedOutput.resolve(entryName).normalize();
        if (!target.startsWith(normalizedOutput)) {
            throw new IOException("Guvenli olmayan arsiv girdisi: " + entryName);
        }
        return target;
    }
}
