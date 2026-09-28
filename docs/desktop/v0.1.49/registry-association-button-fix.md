# 📄 Dosya Yolu: docs/v0.1.53/registry-association-button-fix.md
# 📌 Amac: Registry iliskilendirme syntax hatasini ve + buton metnini duzeltmek
# 📌 Modul - FileType
# Version: 0.1.53
# Aciklama: OpenWithProgids bos deger yazimini kaldirir ve acik metinli iliskilendirme butonlari ekler

Bagimli Oldugu Katman: View | Service | Tool | Language

v0.1.53, Windows `reg.exe` tarafinda `ERROR: Invalid syntax` ureten bos `OpenWithProgids` deger yazimini kaldirir.

Iliskilendirme ana akisi su kayitlari kullanir:

- `HKCU/HKLM\Software\Classes\.<uzanti>` default value: `UniZip.Archive`
- `HKCU/HKLM\Software\Classes\UniZip.Archive`
- `shell\open\command`
- `DefaultIcon`
- `Capabilities\FileAssociations`
- `RegisteredApplications`

Sistem sekmesindeki `+` butonlari artik kullanici tarafinda daha anlasilir olsun diye metinlidir:

- `Iliskilendir`
- `Tumunu iliskilendir`
