package net.claustra01.clayium.datagen.logistics;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlock;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class LogisticsBlockStateProvider extends BlockStateProvider {
    public LogisticsBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Logistics Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        for (Direction direction : Direction.values()) {
            for (BufferBlockEntity.FaceMode mode : new BufferBlockEntity.FaceMode[] {
                    BufferBlockEntity.FaceMode.INPUT, BufferBlockEntity.FaceMode.OUTPUT,
                    BufferBlockEntity.FaceMode.INPUT_OUTPUT}) {
                String name = "buffer_face_" + direction.getName() + "_" + mode.getSerializedName();
                String texture = mode == BufferBlockEntity.FaceMode.OUTPUT ? "export" : "import";
                var model = models().getBuilder(name)
                        .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                        .texture("overlay", ClayiumMod.id("block/" + texture))
                        .texture("particle", ClayiumMod.id("block/" + texture))
                        .renderType("minecraft:cutout");
                switch (direction) {
                    case NORTH -> model.element().from(0, 0, -0.01F).to(16, 16, 0)
                            .face(direction).texture("#overlay").end().end();
                    case SOUTH -> model.element().from(0, 0, 16).to(16, 16, 16.01F)
                            .face(direction).texture("#overlay").end().end();
                    case WEST -> model.element().from(-0.01F, 0, 0).to(0, 16, 16)
                            .face(direction).texture("#overlay").end().end();
                    case EAST -> model.element().from(16, 0, 0).to(16.01F, 16, 16)
                            .face(direction).texture("#overlay").end().end();
                    case UP -> model.element().from(0, 16, 0).to(16, 16.01F, 16)
                            .face(direction).texture("#overlay").end().end();
                    case DOWN -> model.element().from(0, -0.01F, 0).to(16, 0, 16)
                            .face(direction).texture("#overlay").end().end();
                }
            }
        }
        LogisticsRegistration.BUFFERS.values().forEach(definition -> {
            ResourceLocation hull = ClayiumMod.id("block/machinehull-" + (definition.tier() - 1));
            ModelFile model = models().getBuilder(definition.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", hull)
                    .texture("particle", "#hull")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end();
            var multipart = getMultipartBuilder(definition.block().get());
            multipart.part().modelFile(model).addModel().end();
            simpleBlockItem(definition.block().get(), model);
        });
    }
}
