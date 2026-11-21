package net.claustra01.clayium.datagen.iface;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class InterfaceItemModelProvider extends ItemModelProvider {
    public InterfaceItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Clayium interface item models";
    }

    @Override
    protected void registerModels() {
        getBuilder("synchronizer")
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", ClayiumMod.id("item/synchronizer"));
    }
}
