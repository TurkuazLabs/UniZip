/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ThemeModel.java
# 📌 Amac: Tema meta bilgisini temsil etmek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: tema id, ad ve mod bilgisi modeli

Bagimli Oldugu Katman: Repo/Model | View
*/
package com.unizip.desktop.models;

public record ThemeModel(String id, String name, String mode) {
}
