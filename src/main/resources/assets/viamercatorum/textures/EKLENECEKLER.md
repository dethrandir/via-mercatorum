# Eklenecek Texture'lar

Bu modun çalışması için aşağıdaki `.png` dosyalarının ilgili dizinlere eklenmesi gerekir.

---

## 1. GUI Texture

| Dosya | `gui/village_hall.png` |
|---|---|
| **Klasör** | `src/main/resources/assets/viamercatorum/textures/gui/` |
| **Boyut** | 256×256 pixel |
| **Kullanım** | `MerchantScreen.java:18` ve `VillageHallScreen.java:19` — Village Hall bloğu arayüzü arkaplanı |
| **İçerik** | Üst kısımda başlık ve 6 sekme alanı, ortada sekme içerik alanı, alt kısımda oyuncu envanteri. Vanilla villager GUI'sine benzer tarzda, kahverengi/beyaz tonlarda. |

---

## 2. Entity Texture'ları

### 2a. Kervan Tüccarı

| Dosya | `entity/caravan_trader.png` |
|---|---|
| **Klasör** | `src/main/resources/assets/viamercatorum/textures/entity/` |
| **Boyut** | Standart villager skin (64×64 pixel, 64×32 kullanılabilir alan) |
| **Kullanım** | `CaravanEntityRenderer.java:10` — `CaravanEntity` (kervan lideri) üzerinde |
| **İçerik** | Kervan tüccarı villager skin. Sırtında çuval, elinde defter, belinde kese gibi tüccar aksesuarları olabilir. Yeşil/kahverengi tonlarında. |

### 2b. Kervan Muhafızı (Kılıçlı)

| Dosya | `entity/caravan_guard_sword.png` |
|---|---|
| **Klasör** | `src/main/resources/assets/viamercatorum/textures/entity/` |
| **Boyut** | 64×64 pixel |
| **Kullanım** | `CaravanGuardEntityRenderer.java:10` — Kılıçlı `CaravanGuardEntity` üzerinde |
| **İçerik** | Zırhlı villager skin. Elinde kılıç, üzerinde zincir zırh/deri zırh. Kırmızı/gri tonlarında, askeri görünüm. |

### 2c. Kervan Muhafızı (Yaylı)

| Dosya | `entity/caravan_guard_bow.png` |
|---|---|
| **Klasör** | `src/main/resources/assets/viamercatorum/textures/entity/` |
| **Boyut** | 64×64 pixel |
| **Kullanım** | `CaravanGuardEntityRenderer.java:11` — Yaylı `CaravanGuardEntity` üzerinde |
| **İçerik** | Sırtında yay, daha hafif zırhlı villager skin. Yeşil/kahverengi kamuflaj tonlarında, izci görünümü. |

### 2d. Karakter (Genel NPC)

| Dosya | `entity/character.png` |
|---|---|
| **Klasör** | `src/main/resources/assets/viamercatorum/textures/entity/` |
| **Boyut** | 64×64 pixel |
| **Kullanım** | `CharacterEntityRenderer.java:11` — `CharacterEntity` (genel NPC) üzerinde |
| **İçerik** | Humanoid mob skin. Vanilya player model kullanılır (kaftan/domat方向发展). Farklı karakter türleri için placeholder — ileride dynamic skin sistemi eklenecek. |

---

## Özet

| # | Dosya | Boyut | Kullanıldığı Yer | Durum |
|---|---|---|---|---|
| 1 | `gui/village_hall.png` | 256×256 | Village Hall GUI arkaplanı | **EKLENECEK** |
| 2 | `entity/caravan_trader.png` | 64×64 | Kervan tüccarı skin | **EKLENECEK** |
| 3 | `entity/caravan_guard_sword.png` | 64×64 | Kılıçlı muhafız skin | **EKLENECEK** |
| 4 | `entity/caravan_guard_bow.png` | 64×64 | Yaylı muhafız skin | **EKLENECEK** |
| 5 | `entity/character.png` | 64×64 | Karakter NPC skin | **EKLENECEK** |

> **Not:** Texture'lar 1.21.5 (26.1.2) ile uyumlu olmalıdır. Vanilla villager/player skin formatı kullanılır.
> Tüm texture'lar `src/main/resources/assets/viamercatorum/textures/` altına konur.