package com.viamercatorum.core.economy;

import com.viamercatorum.ViaMercatorum;
import com.viamercatorum.core.config.ViaMercatorumConfig;
import com.viamercatorum.core.simulation.EconomyEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BankManager {
    private final Map<String, BankAccount> loans = new ConcurrentHashMap<>();

    public BankAccount takeLoan(String playerId, String villageId, double amount) {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        long currentTick = ViaMercatorum.getInstance().getSimulationTick().getTickCounter();
        long dueTick = currentTick + config.bank.overdueGraceTicks;

        BankAccount account = new BankAccount(
            playerId, villageId, amount,
            config.bank.interestRate, currentTick, dueTick
        );
        loans.put(playerId + "_" + villageId, account);
        return account;
    }

    public double repay(String playerId, String villageId, double amount) {
        String key = playerId + "_" + villageId;
        BankAccount account = loans.get(key);
        if (account == null) return amount;

        double excess = account.repay(amount);
        if (account.getStatus() == BankAccount.LoanStatus.PAID) {
            loans.remove(key);
        }
        return excess;
    }

    public void tickLoans() {
        ViaMercatorumConfig config = ViaMercatorum.getInstance().getConfig();
        long currentTick = ViaMercatorum.getInstance().getSimulationTick().getTickCounter();

        List<BankAccount> overdueAccounts = new ArrayList<>();

        for (BankAccount account : loans.values()) {
            account.accrueInterest(currentTick);

            if (account.getStatus() == BankAccount.LoanStatus.ACTIVE && currentTick > account.getDueTick()) {
                account.markOverdue();
                overdueAccounts.add(account);
            }
        }

        for (BankAccount account : overdueAccounts) {
            ViaMercatorum.getInstance().getReputationSystem().addGlobalReputation(
                UUID.fromString(account.getPlayerId()),
                config.bank.repLossOnOverdue
            );
        }
    }

    public BankAccount getLoan(String playerId, String villageId) {
        return loans.get(playerId + "_" + villageId);
    }

    public List<BankAccount> getOverdueLoans() {
        return loans.values().stream()
            .filter(a -> a.getStatus() == BankAccount.LoanStatus.OVERDUE)
            .toList();
    }

    public List<BankAccount> getPlayerLoans(String playerId) {
        return loans.values().stream()
            .filter(a -> a.getPlayerId().equals(playerId))
            .toList();
    }

    public Collection<BankAccount> getAllLoans() {
        return Collections.unmodifiableCollection(loans.values());
    }

    public boolean hasOverdueLoan(String playerId) {
        return loans.values().stream()
            .anyMatch(a -> a.getPlayerId().equals(playerId) && a.getStatus() == BankAccount.LoanStatus.OVERDUE);
    }

    public void clearLoan(String playerId, String villageId) {
        loans.remove(playerId + "_" + villageId);
    }
}