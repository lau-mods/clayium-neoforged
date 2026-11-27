package mods.clayium.plugin.nei;

import codechicken.nei.recipe.TemplateRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler.CachedRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler.RecipeTransferRect;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import mods.clayium.gui.client.GuiClayMachines;
import mods.clayium.util.UtilTier;
import mods.clayium.util.crafting.Recipes;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public class NEIClayMachines extends NEITemp {
    private Recipes recipes;
    public UtilTier.TierManager tierManager;
    private String overlayId;
    private Comparator<CachedRecipe> comp = new NEIClayMachines.NEIRecipeComparator();

    public NEIClayMachines(String overlayId) {
        this(overlayId, UtilTier.TierManager.getMachineTierManager());
    }

    public NEIClayMachines(String overlayId, UtilTier.TierManager tierManager) {
        super();
        this.recipes = null;
        this.overlayId = overlayId;
        this.tierManager = tierManager;
        this.loadTransferRects();
    }

    public NEIClayMachines(Recipes recipes) {
        this(recipes, UtilTier.TierManager.getMachineTierManager());
    }

    public NEIClayMachines(Recipes recipes, UtilTier.TierManager tierManager) {
        this(recipes.recipeid, tierManager);
        this.recipes = recipes;
    }

    public void setTierManager(UtilTier.TierManager tierManager) {
        if (tierManager != null) {
            this.tierManager = tierManager;
        }
    }

    public Comparator<CachedRecipe> getComparator() {
        return this.comp;
    }

    public Class<? extends GuiContainer> getGuiClass() {
        return GuiClayMachines.class;
    }

    public String getOverlayIdentifier() {
        return this.overlayId;
    }

    public void loadTransferRects() {
        if (this.overlayId != null) {
            this.transferRects.add(new RecipeTransferRect(new Rectangle(69, 20, 28, 18), this.overlayId, new Object[0]));
        }
    }

    public Iterable<INEIRecipeEntry> getMatchedSet() {
        if (this.recipes == null) {
            return null;
        } else {
            List<INEIRecipeEntry> list = new ArrayList<>();

            for (Entry<Recipes.RecipeCondition, Recipes.RecipeResult> entry : this.recipes.recipeResultMap.entrySet()) {
                Object[] materiallist = entry.getKey().getMaterials();
                ItemStack[] resultlist = entry.getValue().getResults();
                int method = entry.getKey().getMethod();
                int tier = entry.getKey().getTier();
                long energy = entry.getValue().getEnergy();
                long time = entry.getValue().getTime();
                if (NEITemp.isAvailable(materiallist)) {
                    NEIClayMachines.NEIRecipeEntry arecipe = new NEIClayMachines.NEIRecipeEntry(
                        materiallist,
                        method,
                        tier,
                        resultlist,
                        (long)((float)energy * this.tierManager.getF("multConsumingEnergy", tier)),
                        (long)((float)time * this.tierManager.getF("multCraftTime", tier))
                    );
                    arecipe.computeVisuals();
                    list.add(arecipe);
                }
            }

            return list;
        }
    }

    public String getRecipeName() {
        return I18n.func_135052_a("recipe." + this.getOverlayIdentifier(), new Object[0]);
    }

    public String getGuiTexture() {
        return "clayium:textures/gui/nei.png";
    }

    public TemplateRecipeHandler newInstance() {
        return new NEIClayMachines(this.recipes, this.tierManager);
    }

    public class NEIRecipeComparator extends NEITemp.NEIEntryComparator {
        public NEIRecipeComparator() {
            super();
        }

        public int compare(CachedRecipe a1, CachedRecipe b1) {
            if (a1 instanceof NEIClayMachines.NEIRecipeEntry && b1 instanceof NEIClayMachines.NEIRecipeEntry) {
                NEIClayMachines.NEIRecipeEntry a = (NEIClayMachines.NEIRecipeEntry)a1;
                NEIClayMachines.NEIRecipeEntry b = (NEIClayMachines.NEIRecipeEntry)b1;
                if (a.method > b.method) {
                    return 1;
                }

                if (b.method > a.method) {
                    return -1;
                }
            }

            return super.compare(a1, b1);
        }
    }

    public class NEIRecipeEntry extends NEITemp.NEITemplateEntry implements NEITemp.INEIEntryTiered, NEITemp.INEIEntryEnergy {
        public int method;
        public int tier;
        public long energy;
        public long time;

        public NEIRecipeEntry(Object[] inputs, int method, int tier, ItemStack[] results, long energy, long time) {
            super(inputs, results);
            this.method = method;
            this.tier = tier;
            this.energy = energy;
            this.time = time;
        }

        public NEIRecipeEntry(List<Object> inputs, int method, int tier, List<ItemStack> results, long energy, long time) {
            this(inputs.toArray(new Object[0]), method, tier, results.toArray(new ItemStack[0]), energy, time);
        }

        public long getEnergy() {
            return this.energy;
        }

        public long getTime() {
            return this.time;
        }

        public int getTier() {
            return this.tier;
        }
    }
}
