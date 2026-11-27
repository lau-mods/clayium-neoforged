package net.claustra01.clayium.block;

import net.minecraft.world.item.ItemStack;

/**
 * PAN Duplicator (metaMode=1 horizontal, machinehull-(rank-1) base, panduplicator front, pancasing other faces).
 */
public class PANDuplicatorBlock extends HorizontalClayContainerBlock {
    private final int duplicatorRank;

    public PANDuplicatorBlock(Properties properties, int rank) {
        super(
                properties,
                rank <= 8 ? 11 : (rank <= 12 ? 12 : 13),
                "machinehull-" + (rank - 1),
                "panduplicator",
                "pan",
                null,
                OverlayMode.PAN_DUPLICATOR
        );
        this.duplicatorRank = rank;
    }

    public int getDuplicatorRank() {
        return this.duplicatorRank;
    }

    @Override
    public int getTier(ItemStack stack) {
        return getTier();
    }
}
