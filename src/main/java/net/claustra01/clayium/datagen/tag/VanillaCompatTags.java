package net.claustra01.clayium.datagen.tag;

import java.util.concurrent.CompletableFuture;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.storage.MetalChestType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class VanillaCompatTags extends ItemTagsProvider {
    public static final TagKey<Item> LASER_TREE_SOURCES = TagKey.create(
            Registries.ITEM, ClayiumMod.id("laser_tree_sources"));
    public static final TagKey<Item> METAL_CHESTS = TagKey.create(Registries.ITEM, ClayiumMod.id("metal_chests"));

    public VanillaCompatTags(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
            ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ClayiumMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(LASER_TREE_SOURCES).addTag(ItemTags.SAPLINGS);
        var metalChests = tag(METAL_CHESTS);
        for (MetalChestType type : MetalChestType.ALL) {
            metalChests.add(StorageRegistration.METAL_CHESTS.get(type.id()).block().get().asItem());
        }
    }

    @Override
    public String getName() {
        return "Vanilla compatibility item tags";
    }
}
