package net.claustra01.clayium.datagen.machine;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.environment.EnvironmentSeries;
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
        MachineRegistration.MACHINES.values().forEach(definition ->
                machine(definition.id(), definition.tier(), definition.series().frontTexture(), definition.block().get()));
        FormingMachineRegistration.MACHINES.values().forEach(definition ->
                machine(definition.id(), definition.tier(), definition.series().frontTexture(), definition.block().get()));
        EnvironmentMachineRegistration.SALT_EXTRACTORS.values().forEach(definition ->
                machine(definition.id(), definition.tier(),
                        EnvironmentSeries.SALT_EXTRACTOR.frontTexture(), definition.block().get()));
        AssemblyMachineRegistration.MACHINES.values().forEach(definition ->
                machine(definition.id(), definition.tier(), definition.series().frontTexture(), definition.block().get()));
    }

    private void machine(String id, int tier, String frontTexture, ClayMachineBlock block) {
        ResourceLocation hull = ClayiumMod.id("block/machinehull-" + Math.max(0, tier - 1));
        ResourceLocation front = ClayiumMod.id("block/" + frontTexture);
        ModelFile model = models().getBuilder(id)
                .parent(new ModelFile.UncheckedModelFile("minecraft:block/block"))
                .texture("hull", hull)
                .texture("front", front)
                .texture("particle", "#hull")
                .renderType("minecraft:cutout")
                .element().from(0, 0, 0).to(16, 16, 16).cube("#hull").end()
                .element().from(0, 0, -0.01F).to(16, 16, 0)
                .face(Direction.NORTH).texture("#front").end().end();
        horizontalBlock(block, model);
        simpleBlockItem(block, model);
    }
}
