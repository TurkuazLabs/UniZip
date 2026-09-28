# 📄 Dosya Yolu: scripts/build-opencart-extension.py
# 📌 Amac: Turkuaz Entitlement API kaynaklarini OpenCart OCMOD ZIP paketine donusturmek
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: install.xml ve upload klasorunu surumlu ocmod.zip olarak paketler
# Bagimli Oldugu Katman: Tool | Config

from __future__ import annotations

import shutil
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
EXTENSION_ROOT = ROOT / "extensions" / "opencart" / "turkuaz-entitlement-api"
DIST_ROOT = ROOT / "dist"
VERSION_FILE = EXTENSION_ROOT / "version.txt"
REQUIRED_PATHS = (
    EXTENSION_ROOT / "install.xml",
    EXTENSION_ROOT / "upload",
)


def read_version() -> str:
    if not VERSION_FILE.is_file():
        raise FileNotFoundError(f"Surum dosyasi bulunamadi: {VERSION_FILE}")

    lines = VERSION_FILE.read_text(encoding="utf-8").splitlines()
    values = [line.strip() for line in lines if line.strip() and not line.lstrip().startswith("#")]
    if not values:
        raise ValueError("OpenCart eklenti surumu bos olamaz.")
    return values[-1]


def validate_sources() -> None:
    missing = [str(path) for path in REQUIRED_PATHS if not path.exists()]
    if missing:
        raise FileNotFoundError("Eksik OCMOD kaynaklari: " + ", ".join(missing))


def build_package() -> Path:
    validate_sources()
    version = read_version()
    DIST_ROOT.mkdir(parents=True, exist_ok=True)

    package_path = DIST_ROOT / f"turkuaz_entitlement_api_v{version}_oc3.ocmod.zip"
    temp_path = package_path.with_suffix(package_path.suffix + ".part")

    if temp_path.exists():
        temp_path.unlink()

    with zipfile.ZipFile(temp_path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        archive.write(EXTENSION_ROOT / "install.xml", "install.xml")

        for source in sorted((EXTENSION_ROOT / "upload").rglob("*")):
            if source.is_file():
                relative = source.relative_to(EXTENSION_ROOT)
                archive.write(source, relative.as_posix())

    with zipfile.ZipFile(temp_path, "r") as archive:
        bad_file = archive.testzip()
        if bad_file is not None:
            raise RuntimeError(f"OCMOD ZIP dogrulamasi basarisiz: {bad_file}")

    shutil.move(str(temp_path), str(package_path))
    return package_path


def main() -> int:
    try:
        package_path = build_package()
    except (OSError, ValueError, RuntimeError) as exc:
        print(f"HATA: {exc}", file=sys.stderr)
        return 1

    print(f"OCMOD paket olusturuldu: {package_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
