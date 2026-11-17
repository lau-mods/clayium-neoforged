package net.claustra01.clayium.datagen.workbench;

import java.util.Set;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class WorkbenchBlockLoot extends BlockLootSubProvider {
    public WorkbenchBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        WorkbenchRegistration.ALL_BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return WorkbenchRegistration.ALL_BLOCKS.stream().map(block -> (Block) block.get()).toList();
    }
}
