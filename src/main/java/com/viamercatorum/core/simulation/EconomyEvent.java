package com.viamercatorum.core.simulation;

import com.viamercatorum.core.world.VillageNode;

import java.util.Map;

public abstract class EconomyEvent {
    private final String villageId;
    private final long gameTime;

    protected EconomyEvent(String villageId, long gameTime) {
        this.villageId = villageId;
        this.gameTime = gameTime;
    }

    public String getVillageId() { return villageId; }
    public long getGameTime() { return gameTime; }

    public static class CaravanArrived extends EconomyEvent {
        private final String caravanId;
        private final String originId;
        private final String destinationId;

        public CaravanArrived(String villageId, long gameTime, String caravanId, String originId, String destinationId) {
            super(villageId, gameTime);
            this.caravanId = caravanId;
            this.originId = originId;
            this.destinationId = destinationId;
        }

        public String getCaravanId() { return caravanId; }
        public String getOriginId() { return originId; }
        public String getDestinationId() { return destinationId; }
    }

    public static class CaravanDeparted extends EconomyEvent {
        private final String caravanId;
        private final String destinationId;

        public CaravanDeparted(String villageId, long gameTime, String caravanId, String destinationId) {
            super(villageId, gameTime);
            this.caravanId = caravanId;
            this.destinationId = destinationId;
        }

        public String getCaravanId() { return caravanId; }
        public String getDestinationId() { return destinationId; }
    }

    public static class PriceChanged extends EconomyEvent {
        private final String goodId;
        private final double oldPrice;
        private final double newPrice;

        public PriceChanged(String villageId, long gameTime, String goodId, double oldPrice, double newPrice) {
            super(villageId, gameTime);
            this.goodId = goodId;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
        }

        public String getGoodId() { return goodId; }
        public double getOldPrice() { return oldPrice; }
        public double getNewPrice() { return newPrice; }
    }

    public static class DisasterEvent extends EconomyEvent {
        private final String disasterType;
        private final int severity;

        public DisasterEvent(String villageId, long gameTime, String disasterType, int severity) {
            super(villageId, gameTime);
            this.disasterType = disasterType;
            this.severity = severity;
        }

        public String getDisasterType() { return disasterType; }
        public int getSeverity() { return severity; }
    }

    public static class TradeCompleted extends EconomyEvent {
        private final String goodId;
        private final double amount;
        private final double price;

        public TradeCompleted(String villageId, long gameTime, String goodId, double amount, double price) {
            super(villageId, gameTime);
            this.goodId = goodId;
            this.amount = amount;
            this.price = price;
        }

        public String getGoodId() { return goodId; }
        public double getAmount() { return amount; }
        public double getPrice() { return price; }
    }

    public static class RaidOccurred extends EconomyEvent {
        private final String routeId;
        private final String caravanId;
        private final String targetVillageId;
        private final boolean isCaravanRaid;
        private final Map<String, Double> lootedGoods;
        private final int chunkX;
        private final int chunkZ;
        private final int raiderCount;
        private final boolean raidersWon;

        public RaidOccurred(String villageId, long gameTime, String routeId, String caravanId,
                            String targetVillageId, boolean isCaravanRaid,
                            Map<String, Double> lootedGoods, int chunkX, int chunkZ,
                            int raiderCount, boolean raidersWon) {
            super(villageId, gameTime);
            this.routeId = routeId;
            this.caravanId = caravanId;
            this.targetVillageId = targetVillageId;
            this.isCaravanRaid = isCaravanRaid;
            this.lootedGoods = lootedGoods != null ? lootedGoods : Map.of();
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.raiderCount = raiderCount;
            this.raidersWon = raidersWon;
        }

        public String getRouteId() { return routeId; }
        public String getCaravanId() { return caravanId; }
        public String getTargetVillageId() { return targetVillageId; }
        public boolean isCaravanRaid() { return isCaravanRaid; }
        public Map<String, Double> getLootedGoods() { return lootedGoods; }
        public int getChunkX() { return chunkX; }
        public int getChunkZ() { return chunkZ; }
        public int getRaiderCount() { return raiderCount; }
        public boolean didRaidersWin() { return raidersWon; }
    }

