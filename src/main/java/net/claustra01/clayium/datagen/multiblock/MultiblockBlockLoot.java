package net.claustra01.clayium.datagen.multiblock;

import java.util.Set;
import net.claustra01.clayium.registry.MultiblockRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class MultiblockBlockLoot extends BlockLootSubProvider {
    public MultiblockBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MultiblockRegistration.CONTROLLERS.values().stream()
                .map(holder -> (Block)holder.get()).toList();
    }
}
