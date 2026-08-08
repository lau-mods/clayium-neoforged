/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.data;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Publishes every interoperable Clayium material form into common {@code c} tags. */
public final class ClayiumItemTagProvider extends ItemTagsProvider {
    public ClayiumItemTagProvider(PackOutput output,CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper files){
        super(output,lookup,CompletableFuture.completedFuture(TagsProvider.TagLookup.<Block>empty()),Clayium.MODID,files);
    }

    @Override protected void addTags(HolderLookup.Provider provider){
        // Iterate the complete item registry rather than only MaterialCatalog: early clay components
        // (clay_plate, dense_clay_plate, etc.) are common material forms as well.
        ClayiumRegistries.ITEMS.getEntries().forEach(holder->{
            var value=holder.get();
            CommonMaterialTags.tagFor(value).ifPresent(materialTag->{
                tag(materialTag).add(value);
                String path=materialTag.location().getPath();
                tag(CommonMaterialTags.common(path.substring(0,path.indexOf('/')))).addTag(materialTag);
            });
        });
        tag(CommonMaterialTags.common("ingots/iron")).add(Items.IRON_INGOT);
        tag(CommonMaterialTags.common("ingots/gold")).add(Items.GOLD_INGOT);
        tag(CommonMaterialTags.common("ingots/copper")).add(Items.COPPER_INGOT);
        tag(CommonMaterialTags.common("ingots")).addTag(CommonMaterialTags.common("ingots/iron"))
                .addTag(CommonMaterialTags.common("ingots/gold")).addTag(CommonMaterialTags.common("ingots/copper"));
        tag(CommonMaterialTags.common("dusts/redstone")).add(Items.REDSTONE);
        tag(CommonMaterialTags.common("gems/diamond")).add(Items.DIAMOND);
        tag(CommonMaterialTags.common("gems/emerald")).add(Items.EMERALD);
        tag(CommonMaterialTags.common("gems/quartz")).add(Items.QUARTZ);
        tag(CommonMaterialTags.common("gems/lapis")).add(Items.LAPIS_LAZULI);
        tag(CommonMaterialTags.common("gems/coal")).add(Items.COAL);
        tag(CommonMaterialTags.common("gems/charcoal")).add(Items.CHARCOAL);
        tag(CommonMaterialTags.common("storage_blocks/coal")).add(Blocks.COAL_BLOCK.asItem());
        tag(CommonMaterialTags.common("storage_blocks")).addTag(CommonMaterialTags.common("storage_blocks/coal"));
        tag(CommonMaterialTags.common("glass_blocks")).addTag(ItemTags.create(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("c","glass_blocks/colorless")));
    }
}
