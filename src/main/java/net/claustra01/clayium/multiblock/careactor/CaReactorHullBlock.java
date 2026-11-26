package net.claustra01.clayium.multiblock.careactor;

import net.minecraft.world.level.block.Block;

public final class CaReactorHullBlock extends Block {
    private final int rank;

    public CaReactorHullBlock(Properties properties, int rank) {
        super(properties);
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }
}
