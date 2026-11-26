package net.claustra01.clayium.recipe;

import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Preserves the original Storage Container stack data while applying a Clay Core. */
public final class StorageContainerExpansionRecipe extends CustomRecipe {
    public StorageContainerExpansionRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findBasicContainer(input) != null && countClayCores(input) == 1
                && countOccupiedSlots(input) == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack basic = ItemStack.EMPTY;
        for (int index = 0; index < input.size(); index++) {
            if (input.getItem(index).is(StorageRegistration.STORAGE_CONTAINER_ITEM.get())) {
                basic = input.getItem(index);
                break;
            }
        }
        ItemStack expanded = basic.copyWithCount(1);
        CustomData oldData = expanded.get(DataComponents.CUSTOM_DATA);
        CompoundTag contents = oldData == null ? new CompoundTag() : oldData.copyTag();
        contents.putBoolean("expanded", true);
        expanded.set(DataComponents.CUSTOM_DATA, CustomData.of(contents));
        return expanded;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistration.STORAGE_CONTAINER_EXPANSION_SERIALIZER.get();
    }

    private static ItemStack findBasicContainer(CraftingInput input) {
        ItemStack found = null;
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (!stack.is(StorageRegistration.STORAGE_CONTAINER_ITEM.get())) {
                continue;
            }
            if (found != null || isExpanded(stack)) {
                return null;
            }
            found = stack;
        }
        return found;
    }

    private static boolean isExpanded(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.copyTag().getBoolean("expanded");
    }

    private static int countClayCores(CraftingInput input) {
        int count = 0;
        for (int index = 0; index < input.size(); index++) {
            if (input.getItem(index).is(MaterialRegistration.COMPONENTS.get("clay_core").get())) {
                count++;
            }
        }
        return count;
    }

    private static int countOccupiedSlots(CraftingInput input) {
        int count = 0;
        for (int index = 0; index < input.size(); index++) {
            if (!input.getItem(index).isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
