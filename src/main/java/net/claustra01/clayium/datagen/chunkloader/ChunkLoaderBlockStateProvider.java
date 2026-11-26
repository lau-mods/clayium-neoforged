package net.claustra01.clayium.datagen.chunkloader;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderBlock;

public final class ChunkLoaderBlockStateProvider extends BlockStateProvider {
    public ChunkLoaderBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile waiting = model(ChunkLoaderRegistration.ID, null);
        ModelFile active = model(ChunkLoaderRegistration.ID + "_active", "import_energy");
        ModelFile unavailable = model(ChunkLoaderRegistration.ID + "_unavailable", "export_rf");
        getVariantBuilder(ChunkLoaderRegistration.CHUNK_LOADER.get()).forAllStates(state -> {
            ModelFile selected = switch (state.getValue(ChunkLoaderBlock.STATUS)) {
                case ACTIVE -> active;
                case UNAVAILABLE -> unavailable;
                case WAITING -> waiting;
            };
            return ConfiguredModel.builder().modelFile(selected)
                    .rotationY(((int)state.getValue(ChunkLoaderBlock.FACING).toYRot() + 180) % 360)
                    .build();
        });
        simpleBlockItem(ChunkLoaderRegistration.CHUNK_LOADER.get(), waiting);
    }

    private ModelFile model(String id, String statusTexture) {
        var builder = models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", ClayiumMod.id("block/machinehull-5"))
                .texture("front", ClayiumMod.id("block/chunkloader"))
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
        if (statusTexture != null) {
            builder.texture("status", ClayiumMod.id("block/" + statusTexture))
                    .element().from(3, 3, -0.5F).to(13, 13, -0.01F)
                    .face(Direction.NORTH).texture("#status").end().end();
        }
        return builder;
    }

    @Override
    public String getName() {
        return "Clayium chunk loader block states";
    }
}
