# Via Mercatorum — Genişletilmiş Plan

---

## 0. Ön Hazırlık: Ekonomik Hesaplama Helperları

### `core/economy/EconomyHelper.java` (yeni)

```java
public class EconomyHelper {
    // bir köyün tüm mal stoğunun toplam zümrüt değeri
    public static double totalWealth(VillageNode village);

    // GDP = tüm supply'lerin productionRate * basePrice toplamı
    public static double gdp(VillageNode village);

    // GDP per capita
    public static double gdpPerCapita(VillageNode village);

    // Refah seviyesi (wealth + gdpPerCapita normalize)
    public static double prosperityIndex(VillageNode village);

    // Bir köyde acil durum var mı? (örn: gıda stoğu kritik)
    public static boolean hasFoodCrisis(VillageNode village);

    // İki köy arası ticarette hangi mallar karlı
    public static List<String> profitableGoods(VillageNode from, VillageNode to);

    // Bir köyün en çok ihtiyaç duyduğu mallar
    public static List<String> urgentGoods(VillageNode village, int limit);
}
```

**Mantık:**
- `FOOD` kategorisindeki supply'ler food crisis için kontrol
- Profitable goods: `from`'da bol (stock/maxCap > 0.6) ve `to`'da az (< 0.3)
- Wealth = `sum(stock * basePrice)`
- GDP = `sum(productionRate * basePrice)`
- Prosperity = `wealth/maxWealth * 0.5 + gdpPerCapita/maxGdp * 0.5`

---

## 1. İtibar Sistemi (Oyuncu Bazlı)

**`player/ReputationSystem.java`** — geliştirme:
```java
public class ReputationSystem {
    // Mevcut: globalReputation + villageReputations

    // YENİ: per-player profil
    public record ReputationProfile(
        double villagerRep,    // köylülerle ilişki (-100..100)
        double caravanRep,     // kervanlarla ilişki (-100..100)
        int totalRaidsStopped,
        double totalTradeVolume
    );

    // YENİ event handler'lar:
    public void onTradeCompleted(Player, String villageId);
    public void onBanditKilled(Player, String villageId);
    public void onCaravanDefended(Player);
    public void onVillageRaided(Player, String villageId);
    public void onPlayerRaidedCaravan(Player);
}
```

**Reputation → Mekanik:**
| Olay | villagerRep | caravanRep | globalRep |
|---|---|---|---|
| Trade completed | +0.5 | 0 | +0.1 |
| Bandit killed near village | +2 | 0 | +1 |
| Caravan defended | 0 | +5 | +2 |
| Player caused village raid | -10 | 0 | -5 |
| Player raided caravan | 0 | -20 | -5 |

**Bağlantılar:**
- `PlayerTradeManager.caravanBuy/Sell()` → `ReputationSystem.onTradeCompleted()`
- `PriceCalculator`: caravanRep'e göre caravan fiyatlarında ek indirim/ zam
- `SpawnManager`: düşük caravanRep → guard'lar oyuncuya agresif

---

## 2. Piracy (Korsanlık) Sistemi

### 2.1 TradeRoute'a Piracy Skoru

```java
public class TradeRoute {
    private double piracyScore;      // 0.0 (güvenli) – 1.0 (çok tehlikeli)
    private long lastRaidTime;       // son baskın zamanı (ruin spawn için)

    public void tickPiracyDecay();   // her SEK tick: piracy -= 0.001
    public void addPiracy(double);   // clamp 0-1
    public double getRiskPremium();  // 1.0 + piracy * 0.3
}
```

**Piracy'yi etkileyen faktörler:**
| Faktör | Etki |
|---|---|
| Biyom: desert/savanna | +0.05 başlangıç |
| Biyom: jungle/swamp | +0.08 başlangıç |
| Biyom: plains | 0 başlangıç |
| Biyom: snowy/taiga | -0.02 başlangıç |
| Yol: DIRT | +0.1 |
| Yol: GRAVEL | +0.05 |
| Yol: STONE | 0 |
| Yol: PAVED | -0.05 |
| Varış köyü zengin (wealth > 1000) | +0.001/tick (cazibe) |
| Kervan baskını oldu | +0.3 |
| Yağmacılar öldürüldü | -0.1 |
| Oyuncu yakında bandit kesti | -0.05 |
| Random fluctuation | ±0.002/tick |

### 2.2 Risk Primi

```java
// Fiyata yansıma:
// CaravanMode MerchantScreen'de:
// buyPrice = basePrice * 1.2 * route.getRiskPremium()
// sellPrice = basePrice * 0.7 / route.getRiskPremium()
```

### 2.3 Piracy Tick'i

