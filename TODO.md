# Via Mercatorum — TODO

> Proje durumu: ~%87 tamam. 2026-06-08 itibarıyla.

---

## ✅ Tamamlanmış Sistemler

| Sistem | Durum |
|--------|--------|
| **SEK** — VillageNode, Caravan, TradeRoute, RouteGraph | Tam |
| **Ekonomi** — 30+ TradeGood, fiyat hesaplama, supply chain, Market Bölgeleri | Tam |
| **EconomyHelper** — GDP, refah, kriz, karlı mal tespiti | Tam |
| **Data-driven JSON** — 7 village + 4 road + 7 caravan profili | Tam |
| **JSON yükleyiciler** — Async PreparableReloadListener, Codec'ler | Tam |
| **A* RoadGenerator** — Perlin noise, eğim, çekim, fallback Bresenham | Tam |
| **RoadFeature** — Blok yerleştirme, chunk clipping, slab desteği | Tam |
| **Yol Bakım** — Degradation, maintenance level, trafik bazlı iyileşme | Tam |
| **VillageDetector** — StructureStart yakalama, profil uygulama | Tam |
| **VillageModifier** — Rota kurma, road cache, chunk road placement | Tam |
| **Entity** — CaravanEntity, CaravanGuardEntity, CaravanRaiderEntity, MerchantShipEntity, LoanSharkEntity | Tam |
| **Block** — VillageHallBlock + BlockEntity (MenuProvider), RaidRemnantBlock | Tam |
| **Oyuncu** — PlayerTradeManager (al/sat), MerchantScreenHandler, MerchantScreen | Tam |
| **ReputationSystem** — Global + village rep, event handlers | Tam |
| **SkillSystem** — 4 skill (trading, sailing, negotiation, exploration) | Tam |
| **MerchantMap** — Keşfedilen köyler + GDP + coastal flag + bölge bilgisi | Tam |
| **NewspaperSystem** — Gazete yayınlama, afet/haber metinleri | Tam |
| **SpawnManager** — Chunk load/unload'da entity spawn/despawn + raid entities | Tam |
| **Piracy** — TradeRoute piracy score, decay, risk premium | Tam |
| **SimulationTick** — Ayrı thread, 50ms, season/raid/caravan/road degradation tick | Tam |
| **CaravanAI** — Piracy-aware rota seçimi, kargo, konaklama | Tam |
| **VillageAI** — Üretim optimizasyonu, ihtiyaç analizi, rota önceliklendirme | Tam |
| **SEKEventBus** — Event-driven mimari | Tam |
| **SeasonManager** — Standalone + Serene Seasons entegrasyonu, mevsim hasat çarpanı, kıtlık/disaster | Tam |
| **MarketRegion** — Coğrafi bölgeleme, bölgesel fiyat hesaplama | Tam |
| **Deniz Ticareti** — RouteType.SEA, coastal village detection, sea routes, MerchantShipEntity | Tam |
| **Liman** — VillageNode.isCoastal, PortDetector, anchor ikonu haritada | Tam |
| **Config** — Piracy, economy, reputation, AI, raid, season, market region, sea trade, road maintenance | Tam |
| **Compat** — SereneSeasonsCompat + PufferfishCompat | Tam |
| **Tüketim/Demand** — ConsumptionProfile (5 JSON), VillageNode.tickConsumption(), starvation | Tam |
| **World Events** — 8 JSON event, EventEngine, EventTriggerResolver, ActiveModifier | Tam |
| **Atölye** — WorkshopDefinition + Loader (3 JSON), WorkshopInstance, WorkshopManager | Tam |
| **Askeri Güç** — MilitaryPower, MobilizationState, VillageNode.canMobilize/canDemobilize | Tam |
| **Köy Hedefleri** — VillageGoal (6 JSON), VillageGoalEngine, condition/action evaluation | Tam |
| **Üretim Ölçekleme** — Tool/raw material/food bonus, mobilizasyon cezaları | Tam |
| **Köy Salonu GUI** — 6 sekmeli panel (Ticaret, Envanter, Atölye, İstatistik, Askeri, Hedefler) | Tam |
| **Karakter Altyapısı** — Character (SEK), CharacterTypeRegistry (12 tür), CharacterManager, CharacterEntity | Tam |
| **Rename** — MarketStall → VillageHall (tüm referanslar) | Tam |

---

## 🐛 Buglar ve Eksikler (Öncelik Sırası)

### Kritik (Oyunu bozan)

