package mods.clayium.util.crafting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public class SmeltingRecipe extends SimpleMachinesRecipes {
    protected static int methodSmelting = 0;
    protected static int tierSmelting = 0;
    public static int energySmelting = 4;
    public static int timeSmelting = 200;

    public SmeltingRecipe(String recipeid) {
        super(recipeid);
    }

    @Deprecated
    protected Map getMaterialMap(List<ItemStack> materials, int method, int tier) {
        if (method == methodSmelting
            && tier >= tierSmelting
            && materials != null
            && materials.size() == 1
            && FurnaceRecipes.func_77602_a().func_151395_a(materials.get(0)) != null) {
            Map keyMap = new HashMap();
            keyMap.put("Material", materials);
            keyMap.put("Method", methodSmelting);
            keyMap.put("Tier", tierSmelting);
            return keyMap;
        } else {
            Entry entry = this.getResultEntry(materials, method, tier);
            return entry == null ? null : (Map)entry.getKey();
        }
    }

    @Deprecated
    protected Map getResultMap(List<ItemStack> materials, int method, int tier) {
        if (method == methodSmelting
            && tier >= tierSmelting
            && materials != null
            && materials.size() == 1
            && FurnaceRecipes.func_77602_a().func_151395_a(materials.get(0)) != null) {
            ItemStack result = FurnaceRecipes.func_77602_a().func_151395_a(materials.get(0));
            Map valueMap = new HashMap();
            valueMap.put("Result", Array2ArrayList(new ItemStack[]{result}));
            valueMap.put("Energy", energySmelting);
            valueMap.put("Time", timeSmelting);
            return valueMap;
        } else {
            Entry entry = this.getResultEntry(materials, method, tier);
            return entry == null ? null : (Map)entry.getValue();
        }
    }

    @Deprecated
    public boolean hasResult(List<ItemStack> materials, int tier) {
        return tier >= tierSmelting && materials != null && materials.size() == 1 && FurnaceRecipes.func_77602_a().func_151395_a(materials.get(0)) != null
            ? true
            : super.hasResult(materials, tier);
    }

    public boolean isCraftable(ItemStack material, int tier) {
        return tier >= tierSmelting && FurnaceRecipes.func_77602_a().func_151395_a(material) != null ? true : super.isCraftable(material, tier);
    }

    public Recipes.RecipeCondition getRecipeConditionFromRecipe(Object[] materials, int method, int tier, boolean sizeCheck) {
        if (materials == null) {
            return null;
        } else if (method == methodSmelting
            && tier >= tierSmelting
            && materials.length == 1
            && materials[0] instanceof ItemStack
            && FurnaceRecipes.func_77602_a().func_151395_a((ItemStack)materials[0]) != null) {
            ItemStack ingred = ((ItemStack)materials[0]).func_77946_l();
            ingred.field_77994_a = 1;
            return new Recipes.RecipeCondition(new ItemStack[]{ingred}, methodSmelting, tierSmelting);
        } else {
            return super.getRecipeConditionFromRecipe(materials, method, tier, sizeCheck);
        }
    }

    public Recipes.RecipeCondition getRecipeConditionFromInventory(ItemStack[] materials, int method, int tier, boolean sizeCheck) {
        if (materials == null) {
            return null;
        } else if (method == methodSmelting
            && tier >= tierSmelting
            && materials.length == 1
            && FurnaceRecipes.func_77602_a().func_151395_a(materials[0]) != null) {
            ItemStack ingred = materials[0].func_77946_l();
            ingred.field_77994_a = 1;
            return new Recipes.RecipeCondition(new ItemStack[]{ingred}, methodSmelting, tierSmelting);
        } else {
            return super.getRecipeConditionFromInventory(materials, method, tier, sizeCheck);
        }
    }

    public Recipes.RecipeResult getRecipeResult(Recipes.RecipeCondition condition) {
        if (condition == null) {
            return null;
        } else {
            ItemStack result;
            return condition.getMethod() == methodSmelting
                    && condition.getTier() >= tierSmelting
                    && condition.getMaterials() != null
                    && condition.getMaterials().length == 1
                    && condition.getMaterials()[0] instanceof ItemStack
                    && (result = FurnaceRecipes.func_77602_a().func_151395_a((ItemStack)condition.getMaterials()[0])) != null
                ? new Recipes.RecipeResult(new ItemStack[]{result}, (long)energySmelting, (long)timeSmelting)
                : super.getRecipeResult(condition);
        }
    }

    public int[] getConsumedStackSize(ItemStack[] materials, int method, int tier) {
        return method == methodSmelting
                && tier >= tierSmelting
                && materials != null
                && materials.length == 1
                && FurnaceRecipes.func_77602_a().func_151395_a(materials[0]) != null
            ? new int[]{1}
            : super.getConsumedStackSize(materials, method, tier);
    }
}
