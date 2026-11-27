package net.claustra01.clayium.block;

/**
 * Upgrade blocks for CA Reactor / Energy upgrades (Resonator, Overclocker, Energy Storage Upgrade).
 */
public class UpgradeBlock extends TieredBlock {
    public enum UpgradeKind {
        RESONATOR,
        OVERCLOCKER,
        ENERGY_STORAGE
    }

    private final UpgradeKind kind;
    private final double factor;
    private final int additionalStorage;

    public UpgradeBlock(Properties properties, int tier, String textureName, UpgradeKind kind, double factor, int additionalStorage) {
        super(properties, tier, textureName);
        this.kind = kind;
        this.factor = factor;
        this.additionalStorage = additionalStorage;
    }

    public UpgradeKind getKind() {
        return this.kind;
    }

    public double getFactor() {
        return this.factor;
    }

    public int getAdditionalStorage() {
        return this.additionalStorage;
    }
}
