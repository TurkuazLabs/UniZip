# 📄 Dosya Yolu: scripts/generate-update-manifest.py
# 📌 Amac: UniZip yayin paketi icin SHA-256 dogrulamali update.yml uretmek
# 📌 Modul - FileType
# Version: 0.1.61
# Aciklama: CI veya yerel release akisinda otomatik guncelleme manifesti olusturur
# Bagimli Oldugu Katman: Tool | Config

from __future__ import annotations

import argparse
import hashlib
from pathlib import Path


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def yaml_escape(value: str) -> str:
    return value.replace("\\", "\\\\").replace('"', '\\"').replace("\r", "").replace("\n", " ")


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate UniZip update.yml")
    parser.add_argument("--file", required=True, type=Path)
    parser.add_argument("--version", required=True)
    parser.add_argument("--download-url", required=True)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--channel", default="stable")
    parser.add_argument("--minimum-version", default="0.1.0")
    parser.add_argument("--published-at", default="")
    parser.add_argument("--notes", default="UniZip update")
    parser.add_argument("--mandatory", action="store_true")
    args = parser.parse_args()

    if not args.file.is_file():
        raise FileNotFoundError(args.file)

    content = f'''# 📄 Dosya Yolu: releases/{args.channel}/update.yml
# 📌 Amac: UniZip istemcilerinin okuyacagi guncelleme manifestini yayinlamak
# 📌 Modul - FileType
# Version: {args.version}
# Aciklama: CI tarafindan SHA-256 ile uretilen guncelleme bildirimi

# Bagimli Oldugu Katman: Config | Tool

release:
  product: "UNIZIP"
  version: "{yaml_escape(args.version)}"
  channel: "{yaml_escape(args.channel)}"
  mandatory: {str(args.mandatory).lower()}
  minimum_version: "{yaml_escape(args.minimum_version)}"
  download_url: "{yaml_escape(args.download_url)}"
  sha256: "{sha256(args.file)}"
  size_bytes: {args.file.stat().st_size}
  published_at: "{yaml_escape(args.published_at)}"
  notes: "{yaml_escape(args.notes)}"
'''
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(content, encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
