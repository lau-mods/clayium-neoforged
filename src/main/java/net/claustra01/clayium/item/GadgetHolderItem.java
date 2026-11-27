package net.claustra01.clayium.item;

/**
 * Wearable/inventory Clay Gadget Holder (menu & gadget ticks wired in Step 10/11).
 */
public class GadgetHolderItem extends SimpleTieredItem {
    public GadgetHolderItem() {
        super(new Properties().stacksTo(1), null, "gadgetholder");
    }
}
