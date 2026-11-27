package net.claustra01.clayium.util;

import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

/**
 * Helper mapping compressed clay tiers {@code 0..13} to items matching 1.7.10 {@code TileAutoClayCondenser} / {@code TileSolarClayFabricator}.
 */
public final class CompressedClayHelper {
    public static final int MAX_CLAY_TIER = 13;
    private static final TagKey<Item> LITHIUM_INGOT_TAG = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", "ingots/lithium"));

    private static final String[] TIER_ITEM_PATHS = {
            null, // Tier 0 is vanilla Clay block (Blocks.CLAY)
            "dense_clay",
            "compressed_clay",
            "industrial_clay",
            "advanced_industrial_clay",
            "energetic_clay",
            "compressed_energetic_clay",
            "double_compressed_energetic_clay",
            "triple_compressed_energetic_clay",
            "quadruple_compressed_energetic_clay",
            "quintuple_compressed_energetic_clay",
            "sextuple_compressed_energetic_clay",
            "septuple_compressed_energetic_clay",
            "octuple_compressed_energetic_clay"
    };

    private CompressedClayHelper() {}

    public static ItemStack getCompressedClay(int tier) {
        return getCompressedClay(tier, 1);
    }

    public static ItemStack getCompressedClay(int tier, int count) {
        if (tier < 0 || tier > MAX_CLAY_TIER || count <= 0) {
            return ItemStack.EMPTY;
        }
        if (tier == 0) {
            return new ItemStack(Blocks.CLAY, count);
        }
        return new ItemStack(ClayiumItems.byPath(TIER_ITEM_PATHS[tier]).get(), count);
    }

    public static int getTierOfCompressedClay(ItemStack stack) {
        return getTierOfCompressedClay(stack, true);
    }

    public static int getTierOfCompressedClay(ItemStack stack, boolean acceptOthers) {
        if (stack.isEmpty()) {
            return -1;
        }
        if (stack.is(Items.CLAY)) {
            return 0;
        }
        for (int i = 1; i <= MAX_CLAY_TIER; i++) {
            if (stack.is(ClayiumItems.byPath(TIER_ITEM_PATHS[i]).get())) {
                return i;
            }
        }
        if (!acceptOthers) {
            return -1;
        }
        if (stack.is(Items.SAND)) {
            return 2;
        }
        if (stack.is(LITHIUM_INGOT_TAG) || stack.is(ClayiumItems.byPath("lithium_ingot").get())) {
            return 8;
        }
        return -1;
    }
}
