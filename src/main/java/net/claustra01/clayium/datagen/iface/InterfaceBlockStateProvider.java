package net.claustra01.clayium.datagen.iface;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.iface.InterfaceBlock;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class InterfaceBlockStateProvider extends BlockStateProvider {
    public InterfaceBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        InterfaceRegistration.INTERFACES.values().forEach(definition -> {
            ModelFile model = interfaceModel(
                    definition.id(), definition.tier(), definition.type().texture(), false, false);
            ModelFile linked = interfaceModel(
                    definition.id() + "_linked", definition.tier(), definition.type().texture(), true, false);
            ModelFile disconnected = interfaceModel(
                    definition.id() + "_disconnected", definition.tier(), definition.type().texture(), true, true);
            getVariantBuilder(definition.block().get()).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(!state.getValue(InterfaceBlock.LINKED)
                            ? model
                            : state.getValue(InterfaceBlock.DISCONNECTED) ? disconnected : linked)
                    .rotationY(((int)state.getValue(InterfaceBlock.FACING).toYRot() + 180) % 360)
                    .build());
            simpleBlockItem(definition.block().get(), model);
        });
    }

    private ModelFile interfaceModel(String id, int tier, String texture, boolean linked, boolean disconnected) {
        var builder = models().getBuilder(id)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", ClayiumMod.id("block/machinehull-" + (tier - 1)))
                    .texture("front", ClayiumMod.id("block/" + texture))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#front").end().end();
        if (linked) {
            builder.element().from(3, 3, -1.0F).to(13, 13, -0.01F)
                    .face(Direction.NORTH).texture("#front").end()
                    .face(Direction.UP).texture("#hull").end()
                    .face(Direction.DOWN).texture("#hull").end()
                    .face(Direction.WEST).texture("#hull").end()
                    .face(Direction.EAST).texture("#hull").end().end();
        }
        if (disconnected) {
            builder.texture("status", ClayiumMod.id("block/export_rf"))
                    .element().from(5, 5, -1.01F).to(11, 11, -1.0F)
                    .face(Direction.NORTH).texture("#status").end().end();
        }
        return builder;
    }

    @Override
    public String getName() {
        return "Clayium interface block states";
    }
}
