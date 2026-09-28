@echo off
REM # 📄 Dosya Yolu: scripts/build-all.bat
REM # 📌 Amac: Windows uzerinde Desktop testini ve OpenCart OCMOD paketini birlikte olusturmak
REM # 📌 Modul - FileType
REM # Version: 0.1.0
REM # Aciklama: Monorepo local CI giris noktasi
REM # Bagimli Oldugu Katman: Tool | Config

setlocal
cd /d "%~dp0.."

call mvn -B clean verify
if errorlevel 1 exit /b 1

python scripts\build-opencart-extension.py
if errorlevel 1 exit /b 1

for /r extensions\opencart\turkuaz-entitlement-api\upload %%F in (*.php) do (
    php -l "%%F"
    if errorlevel 1 exit /b 1
)

endlocal