    public static class BanditsCleared extends EconomyEvent {
        private final String routeId;
        private final int banditCount;

        public BanditsCleared(String villageId, long gameTime, String routeId, int banditCount) {
            super(villageId, gameTime);
            this.routeId = routeId;
            this.banditCount = banditCount;
        }

        public String getRouteId() { return routeId; }
        public int getBanditCount() { return banditCount; }
    }

    public static class SeasonChanged extends EconomyEvent {
        private final String seasonName;

        public SeasonChanged(String villageId, long gameTime, String seasonName) {
            super(villageId, gameTime);
            this.seasonName = seasonName;
        }

        public String getSeasonName() { return seasonName; }
    }

    public static class RoadDegraded extends EconomyEvent {
        private final String routeId;
        private final String oldQuality;
        private final String newQuality;
        private final double maintenanceLevel;

        public RoadDegraded(String villageId, long gameTime, String routeId,
                            String oldQuality, String newQuality, double maintenanceLevel) {
            super(villageId, gameTime);
            this.routeId = routeId;
            this.oldQuality = oldQuality;
            this.newQuality = newQuality;
            this.maintenanceLevel = maintenanceLevel;
        }

        public String getRouteId() { return routeId; }
        public String getOldQuality() { return oldQuality; }
        public String getNewQuality() { return newQuality; }
        public double getMaintenanceLevel() { return maintenanceLevel; }
    }

    public static class RegionPriceShift extends EconomyEvent {
        private final String regionId;
        private final String goodId;
        private final double oldPrice;
        private final double newPrice;

        public RegionPriceShift(String villageId, long gameTime, String regionId,
                                String goodId, double oldPrice, double newPrice) {
            super(villageId, gameTime);
            this.regionId = regionId;
            this.goodId = goodId;
            this.oldPrice = oldPrice;
            this.newPrice = newPrice;
        }

        public String getRegionId() { return regionId; }
        public String getGoodId() { return goodId; }
        public double getOldPrice() { return oldPrice; }
        public double getNewPrice() { return newPrice; }
    }

    public static class SeaRouteCreated extends EconomyEvent {
        private final String routeId;
        private final String fromVillageId;
        private final String toVillageId;

        public SeaRouteCreated(String villageId, long gameTime, String routeId,
                               String fromVillageId, String toVillageId) {
            super(villageId, gameTime);
            this.routeId = routeId;
            this.fromVillageId = fromVillageId;
            this.toVillageId = toVillageId;
        }

        public String getRouteId() { return routeId; }
        public String getFromVillageId() { return fromVillageId; }
        public String getToVillageId() { return toVillageId; }
    }

    public static class VillageResettled extends EconomyEvent {
        private final String villageId;
        private final VillageNode.VillageType newType;

        public VillageResettled(String villageId, long gameTime, VillageNode.VillageType newType) {
            super(villageId, gameTime);
            this.villageId = villageId;
            this.newType = newType;
        }

        public String getVillageId() { return villageId; }
        public VillageNode.VillageType getNewType() { return newType; }
    }

    public static class WorldEventFired extends EconomyEvent {
        private final String eventId;
        private final String scope;
        private final String targetId;

        public WorldEventFired(String villageId, long gameTime, String eventId, String scope, String targetId) {
            super(villageId, gameTime);
            this.eventId = eventId;
            this.scope = scope;
            this.targetId = targetId;
        }

        public String getEventId() { return eventId; }
        public String getScope() { return scope; }
        public String getTargetId() { return targetId; }
    }

    public static class ActiveModifierExpired extends EconomyEvent {
        private final String modifierId;
        private final String targetId;
        private final String sourceEventId;

