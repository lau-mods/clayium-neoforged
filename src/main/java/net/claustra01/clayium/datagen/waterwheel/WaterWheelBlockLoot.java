package net.claustra01.clayium.datagen.waterwheel;

import java.util.Set;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class WaterWheelBlockLoot extends BlockLootSubProvider {
    public WaterWheelBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        WaterWheelRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return WaterWheelRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
