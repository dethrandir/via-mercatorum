package com.viamercatorum.player;

public enum VillageHallTab {
    TRADE(0),
    INVENTORY(1),
    WORKSHOPS(2),
    STATISTICS(3),
    MILITARY(4),
    GOALS(5);

    private final int index;

    VillageHallTab(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static VillageHallTab fromIndex(int index) {
        for (VillageHallTab tab : values()) {
            if (tab.index == index) return tab;
        }
        return TRADE;
    }
}