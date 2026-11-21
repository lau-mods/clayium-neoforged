package net.claustra01.clayium.world.area;

public enum AreaMachineType {
    ITEM_COLLECTOR("area_item_collector", 7, 10, 100),
    MINER("area_miner", 8, 400, 1000),
    ADVANCED_MINER("advanced_area_miner", 9, 400, 1000),
    REPLACER("area_replacer", 10, 400, 1000),
    ACTIVATOR("area_activator", 8, 400, 1000);

    private final String id;
    private final int tier;
    private final int progressPerJob;
    private final long energyPerTick;

    AreaMachineType(String id, int tier, int progressPerJob, long energyPerTick) {
        this.id = id;
        this.tier = tier;
        this.progressPerJob = progressPerJob;
        this.energyPerTick = energyPerTick;
    }

    public String id() {
        return id;
    }

    public int tier() {
        return tier;
    }

    public int progressPerJob() {
        return progressPerJob;
    }

    public long energyPerTick() {
        return energyPerTick;
    }
}
