package net.claustra01.clayium.datagen.ca;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.CaRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CaBlockStateProvider
        extends net.neoforged.neoforge.client.model.generators.BlockStateProvider {
    public CaBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        CaRegistration.MACHINES.values().forEach(definition -> machine(
                definition.id(), definition.tier(), definition.series().frontTexture(), definition.block().get()));
        machine("ca_resonating_collector", 10, "cacollector", CaRegistration.RESONATING_COLLECTOR.get());
        CaRegistration.AUGMENTS.values().forEach(definition -> {
            ModelFile model = models().cubeAll(
                    definition.id(), ClayiumMod.id("block/" + definition.texture()));
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }

    private void machine(String id, int tier, String frontTexture, net.minecraft.world.level.block.Block block) {
        ResourceLocation hull = ClayiumMod.id("block/machinehull-" + (tier - 1));
        ModelFile model = models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", hull)
                .texture("front", ClayiumMod.id("block/" + frontTexture))
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }

    @Override
    public String getName() {
        return "CA machines and augments";
    }
}
