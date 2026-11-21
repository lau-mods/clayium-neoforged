package net.claustra01.clayium.datagen.miner;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MinerRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MinerBlockStateProvider extends BlockStateProvider {
    public MinerBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Basic Operation Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        ResourceLocation hull = ClayiumMod.id("block/machinehull-5");
        ModelFile breaker = frontModel(
                MinerRegistration.BLOCK_BREAKER.getId().getPath(), hull, ClayiumMod.id("block/minerback"));
        ModelFile activator = frontModel(
                MinerRegistration.ACTIVATOR.getId().getPath(), hull, ClayiumMod.id("block/areaactivator"));
        horizontalBlock(MinerRegistration.BLOCK_BREAKER.get(), breaker);
        horizontalBlock(MinerRegistration.ACTIVATOR.get(), activator);
        simpleBlockItem(MinerRegistration.BLOCK_BREAKER.get(), breaker);
        simpleBlockItem(MinerRegistration.ACTIVATOR.get(), activator);
    }

    private ModelFile frontModel(String id, ResourceLocation hull, ResourceLocation front) {
        return models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", hull)
                .texture("front", front)
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
    }
}
