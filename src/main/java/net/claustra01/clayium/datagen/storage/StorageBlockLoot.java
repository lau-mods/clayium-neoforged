package net.claustra01.clayium.datagen.storage;

import java.util.Set;
import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.block.Block;

public final class StorageBlockLoot extends BlockLootSubProvider {
    public StorageBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        StorageRegistration.BLOCKS.forEach(block -> {
            if (block == StorageRegistration.STORAGE_CONTAINER) {
                add(block.get(), createSingleItemTable(block.get()).apply(
                        CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                                .include(DataComponents.CUSTOM_DATA)));
            } else {
                dropSelf(block.get());
            }
        });
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return StorageRegistration.BLOCKS.stream().map(block -> (Block)block.get()).toList();
    }
}
