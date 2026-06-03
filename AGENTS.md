## Commands

```bash
./gradlew build          # full build (compile + resources + jar)
./gradlew compileJava    # fast compile-only check
# no tests exist yet — do not run a test command
```

## Setup

- **JDK 25** required (set in `gradle.properties` → `org.gradle.java.home`). Minecraft 26.1.2 classes need class version 69.
- **Mojang official mappings only** — never use Yarn names. Package/class differences from Yarn: `ServerLevel` (not `ServerWorld`), `ChunkAccess` (not `Chunk`), `Identifier.fromNamespaceAndPath()`, `ResourceKey.identifier()`, `Holder.unwrapKey()`, `Registries.STRUCTURE` (not `RegistryKeys.STRUCTURE`).
- Fabric Loom 1.16, Fabric API 0.150.0+26.1.2, Fabric Loader 0.19.2.

## Architecture

Mod ID: `viamercatorum`. Entrypoints:
- Main: `com.viamercatorum.ViaMercatorum` (implements `ModInitializer`)
- Client: `com.viamercatorum.client.ViaMercatorumClient` (separate `src/client/` source set)

### Core layers (src/main/java/com/viamercatorum/)

| Package | Purpose |
|---------|---------|
| `core/` | Minecraft-independent economy simulation (SEK) |
| `core/economy/` | TradeGood, Supply, Market, PriceCalculator |
| `core/simulation/` | SEKEventBus, SimulationTick (separate thread) |
| `core/world/` | RouteGraph, VillageNode, TradeRoute, Caravan |
| `data/` | JSON-loadable profiles: VillageProfile, RoadProfile + loaders |
| `world/worldgen/` | VillageDetector, VillageModifier, RoadGenerator (A*), RoadFeature |
| `world/spawn/` | SpawnManager (chunk-based entity tracking) |
| `world/sync/` | SEKSyncManager (SEK ↔ physical world bridge) |
| `player/` | ReputationSystem, SkillSystem, MerchantMap, NewspaperSystem |
| `mixin/` | StructurePoolAccessor (only mixin, registered in `viamercatorum.mixins.json`) |
| `compat/` | SereneSeasonsCompat, PufferfishCompat |
| `api/` | ViaMercatorumAPI (public API for other mods) |

### Data-driven JSON under `src/main/resources/data/viamercatorum/`

- `village_profiles/<biome>_village.json` — 7 profiles (plains, desert, savanna, taiga, snowy, jungle, swamp)
- `road_profiles/<quality>.json` — 4 tiers (dirt → gravel → stone → paved), auto-upgrade via `next_quality`
- Loaded asynchronously via `PreparableReloadListener` (vanilla pattern, not Fabric synchronous)
- Registered with `ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener()`

### Worldgen integration

- `ServerChunkEvents.CHUNK_GENERATE` in `ViaMercatorum.java` detects villages + places roads
- `ChunkPos` fields (`x`, `z`) are **private** in 26.1.2; access via `pos.getMiddleBlockX() >> 4` to get chunk coords
- `ChunkStatus` package: `net.minecraft.world.level.chunk.status.ChunkStatus` (moved in 1.21.5)
- `LevelChunk` has no `getStatus()` method in 26.1.2
- Roads use A* pathfinding with Perlin noise, slope cost, and village attractors — **not** straight Bresenham lines
- `RoadGenerator` operates on chunk coordinates, `RoadFeature` places blocks, `placeRoadSegmentInChunk()` clips to a single chunk

### Village detection (VillageDetector)

Called on `onStructureStart(ServerLevel, StructureStart)`. Deduplicates by generating a village ID from structure center chunk coords (`"village_" + cx + "_" + cz`) and checking `routeGraph.getVillage(id)`.

### SEK simulation

- `SimulationTick` runs on a separate thread (50ms interval)
- Village nodes produce goods, caravans move along routes, markets update prices
- Physical world entities spawn/despawn via `SpawnManager` when player chunks overlap

## Conventions

- All strings use **Turkish comments** (when present) and English identifiers
- `TradeGoodRegistry` enumerates ~30 goods (pre-registered init-time)
- A* road cache in `VillageModifier.roadCache` prevents recomputation per chunk
- Event-driven via `SEKEventBus`; systems are loosely coupled
- Road qualities: `RouteQuality.DIRT / GRAVEL / STONE / PAVED`
- Village types: `VillageNode.VillageType.HAMLET / VILLAGE / TOWN / CITY / FORTIFIED`

## References

- `hayalimdeki_plan.md` — original design document (Turkish, aspirational)
- `yol_olusturma_hakkinda.md` — road generation ideas (A*, noise, cost map)
- `MIMARI.md` — full architecture document
