package net.claustra01.clayium.block;

import javax.annotation.Nullable;

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
        this(properties, tier, textureName, kind, factor, additionalStorage, null);
    }

    public UpgradeBlock(
            Properties properties,
            int tier,
            String textureName,
            UpgradeKind kind,
            double factor,
            int additionalStorage,
            @Nullable String tooltipKey) {
        super(properties, tier, textureName, tooltipKey);
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