`SimulationTick`'e her 50 tick:
```java
for (TradeRoute route : graph.getAllRoutes()) {
    route.tickPiracyDecay();
    route.addPiracy((random.nextDouble() - 0.5) * 0.004);
    if (EconomyHelper.totalWealth(route.getTo()) > 1000)
        route.addPiracy(0.001);
}
```

---

## 3. Yağmacı (Raider) Sistemi

### 3.1 Yeni Event'ler

`core/simulation/EconomyEvent.java`:
```java
public static class RaidOccurred extends EconomyEvent {
    String routeId, caravanId, targetId;
    boolean isCaravanRaid;
    Map<String, Double> lootedGoods;
    int chunkX, chunkZ;  // ruin spawn için
}

public static class BanditsCleared extends EconomyEvent {
    String routeId, villageId;
    int banditCount;
}
```

### 3.2 Raid Mekaniği

**Kervan baskını** (piracy > 0.4, chunk loaded):
1. `SpawnManager` periyodik kontrol: piracy > 0.4 olan rotalardaki kervanlar
2. Random seçim (ihtimal = piracy * 0.1)
3. Kervan chunk'ına `CaravanRaiderEntity` spawn (3-8 adet)
4. Yağmacılar → kervana saldırır
5. Guard'lar karşı koyar
6. Sonuç:
   - Kervan kazanırsa → `BanditsCleared` event, piracy -0.1
   - Yağmacılar kazanırsa → kervan yok olur, kargo kaybolur, `RaidOccurred` event, piracy +0.3
   - Oyuncu müdahil olursa → reputation

