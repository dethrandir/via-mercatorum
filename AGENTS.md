## Commands

```bash
./gradlew build          # full build (compile + resources + jar)
./gradlew compileJava    # fast compile-only check
# no tests exist yet — do not run a test command
```

## Setup

- **JDK 25** (set `org.gradle.java.home` in `gradle.properties`). Minecraft 26.1.2 needs class version 69.
- **Mojang official mappings only** — never use Yarn names. Key differences: `ServerLevel` (not `ServerWorld`), `ChunkAccess` (not `Chunk`), `Identifier.fromNamespaceAndPath()`, `ResourceKey.identifier()`, `Holder.unwrapKey()`, `Registries.STRUCTURE` (not `RegistryKeys.STRUCTURE`).
- Fabric Loom 1.16, Fabric API 0.150.0+26.1.2, Fabric Loader 0.19.2.

## Architecture

Mod ID: `viamercatorum`. Entrypoints:
- Main: `com.viamercatorum.ViaMercatorum` (implements `ModInitializer`)
- Client: `com.viamercatorum.client.ViaMercatorumClient` (separate `src/client/` source set, split by `loom.splitEnvironmentSourceSets()`)

### Package map (`src/main/java/com/viamercatorum/`)

| Package | Key contents |
|---------|-------------|
| `core/` | Minecraft-independent economy simulation (SEK) — no Minecraft imports |
| `core/economy/` | TradeGood, TradeGoodRegistry, Market, MarketRegion, PriceCalculator, Supply, BankManager, BankAccount, EconomyHelper, GoodItemMapping |
| `core/simulation/` | SEKEventBus, SimulationTick (50ms separate thread), EventEngine, EventEffectResolver, EventTriggerResolver, Character/CharacterManager/CharacterTypeRegistry, SeasonManager (standalone + SereneSeasons compat), VillageGoalEngine, ActiveModifierManager, SEKStandalone (test harness) |
| `core/world/` | RouteGraph, VillageNode (5 types), TradeRoute, Caravan, CaravanAI, VillageAI, WorkshopManager, WorkshopInstance, MilitaryPower |
| `data/` | JSON-loadable profiles + async `PreparableReloadListener` loaders. Types: VillageProfile, RoadProfile, CaravanProfile, ConsumptionProfile, WorldEventProfile, WorkshopDefinition, VillageGoal |
| `world/worldgen/` | VillageDetector (structure start detection), VillageModifier (rotas + A* road cache + chunk placement), RoadGenerator (A* + Perlin noise + slope), RoadFeature (block placement, chunk clipping) |
| `world/spawn/` | SpawnManager (chunk load/unload entity spawn/despawn), PortDetector (coastal detection), RaidRemnantManager, RaidEventCache |
| `world/sync/` | SEKSyncManager (SEK ↔ physical world), RoadSpeedHandler (speed effects on roads) |
| `player/` | ReputationSystem, SkillSystem (4 skills), MerchantMap, PlayerTradeManager, NewspaperSystem, MerchantScreenHandler, VillageHallScreenHandler + inventory classes (ModItems, ModRecipes, ModScreenHandlers) |
| `entity/` | CaravanEntity, CaravanGuardEntity, CaravanRaiderEntity, MerchantShipEntity, LoanSharkEntity, CharacterEntity, plus registries (ModEntities, ModProfessions, ModPOIs, CaravanComposition) |
| `block/` | ModBlocks, ModBlockEntities, VillageHallBlock (+ BE), RaidRemnantBlock (+ BE) |
| `command/` | ViaMercatorumCommand (`/viamercatorum`) |
| `mixin/` | `StructurePoolAccessor` only — registered in `viamercatorum.mixins.json` |
| `compat/` | SereneSeasonsCompat, PufferfishCompat |
| `api/` | ViaMercatorumAPI (public API for other mods) |
| `core/config/` | ViaMercatorumConfig (TOML-loaded from `config/viamercatorum.toml`) |

