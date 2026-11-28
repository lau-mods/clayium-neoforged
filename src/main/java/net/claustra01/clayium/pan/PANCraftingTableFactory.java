package net.claustra01.clayium.pan;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * PAN Adapter factory for adjacent vanilla Crafting Tables (fixed cost: 10 CE).
 */
public class PANCraftingTableFactory implements IPANAdapterConversionFactory {
    public static final double ENERGY = 10.0;

    @Override
    public boolean matches(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.CRAFTING_TABLE);
    }

    @Override
    @Nullable
    public PANConversion getConversion(IPANAdapter adapter) {
        Level level = adapter.getConnectedLevel();
        if (level == null) {
            return null;
        }
        List<ItemStack> pattern = adapter.getPatternItems();
        boolean allEmpty = true;
        for (ItemStack stack : pattern) {
            if (!stack.isEmpty()) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) {
            return null;
        }

        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(i < pattern.size() ? pattern.get(i) : ItemStack.EMPTY);
        }
        CraftingInput craftingInput = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> found =
                level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInput, level);
        if (found.isEmpty()) {
            return null;
        }
        CraftingRecipe recipe = found.get().value();
        if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
            return null;
        }

        ItemStack result = recipe.assemble(craftingInput, level.registryAccess());
        if (result.isEmpty()) {
            return null;
        }
        List<CountedIngredient> inputs = new ArrayList<>();
        for (Ingredient ing : recipe.getIngredients()) {
            if (!ing.isEmpty()) {
                inputs.add(new CountedIngredient(ing, 1));
            }
        }
        return new PANConversion(inputs, List.of(result), ENERGY);
    }
}
