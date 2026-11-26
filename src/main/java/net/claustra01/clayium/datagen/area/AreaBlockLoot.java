package net.claustra01.clayium.datagen.area;

import java.util.Set;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class AreaBlockLoot extends BlockLootSubProvider {
    public AreaBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        AreaMachineRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return AreaMachineRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
