package net.claustra01.clayium.multiblock.careactor;

import net.minecraft.world.level.block.Block;

public final class CaReactorCoilBlock extends Block {
    private final int tier;

    public CaReactorCoilBlock(Properties properties, int tier) {
        super(properties);
        this.tier = tier;
    }

    public int tier() {
        return tier;
    }
}
