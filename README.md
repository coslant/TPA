# Tpa

> Işınlanma istekleri, ayarlanabilir bekleme süresi ve ekranda geri sayım içeren Spigot/Paper eklentisi.

![Minecraft](https://img.shields.io/badge/Minecraft-1.8--1.21-brightgreen)
![Java](https://img.shields.io/badge/Java-8-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

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

1. `Tpa.jar` dosyasını `plugins/` klasörüne at.
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

## 📄 Lisans

MIT — dilediğin gibi kullan, değiştir, dağıt.
