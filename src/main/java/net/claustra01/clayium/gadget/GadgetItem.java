package net.claustra01.clayium.gadget;

import net.claustra01.clayium.item.FoundationItem;

public final class GadgetItem extends FoundationItem {
    private final GadgetType type;

    public GadgetItem(GadgetType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public GadgetType type() {
        return type;
    }
}
