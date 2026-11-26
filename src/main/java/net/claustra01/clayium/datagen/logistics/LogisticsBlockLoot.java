package net.claustra01.clayium.datagen.logistics;

import java.util.Set;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class LogisticsBlockLoot extends BlockLootSubProvider {
    public LogisticsBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        LogisticsRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return LogisticsRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