| # | Bug | Dosya | Durum |
|---|-----|-------|-------|
| B1 | VillageHallScreenHandler client-side data sync bozuk — DataSlot[] sayısal veriyi taşır ama supplyEntries, tradeEntries, workshopEntries, goalEntries, militaryBreakdown listeleri client'a gönderilmiyor. Sadece STATISTICS sekmesi çalışır. | `VillageHallScreenHandler.java` | ❌ |
| B2 | VillageHallBlockEntity'ye villageId hiç set edilmiyor. VillageDetector köy bulduğunda block entity'ye yazmıyor. | `VillageDetector.java`, `VillageHallBlockEntity.java` | ❌ |
| B3 | CaravanEntity & CaravanGuardEntity createAttributes() eksik — ModEntities referans veriyor ama metot yok. Villager'dan miras aldığı için runtime'da düşebilir. | `CaravanEntity.java`, `CaravanGuardEntity.java` | ❌ |

### Önemli (Feature eksik/bozuk)

| # | Bug | Dosya | Durum |
|---|-----|-------|-------|
| B4 | 11 adet texture dosyası yok (7 entity + 1 GUI + 3 item) | `textures/` | ❌ |
| B5 | VillageHall blockstate/model/item model dosyaları yok, BlockItem yok | `resources/` | ❌ |
| B6 | DIRT ve GRAVEL yol hız efektleri eksik — RoadSpeedHandler sadece PAVED ve STONE kontrol ediyor | `RoadSpeedHandler.java`, `ViaMercatorumConfig.java` | ❌ |
| B7 | MerchantShipEntity spawn edilmiyor + AI'sız (boş registerGoals) | `SpawnManager.java`, `MerchantShipEntity.java` | ❌ |
| B8 | CharacterEntity spawn edilmiyor — SpawnManager'da spawn mantığı yok | `SpawnManager.java` | ❌ |
| B9 | CaravanGuardEntity variant texture değişmiyor (her zaman sword) | `CaravanGuardEntityRenderer.java` | ❌ |
| B10 | ViaMercatorumAPI.addReputation() boş stub | `ViaMercatorumAPI.java` | ❌ |
| B11 | ModRecipes.register() hiç çağrılmıyor + boş metod | `ModRecipes.java`, `ViaMercatorum.java` | ❌ |
| B12 | CaravanEntity "village_hall" translation key kullanıyor (caravan olmalı) | `CaravanEntity.java:55` | ❌ |

### Orta

| # | Bug | Dosya | Durum |
|---|-----|-------|-------|
| B13 | LoanSharkEntity despawn timer NBT'ye kaydedilmiyor | `LoanSharkEntity.java` | ❌ |
| B14 | SEKSyncManager hiç kullanılmıyor (dead code) | `SEKSyncManager.java` | ❌ |
| B15 | Mixin compatibilityLevel JAVA_21 (JDK 25 olmalı) | `viamercatorum.mixins.json` | ❌ |
| B16 | Config dosyası JSON ama AGENTS.md'de TOML yazıyor | `ViaMercatorumConfig.java`, `AGENTS.md` | ❌ |

### Minor

| # | Bug | Dosya | Durum |
|---|-----|-------|-------|
| B17 | EKLENECEKLER.md 3 entity texture + 3 item texture eksik listelemiyor | `EKLENECEKLER.md` | ❌ |
| B18 | Creative tab / spawn egg yok | `ModItems.java`, `ModBlocks.java` | ❌ |
| B19 | Loot table yok | `resources/` | ❌ |
| B20 | MerchantScreen ile VillageHallScreen aynı texture'ı kullanıyor | `MerchantScreen.java:18` | ❌ |

---

## 🚀 Yapılması Gerekenler (Öncelik Sırası)

### 1. S2C Paket Altyapısı (B1 fix + gelecek ihtiyacı)

Fabric Networking API ile server→client paket sistemi. VillageHallScreenHandler listeleri için gerekli.

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 1.1 | `network/ModPackets.java` | Payload sınıfları + register | ❌ |
| 1.2 | `network/VillageHallDataPayload.java` | Köy verisi S2C payload | ❌ |
| 1.3 | `VillageHallScreenHandler.java` | refreshData() sonrası S2C paket gönder | ❌ |
| 1.4 | `client/ViaMercatorumClient.java` | Client receiver kaydı | ❌ |

### 2. VillageHall villageId Set (B2 fix)

VillageDetector köy bulduğunda block entity'ye villageId yazmalı.

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 2.1 | `world/worldgen/VillageDetector.java` veya `VillageModifier.java` | Köy tespitinde VillageHallBlockEntity.setVillageId() | ❌ |

### 3. Entity Attribute Fix (B3 fix)

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 3.1 | `CaravanEntity.java` | createAttributes() ekle (Villager.createAttributes() delegate) | ❌ |
| 3.2 | `CaravanGuardEntity.java` | createAttributes() ekle | ❌ |

