# Via Mercatorum — Mimari Dokümanı

> *"Ticaret yolları canlanıyor..."*

## 1. Proje Vizyonu

**Via Mercatorum**, Minecraft'a derin bir ekonomi, lojistik ve ticaret simülasyonu ekleyen bir Fabric modudur. Dünya generate edilirken köyler arasında ticaret rotaları oluşur, kervanlar yollarda hareket eder, her köyün uzmanlaştığı üretim kalemleri vardır ve oyuncu bu ekonominin içinde alım-satım, kervan koruma, itibar yönetimi gibi rolleri üstlenir.

### Temel Prensipler

- **Sanal Ekonomi Katmanı (SEK):** Minecraft chunk'larından bağımsız, sürekli çalışan bir simülasyon. Fiziksel dünya sadece oyuncunun yakınındayken spawn edilir.
- **Data-Driven:** Köy profilleri, yol kaliteleri, trade good'ları JSON ile tanımlanır. Yeni biyom, ürün veya yol tipi eklemek kod yazmayı gerektirmez.
- **Event-Driven:** SEKEventBus ile sistemler birbirinden habersiz çalışır. Yeni özellik eklemek mevcut koda dokunmaz.
- **Modüler Monolith:** Tek mod ama içi katmanlı. Core hiçbir Minecraft import'u içermez, test edilebilir.

---

## 2. Dizin Yapısı

```
src/main/java/com/viamercatorum/
├── ViaMercatorum.java              # Ana mod giriş noktası
├── api/
│   └── ViaMercatorumAPI.java       # Diğer modlar için public API
├── core/                           # SEK çekirdeği (Minecraft bağımsız)
│   ├── economy/
│   │   ├── Market.java             # Global arz/talep yönetimi
│   │   ├── PriceCalculator.java    # Fiyat hesaplama (elastikiyet, itibar)
│   │   ├── Supply.java             # Stok, üretim hızı, kapasite
│   │   ├── TradeGood.java          # Trade good tanımı (id, isim, kategori)
│   │   └── TradeGoodRegistry.java  # Tüm trade good'larının kaydı
│   ├── simulation/
│   │   ├── EconomyEvent.java       # Olay tipleri (CaravanArrived, PriceChanged, vs)
│   │   ├── SEKEventBus.java        # Event bus (register/emit)
│   │   └── SimulationTick.java     # Ayrı thread'de çalışan tick sistemi
│   └── world/
│       ├── Caravan.java            # Kervan (id, rota, yük, ilerleme)
│       ├── RouteGraph.java         # Köyler + rotalar + kervanlar grafiği
│       ├── TradeRoute.java         # İki köy arası ticaret rotası
│       └── VillageNode.java        # Köy düğümü (üretim, nüfus, itibar)
├── data/                           # Data-driven JSON yükleyicileri
│   ├── RoadProfile.java            # Yol kalitesi + blok eşleme (Codec)
│   ├── RoadProfileLoader.java      # road_profiles/ JSON yükleyici
│   ├── VillageProfile.java         # Köy profili (Codec)
│   └── VillageProfileLoader.java   # village_profiles/ JSON yükleyici
├── mixin/
│   └── StructurePoolAccessor.java  # Village pool'larına erişim
├── player/
│   ├── MerchantMap.java            # Merchant's World Map (keşif + olaylar)
│   ├── NewspaperSystem.java        # Gazete sistemi (bölgesel haberler)
│   ├── ReputationSystem.java       # İtibar (global + köy bazlı)
│   └── SkillSystem.java            # Yetenekler (ticaret, denizcilik, vs)
├── world/
│   ├── spawn/
│   │   └── SpawnManager.java       # Chunk bazlı spawn/despawn yöneticisi
│   ├── sync/
│   │   └── SEKSyncManager.java     # SEK ←→ fiziksel dünya senkronizasyonu
│   └── worldgen/
│       ├── RoadFeature.java        # Fiziksel yol bloklarını yerleştirir
│       ├── RoadGenerator.java      # A* pathfinding + terrain cost + noise
│       ├── VillageDetector.java    # Worldgen'de köyleri tespit eder
│       └── VillageModifier.java    # Köyleri SEK'e kaydeder, rotaları kurar
├── compat/
│   ├── PufferfishCompat.java       # Pufferfish Skills entegrasyonu
│   └── SereneSeasonsCompat.java    # Serene Seasons entegrasyonu

src/main/resources/
├── fabric.mod.json
├── viamercatorum.mixins.json
├── assets/viamercatorum/icon.png
└── data/viamercatorum/
    ├── village_profiles/           # 7 biyom profili (JSON)
    │   ├── plains_village.json
    │   ├── desert_village.json
    │   ├── savanna_village.json
    │   ├── taiga_village.json
    │   ├── snowy_village.json
    │   ├── jungle_village.json
    │   └── swamp_village.json
    └── road_profiles/              # 4 yol kalitesi (JSON)
        ├── dirt.json
        ├── gravel.json
        ├── stone.json
        └── paved.json
```

