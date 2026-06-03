# Eklenecek Texture'lar

Bu modun çalışması için aşağıdaki `.png` dosyalarının ilgili dizinlere eklenmesi gerekir.

---

## 1. GUI Texture

| Dosya | `gui/market_stall.png` |
|---|---|
| **Klasör** | `src/main/resources/assets/viamercatorum/textures/gui/` |
| **Boyut** | 256×256 pixel |
| **Kullanım** | `MerchantScreen.java:15` — Market Stall bloğu ve kervan ticareti arayüzü arkaplanı |
| **İçerik** | Üst kısımda başlık alanı, alt kısımda oyuncu envanteri, ortada ticaret listesi için boş alan. Vanilla villager GUI'sine benzer tarzda, kahverengi/beyaz tonlarda. |

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
| **Kullanım** | `CaravanGuardEntityRenderer.java:11` (kodda tanımlı, henüz `getTextureLocation()` içinde dönmüyor) — Yaylı `CaravanGuardEntity` üzerinde |
| **İçerik** | Sırtında yay, daha hafif zırhlı villager skin. Yeşil/kahverengi kamuflaj tonlarında, izci görünümü. |

---

## Özet

| # | Dosya | Boyut | Kullanıldığı Yer | Durum |
|---|---|---|---|---|
| 1 | `gui/market_stall.png` | 256×256 | Trade GUI arkaplanı | **EKLENECEK** |
| 2 | `entity/caravan_trader.png` | 64×64 | Kervan tüccarı skin | **EKLENECEK** |
| 3 | `entity/caravan_guard_sword.png` | 64×64 | Kılıçlı muhafız skin | **EKLENECEK** |
| 4 | `entity/caravan_guard_bow.png` | 64×64 | Yaylı muhafız skin | **EKLENECEK** |

> **Not:** Texture'lar 1.21.5 (26.1.2) ile uyumlu olmalıdır. Vanilla villager skin formatı kullanılır.
> Tüm texture'lar `src/main/resources/assets/viamercatorum/textures/` altına konur.
