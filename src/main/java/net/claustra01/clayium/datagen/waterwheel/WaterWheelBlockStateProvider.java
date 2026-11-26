package net.claustra01.clayium.datagen.waterwheel;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.waterwheel.WaterWheelBlock;
import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class WaterWheelBlockStateProvider extends BlockStateProvider {
    public WaterWheelBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Water Wheel Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        WaterWheelRegistration.DEFINITIONS.forEach(definition -> {
            ResourceLocation hull = ClayiumMod.id("block/machinehull-" + (definition.tier() - 1));
            ModelFile idle = model(definition.id(), hull, ClayiumMod.id("block/waterwheel"));
            ModelFile active = model(definition.id() + "_active", hull, ClayiumMod.id("block/waterwheel_1"));
            getVariantBuilder(definition.block().get()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(state.getValue(WaterWheelBlock.ACTIVE) ? active : idle)
                    .rotationY(rotation(state.getValue(WaterWheelBlock.FACING)))
                    .build());
            simpleBlockItem(definition.block().get(), idle);
        });
    }

    private ModelFile model(String id, ResourceLocation hull, ResourceLocation front) {
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

    private static int rotation(Direction direction) {
        return switch (direction) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }
}