### Data-driven JSON (`src/main/resources/data/viamercatorum/`)

- `village_profiles/<biome>_village.json` — 7 biomes (plains, desert, savanna, taiga, snowy, jungle, swamp)
- `road_profiles/<quality>.json` — 4 tiers (dirt → gravel → stone → paved) with `next_quality` upgrade chain
- `caravan_profiles/<biome>_caravan.json` — 7 biome-specific caravan compositions
- `consumption_profiles/<type>_consumption.json` — 5 village types (hamlet, village, town, city, fortified)
- `workshop_definitions/<name>.json` — 3 workshops (carpenter, bakery, smithy)
- `world_events/<name>.json` — 8 world events (famine, war, harvest_festival, gold_rush, bandit_camp, trade_boom, drought, plague)
- `village_goals/<name>.json` — 6 goals (industrialization, road_security, population_growth, trade_expansion, military_readiness, food_security)
- All loaded async via `PreparableReloadListener` (vanilla pattern, not Fabric synchronous), registered with `ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener()`

### Worldgen integration

- `ServerChunkEvents.CHUNK_GENERATE` in `ViaMercatorum.java` detects villages + places roads
- `ChunkPos` fields (`x`, `z`) are **private** in 26.1.2; access via `pos.getMiddleBlockX() >> 4` to get chunk coords
- `ChunkStatus` package: `net.minecraft.world.level.chunk.status.ChunkStatus` (moved in 1.21.5)
- `LevelChunk` has no `getStatus()` method in 26.1.2
- Roads use A* pathfinding with Perlin noise, slope cost, and village attractors — not straight Bresenham lines (fallback available)
- A* road cache in `VillageModifier.roadCache` prevents recomputation per chunk
- `RoadGenerator` operates on chunk coordinates; `RoadFeature.placeRoadSegmentInChunk()` clips to a single chunk
- Road degradation + traffic-based auto-upgrade supported
- Sea routes (`RouteType.SEA`) enabled; coastal villages detected via `PortDetector`

### SEK simulation

- `SimulationTick` runs on a separate thread (50ms interval)
- Village nodes produce goods, caravans move along routes (with piracy-aware routing), markets update prices
- Event engine (`EventEngine`) resolves world events that modify production/consumption temporarily
- `SeasonManager` affects harvest and can trigger disasters (drought, famine) independently or via SereneSeasons
- Physical world entities spawn/despawn via `SpawnManager` when player chunks overlap

### Village detection (`VillageDetector`)

Called on `onStructureStart(ServerLevel, StructureStart)`. Deduplicates by generating a village ID from structure center chunk coords (`"village_" + cx + "_" + cz`) and checking `routeGraph.getVillage(id)`.

## Conventions

- Turkish comments, English identifiers
- `TradeGoodRegistry` enumerates ~30 goods pre-registered at init-time
- Event-driven via `SEKEventBus`; systems are loosely coupled
- `RouteQuality.DIRT / GRAVEL / STONE / PAVED`
- `VillageNode.VillageType.HAMLET / VILLAGE / TOWN / CITY / FORTIFIED`
- Road qualities (speed multiplier, block type, width) defined in JSON
- `core/` package has zero Minecraft imports — testable standalone

## Git

Her en azindan kucuk-orta degisiklikten sonra commit atilsin. Commit mesajlari convention:
- `fix:` — bug fix or crash resolution
- `feat:` — new feature
- `chore:` — cleanup, wiring, integration, misc

Ayni commit'e cok farkli alanlardan degisiklik koyma; mantiksal gruplara bol.

## References

- `MIMARI.md` — full architecture document (Turkish)
- `hayalimdeki_plan.md` — original design document (Turkish, aspirational)
- `yol_olusturma_hakkinda.md` — road generation ideas (A*, noise, cost map)
- `TODO.md` — ~85% complete status, organized by completed/pending
