package net.claustra01.clayium.datagen.logistics;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class LogisticsItemModelProvider extends ItemModelProvider {
    public LogisticsItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Logistics Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        getBuilder("white_list_item_filter")
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", ClayiumMod.id("item/filterwhitelist"));
    }
}
