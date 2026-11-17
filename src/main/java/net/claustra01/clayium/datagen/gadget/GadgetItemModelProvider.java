package net.claustra01.clayium.datagen.gadget;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.gadget.GadgetType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class GadgetItemModelProvider extends ItemModelProvider {
    public GadgetItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Gadget Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        for (GadgetType gadget : GadgetType.values()) {
            getBuilder(gadget.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", ClayiumMod.id("item/" + gadget.texture()));
        }
    }
}
