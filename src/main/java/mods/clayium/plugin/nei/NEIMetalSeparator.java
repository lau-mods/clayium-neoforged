package mods.clayium.plugin.nei;

import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.CachedRecipe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import mods.clayium.block.tile.TileChemicalMetalSeparator;
import mods.clayium.item.CMaterials;
import mods.clayium.util.crafting.WeightedList;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class NEIMetalSeparator extends NEIClayMachines {
    private Comparator<CachedRecipe> comp = new NEIMetalSeparator.NEIRecipeComparator();

    public NEIMetalSeparator() {
        super("ChemicalMetalSeparator");
    }

    public Comparator<CachedRecipe> getComparator() {
        return this.comp;
    }

    public Iterable<INEIRecipeEntry> getMatchedSet() {
        List<INEIRecipeEntry> list = new ArrayList<>();
        WeightedList<ItemStack> recipeResults = TileChemicalMetalSeparator.results;

        for (int i = 0; i < recipeResults.size(); i++) {
            list.add(this.createRecipeCacher(recipeResults, i));
        }

        return list;
    }

    public List<Object> getMaterialList(WeightedList<ItemStack> recipeResults, int index) {
        List<Object> materiallist = new ArrayList<>();
        materiallist.add(CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST));
        return materiallist;
    }

    public List<ItemStack> getResultList(WeightedList<ItemStack> recipeResults, int index) {
        List<ItemStack> resultlist = new ArrayList<>();
        resultlist.add(recipeResults.get(index).result);
        return resultlist;
    }

    public NEIMetalSeparator.NEIRecipeEntryMetalSeparator createRecipeCacher(WeightedList<ItemStack> recipeResults, int index) {
        List<Object> materiallist = this.getMaterialList(recipeResults, index);
        List<ItemStack> resultlist = this.getResultList(recipeResults, index);
        return new NEIMetalSeparator.NEIRecipeEntryMetalSeparator(
            materiallist,
            0,
            6,
            resultlist,
            TileChemicalMetalSeparator.baseConsumingEnergy,
            TileChemicalMetalSeparator.baseCraftTime,
            (double)recipeResults.get(index).weight / (double)recipeResults.getWeightSum()
        );
    }

    public TemplateRecipeHandler newInstance() {
        return new NEIMetalSeparator();
    }

    public class NEIRecipeComparator extends NEIClayMachines.NEIRecipeComparator {
        public NEIRecipeComparator() {
            super();
        }

        public int compare(CachedRecipe a1, CachedRecipe b1) {
            if (a1 instanceof NEIMetalSeparator.NEIRecipeEntryMetalSeparator && b1 instanceof NEIMetalSeparator.NEIRecipeEntryMetalSeparator) {
                NEIMetalSeparator.NEIRecipeEntryMetalSeparator a = (NEIMetalSeparator.NEIRecipeEntryMetalSeparator)a1;
                NEIMetalSeparator.NEIRecipeEntryMetalSeparator b = (NEIMetalSeparator.NEIRecipeEntryMetalSeparator)b1;
                if (a.probability < b.probability) {
                    return 1;
                } else {
                    return b.probability < a.probability ? -1 : super.compare(a1, b1);
                }
            } else {
                return 0;
            }
        }
    }

    public class NEIRecipeEntryMetalSeparator extends NEIClayMachines.NEIRecipeEntry {
        public double probability;

        public NEIRecipeEntryMetalSeparator(Object[] inputs, int method, int tier, ItemStack[] results, int energy, int time, double probability) {
            super(inputs, method, tier, results, (long)energy, (long)time);
            this.probability = probability;
        }

        public NEIRecipeEntryMetalSeparator(List<Object> inputs, int method, int tier, List<ItemStack> results, int energy, int time, double probability) {
            super(inputs, method, tier, results, (long)energy, (long)time);
            this.probability = probability;
        }

        public void drawExtras() {
            super.drawExtras();
            Minecraft.func_71410_x().field_71466_p.func_78276_b(String.format("%3.2f%%", this.probability * 100.0), 69, 10, -16777216);
        }
    }
}
