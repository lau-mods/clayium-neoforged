package net.claustra01.clayium.datagen.energyconverter;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.fe.EnergyConverterBlock;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class EnergyConverterBlockStateProvider extends BlockStateProvider {
    public EnergyConverterBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Energy Converter Block States: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        EnergyConverterRegistration.CONVERTERS.values().forEach(definition -> {
            EnergyConverterBlock block = definition.block().get();
            String id = definition.specification().id();
            int tier = definition.specification().tier();
            ResourceLocation hull = ClayiumMod.id("block/machinehull-" + (tier - 1));
            ModelFile model = models().getBuilder(id)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                    .texture("hull", hull)
                    .texture("front", ClayiumMod.id("block/rfgenerator"))
                    .texture("particle", "#hull")
                    .renderType("minecraft:cutout")
                    .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                    .element().from(0, 0, -0.01F).to(16, 16, 0)
                    .face(Direction.NORTH).texture("#front").end().end();
            horizontalBlock(block, model);
            simpleBlockItem(block, model);
        });
    }
}