---

## 3. Sanal Ekonomi Katmanı (SEK)

### 3.1 Mimarisi

```
[SEK — Her zaman çalışır, ayrı thread]
  ├── VillageNode'lar (üretim, stok, nüfus, tip)
  ├── TradeRoute'lar (kalite, trafik, hız çarpanı)
  ├── Caravan'lar (id, rota, yük, ilerleme yüzdesi)
  ├── Market (global arz/talep)
  └── SEKEventBus (olaylar)

[Fiziksel Dünya — Sadece yüklü chunk'larda]
  └── SEK'teki varlık yakınsa → spawn et
      SEK'teki varlık geçtiyse → despawn et
```

### 3.2 Tick Döngüsü (50ms aralıklarla)

1. `Market.tickAll()` → tüm global stokları güncelle
2. `VillageNode.tickProduction()` → her köy kendi üretimini yapsın
3. `routeGraph.tickAllCaravans()` → kervanları ilerlet
4. Her 20 tick'te → varan kervanları işle (stok ekle, trafik arttır, event fırlat)
5. Her 1200 tick'te → yeni kervan spawn et

### 3.3 Fiyat Hesaplama

```
fiyat = basePrice × (1.0 + 0.5 × (0.5 - stock/demand))
alış_fiyatı = fiyat × itibar_indirimi
satış_fiyatı = fiyat × 0.7 × itibar_primi

egzotik_fiyat = basePrice × (1.0 + mesafe × egzotik_katsayı)
```

---

## 4. Village Profilleri (Data-Driven)

### 4.1 Biyom → Uzmanlık Tablosu

| Biyom | Ana Uzmanlık | Üretim | İthal Ettiği |
|-------|-------------|--------|-------------|
| Plains | Buğday, havuç, patates | 12/8/8 birim/tick | Demir, silah |
| Desert | Altın, lapis, zümrüt | 3/4/1.5 birim/tick | Buğday, deri |
| Savanna | Deri, sığır eti | 6/5 birim/tick | Demir, silah |
| Taiga | Ladin kütüğü, kömür, balık | 15/5/4 birim/tick | Buğday, deri |
| Snowy | Demir, kömür, zümrüt | 5/6/1 birim/tick | Buğday, deri |
| Jungle | Kabak, kavun, kitap | 6/7/2 birim/tick | Demir, deri |
| Swamp | Redstone, balık, altın | 4/4/1 birim/tick | Buğday, demir |

### 4.2 Temel Üretim (Her Köyde Ortak)

Her köy, uzmanlığının yanında şu temel ihtiyaçları az miktarda üretir:
- Taş, odun, kömür, ekmek, demir (düşük oranlarda)
- Böylece ekonomi tamamen dışa bağımlı olmaz

### 4.3 Köy Tipleri

| Tip | Parça Sayısı | Açıklama |
|-----|-------------|----------|
| HAMLET | <5 parça | Küçük yerleşim, 2-3 bina |
| VILLAGE | 5-9 parça | Normal vanilla köyü |
| TOWN | 10-19 parça | Büyük kasaba |
| CITY | 20+ parça | Şehir |

---

## 5. Yol Sistemi

### 5.1 Yol Generation (A* Pathfinding)

Bresenham düz çizgi yerine A* algoritması kullanılır:

```
maliyet = taban_maliyet × eğim_çarpanı × noise_çarpanı - çekim_indirimi

eğim_çarpanı = 1.0 + yükseklik_farkı² × 2.0
  → Düz alanlar tercih edilir, yüksek eğimlerden kaçınılır

noise_çarpanı = 1.0 + (perlin_noise - 0.5) × 0.3
  → Yola organik kıvrımlar ekler

çekim_indirimi = (1.0 - mesafe/10) × 0.4  (mesafe < 10 chunk ise)
  → Yol yakındaki köylere hafifçe sapar
```

### 5.2 Yol Kalitesi ve Evrimi

| Kalite | Hız Çarpanı | Trafik Eşiği | Yüzey Bloğu | Genişlik |
|--------|------------|-------------|------------|----------|
| DIRT | 1.0× | 0 | Grass Path | 1 |
| GRAVEL | 1.3× | 50 | Gravel | 1 |
| STONE | 1.6× | 150 | Cobblestone | 2 |
| PAVED | 2.0× | 300 | Stone Bricks | 2 |

Yollar trafik arttıkça kendiliğinden iyileşir. Tüm yol profilleri JSON'da tanımlıdır.

