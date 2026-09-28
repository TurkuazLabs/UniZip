@echo off
REM 📄 Dosya Yolu: scripts/run-desktop.bat
REM 📌 Amac: Windows ortaminda UniZip Desktop uygulamasini Maven ile calistirmak
REM 📌 Modul - FileType
REM Version: 0.1.0
REM Aciklama: gelistirme calistirma komutu

cd /d "%~dp0..\apps\desktop"
mvn clean package exec:java
