/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ExtractOverwriteMode.java
# 📌 Amac: ZIP cikarma sirasinda hedef dosya cakismalarinda uygulanacak karari modellemek
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: uzerine yaz, atla veya otomatik yeniden adlandir cikarma politikasini tutar

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public enum ExtractOverwriteMode {
    OVERWRITE,
    SKIP,
    RENAME
}
