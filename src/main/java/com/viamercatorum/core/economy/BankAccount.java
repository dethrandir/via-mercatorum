package com.viamercatorum.core.economy;

import java.util.*;

public class BankAccount {
    private final String playerId;
    private final String villageId;
    private double principal;
    private double interestAccrued;
    private double interestRate;
    private long loanTick;
    private long dueTick;
    private LoanStatus status;

    public enum LoanStatus {
        ACTIVE, OVERDUE, PAID
    }

    public BankAccount(String playerId, String villageId, double principal, double interestRate, long loanTick, long dueTick) {
        this.playerId = playerId;
        this.villageId = villageId;
        this.principal = principal;
        this.interestRate = interestRate;
        this.loanTick = loanTick;
        this.dueTick = dueTick;
        this.interestAccrued = 0;
        this.status = LoanStatus.ACTIVE;
    }

    public void accrueInterest(long currentTick) {
        if (status == LoanStatus.PAID) return;
        long ticksElapsed = currentTick - loanTick;
        interestAccrued = principal * interestRate * (ticksElapsed / 1000.0);
    }

    public double getTotalDebt() {
        return principal + interestAccrued;
    }

    public double repay(double amount) {
        double total = getTotalDebt();
        if (amount >= total) {
            status = LoanStatus.PAID;
            double excess = amount - total;
            principal = 0;
            interestAccrued = 0;
            return excess;
        }
        if (amount >= interestAccrued) {
            amount -= interestAccrued;
            interestAccrued = 0;
            principal -= amount;
        } else {
            interestAccrued -= amount;
        }
        return 0;
    }

    public void markOverdue() {
        this.status = LoanStatus.OVERDUE;
    }

    public String getPlayerId() { return playerId; }
    public String getVillageId() { return villageId; }
    public double getPrincipal() { return principal; }
    public double getInterestAccrued() { return interestAccrued; }
    public double getInterestRate() { return interestRate; }
    public long getLoanTick() { return loanTick; }
    public long getDueTick() { return dueTick; }
    public LoanStatus getStatus() { return status; }
    public void setDueTick(long dueTick) { this.dueTick = dueTick; }
}