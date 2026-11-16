package net.claustra01.clayium.datagen.material;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.ClayMaterials;
import net.claustra01.clayium.material.MaterialShape;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class MaterialItemTagProvider extends ItemTagsProvider {
    private static final Map<MaterialShape, String> TAG_PATHS = Map.of(
            MaterialShape.DUST, "dusts",
            MaterialShape.INGOT, "ingots",
            MaterialShape.PLATE, "plates",
            MaterialShape.LARGE_PLATE, "plates/large",
            MaterialShape.GEM, "gems");

    public MaterialItemTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (var variant : ClayMaterials.VARIANTS) {
            String root = TAG_PATHS.get(variant.shape());
            if (root == null) {
                continue;
            }
            Item item = MaterialRegistration.MATERIAL_ITEMS.get(variant.id()).get();
            tag(common(root)).add(item);
            String materialName = variant.id();
            if (variant.shape() == MaterialShape.LARGE_PLATE && materialName.startsWith("large_")) {
                materialName = materialName.substring(6);
            }
            String suffix = variant.shape() == MaterialShape.LARGE_PLATE
                    ? "_plate" : "_" + variant.shape().name().toLowerCase();
            if (materialName.endsWith(suffix)) {
                materialName = materialName.substring(0, materialName.length() - suffix.length());
            }
            tag(common(root + "/" + materialName)).add(item);
        }
        tag(common("ingots")).add(MaterialRegistration.MATERIAL_ITEMS.get("impure_osmium_ingot").get());
        tag(common("ingots/impure_osmium")).add(MaterialRegistration.MATERIAL_ITEMS.get("impure_osmium_ingot").get());
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
