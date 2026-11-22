package net.claustra01.clayium.datagen.storage;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class StorageItemModelProvider extends ItemModelProvider {
    public StorageItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Storage Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        StorageRegistration.METAL_CHESTS.values().forEach(definition -> getBuilder(definition.type().id())
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/chest"))
                .texture("particle", ClayiumMod.id("block/metalchest_particle")));
    }
}
