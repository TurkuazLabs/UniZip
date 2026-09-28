#!/usr/bin/env bash
# 📄 Dosya Yolu: scripts/run-desktop.sh
# 📌 Amac: Linux/macOS ortaminda UniZip Desktop uygulamasini Maven ile calistirmak
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: gelistirme calistirma komutu

cd "$(dirname "$0")/../apps/desktop" || exit 1
mvn clean package exec:java
