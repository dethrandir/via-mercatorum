package com.viamercatorum.core.economy;

public class Supply {
    private final TradeGood good;
    private double stock;
    private double productionRate;
    private double maxCapacity;

    public Supply(TradeGood good, double stock, double productionRate, double maxCapacity) {
        this.good = good;
        this.stock = stock;
        this.productionRate = productionRate;
        this.maxCapacity = maxCapacity;
    }

    public void tickProduction() {
        stock = Math.min(maxCapacity, stock + productionRate);
    }

    public void consume(double amount) {
        stock = Math.max(0, stock - amount);
    }

    public void addStock(double amount) {
        stock = Math.min(maxCapacity, stock + amount);
    }

    public TradeGood getGood() { return good; }
    public double getStock() { return stock; }
    public double getProductionRate() { return productionRate; }
    public double getMaxCapacity() { return maxCapacity; }
    public void setProductionRate(double rate) { this.productionRate = rate; }
    public void setMaxCapacity(double capacity) { this.maxCapacity = capacity; }
}
