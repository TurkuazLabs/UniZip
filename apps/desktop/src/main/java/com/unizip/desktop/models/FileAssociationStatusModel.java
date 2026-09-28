/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/FileAssociationStatusModel.java
# 📌 Amac: Bir arsiv uzantisinin Windows dosya iliskilendirme durumunu tasimak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: uzanti, gecerli kullanici ve tum kullanicilar Registry durumlarini tabloya aktarir

Bagimli Oldugu Katman: Repo/Model | Service | View
*/
package com.unizip.desktop.models;

public record FileAssociationStatusModel(
        String extension,
        String currentUserAssociation,
        String allUsersAssociation
) {
}
