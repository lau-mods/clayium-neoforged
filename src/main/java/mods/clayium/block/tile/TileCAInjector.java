package mods.clayium.block.tile;

import mods.clayium.util.crafting.Recipes;
import net.minecraft.item.ItemStack;

public class TileCAInjector extends TileCAMachines {
    private Recipes recipe;
    protected double caFactorExponent = 1.0;

    public TileCAInjector() {
        super();
    }

    public void refreshRecipe() {
        Recipes recipe = Recipes.GetRecipes(this.recipeId);
        if (recipe != null) {
            this.recipe = recipe;
        }
    }

    public void initParams() {
        this.containerItemStacks = new ItemStack[6];
        this.clayEnergySlot = 5;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{5});
        this.listSlotsExtract.add(new int[]{2});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 5};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    public void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        switch (tier) {
            case 9:
                this.caFactorExponent = 0.2;
                break;
            case 10:
                this.caFactorExponent = 0.9;
                break;
            case 11:
                this.caFactorExponent = 3.0;
        }
    }

    protected boolean canCraft(ItemStack[] materials) {
        int method = 0;
        if (materials != null && this.recipe != null) {
            ItemStack[] itemstacks = this.recipe.getResult(materials, method, this.baseTier);
            if (itemstacks == null) {
                return false;
            } else if (this.containerItemStacks[2] == null) {
                return true;
            } else if (!this.containerItemStacks[2].func_77969_a(itemstacks[0])) {
                return false;
            } else {
                int result = this.containerItemStacks[2].field_77994_a + itemstacks[0].field_77994_a;
                return result <= this.func_70297_j_() && result <= this.containerItemStacks[2].func_77976_d();
            }
        } else {
            return false;
        }
    }

    protected int[] getCraftPermutation(ItemStack[] materials) {
        if (this.canCraft(materials)) {
            return new int[]{0, 1};
        } else if (this.canCraft(new ItemStack[]{materials[1], materials[0]})) {
            return new int[]{1, 0};
        } else if (this.canCraft(new ItemStack[]{materials[0]})) {
            return new int[]{0};
        } else {
            return this.canCraft(new ItemStack[]{materials[1]}) ? new int[]{1} : null;
        }
    }

    public boolean canProceedCraft() {
        if (this.containerItemStacks[3] == null && this.containerItemStacks[4] == null) {
            ItemStack[] itemstacks = new ItemStack[]{this.containerItemStacks[0], this.containerItemStacks[1]};
            return this.getCraftPermutation(itemstacks) != null;
        } else {
            ItemStack[] itemstacks = new ItemStack[]{this.containerItemStacks[3], this.containerItemStacks[4]};
            return this.getCraftPermutation(itemstacks) != null;
        }
    }

    public double getCraftTimeMultiplier() {
        return Math.pow(this.getResonance(), -this.caFactorExponent);
    }

    public void proceedCraft() {
        int method = 0;
        if (this.containerItemStacks[3] == null && this.containerItemStacks[4] == null) {
            ItemStack[] mats = new ItemStack[]{this.containerItemStacks[0], this.containerItemStacks[1]};
            int[] perm = this.getCraftPermutation(mats);
            if (perm == null) {
                throw new RuntimeException("Invalid recipe reference : The permutation variable is null!");
            }

            ItemStack[] itemstacks = new ItemStack[perm.length];

            for (int i = 0; i < perm.length; i++) {
                itemstacks[i] = mats[perm[i]];
            }

            this.machineConsumingEnergy = (long)((float)this.recipe.getEnergy(itemstacks, method, this.baseTier) * this.multConsumingEnergy);
        }

        if (this.consumeClayEnergy(this.machineConsumingEnergy)) {
            if (this.containerItemStacks[3] == null && this.containerItemStacks[4] == null) {
                ItemStack[] mats = new ItemStack[]{this.containerItemStacks[0], this.containerItemStacks[1]};
                int[] perm = this.getCraftPermutation(mats);
                if (perm == null) {
                    throw new RuntimeException("Invalid recipe reference : The permutation variable is null!");
                }

                ItemStack[] itemstacks = new ItemStack[perm.length];

                for (int i = 0; i < perm.length; i++) {
                    itemstacks[i] = mats[perm[i]];
                }

                this.machineTimeToCraft = (long)(
                    (double)this.recipe.getTime(itemstacks, method, this.baseTier) * this.getCraftTimeMultiplier() * (double)this.multCraftTime
                );
                int[] consumedStackSize = this.recipe.getConsumedStackSize(itemstacks, method, this.baseTier);

                for (int i = 0; i < perm.length; i++) {
                    this.containerItemStacks[i + 3] = this.containerItemStacks[perm[i]].func_77979_a(consumedStackSize[i]);
                    if (this.containerItemStacks[perm[i]].field_77994_a <= 0) {
                        this.containerItemStacks[perm[i]] = null;
                    }
                }
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack[] mats = new ItemStack[]{this.containerItemStacks[3], this.containerItemStacks[4]};
                int[] perm = this.getCraftPermutation(mats);
                if (perm == null) {
                    throw new RuntimeException("Invalid recipe reference : The permutation variable is null!");
                }

                ItemStack[] itemstacks = new ItemStack[perm.length];

                for (int ix = 0; ix < perm.length; ix++) {
                    itemstacks[ix] = mats[perm[ix]];
                }

                ItemStack result = this.recipe.getResult(itemstacks, method, this.baseTier)[0];
                int[] consumedStackSize = this.recipe.getConsumedStackSize(itemstacks, method, this.baseTier);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (this.containerItemStacks[2] == null) {
                    this.containerItemStacks[2] = result.func_77946_l();
                } else if (this.containerItemStacks[2].func_77973_b() == result.func_77973_b()) {
                    this.containerItemStacks[2].field_77994_a = this.containerItemStacks[2].field_77994_a + result.field_77994_a;
                }

                for (int ix = 0; ix < perm.length; ix++) {
                    if ((this.containerItemStacks[ix + 3].field_77994_a = this.containerItemStacks[ix + 3].field_77994_a - consumedStackSize[ix]) <= 0) {
                        this.containerItemStacks[ix + 3] = null;
                    }
                }

                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
            }
        }
    }
}
