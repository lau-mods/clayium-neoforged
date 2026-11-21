package net.claustra01.clayium.datagen.laser;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.laser.ClayEnergyLaserBlock;
import net.claustra01.clayium.laser.LaserReflectorBlock;
import net.claustra01.clayium.registry.LaserRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class LaserBlockStateProvider extends BlockStateProvider {
    public LaserBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        LaserRegistration.LASERS.values().forEach(definition -> {
            ModelFile model = models().getBuilder(definition.specification().id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", ClayiumMod.id("block/machinehull-" + (definition.specification().tier() - 1)))
                    .texture("front", ClayiumMod.id("block/energylaser"))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#front").end().end();
            directionalVariants(definition.block().get(), ClayEnergyLaserBlock.FACING, model);
            simpleBlockItem(definition.block().get(), model);
        });

        ModelFile reflector = models().getBuilder("laser_reflector")
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("reflector", ClayiumMod.id("block/laserreflector"))
                .texture("particle", "#reflector")
                .renderType("minecraft:translucent");
        directionalVariants(LaserRegistration.LASER_REFLECTOR.get(), LaserReflectorBlock.FACING, reflector);
        ModelFile reflectorItem = models().getBuilder("laser_reflector_item")
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("reflector", ClayiumMod.id("block/laserreflector"))
                .texture("particle", "#reflector")
                .renderType("minecraft:translucent")
                .element().from(4, 2, 4).to(12, 14, 12)
                .face(Direction.DOWN).texture("#reflector").end()
                .face(Direction.UP).texture("#reflector").end()
                .face(Direction.NORTH).texture("#reflector").end()
                .face(Direction.SOUTH).texture("#reflector").end()
                .face(Direction.WEST).texture("#reflector").end()
                .face(Direction.EAST).texture("#reflector").end().end();
        itemModels().getBuilder("laser_reflector").parent(reflectorItem);
    }

    private void directionalVariants(
            net.minecraft.world.level.block.Block block,
            net.minecraft.world.level.block.state.properties.DirectionProperty facing,
            ModelFile model) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction direction = state.getValue(facing);
            int rotationX = switch (direction) {
                case UP -> 270;
                case DOWN -> 90;
                default -> 0;
            };
            int rotationY = direction.getAxis().isVertical() ? 0 : ((int)direction.toYRot() + 180) % 360;
            return ConfiguredModel.builder().modelFile(model)
                    .rotationX(rotationX).rotationY(rotationY).build();
        });
    }

    @Override
    public String getName() {
        return "Clay laser block states";
    }
}
