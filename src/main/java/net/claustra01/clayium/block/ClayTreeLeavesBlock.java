package net.claustra01.clayium.block;

import net.minecraft.world.level.block.LeavesBlock;

public class ClayTreeLeavesBlock extends LeavesBlock implements ITieredBlock {
    public ClayTreeLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getTier() {
        return 7;
    }
}
