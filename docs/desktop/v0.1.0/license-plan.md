# 📄 Dosya Yolu: docs/v0.1.0/license-plan.md
# 📌 Amac: UniZip Pro lisans sisteminin Community core disinda planlanmasi
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: lisans, OpenCart ve cihaz limiti icin ayrim plani

Bagimli Oldugu Katman: Service | Repo | Tool | Config

# License Plan

Community core icinde lisans kontrol kodu bulunmaz.

Private kalacak bolumler:

- License API
- OpenCart 3.x aktivasyon modulu
- Cihaz limiti
- Offline lisans cache
- FeatureGateService
- Pro moduller

Ilk ticari model:

- Community: ucretsiz
- Pro Personal: 2 PC
- Pro Business: cihaz basina
- Ek cihaz: ucretli

Lisans mimarisi:

```text
OpenCart -> License API -> imzali aktivasyon -> UniZip Pro
```

Private key uygulama icine konmaz. UniZip icinde sadece public key bulunabilir.