**Köy baskını** (her 1000 tick'te ~%1):
- Zengin olmayan, küçük köyler hedef (wealth < 500, type=HAMLET/VILLAGE)
- Köy chunk'ında spawn
- Başarılı: supply'ler %10-30 azalır

### 3.3 CaravanRaiderEntity

```java
public class CaravanRaiderEntity extends /* abstract illager-like */ {
    // ModEntities'de: EntityType<CaravanRaiderEntity>
    // AI: MeleeAttackGoal (CaravanEntity, CaravanGuardEntity, Player, Villager)
    // 30 saniye despawn timer
    // Equipment: iron axe veya crossbow
}
```

- Render: Pillager model + custom texture
- `EntityRendererRegistry.register` ViaMercatorumClient'da

### 3.4 Kervan Kalıntısı (RaidRemnant)

**Baskından ~3 gün sonra baskın chunk'ında:**
- `RaidRemnantBlock` + `RaidRemnantBlockEntity`
- Lootable: 1-3 adet düşük değerli item
  - `rotten_flesh`, `bone`, `string`, `coal`, `stone`
  - `oak_log`, `stick`
- 7-14 gün sonra kaybolur (tick takibi)
- Dekoratif: yanmış bloklar, kırık sandık görünümü

**`world/spawn/RaidRemnantManager.java`:**
- `RaidOccurred` event'ini dinler
- chunkX, chunkZ, timestamp kaydeder
- 3 gün sonra `RaidRemnantBlock` spawn eder
- 14 gün sonra kaldırır

---

## 4. AI Componentleri

### 4.1 `core/world/CaravanAI.java`

```java
public class CaravanAI {
    // Rota seçimi: düşük piracy + yüksek kar
    public static TradeRoute chooseRoute(VillageNode from, List<TradeRoute> candidates);

    // Kargo seçimi: profitableGoods() kullan
    public static Map<String, Double> chooseCargo(VillageNode from, VillageNode to, Random rng);

    // Konaklama süresi: ihtiyaca göre
    public static int determineStayDuration(Caravan c, VillageNode current);
}
```

**Mantık:**
- `chooseRoute()`: piracy skoru ne kadar düşükse o kadar yüksek weight. Ama %20 random factor (çeşitlilik)
- `chooseCargo()`: `EconomyHelper.profitableGoods(from, to)` ilk 3 mal, kalan kapasite random
- `determineStayDuration()`: varış köyünde food crisis varsa 2-3x daha uzun kal

### 4.2 `core/world/VillageAI.java`

```java
public class VillageAI {
    // Üretim optimizasyonu: karlı mallara yönel
    public static void optimizeProduction(VillageNode village, RouteGraph graph);

    // Rota önceliklendirme: en acil ihtiyaca göre
    public static List<TradeRoute> prioritizeRoutes(VillageNode village, List<TradeRoute> routes);

    // Köy ihtiyaç analizi
    public static VillageNeed assessNeeds(VillageNode village);

    public enum VillageNeed { FOOD_CRISIS, DEFENSE_CRISIS, SURPLUS, STABLE }
}
```

**Mantık:**
- `optimizeProduction()`: Her 100 tick'te bir, bağlı rotalardaki karlı mallara productionRate +%10 (max 2x base)
- `assessNeeds()`: FOOD stock < 0.2*maxCapacity → FOOD_CRISIS

---

## 5. Pazar Bölgeleri (Market Region)

### 5.1 `core/economy/MarketRegion.java`

```java
public class MarketRegion {
    String id, name;
    List<VillageNode> members;
    Map<String, Double> aggregatedSupply;   // member'ların toplam stock'u
    Map<String, Double> aggregatedDemand;   // member'ların toplam maxCapacity*0.5
    VillageNode marketCenter;               // en büyük köy

    // Bölgesel fiyat hesaplama
    public double getRegionalPrice(String goodId);
}
```

### 5.2 Oluşum

- `RouteGraph`'e eklendikçe köyler coğrafi yakınlığa göre gruplanır
- Her 8-12 köy bir MarketRegion
- API: `MarketRegionManager.getRegionForVillage(villageId)`
- `PriceCalculator`: önce bölgesel fiyatı dene, yoksa village-level kullan

### 5.3 UI'da Görünüm

- `MerchantScreen`'de bölge seçici (dropdown)
- Her bölgenin fiyatları ayrı gösterilir
- Bölgeler arası fiyat farkı → ticaret potansiyeli

---

## 6. Uygulama Sırası

### Faz 1: Ekonomik Helper + İtibar (hemen)
| # | Dosya | İş |
|---|---|---|
| 1 | `core/economy/EconomyHelper.java` | YENİ |
| 2 | `core/economy/PriceCalculator.java` | caravan mode'da risk primi + reputation |
| 3 | `player/ReputationSystem.java` | Per-player detay + event bağlantısı |
| 4 | `player/PlayerTradeManager.java` | Reputation update ekle |

### Faz 2: Piracy
| # | Dosya | İş |
|---|---|---|
| 5 | `core/world/TradeRoute.java` | `piracyScore` alanı + decay tick |
| 6 | `core/simulation/SimulationTick.java` | Piracy tick'i |
| 7 | `core/world/CaravanAI.java` (yeni) | Rota seçimi (piracy-aware) |
| 8 | `core/economy/PriceCalculator.java` | Risk primi |

### Faz 3: Yağmacı Entity + Kalıntı
| # | Dosya | İş |
|---|---|---|
| 9 | `entity/ModEntities.java` | `CARAVAN_RAIDER` EntityType |
| 10 | `entity/CaravanRaiderEntity.java` (yeni) | Entity sınıfı |
| 11 | `client/entity/CaravanRaiderRenderer.java` (yeni) | Renderer |
| 12 | `world/spawn/SpawnManager.java` | Raid spawn mantığı |
| 13 | `world/spawn/RaidRemnantManager.java` (yeni) | Kalıntı spawn |
| 14 | `block/RaidRemnantBlock.java` (yeni) | Block |
| 15 | `block/RaidRemnantBlockEntity.java` (yeni) | Lootable BE |

### Faz 4: AI Componentleri
| # | Dosya | İş |
|---|---|---|
| 16 | `core/world/VillageAI.java` (yeni) | Üretim optimizasyonu |
| 17 | `core/world/CaravanAI.java` | Kargo seçimi |
| 18 | `core/world/RouteGraph.java` | AI çağrıları |
| 19 | `data/village_profiles/*.json` | AI parametreleri |

### Faz 5: Pazar Bölgeleri (en son)
| # | Dosya | İş |
|---|---|---|
| 20 | `core/economy/MarketRegion.java` (yeni) | Veri modeli |
| 21 | `core/economy/MarketRegionManager.java` (yeni) | Yönetim |
| 22 | `player/MerchantScreenHandler.java` | Bölge seçici |
| 23 | `client/player/MerchantScreen.java` | Dropdown UI |

---

## 7. Toplam

| Tür | Sayı |
|---|---|
| Yeni Java dosyası | ~10 |
| Değişen dosya | ~12 |
| Yeni entity | 1 (CaravanRaiderEntity) |
| Yeni block | 1 (RaidRemnantBlock) |

---

## 8. Sorular

1. **Yağmacı görünümü**: Pillager modeli + custom texture kullanılsın mı? (Vanilla'da var, texture override yeterli)
2. **Kervan kalıntısı loot**: JSON data-driven mı olsun, hardcoded mı?
3. **Pazar bölgeleri**: Configurable olsun mu? (`village_profiles` JSON'ına `market_region` alanı)
4. **Köy baskınları**: Vanilla raid mekaniğini mi kullanalım yoksa tamamen custom mı?
5. **Faz sıralaması**: Faz 1 + 2'yi birlikte yapıp hemen başlayalım mı?
