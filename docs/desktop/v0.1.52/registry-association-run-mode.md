# 📄 Dosya Yolu: docs/v0.1.53/registry-association-run-mode.md
# 📌 Amac: Windows Registry dosya iliskilendirme akisini ve NetBeans run mode sinirini aciklamak
# 📌 Modul - FileType
# Version: 0.1.53
# Aciklama: .reg import tabanli iliskilendirme, UserChoice siniri ve test notlari

Bagimli Oldugu Katman: Service | Tool | View | Config

## Duzeltme

v0.1.53 ile Registry yazimi tek tek `reg add` komutlari yerine gecici `.reg` dosyasi uzerinden `reg import` ile yapilir.

Bu sayede `shell/open/command`, quoted path ve `%1` argumanindan kaynaklanabilecek `Invalid syntax` hatasi azaltilir.

## Run Project notu

NetBeans Run Project ile iliskilendirme yapilirsa Registry komutu mevcut Java classpath uzerinden yazilir. Bu test icin uygundur ama proje tasinirsa veya target klasoru temizlenirse Windows cift tik acisi bozulabilir.

Kalici dagitim icin jar/exe/installer yolu kullanilmalidir.

## Windows UserChoice siniri

Windows 10/11 bazi uzantilarda `UserChoice` kaydi kullanir. Bu kayit Microsoft hash korumalidir. UniZip Registry'ye kaydolsa bile Windows eski uygulamayi acmaya devam ederse kullanici Varsayilan Uygulamalar ekranindan UniZip'i secmelidir.
