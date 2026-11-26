package net.claustra01.clayium.datagen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.FoundationBlockRegistration;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class FoundationBlockStateProvider extends BlockStateProvider {
    public FoundationBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        cubeAll(FoundationBlockRegistration.DENSE_CLAY.get(), "compressedclay-0");
        cubeAll(FoundationBlockRegistration.COMPRESSED_CLAY.get(), "compressedclay-1");
        cubeAll(FoundationBlockRegistration.CLAYIUM_BLOCK.get(), "clayium");

    }

    private void cubeAll(Block block, String textureName) {
        ModelFile model = models().cubeAll(
                BuiltInRegistries.BLOCK.getKey(block).getPath(), texture(textureName));
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    private ResourceLocation texture(String name) {
        return ClayiumMod.id("block/" + name);
    }
}
