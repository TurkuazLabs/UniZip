/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveOperationResult.java
# 📌 Amac: Arsiv islemi sonucunu tasimak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: basari, mesaj ve etkilenen dosya sayisi modeli

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public record ArchiveOperationResult(boolean success, String message, int affectedEntries) {
}
