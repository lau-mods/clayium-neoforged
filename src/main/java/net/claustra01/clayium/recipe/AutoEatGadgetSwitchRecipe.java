package net.claustra01.clayium.recipe;

import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Shapeless toggle recipe between gadget_auto_eat0 and gadget_auto_eat1 preserving FILTER_CONFIG DataComponent.
 */
public class AutoEatGadgetSwitchRecipe extends CustomRecipe {
    public AutoEatGadgetSwitchRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static ItemStack findSingleGadget(CraftingInput input) {
        ItemStack found = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (!found.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                found = stack;
            }
        }
        return found;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack found = findSingleGadget(input);
        if (found.isEmpty()) {
            return false;
        }
        return found.is(ClayiumItems.byPath("gadget_auto_eat0").get())
                || found.is(ClayiumItems.byPath("gadget_auto_eat1").get());
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack found = findSingleGadget(input);
        Item g0 = ClayiumItems.byPath("gadget_auto_eat0").get();
        Item g1 = ClayiumItems.byPath("gadget_auto_eat1").get();
        Item target = found.is(g0) ? g1 : g0;
        ItemStack out = new ItemStack(target, 1);
        var cfg = found.get(ClayiumDataComponents.FILTER_CONFIG.get());
        if (cfg != null) {
            out.set(ClayiumDataComponents.FILTER_CONFIG.get(), cfg);
        }
        return out;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.AUTO_EAT_GADGET_SWITCH.get();
    }
}
