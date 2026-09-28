/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/ArchiveEngineProvider.java
# 📌 Amac: GitHub commit veya classpath plugin ile arsiv motoru saglamak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: ServiceLoader uzerinden dis engine katkilarinin otomatik bulunmasini saglar

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.util.List;

public interface ArchiveEngineProvider {
    String providerId();

    List<ArchiveEngine> engines();
}
