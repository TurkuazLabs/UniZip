@echo off
REM # 📄 Dosya Yolu: scripts/configure-dual-remotes.bat
REM # 📌 Amac: Ayni local repository icin GitHub ve Gitea remote adreslerini ayarlamak
REM # 📌 Modul - FileType
REM Version: 0.1.0
REM Aciklama: Iki remote ekler veya mevcut adresleri gunceller
REM Bagimli Oldugu Katman: Tool | Config

setlocal
if "%~2"=="" (
    echo Kullanim: %~nx0 GITHUB_REPOSITORY_URL GITEA_REPOSITORY_URL
    exit /b 1
)

set "GITHUB_URL=%~1"
set "GITEA_URL=%~2"

git remote get-url github >nul 2>&1
if errorlevel 1 (
    git remote add github "%GITHUB_URL%"
) else (
    git remote set-url github "%GITHUB_URL%"
)

git remote get-url gitea >nul 2>&1
if errorlevel 1 (
    git remote add gitea "%GITEA_URL%"
) else (
    git remote set-url gitea "%GITEA_URL%"
)

git remote -v
endlocal
