package net.claustra01.clayium.datagen.machine;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class AssemblyMachineModels extends BlockStateProvider {
    public AssemblyMachineModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Assembly Machine Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerStatesAndModels() {
        AssemblyMachineRegistration.MACHINE_HULLS.forEach((tier, hull) ->
                hull(hull.getId().getPath(), hull.get(), tier));
    }

    private void hull(String id, Block block, int tier) {
        ResourceLocation texture = ClayiumMod.id("block/machinehull-" + (tier - 1));
        ModelFile model = models().cubeAll(id, texture);
        simpleBlockWithItem(block, model);
    }
}
