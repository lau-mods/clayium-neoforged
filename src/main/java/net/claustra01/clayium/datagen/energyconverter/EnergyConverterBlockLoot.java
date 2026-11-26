package net.claustra01.clayium.datagen.energyconverter;

import java.util.Set;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class EnergyConverterBlockLoot extends BlockLootSubProvider {
    public EnergyConverterBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return EnergyConverterRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
