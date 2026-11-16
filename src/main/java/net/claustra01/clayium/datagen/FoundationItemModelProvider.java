package net.claustra01.clayium.datagen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.FoundationItemRegistration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class FoundationItemModelProvider extends ItemModelProvider {
    public FoundationItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        generated(FoundationItemRegistration.INDUSTRIAL_CLAY_DUST.getId().getPath(), "indclaydust");
        generated(FoundationItemRegistration.CLAY_WRENCH.getId().getPath(), "claywrench");
        generated(FoundationItemRegistration.CLAY_PICKAXE.getId().getPath(), "claypickaxe");
        generated(FoundationItemRegistration.GADGET_HOLDER.getId().getPath(), "gadgetholder");
    }

    private void generated(String itemId, String textureName) {
        getBuilder(itemId)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", ClayiumMod.id("item/" + textureName));
    }
}