### 4. VillageHall Block Assets (B5 fix)

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 4.1 | `blockstates/village_hall.json` | Blockstate tanımı | ❌ |
| 4.2 | `models/block/village_hall.json` | Blok modeli | ❌ |
| 4.3 | `models/item/village_hall.json` | Item modeli | ❌ |
| 4.4 | `ModItems.java` | BlockItem kaydı | ❌ |

### 5. Yol Hız Efektleri — DIRT/GRAVEL (B6 fix)

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 5.1 | `RoadSpeedHandler.java` | DIRT/GRAVEL blok setleri + Speed I ekle | ❌ |
| 5.2 | `ViaMercatorumConfig.java` | dirtSpeedLevel, gravelSpeedLevel config ekle | ❌ |

### 6. GDP Gösterimi VillageHall GUI'de

EconomyHelper.gdp() ve gdpPerCapita() zaten mevcut. STATISTICS sekmesine eklenecek.

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 6.1 | `VillageHallScreenHandler.java` | DATA_GDP ve DATA_GDP_PER_CAPITA DataSlot ekle | ❌ |
| 6.2 | `VillageHallScreen.java` | STATISTICS sekmesinde GDP gösterimi | ❌ |

### 7. Dialog/Conversation Sistemi

JSON-based diyalog ağaçları. RPG NPC sistemi. BYOK AI desteği için genişletilebilir.

| # | Dosya | İş | Durum |
|---|-------|-----|-------|
| 7.1 | `data/DialogNode.java` | Record + Codec (id, text, choices, nextId, action) | ❌ |
| 7.2 | `data/DialogTree.java` | Record + Codec (treeId, startingNode, characters) | ❌ |
| 7.3 | `data/DialogTreeLoader.java` | PreparableReloadListener | ❌ |
| 7.4 | `core/simulation/DialogManager.java` | Aktif diyalog takibi, seçim işleme | ❌ |
| 7.5 | `network/DialogS2CPayload.java` | Server→client diyalog paketi | ❌ |
| 7.6 | `network/DialogC2SPayload.java` | Client→server diyalog seçimi paketi | ❌ |
| 7.7 | `client/player/DialogScreen.java` | RPG diyalog ekranı (portrait, metin, seçenekler) | ❌ |
| 7.8 | `player/DialogScreenHandler.java` | Diyalog screen handler | ❌ |
| 7.9 | `entity/CharacterEntity.java` | right-click → diyalog aç | ❌ |
| 7.10 | `VillageHallScreen.java` | Muhtarla konuş butonu | ❌ |
| 7.11 | JSON diyalog dosyaları | `data/viamercatorum/dialogs/` altında örnekler | ❌ |
| 7.12 | `player/ModScreenHandlers.java` | DIALOG MenuType kaydı | ❌ |
| 7.13 | Lang dosyaları | Diyalog UI çevirileri (en_us + tr_tr) | ❌ |

### 8. Diğer Bugler

| # | Bug | Durum |
|---|-----|-------|
| B7 | MerchantShipEntity spawn + AI | ❌ |
| B8 | CharacterEntity spawn mantığı | ❌ |
| B9 | CaravanGuardEntity variant texture | ❌ |
| B10 | ViaMercatorumAPI.addReputation() impl | ❌ |
| B11 | ModRecipes register + crafting recipes | ❌ |
| B12 | CaravanEntity translation key fix | ❌ |
| B13 | LoanSharkEntity NBT despawn timer | ❌ |

### 9. Texture'lar

| # | Dosya | Boyut | Durum |
|---|-------|-------|-------|
| T1 | `gui/village_hall.png` | 256×256 | ❌ |
| T2 | `entity/caravan_trader.png` | 64×64 | ❌ |
| T3 | `entity/caravan_guard_sword.png` | 64×64 | ❌ |
| T4 | `entity/caravan_guard_bow.png` | 64×64 | ❌ |
| T5 | `entity/character.png` | 64×64 | ❌ |
| T6 | `entity/loan_shark.png` | 64×64 | ❌ |
| T7 | `entity/merchant_ship.png` | 64×64 | ❌ |
| T8 | `entity/caravan_raider.png` | 64×64 | ❌ |
| T9 | `item/newspaper.png` | 16×16 | ❌ |
| T10 | `item/merchant_map.png` | 16×16 | ❌ |
| T11 | `item/debt_note.png` | 16×16 | ❌ |

### 10. Testler

| # | İş | Durum |
|---|-----|-------|
| 10.1 | core/ paketi için JUnit 5 unit testleri | ❌ |
| 10.2 | EconomyHelper, PriceCalculator testleri | ❌ |
| 10.3 | VillageGoalEngine condition evaluation testleri | ❌ |