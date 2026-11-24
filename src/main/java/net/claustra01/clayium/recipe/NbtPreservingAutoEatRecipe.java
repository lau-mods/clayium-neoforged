package net.claustra01.clayium.recipe;

import net.claustra01.clayium.gadget.GadgetType;
import net.claustra01.clayium.registry.GadgetRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Original bidirectional Auto Eat conversion, retaining every input data component. */
public final class NbtPreservingAutoEatRecipe extends CustomRecipe {
    public NbtPreservingAutoEatRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findOnlyGadget(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack source = findOnlyGadget(input);
        if (source == null) {
            return ItemStack.EMPTY;
        }
        Item target = source.is(gadget(GadgetType.ECONOMICAL_AUTO_EAT))
                ? gadget(GadgetType.AUTO_EAT)
                : gadget(GadgetType.ECONOMICAL_AUTO_EAT);
        return source.transmuteCopy(target, 1);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(
                gadget(GadgetType.ECONOMICAL_AUTO_EAT), gadget(GadgetType.AUTO_EAT)));
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(gadget(GadgetType.AUTO_EAT));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistration.NBT_PRESERVING_AUTO_EAT_SERIALIZER.get();
    }

    private static ItemStack findOnlyGadget(CraftingInput input) {
        ItemStack found = null;
        for (int index = 0; index < input.size(); index++) {
            ItemStack stack = input.getItem(index);
            if (stack.isEmpty()) {
                continue;
            }
            if (found != null || (!stack.is(gadget(GadgetType.ECONOMICAL_AUTO_EAT))
                    && !stack.is(gadget(GadgetType.AUTO_EAT)))) {
                return null;
            }
            found = stack;
        }
        return found;
    }

    private static Item gadget(GadgetType type) {
        return GadgetRegistration.GADGETS_BY_TYPE.get(type).get();
    }
}
