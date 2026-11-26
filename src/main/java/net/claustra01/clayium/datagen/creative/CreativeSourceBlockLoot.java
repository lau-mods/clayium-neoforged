package net.claustra01.clayium.datagen.creative;

import java.util.Set;
import net.claustra01.clayium.registry.CreativeSourceRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class CreativeSourceBlockLoot extends BlockLootSubProvider {
    public CreativeSourceBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get());
    }
}
