package net.claustra01.clayium.datagen.pan;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.pan.PanCableBlock;
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
            if (definition.kind() == PanBlock.Kind.CABLE) {
                registerCable();
            } else {
                String texture = definition.kind() == PanBlock.Kind.CORE ? "pancore"
                        : definition.kind() == PanBlock.Kind.ADAPTER ? "panadapter" : "panduplicator";
                ModelFile model = models().getBuilder(definition.id())
                        .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                        .texture("hull", modLoc("block/machinehull-" + (definition.tier() - 1)))
                        .texture("casing", ClayiumMod.id("block/pancasing"))
                        .texture("front", ClayiumMod.id("block/" + texture))
                        .texture("particle", "#hull")
                        .renderType("minecraft:cutout")
                        .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                        .element().from(-0.01F, -0.01F, -0.01F).to(16.01F, 16.01F, 16.01F)
                        .allFaces((direction, face) -> face.texture("#casing").uvs(0, 0, 16, 16)).end()
                        .element().from(0, 0, -0.02F).to(16, 16, 0)
                        .face(Direction.NORTH).texture("#front").end().end();
                simpleBlock(definition.block().get(), model);
                simpleBlockItem(definition.block().get(), model);
            }
        });
    }

    private void registerCable() {
        ModelFile center = cableElement("pan_cable_center", 6, 6, 6, 10, 10, 10);
        var multipart = getMultipartBuilder(PanRegistration.PAN_CABLE.get());
        multipart.part().modelFile(center).addModel().end();
        for (Direction direction : Direction.values()) {
            float[] bounds = switch (direction) {
                case DOWN -> new float[]{6, 0, 6, 10, 6, 10};
                case UP -> new float[]{6, 10, 6, 10, 16, 10};
                case NORTH -> new float[]{6, 6, 0, 10, 10, 6};
                case SOUTH -> new float[]{6, 6, 10, 10, 10, 16};
                case WEST -> new float[]{0, 6, 6, 6, 10, 10};
                case EAST -> new float[]{10, 6, 6, 16, 10, 10};
            };
            ModelFile arm = cableElement("pan_cable_" + direction.getName(),
                    bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
            multipart.part().modelFile(arm).addModel()
                    .condition(PanCableBlock.connectionProperty(direction), true).end();
        }

        var item = models().getBuilder("pan_cable_inventory")
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("cable", ClayiumMod.id("block/pancable"))
                .texture("particle", "#cable")
                .renderType("minecraft:translucent")
                .element().from(6, 6, 6).to(10, 10, 10).cube("#cable").end()
                .element().from(6, 0, 6).to(10, 6, 10).cube("#cable").end()
                .element().from(6, 10, 6).to(10, 16, 10).cube("#cable").end()
                .element().from(6, 6, 0).to(10, 10, 6).cube("#cable").end()
                .element().from(6, 6, 10).to(10, 10, 16).cube("#cable").end()
                .element().from(0, 6, 6).to(6, 10, 10).cube("#cable").end()
                .element().from(10, 6, 6).to(16, 10, 10).cube("#cable").end();
        simpleBlockItem(PanRegistration.PAN_CABLE.get(), item);
    }

    private ModelFile cableElement(
            String name, float fromX, float fromY, float fromZ,
            float toX, float toY, float toZ) {
        return models().getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("cable", ClayiumMod.id("block/pancable"))
                .texture("particle", "#cable")
                .renderType("minecraft:translucent")
                .element().from(fromX, fromY, fromZ).to(toX, toY, toZ).cube("#cable").end();
    }

    @Override
    public String getName() {
        return "PAN network blocks";
    }
}
