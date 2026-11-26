package net.claustra01.clayium.datagen.iface;

import java.util.Set;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class InterfaceBlockLoot extends BlockLootSubProvider {
    public InterfaceBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        InterfaceRegistration.BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return InterfaceRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
