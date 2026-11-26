package net.claustra01.clayium.datagen;

import java.util.Set;
import net.claustra01.clayium.registry.FoundationBlockRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class FoundationBlockLoot extends BlockLootSubProvider {
    public FoundationBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        FoundationBlockRegistration.ALL.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return FoundationBlockRegistration.ALL.stream().map(block -> (Block) block.get()).toList();
    }
}
