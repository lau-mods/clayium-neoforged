package net.claustra01.clayium.datagen.machine;

import java.util.Set;
import net.claustra01.clayium.registry.MachineRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class MachineBlockLoot extends BlockLootSubProvider {
    public MachineBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        MachineRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MachineRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
