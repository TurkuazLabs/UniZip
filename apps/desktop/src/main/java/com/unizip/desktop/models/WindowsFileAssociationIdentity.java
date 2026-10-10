/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/WindowsFileAssociationIdentity.java
# 📌 Amac: Community ve Pro onizleme paketlerinin Windows aday uygulama kayitlarini cakistirmadan tanimlamak
# 📌 Modul - Java
# Version: 0.3.2
# Aciklama: Legacy iki ayri EXE doneminde uygulama, ProgID ve Capabilities kimliklerini merkezilestirir

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

import java.util.Objects;

public record WindowsFileAssociationIdentity(
        String applicationName,
        String progId,
        String progIdDescription,
        String capabilitiesKey,
        String applicationDescription
) {
    public static final String COMMUNITY_EXECUTABLE = "UniZip.exe";
    public static final String PRO_EXECUTABLE = "UniZip Pro.exe";

    private static final WindowsFileAssociationIdentity COMMUNITY =
            new WindowsFileAssociationIdentity(
                    "UniZip", "UniZip.Archive", "UniZip Archive",
                    "Software\\UniZip\\Capabilities",
                    "UniZip Community archive manager"
            );
    private static final WindowsFileAssociationIdentity PRO =
            new WindowsFileAssociationIdentity(
                    "UniZip Pro", "UniZip.Pro.Archive", "UniZip Pro Archive",
                    "Software\\UniZip\\Pro\\Capabilities",
                    "UniZip Pro archive manager"
            );

    public WindowsFileAssociationIdentity {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(progId, "progId");
        Objects.requireNonNull(progIdDescription, "progIdDescription");
        Objects.requireNonNull(capabilitiesKey, "capabilitiesKey");
        Objects.requireNonNull(applicationDescription, "applicationDescription");
    }

    public static WindowsFileAssociationIdentity forExecutable(String executableName) {
        return PRO_EXECUTABLE.equalsIgnoreCase(executableName) ? PRO : COMMUNITY;
    }

    public static WindowsFileAssociationIdentity community() {
        return COMMUNITY;
    }

    public static WindowsFileAssociationIdentity pro() {
        return PRO;
    }
}
