package net.claustra01.clayium.datagen.machine;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MachineRegistration;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MachineBlockStateProvider extends BlockStateProvider {
    public MachineBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Processing Machine Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        MachineRegistration.MACHINES.values().forEach(definition -> {
            String id = definition.id();
            ResourceLocation hull = ClayiumMod.id("block/machinehull-" + Math.max(0, definition.tier() - 1));
            ResourceLocation front = ClayiumMod.id("block/" + definition.series().frontTexture());
            ModelFile model = models().getBuilder(id)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", hull)
                    .texture("front", front)
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#front").end().end();
            horizontalBlock(definition.block().get(), model);
            simpleBlockItem(definition.block().get(), model);
        });
    }
}
