package net.claustra01.clayium.datagen.clayproduction;

import java.util.Set;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class ClayProductionBlockLoot extends BlockLootSubProvider {
    public ClayProductionBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        ClayProductionRegistration.BLOCKS.values().forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ClayProductionRegistration.BLOCKS.values().stream().map(block -> (Block)block.get()).toList();
    }
}
