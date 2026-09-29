<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=soft&color=0:FCAF01,50:FE2C6B,100:FC05AC&height=160&section=header&text=Tpa&fontSize=64&fontColor=FFFFFF&fontAlignY=42&desc=Spigot%20%2F%20Paper%20%E2%80%A2%201.8%20%E2%86%92%201.21&descSize=16&descAlignY=72&animation=fadeIn" alt="Tpa" width="100%" />
</p>

<p align="center">
  <a href="https://github.com/coslant/TPA/releases/latest"><img src="https://img.shields.io/github/v/release/coslant/TPA?style=for-the-badge&label=%C4%B0ndir&color=FE2C6B&logo=github" alt="Download" /></a>
  <a href="https://discord.gg/forges"><img src="https://img.shields.io/badge/Discord-Forges%20Studio-5865F2?style=for-the-badge&logo=discord&logoColor=white" alt="Discord: Forges Studio" /></a>
</p>

<p align="center">
  <a href="https://github.com/coslant/TPA/actions/workflows/build.yml"><img src="https://img.shields.io/github/actions/workflow/status/coslant/TPA/build.yml?branch=main&style=flat-square&label=build" alt="Build" /></a>
  <img src="https://img.shields.io/badge/Minecraft-1.8%20%E2%86%92%201.21-FCAF01?style=flat-square" alt="Minecraft 1.8 - 1.21" />
  <img src="https://img.shields.io/badge/Spigot%20%7C%20Paper-FE9808?style=flat-square" alt="Spigot | Paper" />
  <img src="https://img.shields.io/badge/Java-8-FD6C29?style=flat-square&logo=openjdk&logoColor=white" alt="Java 8" />
  <a href="LICENSE"><img src="https://img.shields.io/github/license/coslant/TPA?style=flat-square&color=FB0C9E&label=lisans" alt="License" /></a>
</p>

<p align="center">
  <b>Işınlanma istekleri, ayarlanabilir bekleme süresi ve ekranda geri sayım içeren Spigot/Paper eklentisi.</b>
  <br>
  <sub><kbd>EN</kbd> Teleport requests with a configurable warmup, on-screen countdown and cancel-on-move.</sub>
</p>

Oyuncular `/tpa` ile birbirinin yanına ışınlanma isteği gönderir. İstek kabul edilince
**ayarlanabilir bir bekleme süresi** (varsayılan 5 sn) başlar; bu süre ekranda **geri sayarak**
gösterilir. Beklerken hareket edilirse ışınlanma iptal olur. Tek jar **1.8 – 1.21** arası çalışır.

## ✨ Özellikler

- ⏳ Ayarlanabilir ışınlanma bekleme süresi (varsayılan 5 sn)
- 🔢 Bekleme süresi ekranda **geri sayarak** gösterilir (action bar veya title)
- 🚶 Hareket edince iptal (configden açılıp kapatılabilir)
- 🔒 Aynı anda tek bekleyen istek — biri beklerken başkasına istek atılamaz
- ⌛ İstek zaman aşımı ve isteğe bağlı bekleme (cooldown) configden
- ⚡ `tpa.bypass` yetkisi olanlar anında ışınlanır

## 📥 Kurulum

1. [Releases](https://github.com/coslant/TPA/releases/latest) sayfasından `Tpa.jar` dosyasını indir ve sunucunun `plugins/` klasörüne at.
2. Sunucuyu yeniden başlat.
3. `plugins/Tpa/config.yml` dosyasından süreleri ve mesajları düzenle.

## 🎮 Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/tpa <oyuncu>` | Oyuncunun yanına gitmek için istek gönderir | `tpa.use` |
| `/tpaccept [oyuncu]` | Gelen isteği kabul eder | `tpa.use` |
| `/tpdeny [oyuncu]` | Gelen isteği reddeder | `tpa.use` |
| `/tpacancel` | Gönderdiğin isteği iptal eder | `tpa.use` |

## 🔑 Yetkiler

| Yetki | Açıklama | Varsayılan |
|-------|----------|------------|
| `tpa.use` | Işınlanma komutlarını kullanır | herkes |
| `tpa.bypass` | Beklemeden anında ışınlanır | kapalı (verilmeli) |

> `tpa.bypass` varsayılan **kapalıdır** — OP dahil herkes bekleme süresine tabidir. Yalnızca bu
> yetki açıkça verilen oyuncular (örn. LuckPerms ile) anında ışınlanır.

## ⚙️ Ayarlar (config.yml)

| Anahtar | Açıklama |
|---------|----------|
| `warmup` | Işınlanma bekleme süresi (saniye, 0 = anında) |
| `cancel-on-move` | Hareket edince iptal et |
| `request-timeout` | İstek kaç saniye sonra zaman aşımına uğrar |
| `cooldown` | İki istek arası bekleme (saniye, 0 = kapalı) |
| `countdown.type` | `ACTIONBAR` (küçük) veya `TITLE` (ekran ortası) |
| `countdown.message` / `title` / `subtitle` | Geri sayım metinleri (`%time%`) |
| `messages.*` | Tüm mesajlar |

## 🛠️ Derleme

```bash
mvn clean package
```

Çıktı: `target/Tpa.jar`

> 1.8 uyumluluğu için Java 8 hedefiyle derlenir. En sorunsuz derleme **JDK 8, 11 veya 17** iledir.

Her push'ta [GitHub Actions](https://github.com/coslant/TPA/actions/workflows/build.yml) projeyi otomatik derler; etiketlenen sürümler (`v*`) jar'ıyla birlikte [Releases](https://github.com/coslant/TPA/releases) sayfasında yayınlanır.

## 💬 Destek

- 🐛 Hata bildirimi ve öneriler için [Issues](https://github.com/coslant/TPA/issues) sekmesini kullan.
- 💬 Sorular ve destek için [Forges Studio Discord](https://discord.gg/forges) sunucusuna katıl.

## 📄 Lisans

[MIT](LICENSE) — dilediğin gibi kullan, değiştir, dağıt.

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=rect&color=0:FCAF01,50:FE2C6B,100:FC05AC&height=4&section=footer" width="100%" alt="" />
  <br>
  <sub>🔥 <a href="https://github.com/coslant">coslant</a> tarafından geliştirildi · <a href="https://discord.gg/forges">Forges Studio</a></sub>
</p>
