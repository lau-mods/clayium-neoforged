package net.claustra01.clayium.item;

/**
 * Individual gadget modules inserted into Clay Gadget Holder (Step 11 wires passive/active effects).
 */
public class GadgetItem extends SimpleTieredItem {
    public enum GadgetFamily {
        OVERCLOCK,
        FLIGHT,
        HEALTH,
        AUTO_EAT,
        REPEATEDLY_ATTACK,
        LONG_ARM,
        BLANK
    }

    private final GadgetFamily family;
    private final int level;

    public GadgetItem(int tier, String iconName, GadgetFamily family, int level) {
        super(new Properties().stacksTo(1), tier, iconName);
        this.family = family;
        this.level = level;
    }

    public GadgetFamily getFamily() {
        return this.family;
    }

    public int getLevel() {
        return this.level;
    }
}
