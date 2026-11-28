package net.claustra01.clayium.pan;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * PAN Adapter factory for adjacent vanilla Furnaces (cost: 4 CE/t * 200t = 800 CE).
 */
public class PANFurnaceFactory implements IPANAdapterConversionFactory {
    public static final double ENERGY = 4.0 * 200.0;

    @Override
    public boolean matches(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.FURNACE);
    }

    @Override
    @Nullable
    public PANConversion getConversion(IPANAdapter adapter) {
        Level level = adapter.getConnectedLevel();
        if (level == null) {
            return null;
        }
        ItemStack ingredient = ItemStack.EMPTY;
        for (ItemStack p : adapter.getPatternItems()) {
            if (!p.isEmpty()) {
                if (!ingredient.isEmpty()) {
                    return null;
                }
                ingredient = p.copyWithCount(1);
            }
        }
        if (ingredient.isEmpty()) {
            return null;
        }

        SingleRecipeInput input = new SingleRecipeInput(ingredient);
        Optional<RecipeHolder<SmeltingRecipe>> found =
                level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
        if (found.isEmpty()) {
            return null;
        }
        SmeltingRecipe recipe = found.get().value();
        ItemStack result = recipe.assemble(input, level.registryAccess());
        if (result.isEmpty()) {
            return null;
        }
        Ingredient recipeIng = recipe.getIngredients().isEmpty()
                ? Ingredient.of(ingredient)
                : recipe.getIngredients().getFirst();
        return new PANConversion(List.of(new CountedIngredient(recipeIng, 1)), List.of(result), ENERGY);
    }
}
