package net.claustra01.clayium.datagen.chunkloader;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class ChunkLoaderBlockStateProvider extends BlockStateProvider {
    public ChunkLoaderBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile model = models().getBuilder(ChunkLoaderRegistration.ID)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", ClayiumMod.id("block/machinehull-5"))
                .texture("front", ClayiumMod.id("block/chunkloader"))
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
        horizontalBlock(ChunkLoaderRegistration.CHUNK_LOADER.get(), model);
        simpleBlockItem(ChunkLoaderRegistration.CHUNK_LOADER.get(), model);
    }

    @Override
    public String getName() {
        return "Clayium chunk loader block states";
    }
}
