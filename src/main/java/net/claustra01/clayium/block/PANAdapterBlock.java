package net.claustra01.clayium.block;

import net.minecraft.world.item.ItemStack;

/**
 * PAN Adapter (metaMode=2, all-face panadapter overlay, visual hull uses machineTier while item Tier >= 11).
 */
public class PANAdapterBlock extends DirectionalClayContainerBlock {
    public PANAdapterBlock(Properties properties, int tier, String baseTexture) {
        super(properties, tier, baseTexture, "panadapter", "pan", null, OverlayMode.ALL_FACES);
    }

    @Override
    public int getTier() {
        return Math.max(super.getTier(), 11);
    }

    @Override
    public int getTier(ItemStack stack) {
        return getTier();
    }

    public int getMachineTier() {
        return super.getTier();
    }
}
