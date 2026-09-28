# 📄 Dosya Yolu: docs/v0.1.0/github-plan.md
# 📌 Amac: UniZip GitHub kullanim planini belirlemek
# 📌 Modul - FileType
# Version: 0.1.0
# Aciklama: private/public repo ayrimi ve branch plani

Bagimli Oldugu Katman: Controller | Service | Repo | Tool | View | Language

# GitHub Plan

Baslangic icin:

```text
UniZip             private gelistirme repo
UniZip-Community   public community repo
UniZip-Pro         private pro repo
UniZip-License     private license api repo
UniZip-OpenCart    private OpenCart 3.x modul repo
```

Branch yapisi:

```text
main   calisan stabil kod
dev    gelistirme kodu
```

Commit ornekleri:

```text
init: add unizip community skeleton
feat: add java zip extract tool
fix: safe extract path validation
```

Secret dosyalari GitHub'a konmaz.
