package net.claustra01.clayium.block;

import net.minecraft.world.level.block.RotatedPillarBlock;

public class ClayTreeLogBlock extends RotatedPillarBlock implements ITieredBlock {
    public ClayTreeLogBlock(Properties properties) {
        super(properties);
    }

    @Override
    public int getTier() {
        return 7;
    }
}
