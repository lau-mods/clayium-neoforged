package net.claustra01.clayium.datagen.material;

import java.util.Set;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class MaterialBlockLoot extends BlockLootSubProvider {
    public MaterialBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        MaterialRegistration.MATERIAL_OWNED_BLOCKS.forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MaterialRegistration.MATERIAL_OWNED_BLOCKS.stream().map(block -> (Block) block.get()).toList();
    }
}
