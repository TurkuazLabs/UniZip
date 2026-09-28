/*
# 📄 Dosya Yolu: apps/desktop/src/main/java/com/unizip/desktop/models/ArchiveCapabilities.java
# 📌 Amac: Bir arsiv motorunun destekledigi islemleri merkezi model olarak tutmak
# 📌 Modul - FileType
# Version: 0.1.57
# Aciklama: UI butonlari ve service guard kontrolleri icin capability modeli

Bagimli Oldugu Katman: Repo/Model
*/
package com.unizip.desktop.models;

public record ArchiveCapabilities(
        boolean list,
        boolean extract,
        boolean test,
        boolean add,
        boolean delete,
        boolean rename,
        boolean editSaveBack,
        boolean create,
        boolean passwordRead,
        boolean passwordWrite,
        boolean solidArchive,
        boolean multiVolume
) {
    public static ArchiveCapabilities none() {
        return builder().build();
    }

    public static ArchiveCapabilities zipFull() {
        return builder()
                .list(true)
                .extract(true)
                .test(true)
                .add(true)
                .delete(true)
                .rename(true)
                .editSaveBack(true)
                .create(true)
                .passwordRead(false)
                .passwordWrite(false)
                .solidArchive(false)
                .multiVolume(false)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private boolean list;
        private boolean extract;
        private boolean test;
        private boolean add;
        private boolean delete;
        private boolean rename;
        private boolean editSaveBack;
        private boolean create;
        private boolean passwordRead;
        private boolean passwordWrite;
        private boolean solidArchive;
        private boolean multiVolume;

        public Builder list(boolean value) {
            list = value;
            return this;
        }

        public Builder extract(boolean value) {
            extract = value;
            return this;
        }

        public Builder test(boolean value) {
            test = value;
            return this;
        }

        public Builder add(boolean value) {
            add = value;
            return this;
        }

        public Builder delete(boolean value) {
            delete = value;
            return this;
        }

        public Builder rename(boolean value) {
            rename = value;
            return this;
        }

        public Builder editSaveBack(boolean value) {
            editSaveBack = value;
            return this;
        }

        public Builder create(boolean value) {
            create = value;
            return this;
        }

        public Builder passwordRead(boolean value) {
            passwordRead = value;
            return this;
        }

        public Builder passwordWrite(boolean value) {
            passwordWrite = value;
            return this;
        }

        public Builder solidArchive(boolean value) {
            solidArchive = value;
            return this;
        }

        public Builder multiVolume(boolean value) {
            multiVolume = value;
            return this;
        }

        public ArchiveCapabilities build() {
            return new ArchiveCapabilities(
                    list,
                    extract,
                    test,
                    add,
                    delete,
                    rename,
                    editSaveBack,
                    create,
                    passwordRead,
                    passwordWrite,
                    solidArchive,
                    multiVolume
            );
        }
    }
}
