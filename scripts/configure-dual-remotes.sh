#!/usr/bin/env sh
# 📄 Dosya Yolu: scripts/configure-dual-remotes.sh
# 📌 Amac: Ayni local repository icin GitHub ve Gitea remote adreslerini ayarlamak
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: Iki remote ekler veya mevcut adresleri gunceller
# Bagimli Oldugu Katman: Tool | Config

set -eu

if [ "$#" -ne 2 ]; then
  echo "Kullanim: $0 GITHUB_REPOSITORY_URL GITEA_REPOSITORY_URL" >&2
  exit 1
fi

GITHUB_URL="$1"
GITEA_URL="$2"

set_remote() {
  name="$1"
  url="$2"
  if git remote get-url "$name" >/dev/null 2>&1; then
    git remote set-url "$name" "$url"
  else
    git remote add "$name" "$url"
  fi
}

set_remote github "$GITHUB_URL"
set_remote gitea "$GITEA_URL"

git remote -v
