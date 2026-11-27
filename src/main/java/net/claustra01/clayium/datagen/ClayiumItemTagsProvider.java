package net.claustra01.clayium.datagen;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.block.MetalMaterialBlock;
import net.claustra01.clayium.block.SiliconeColoredBlock;
import net.claustra01.clayium.item.ClayMaterialItem;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ClayiumItemTagsProvider extends ItemTagsProvider {
    public ClayiumItemTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

        for (var entry : ClayiumBlocks.all().entrySet()) {
            Block block = entry.getValue().get();
            Item item = ClayiumItems.byPath(entry.getKey()).get();
            if (block instanceof MetalMaterialBlock metalBlock) {
                tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + metalBlock.getMaterial().idPrefix())))
                        .add(item);
            } else if (block instanceof SiliconeColoredBlock) {
                tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/silicone")))
                        .add(item);
            } else if (block instanceof ClayOreBlock) {
                tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/clay")))
                        .add(item);
            }
        }

        for (var entry : ClayiumItems.standaloneItems().entrySet()) {
            Item item = entry.getValue().get();
            if (item instanceof ClayMaterialItem matItem) {
                CShape shape = matItem.getShape();
                String matKey = matItem.getMaterial().idPrefix();
                tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", shape.tagFolder()))).add(item);
                tag(ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", shape.tagFolder() + "/" + matKey))).add(item);
                tag(ItemTags.create(ClayiumMod.id("shapes/" + shape.idSuffix()))).add(item);
            }
        }

        // Circuit & special OreDict aliases from CItems.java & CMaterials.java
        tag(ItemTags.create(ClayiumMod.id("circuits/basic"))).add(ClayiumItems.byPath("advanced_circuit").get());
        tag(ItemTags.create(ClayiumMod.id("circuits/advanced"))).add(ClayiumItems.byPath("precision_circuit").get());
        tag(ItemTags.create(ClayiumMod.id("circuits/elite"))).add(ClayiumItems.byPath("integrated_circuit").get());
        tag(ItemTags.create(ClayiumMod.id("circuits/ultimate"))).add(ClayiumItems.byPath("clay_core").get());
        tag(ItemTags.create(ClayiumMod.id("silicon"))).add(ClayiumItems.byPath("silicon_plate").get());
        tag(ItemTags.create(ClayiumMod.id("salt"))).add(ClayiumItems.byPath("salt").get());
    }
}
