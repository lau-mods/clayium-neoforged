package net.claustra01.clayium.datagen.careactor;

import java.util.Set;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class CaReactorBlockLoot extends BlockLootSubProvider {
    public CaReactorBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return CaReactorRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
