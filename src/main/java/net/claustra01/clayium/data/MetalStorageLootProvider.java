/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class MetalStorageLootProvider {
    private MetalStorageLootProvider() {}

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(
                provider -> new BlockLootSubProvider(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider) {
                    @Override protected void generate() {
                        ClayiumRegistries.DECORATIVE_METAL_BLOCKS.values().forEach(holder -> dropSelf(holder.get()));
                        ClayiumRegistries.METAL_CHEST_BLOCKS.values().forEach(holder -> dropSelf(holder.get()));
                    }

                    @Override protected Iterable<Block> getKnownBlocks() {
                        return java.util.stream.Stream.concat(
                                ClayiumRegistries.DECORATIVE_METAL_BLOCKS.values().stream().map(holder -> (Block) holder.get()),
                                ClayiumRegistries.METAL_CHEST_BLOCKS.values().stream().map(holder -> (Block) holder.get()))
                                .toList();
                    }
                }, LootContextParamSets.BLOCK)), registries);
    }
}
