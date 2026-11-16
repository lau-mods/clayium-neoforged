package net.claustra01.clayium.datagen.material;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.ClayMaterials;
import net.claustra01.clayium.material.MaterialAppearance;
import net.claustra01.clayium.material.MaterialShape;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MaterialItemModelProvider extends ItemModelProvider {
    public MaterialItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Material Item Models: " + ClayiumMod.MOD_ID;
    }

    @Override
    protected void registerModels() {
        for (var variant : ClayMaterials.VARIANTS) {
            if (variant.shape() == MaterialShape.BLOCK || variant.id().startsWith("minecraft:")
                    || variant.id().equals("industrial_clay_dust")) {
                continue;
            }
            String dedicated = MaterialAppearance.dedicatedTexture(variant);
            if (dedicated != null) {
                ResourceLocation texture = dedicated.contains(":")
                        ? ResourceLocation.parse(dedicated)
                        : ClayiumMod.id("item/" + dedicated);
                getBuilder(variant.id())
                        .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                        .texture("layer0", texture);
            } else {
                if (variant.shape() == MaterialShape.BALL) {
                    getBuilder(variant.id())
                            .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                            .texture("layer0", ResourceLocation.parse("minecraft:item/clay_ball"));
                    continue;
                }
                String generic = MaterialAppearance.genericTexture(variant);
                getBuilder(variant.id())
                        .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                        .texture("layer0", ClayiumMod.id("item/" + generic + "_base"))
                        .texture("layer1", ClayiumMod.id("item/" + generic + "_dark"))
                        .texture("layer2", ClayiumMod.id("item/" + generic + "_light"));
            }
        }
        getBuilder("impure_osmium_ingot")
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", ClayiumMod.id("item/ingot_base"))
                .texture("layer1", ClayiumMod.id("item/ingot_dark"))
                .texture("layer2", ClayiumMod.id("item/ingot_light"));
        for (MaterialRegistration.ComponentDefinition definition : MaterialRegistration.COMPONENT_DEFINITIONS) {
            getBuilder(definition.id())
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", ClayiumMod.id("item/" + definition.texture()));
        }
    }
}
