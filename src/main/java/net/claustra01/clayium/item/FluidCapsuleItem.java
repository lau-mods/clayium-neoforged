package net.claustra01.clayium.item;

/**
 * Fluid capsule item (1000 / 125 / 25 / 5 / 1 mB) storing fluid via FLUID_CONTENT DataComponent.
 */
public class FluidCapsuleItem extends SimpleTieredItem {
    private final int capacityMb;

    public FluidCapsuleItem(int capacityMb, String iconName) {
        super(new Properties().stacksTo(64), null, iconName);
        this.capacityMb = capacityMb;
    }

    public int getCapacityMb() {
        return this.capacityMb;
    }
}
