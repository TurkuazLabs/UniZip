/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/BuiltInArchiveEngineProvider.java
# 📌 Amac: UniZip icinde gelen varsayilan arsiv motorlarini saglamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ZIP engine ve gelecekteki built-in engine listesini merkezi olarak verir

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.util.List;

public final class BuiltInArchiveEngineProvider implements ArchiveEngineProvider {
    @Override
    public String providerId() {
        return "unizip.provider.builtin";
    }

    @Override
    public List<ArchiveEngine> engines() {
        SafeExtractTool safeExtractTool = new SafeExtractTool();
        JavaZipTool javaZipTool = new JavaZipTool(safeExtractTool);
        return List.of(new ZipArchiveEngine(javaZipTool));
    }
}
