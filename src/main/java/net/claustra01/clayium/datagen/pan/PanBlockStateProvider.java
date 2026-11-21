package net.claustra01.clayium.datagen.pan;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.registry.PanRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class PanBlockStateProvider
        extends net.neoforged.neoforge.client.model.generators.BlockStateProvider {
    public PanBlockStateProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, ClayiumMod.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        PanRegistration.DEFINITIONS.values().forEach(definition -> {
            String texture = switch (definition.kind()) {
                case CABLE -> "pancable";
                case CORE -> "pancore";
                case ADAPTER -> "panadapter";
                case DUPLICATOR -> "panduplicator";
            };
            ModelFile model;
            if (definition.kind() == PanBlock.Kind.CABLE) {
                model = models().cubeAll(definition.id(), ClayiumMod.id("block/" + texture));
            } else {
                model = models().getBuilder(definition.id())
                        .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                        .texture("casing", ClayiumMod.id("block/pancasing"))
                        .texture("front", ClayiumMod.id("block/" + texture))
                        .texture("particle", "#casing")
                        .element().from(0, 0, 0).to(16, 16, 16).cube("#casing").end()
                        .element().from(0, 0, -0.01F).to(16, 16, 0)
                        .face(Direction.NORTH).texture("#front").end().end();
            }
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }

    @Override
    public String getName() {
        return "PAN network blocks";
    }
}
