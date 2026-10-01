# 📄 Dosya Yolu: docs/desktop/v0.2.0/update-source-port.md
# 📌 Amac: UniZip Community public update source portunu ve edition sinirini belgelemek
# 📌 Modul - Markdown
# Version: 0.2.0
# Aciklama: UpdateService kanal secimini Community core'dan ayirir ve harici edition policy implementasyonlari icin public contract tanimlar
# Bagimli Oldugu Katman: Service | Repo/Model | Config

# Update Source Port

UniZip Community v0.2.0 ile update kanal karari UpdateService icinden ayrildi.

## Public Contract

UpdateConfig -> UpdateSourcePolicyService -> UpdateSource -> UpdateService

Community build varsayilan olarak CommunityUpdateSourcePolicyService kullanir.

## Edition Siniri

- Community repository Pro kaynak koduna bagimli degildir.
- Harici edition implementasyonu public UpdateSourcePolicyService portunu uygulayabilir.
- UpdateSource yalniz kanal ve manifest adresi tasir.
- Paket dogrulama Community UpdateService icinde kalir.
- SHA-256, boyut ve HTTPS guvenlik kontrolleri edition policy tarafina tasinmaz.

## Manifest Template

Varsayilan template:

https://unizip.turkuaz.com/releases/{channel}/update.yml

Community exact manifest:

https://unizip.turkuaz.com/releases/stable/update.yml

Alternatif kanal secildiginde {channel} token'i public config tarafindan cozulur. Token bulunmuyorsa alternatif kanal fail-closed reddedilir.
