@echo off
REM # 📄 Dosya Yolu: scripts/run-ci-local.bat
REM # 📌 Amac: Windows uzerinde UniZip monorepo test ve paket kontrollerini calistirmak
REM # 📌 Modul - FileType
REM Version: 0.1.61
REM Aciklama: Desktop Maven verify ve OpenCart OCMOD/PHP lint kontrolu
REM Bagimli Oldugu Katman: Tool | Config

cd /d "%~dp0.."
call scripts\build-all.bat
