package net.claustra01.clayium.datagen.autocrafter;

import java.util.Set;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class AutoCrafterBlockLoot extends BlockLootSubProvider {
    public AutoCrafterBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        AutoCrafterRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return AutoCrafterRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
