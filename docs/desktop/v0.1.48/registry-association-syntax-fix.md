# 📄 Dosya Yolu: docs/v0.1.53/registry-association-syntax-fix.md
# 📌 Amac: Windows Registry dosya iliskilendirme komutundaki syntax hatasini aciklamak
# 📌 Modul - FileType
# Version: 0.1.53
# Aciklama: OpenWithProgids bos deger yazimi ve reg.exe komut davranisi

Bagimli Oldugu Katman: Service | Tool | View

## Sorun

Dosya iliskilendirme sirasinda `ERROR: Invalid syntax. Type REG ADD /? for usage.` hatasi goruluyordu.

## Neden

`OpenWithProgids` altina `UniZip.Archive` bos degeri yazilirken reg.exe komutuna bos `/d` parametresi veriliyordu. Windows tarafinda bu komut invalid syntax olarak donuyordu.

## Cozum

- Bos deger yazimi icin `/d` parametresi kaldirildi.
- `OpenWithProgids\UniZip.Archive` degeri `REG_NONE` olarak yazildi.
- Genel named value yaziminda value bos ise `/d` komuta eklenmez.
