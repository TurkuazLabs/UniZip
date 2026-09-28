# 📄 Dosya Yolu: docs/v0.1.30/structure-review.md
# 📌 Amac: UniZip v0.1.29 checkpoint yapisini kontrol edip v0.1.30 icin guvenli devam kararini belgelemek
# 📌 Modul - FileType
# Version: 0.1.30
# Aciklama: katman ayrimi, riskler ve devam karari

Bagimli Oldugu Katman: Controller | Service | Repo/Model | Tool | View | Language

# UniZip v0.1.29 Structure Review

## Sonuc

Yapi genel olarak dogru yolda ve checkpoint olarak devam edilebilir.

## Dogru Noktalar

- NetBeans import uyumlu paket yapisi korunmus.
- Maven proje yapisi `apps/desktop` altinda temiz duruyor.
- Controller, Service, Tool, View, Repo/Model ve Language klasorleri mevcut.
- ZIP isleri `JavaZipTool` icinde toplanmis.
- Is akisi `ArchiveController` uzerinden `ArchiveService` katmanina yonleniyor.
- Ayar ve log tarafinda repository ayrimi baslatilmis.
- Dil dosyalari kaynak klasorunde tutuluyor.
- Surukle-birak, add, delete, test ve preview ayni arsiv modeli etrafinda calisiyor.

## Duzeltilen Noktalar

- `pom.xml` proje surumu 0.1.30 yapildi.
- `version.txt` 0.1.30 yapildi.
- `app.yml` icindeki eski 0.1.7 surum bilgisi 0.1.30 yapildi.
- Dil dosyalarindaki pencere basligi ve About metni 0.1.30 yapildi.
- `ArchiveListPanel` bos filtre sonucunda eski secimi tutmayacak sekilde guvenli temizlendi.

## Riskler

### MainFrame buyuyor

`MainFrame` su anda sadece ekrani cizmiyor; klasor gezinme, liste filtreleme, preview yonlendirme, drag-drop ve shortcut akislarini da tasiyor. Kisa vadede calisir ama v0.2.x tarafinda bolunmeli.

Onerilen gelecek bolumleme:

- `ToolbarPanel`
- `SearchFilterPanel`
- `PreviewPanel`
- `ArchiveTreePanel`
- `StatusBarPanel`

### Dil anahtarlari merkezi degil

Dil dosyalari var ama Java tarafinda sabit key sinifi yok. Sonraki mimari temizlikte `LanguageKeys` eklenebilir.

### ZIP motoru tek formatli

Community icin normal. Pro veya ileride 7z/rar gibi formatlar istenirse `ArchiveTool` arayuzu eklenmeli.

## Devam Karari

v0.1.29 checkpoint bozulmadan v0.1.30 surumune gecildi.

v0.1.30 hedefi:

- Arama kutusu
- Dosya turu filtresi
- Ctrl+F kisayolu
- Filtre sayaci
- Bos filtre sonucunda guvenli secim temizligi