        public ActiveModifierExpired(String villageId, long gameTime, String modifierId, String targetId, String sourceEventId) {
            super(villageId, gameTime);
            this.modifierId = modifierId;
            this.targetId = targetId;
            this.sourceEventId = sourceEventId;
        }

        public String getModifierId() { return modifierId; }
        public String getTargetId() { return targetId; }
        public String getSourceEventId() { return sourceEventId; }
    }

    public static class LoanOverdue extends EconomyEvent {
        private final String playerId;
        private final double amountOverdue;

        public LoanOverdue(String villageId, long gameTime, String playerId, double amountOverdue) {
            super(villageId, gameTime);
            this.playerId = playerId;
            this.amountOverdue = amountOverdue;
        }

        public String getPlayerId() { return playerId; }
        public double getAmountOverdue() { return amountOverdue; }
    }

    public static class WorkshopCreated extends EconomyEvent {
        private final String instanceId;
        private final String workshopId;
        private final String ownerUuid;

        public WorkshopCreated(String villageId, long gameTime, String instanceId, String workshopId, String ownerUuid) {
            super(villageId, gameTime);
            this.instanceId = instanceId;
            this.workshopId = workshopId;
            this.ownerUuid = ownerUuid;
        }

        public String getInstanceId() { return instanceId; }
        public String getWorkshopId() { return workshopId; }
        public String getOwnerUuid() { return ownerUuid; }
    }

    public static class WorkshopDestroyed extends EconomyEvent {
        private final String instanceId;
        private final String workshopId;

        public WorkshopDestroyed(String villageId, long gameTime, String instanceId, String workshopId) {
            super(villageId, gameTime);
            this.instanceId = instanceId;
            this.workshopId = workshopId;
        }

        public String getInstanceId() { return instanceId; }
        public String getWorkshopId() { return workshopId; }
    }

    public static class WorkshopUpgraded extends EconomyEvent {
        private final String instanceId;
        private final String workshopId;
        private final int newLevel;

        public WorkshopUpgraded(String villageId, long gameTime, String instanceId, String workshopId, int newLevel) {
            super(villageId, gameTime);
            this.instanceId = instanceId;
            this.workshopId = workshopId;
            this.newLevel = newLevel;
        }

        public String getInstanceId() { return instanceId; }
        public String getWorkshopId() { return workshopId; }
        public int getNewLevel() { return newLevel; }
    }

    public static class WorkshopProductionComplete extends EconomyEvent {
        private final String instanceId;
        private final String workshopId;
        private final int levelMultiplier;

        public WorkshopProductionComplete(String villageId, long gameTime, String instanceId, String workshopId, int levelMultiplier) {
            super(villageId, gameTime);
            this.instanceId = instanceId;
            this.workshopId = workshopId;
            this.levelMultiplier = levelMultiplier;
        }

        public String getInstanceId() { return instanceId; }
        public String getWorkshopId() { return workshopId; }
        public int getLevelMultiplier() { return levelMultiplier; }
    }

    public static class WorkshopProductionFailed extends EconomyEvent {
        private final String instanceId;
        private final String workshopId;

        public WorkshopProductionFailed(String villageId, long gameTime, String instanceId, String workshopId) {
            super(villageId, gameTime);
            this.instanceId = instanceId;
            this.workshopId = workshopId;
        }

        public String getInstanceId() { return instanceId; }
        public String getWorkshopId() { return workshopId; }
    }

    public static class VillageMobilized extends EconomyEvent {
        private final String newState;

        public VillageMobilized(String villageId, long gameTime, String newState) {
            super(villageId, gameTime);
            this.newState = newState;
        }

        public String getNewState() { return newState; }
    }

    public static class VillageGoalSet extends EconomyEvent {
        private final String goalId;
        private final String previousGoalId;

        public VillageGoalSet(String villageId, long gameTime, String goalId, String previousGoalId) {
            super(villageId, gameTime);
            this.goalId = goalId;
            this.previousGoalId = previousGoalId;
        }

        public String getGoalId() { return goalId; }
        public String getPreviousGoalId() { return previousGoalId; }
    }
}
