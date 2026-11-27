package mods.clayium.util.crafting;

import net.minecraft.item.ItemStack;

public class SimpleMachinesRecipes extends Recipes {
    public SimpleMachinesRecipes(String recipeid) {
        super(recipeid);
    }

    public void addRecipe(Object materials, int tier, ItemStack results, long energy, long time) {
        this.addRecipe(new Object[]{materials}, 0, tier, new ItemStack[]{results}, energy, time);
    }

    public int getConsumedStackSize(ItemStack materials, int tier) {
        int[] res = this.getConsumedStackSize(new ItemStack[]{materials}, 0, tier);
        return res == null ? 0 : res[0];
    }

    public ItemStack getResult(ItemStack materials, int tier) {
        ItemStack[] res = this.getResult(new ItemStack[]{materials}, 0, tier);
        return res == null ? null : res[0];
    }

    public long getEnergy(ItemStack materials, int tier) {
        return this.getEnergy(new ItemStack[]{materials}, 0, tier);
    }

    public long getTime(ItemStack materials, int tier) {
        return this.getTime(new ItemStack[]{materials}, 0, tier);
    }

    public boolean hasResult(ItemStack materials, int tier) {
        return this.hasResult(new ItemStack[]{materials}, tier);
    }

    public void addRecipe(Object materials, ItemStack results) {
        this.addRecipe(materials, results, 1L);
    }

    public void addRecipe(Object materials, ItemStack results, long time) {
        this.addRecipe(materials, results, 1L, time);
    }

    public void addRecipe(Object materials, ItemStack results, long energy, long time) {
        this.addRecipe(materials, 0, results, energy, time);
    }

    public int getConsumedStackSize(ItemStack materials) {
        return this.getConsumedStackSize(materials, 0);
    }

    public ItemStack getResult(ItemStack materials) {
        return this.getResult(materials, 0);
    }

    public long getEnergy(ItemStack materials) {
        return this.getEnergy(materials, 0);
    }

    public long getTime(ItemStack materials) {
        return this.getTime(materials, 0);
    }

    public boolean hasResult(ItemStack materials) {
        return this.hasResult(materials, 0);
    }
}
