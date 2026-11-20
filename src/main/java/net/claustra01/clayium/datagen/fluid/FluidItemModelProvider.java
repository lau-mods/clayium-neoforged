package net.claustra01.clayium.datagen.fluid;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class FluidItemModelProvider extends ItemModelProvider {
    public FluidItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Fluid Transport Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        getBuilder("fluid_filter")
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", ClayiumMod.id("item/filterwhitelist"));
    }
}
