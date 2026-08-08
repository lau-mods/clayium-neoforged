/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MetalStorageModelProvider extends BlockStateProvider {
    public MetalStorageModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, Clayium.MODID, files);
    }

    @Override protected void registerStatesAndModels() {
        ModelFile metal = models().getExistingFile(Clayium.id("block/decorative_metal_block"));
        ClayiumRegistries.DECORATIVE_METAL_BLOCKS.values().forEach(holder -> {
            simpleBlock(holder.get(), metal);
            simpleBlockItem(holder.get(), metal);
        });
        ModelFile chest = models().getExistingFile(Clayium.id("block/metal_chest"));
        ClayiumRegistries.METAL_CHEST_BLOCKS.values().forEach(holder -> {
            horizontalBlock(holder.get(), chest);
            simpleBlockItem(holder.get(), chest);
        });
    }
}