---

## 6. Worldgen Entegrasyonu

### 6.1 Village Tespiti

`ServerChunkEvents.CHUNK_GENERATE` olayı dinlenir:
- Her chunk generate olduğunda yapıları kontrol et
- Village bulunursa biyomunu tespit et
- Uygun profil varsa uygula, yoksa plains varsayılanını kullan
- Köyü SEK RouteGraph'ine kaydet
- Yakındaki köylerle ticaret rotası oluştur

### 6.2 Fiziksel Yollar

Chunk generate olurken SEK'te var olan rotalar kontrol edilir:
- Rota o chunk'tan geçiyorsa fiziksel blokları yerleştir
- Yol kalitesine göre blok seçimi (JSON'dan okunur)
- Yol genişliği kaliteye göre artar

### 6.3 Village Pool'larına Bina Ekleme

`ServerLifecycleEvents.SERVER_STARTING` ile:
- Her biyomun village pool'una özel binalar (pazar yeri, kervan durağı) eklenir
- `StructurePoolAccessor` mixin ile pool'lara erişilir

---

## 7. Oyuncu Sistemleri

### 7.1 İtibar (Reputation)

- Global itibar: Tüm dünyada geçerli
- Köy bazlı itibar: Her köy için ayrı
- Kervan yağmalama → itibar kaybı
- Haydut temizleme, ticaret → itibar kazancı
- Yüksek itibar → alışverişte indirim

### 7.2 Yetenekler (Skills)

- **Ticaret:** Daha iyi fiyatlar
- **Denizcilik:** Deniz ticareti bonusları
- **Müzakere:** İtibar kazanım hızı
- **Keşif:** Yeni köy bulma menzili

### 7.3 Merchant's World Map

Oyuncunun keşfettiği köyleri, üretim sembollerini, ticaret rotalarını ve güncel olayları gösterir.

### 7.4 Gazete Sistemi

- **Dedikodu (lokal):** Tavernalardan, gecikmeli ve bazen yanlış
- **Gazete (bölgesel):** Büyük kasabalarda basılır, daha güvenilir
- İçerik: Kıtlık, yağma, ticaret fuarı, salgın haberleri

---

## 8. Data-Driven Yapılandırma

### 8.1 Dosya Yolları

```
data/viamercatorum/
├── village_profiles/<biome>_village.json    # Köy profilleri
├── road_profiles/<quality>.json             # Yol kaliteleri
└── trade_goods/                             # (gelecekte) özel trade good'lar
```

### 8.2 Profil Eklemek

Yeni bir biyom için köy profili eklemek:
1. `data/viamercatorum/village_profiles/` altına JSON dosyası oluştur
2. Kod yazmaya gerek yok, reload komutu ile yüklenir

Yeni yol kalitesi eklemek:
1. `data/viamercatorum/road_profiles/` altına JSON dosyası oluştur
2. `next_quality` alanı ile kalite zincirini belirle

---

## 9. Gelecek Planlar

### Kısa Vade
- Kervan NPC entity'si (fiziksel dünyada görünür kervanlar)
- Tüccar meslekleri (villager profession)
- Merchant's World Map item'i
- Gazete item'i
- `/viamercatorum` komutları
- Haydut/illager ekonomisi

### Orta Vade
- Deniz ticareti ve tüccar gemileri
- Mevsim/hasat sistemi (Serene Seasons entegrasyonu)
- Yetenek sistemi (Pufferfish Skills entegrasyonu veya standalone)
- Kredi/borç sistemi (banker NPC)
- Yol bakım mekaniği (bakımsız yollar bozulur)

### Uzun Vade
- Dedikodu ağı ve ajan sistemi
- Ticaret fuarları ve özel etkinlikler
- Kendi kervanını/köyünü kurma
- Çok oyunculu senkronizasyon iyileştirmeleri
- Performans optimizasyonu (1000+ köy, 5000+ kervan)

---

## 10. Teknik Detaylar

### Bağımlılıklar

| Kütüphane | Tür | Amaç |
|-----------|-----|------|
| Fabric API | Zorunlu | Event system, resource loading |
| Serene Seasons | Opsiyonel | Mevsim verisi |
| Pufferfish Skills | Opsiyonel | Yetenek entegrasyonu |

### Derleme

```
Java 25+ (JDK)
Minecraft 26.1.2 (1.21.5 snapshot)
Fabric Loom 1.16+
```

```bash
./gradlew build
```

### Performans Notları

- SEK ayrı bir thread'de çalışır, main thread'i bloklamaz
- Fiziksel spawn sadece oyuncunun yüklü chunk'larında olur
- Event bus ile sistemler gevşek bağlıdır
- JSON yüklemeleri asenkron prepareExecutor'da yapılır
