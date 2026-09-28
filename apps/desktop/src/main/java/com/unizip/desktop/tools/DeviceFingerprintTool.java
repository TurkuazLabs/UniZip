/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/tools/DeviceFingerprintTool.java
# 📌 Amac: UniZip lisans aktivasyonu icin kararlı cihaz kimligi uretmek
# 📌 Modul - FileType
# Version: 0.1.59
# Aciklama: Kisisel veri saklamadan OS, kullanici ve makine bilgisinden SHA-256 tabanli device_id uretir

Bagimli Oldugu Katman: Tool
*/
package com.unizip.desktop.tools;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public final class DeviceFingerprintTool {
    public String deviceId() {
        String raw = System.getProperty("os.name", "")
                + "|" + System.getProperty("os.arch", "")
                + "|" + System.getProperty("user.name", "")
                + "|" + hostName();
        return sha256(raw).substring(0, 32);
    }

    public String deviceName() {
        String host = hostName();
        String os = System.getProperty("os.name", "OS");
        return (host.isBlank() ? "UniZip Device" : host) + " - " + os;
    }

    private String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "unknown-host";
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte current : bytes) {
                builder.append(String.format(Locale.ROOT, "%02x", current));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 desteklenmiyor", exception);
        }
    }
}
