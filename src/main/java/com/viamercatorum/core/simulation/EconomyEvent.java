package com.viamercatorum.core.simulation;

import com.viamercatorum.core.world.VillageNode;

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
}
