package com.viamercatorum.core.world;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorkshopInstance {
    private final String instanceId;
    private final String workshopId;
    private final String villageId;
    private final String ownerUuid;
    private int level;
    private boolean enabled;
    private boolean autoBuyMaterials;
    private boolean autoSellOutput;
    private final Map<String, Double> storedInput;
    private final Map<String, Double> accumulatedOutput;
    private long tickCounter;

    public WorkshopInstance(String workshopId, String villageId, String ownerUuid) {
        this.instanceId = UUID.randomUUID().toString().substring(0, 8);
        this.workshopId = workshopId;
        this.villageId = villageId;
        this.ownerUuid = ownerUuid;
        this.level = 1;
        this.enabled = true;
        this.autoBuyMaterials = true;
        this.autoSellOutput = true;
        this.storedInput = new HashMap<>();
        this.accumulatedOutput = new HashMap<>();
        this.tickCounter = 0;
    }

    public WorkshopInstance(String workshopId, String villageId) {
        this(workshopId, villageId, null);
    }

    public String getInstanceId() { return instanceId; }
    public String getWorkshopId() { return workshopId; }
    public String getVillageId() { return villageId; }
    public String getOwnerUuid() { return ownerUuid; }
    public boolean isPlayerOwned() { return ownerUuid != null; }
    public boolean isVillageOwned() { return ownerUuid == null; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isAutoBuyMaterials() { return autoBuyMaterials; }
    public void setAutoBuyMaterials(boolean autoBuyMaterials) { this.autoBuyMaterials = autoBuyMaterials; }

    public boolean isAutoSellOutput() { return autoSellOutput; }
    public void setAutoSellOutput(boolean autoSellOutput) { this.autoSellOutput = autoSellOutput; }

    public Map<String, Double> getStoredInput() { return storedInput; }
    public Map<String, Double> getAccumulatedOutput() { return accumulatedOutput; }

    public long getTickCounter() { return tickCounter; }
    public void incrementTick() { tickCounter++; }
    public void resetTick() { tickCounter = 0; }
}