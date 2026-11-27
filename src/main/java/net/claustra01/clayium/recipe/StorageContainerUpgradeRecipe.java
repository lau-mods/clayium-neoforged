package net.claustra01.clayium.recipe;

import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Port of 1.7.10 CRecipes L2294: upgrades a 65536-capacity storage_container with clay_core
 * to Integer.MAX_VALUE capacity while preserving stored item and count.
 */
public class StorageContainerUpgradeRecipe extends CustomRecipe {
    public StorageContainerUpgradeRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findPair(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack container = findPair(input);
        if (container == null) {
            return ItemStack.EMPTY;
        }
        ItemStack upgraded = container.copyWithCount(1);
        StorageContentData existing = upgraded.get(ClayiumDataComponents.STORAGE_CONTENT.get());
        if (existing == null) {
            upgraded.set(ClayiumDataComponents.STORAGE_CONTENT.get(),
                    new StorageContentData(Items.AIR, 0L, Integer.MAX_VALUE));
        } else {
            upgraded.set(ClayiumDataComponents.STORAGE_CONTENT.get(),
                    new StorageContentData(existing.item(), existing.count(), Integer.MAX_VALUE));
        }
        return upgraded;
    }

    private static ItemStack findPair(CraftingInput input) {
        Item storageItem = ClayiumItems.byPath("storage_container").get();
        Item coreItem = ClayiumItems.byPath("clay_core").get();
        ItemStack foundContainer = null;
        boolean foundCore = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(storageItem)) {
                if (foundContainer != null) {
                    return null;
                }
                StorageContentData data = stack.get(ClayiumDataComponents.STORAGE_CONTENT.get());
                int max = data == null ? StorageContentData.DEFAULT_MAX_STORAGE : data.maxStorage();
                if (max != StorageContentData.DEFAULT_MAX_STORAGE) {
                    return null;
                }
                foundContainer = stack;
            } else if (stack.is(coreItem)) {
                if (foundCore) {
                    return null;
                }
                foundCore = true;
            } else {
                return null;
            }
        }
        return foundContainer != null && foundCore ? foundContainer : null;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.STORAGE_CONTAINER_UPGRADE.get();
    }
}
