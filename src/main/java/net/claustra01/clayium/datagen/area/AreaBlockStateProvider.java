package net.claustra01.clayium.datagen.area;

import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.world.area.AreaMachineBlock;
import net.claustra01.clayium.world.area.AreaMachineType;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;

public final class AreaBlockStateProvider extends BlockStateProvider {
    private static final Map<AreaMachineType, String> FRONTS = Map.of(
            AreaMachineType.ITEM_COLLECTOR, "areacollector",
            AreaMachineType.MINER, "areaminer",
            AreaMachineType.ADVANCED_MINER, "advareaminer",
            AreaMachineType.REPLACER, "areareplacer",
            AreaMachineType.ACTIVATOR, "areaactivator");

    public AreaBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        AreaMachineRegistration.MARKERS.values().forEach(definition -> {
            String id = definition.type().id();
            ModelFile model = models().getBuilder(id)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("body", switch (definition.type()) {
                        case NORMAL -> ResourceLocation.withDefaultNamespace("block/clay");
                        case OPEN_PIT -> ClayiumMod.id("block/compressedclay-0");
                        case GROUND_LEVELING -> ClayiumMod.id("block/compressedclay-1");
                        case PRISM -> ClayiumMod.id("block/compressedclay-2");
                    })
                    .texture("particle", "#body")
                    .element().from(5, 5, 5).to(11, 11, 11)
                    .face(Direction.DOWN).texture("#body").end()
                    .face(Direction.UP).texture("#body").end()
                    .face(Direction.NORTH).texture("#body").end()
                    .face(Direction.SOUTH).texture("#body").end()
                    .face(Direction.WEST).texture("#body").end()
                    .face(Direction.EAST).texture("#body").end().end();
            simpleBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
        AreaMachineRegistration.MACHINES.values().forEach(definition -> {
            AreaMachineType type = definition.type();
            ModelFile model = machineModel(type.id(), type, false);
            ModelFile active = machineModel(type.id() + "_active", type, true);
            getVariantBuilder(definition.block().get()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(state.getValue(AreaMachineBlock.ACTIVE) ? active : model)
                    .rotationY(((int)state.getValue(AreaMachineBlock.FACING).toYRot() + 180) % 360)
                    .build());
            simpleBlockItem(definition.block().get(), model);
        });
    }

    private ModelFile machineModel(String id, AreaMachineType type, boolean active) {
        String frontTexture = FRONTS.get(type) + (active ? "_1" : "");
        return models().getBuilder(id)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", ClayiumMod.id("block/machinehull-" + (type.tier() - 1)))
                    .texture("front", ClayiumMod.id("block/" + frontTexture))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#front").end().end();
    }

    @Override
    public String getName() {
        return "Clayium area markers and machines";
    }
}
