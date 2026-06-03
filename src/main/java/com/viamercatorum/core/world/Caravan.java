package com.viamercatorum.core.world;

import java.util.Map;

public class Caravan {
    private final String id;
    private final String routeId;
    private final VillageNode origin;
    private final VillageNode destination;
    private final Map<String, Double> cargo;
    private double emeralds;
    private final double totalTravelTime;
    private double elapsed;
    private boolean arrived;

    public Caravan(String id, String routeId, VillageNode origin, VillageNode destination,
                   Map<String, Double> cargo, double totalTravelTime) {
        this.id = id;
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.cargo = cargo;
        this.emeralds = 0;
        this.totalTravelTime = totalTravelTime;
        this.elapsed = 0;
        this.arrived = false;
    }

    public void addEmeralds(double amount) {
        this.emeralds += amount;
    }

    public boolean spendEmeralds(double amount) {
        if (this.emeralds >= amount) {
            this.emeralds -= amount;
            return true;
        }
        return false;
    }

    public double getEmeralds() {
        return emeralds;
    }

    public void tick() {
        elapsed += 0.05;
        if (elapsed >= totalTravelTime) {
            arrived = true;
        }
    }

    public double getProgress() {
        return Math.min(1.0, elapsed / totalTravelTime);
    }

    public boolean hasArrived() { return arrived; }
    public String getId() { return id; }
    public String getRouteId() { return routeId; }
    public VillageNode getOriginNode() { return origin; }
    public VillageNode getDestinationNode() { return destination; }
    public Map<String, Double> getCargo() { return cargo; }
}
