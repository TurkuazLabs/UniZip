# 📄 Dosya Yolu: docs/desktop/v0.3.0/runtime-bootstrap.md
# 📌 Amac: UniZip Community v0.3.0 public desktop runtime bootstrap contractini belgelemek
# 📌 Modul - Markdown
# Version: 0.3.0
# Aciklama: Community default davranis, harici edition service injection ve repository dependency sinirlarini tanimlar
# Bagimli Oldugu Katman: Service | Config

# UniZip Desktop v0.3.0 Runtime Bootstrap

## Public Contract

Community Desktop su public composition modelini saglar:

```text
DesktopRuntimeServices
        |
        +--> UpdateSourcePolicyService
```

Public launch hook:

```text
MainApp.launch(args, runtimeServices)
```

## Community Default

Normal Community entrypoint:

```text
MainApp.main(args)
        |
        v
DesktopRuntimeServices.community()
        |
        v
CommunityUpdateSourcePolicyService
```

Bu nedenle Community davranisi v0.2.0 ile geriye uyumludur.

## Harici Edition

Private veya harici edition dagitimi:

1. Community public artifact'ini dependency olarak kullanir.
2. Kendi `UpdateSourcePolicyService` implementasyonunu olusturur.
3. `DesktopRuntimeServices` icine enjekte eder.
4. `MainApp.launch(...)` ile Community UI/core'u baslatir.

Community repository private edition kaynagina bagimli olmaz.

## Guvenlik Siniri

Harici update policy yalniz kanal/manifest kaynagini secer.

Manifest fetch, HTTPS, channel match, dosya boyutu ve SHA-256 dogrulama kurallari Community `UpdateService` icinde kalir.
