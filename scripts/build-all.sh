#!/usr/bin/env sh
# 📄 Dosya Yolu: scripts/build-all.sh
# 📌 Amac: Linux/macOS uzerinde Desktop testini ve OpenCart OCMOD paketini birlikte olusturmak
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: Monorepo local CI giris noktasi
# Bagimli Oldugu Katman: Tool | Config

set -eu
cd "$(dirname "$0")/.."

mvn -B clean verify
python3 scripts/build-opencart-extension.py

find extensions/opencart/turkuaz-entitlement-api/upload -name '*.php' -type f -print0 \
  | xargs -0 -n1 php -l
