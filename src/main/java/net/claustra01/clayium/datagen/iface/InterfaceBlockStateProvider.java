package net.claustra01.clayium.datagen.iface;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class InterfaceBlockStateProvider extends BlockStateProvider {
    public InterfaceBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        InterfaceRegistration.INTERFACES.values().forEach(definition -> {
            ModelFile model = models().getBuilder(definition.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", ClayiumMod.id("block/machinehull-" + (definition.tier() - 1)))
                    .texture("front", ClayiumMod.id("block/" + definition.type().texture()))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#front").end().end();
            horizontalBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }

    @Override
    public String getName() {
        return "Clayium interface block states";
    }
}
