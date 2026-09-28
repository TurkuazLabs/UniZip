#!/usr/bin/env sh
# 📄 Dosya Yolu: scripts/run-ci-local.sh
# 📌 Amac: Linux/macOS uzerinde UniZip monorepo test ve paket kontrollerini calistirmak
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: Desktop Maven verify ve OpenCart OCMOD/PHP lint kontrolu
# Bagimli Oldugu Katman: Tool | Config

set -eu
cd "$(dirname "$0")/.."
exec scripts/build-all.sh
