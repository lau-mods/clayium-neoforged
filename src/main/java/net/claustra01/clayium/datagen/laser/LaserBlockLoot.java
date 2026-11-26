package net.claustra01.clayium.datagen.laser;

import java.util.Set;
import net.claustra01.clayium.registry.LaserRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class LaserBlockLoot extends BlockLootSubProvider {
    public LaserBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        LaserRegistration.LASER_BLOCKS.forEach(block -> dropSelf(block.get()));
        dropSelf(LaserRegistration.LASER_REFLECTOR.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return java.util.stream.Stream.concat(
                LaserRegistration.LASER_BLOCKS.stream().map(block -> (Block)block.get()),
                java.util.stream.Stream.of(LaserRegistration.LASER_REFLECTOR.get())).toList();
    }
}
