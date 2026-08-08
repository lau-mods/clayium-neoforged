/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

/** Maps material forms to the modern {@code c} common tags replacing OreDictionary inputs. */
public final class CommonMaterialTags {
    private record Form(String suffix,String directory) {}
    private static final Form[] FORMS={
            new Form("_large_plate","large_plates"),new Form("_ingot","ingots"),
            new Form("_dust","dusts"),new Form("_plate","plates"),new Form("_block","storage_blocks")};

    private CommonMaterialTags() {}

    public static Ingredient ingredient(ItemLike fallback){
        return tagFor(fallback).map(Ingredient::of).orElseGet(()->Ingredient.of(fallback));
    }

    public static Ingredient any(ItemLike...values){
        return net.neoforged.neoforge.common.crafting.CompoundIngredient.of(
                java.util.Arrays.stream(values).map(CommonMaterialTags::ingredient).toArray(Ingredient[]::new));
    }

    public static Optional<TagKey<Item>> tagFor(ItemLike value){
        Item item=value.asItem();
        if(item==Items.IRON_INGOT)return Optional.of(common("ingots/iron"));
        if(item==Items.GOLD_INGOT)return Optional.of(common("ingots/gold"));
        if(item==Items.COPPER_INGOT)return Optional.of(common("ingots/copper"));
        if(item==Items.REDSTONE)return Optional.of(common("dusts/redstone"));
        if(item==Items.DIAMOND)return Optional.of(common("gems/diamond"));
        if(item==Items.EMERALD)return Optional.of(common("gems/emerald"));
        if(item==Items.QUARTZ)return Optional.of(common("gems/quartz"));
        if(item==Items.LAPIS_LAZULI)return Optional.of(common("gems/lapis"));
        if(item==Items.COAL)return Optional.of(common("gems/coal"));
        if(item==Items.CHARCOAL)return Optional.of(common("gems/charcoal"));
        if(item==Blocks.COAL_BLOCK.asItem())return Optional.of(common("storage_blocks/coal"));
        ResourceLocation key=BuiltInRegistries.ITEM.getKey(item);
        if(!key.getNamespace().equals("clayium_neoforged"))return Optional.empty();
        String path=key.getPath();
        // The original early-game component predates the material_form naming used by later plates.
        if(path.equals("large_clay_plate"))return Optional.of(common("large_plates/clay"));
        for(Form form:FORMS)if(path.endsWith(form.suffix())){
            String material=canonicalMaterial(path.substring(0,path.length()-form.suffix().length()));
            return Optional.of(common(form.directory()+"/"+material));
        }
        return Optional.empty();
    }

    public static TagKey<Item> common(String path){
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c",path));
    }

    public static String canonicalMaterial(String material){
        return material.equals("aluminium")?"aluminum":material;
    }
}
