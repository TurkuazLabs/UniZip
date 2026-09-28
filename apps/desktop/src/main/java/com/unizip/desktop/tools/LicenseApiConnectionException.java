/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/LicenseApiConnectionException.java
# 📌 Amac: Lisans API baglanti hatalarini gecersiz lisans sonucundan ayirmak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Offline tolerans akisini tetikleyen transport exception tipidir

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

public final class LicenseApiConnectionException extends Exception {
    public LicenseApiConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public LicenseApiConnectionException(String message) {
        super(message);
    }
}
