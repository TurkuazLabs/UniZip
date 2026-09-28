/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveEntryModel.java
# 📌 Amac: Arsiv icerisindeki tek girdiyi temsil etmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: dosya adi, boyut ve klasor bilgisi modeli

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public record ArchiveEntryModel(String name, long size, boolean directory) {
}
