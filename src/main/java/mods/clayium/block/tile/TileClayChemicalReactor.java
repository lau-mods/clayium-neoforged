package mods.clayium.block.tile;

import mods.clayium.util.crafting.Recipes;
import net.minecraft.item.ItemStack;

public class TileClayChemicalReactor extends TileClayMachines {
    private Recipes recipe;
    protected int resultSlotNum = 2;

    public TileClayChemicalReactor() {
        super();
    }

    public void refreshRecipe() {
        Recipes recipe = Recipes.GetRecipes(this.recipeId);
        if (recipe != null) {
            this.recipe = recipe;
        }
    }

    public void initParams() {
        this.containerItemStacks = new ItemStack[7];
        this.clayEnergySlot = 6;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{6});
        this.listSlotsExtract.add(new int[]{2});
        this.listSlotsExtract.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{2, 3});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{2, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1, -1, -1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 6};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    protected boolean canCraft(ItemStack[] materials) {
        int method = 0;
        if (materials != null && this.recipe != null) {
            ItemStack[] itemstacks = this.recipe.getResult(materials, method, this.baseTier);
            if (itemstacks == null) {
                return false;
            } else {
                for (int i = 0; i < Math.min(this.resultSlotNum, itemstacks.length); i++) {
                    if (this.containerItemStacks[i + 2] != null && itemstacks[i] != null) {
                        if (!this.containerItemStacks[i + 2].func_77969_a(itemstacks[i])) {
                            return false;
                        }

                        int result = this.containerItemStacks[i + 2].field_77994_a + itemstacks[i].field_77994_a;
                        if (result > this.func_70297_j_() || result > this.containerItemStacks[i + 2].func_77976_d()) {
                            return false;
                        }
                    }
                }

                return true;
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
        if (this.containerItemStacks[4] == null && this.containerItemStacks[5] == null) {
            ItemStack[] itemstacks = new ItemStack[]{this.containerItemStacks[0], this.containerItemStacks[1]};
            return this.getCraftPermutation(itemstacks) != null;
        } else {
            ItemStack[] itemstacks = new ItemStack[]{this.containerItemStacks[4], this.containerItemStacks[5]};
            return this.getCraftPermutation(itemstacks) != null;
        }
    }

    public void proceedCraft() {
        int method = 0;
        if (this.containerItemStacks[4] == null && this.containerItemStacks[5] == null) {
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
            if (this.containerItemStacks[4] == null && this.containerItemStacks[5] == null) {
                ItemStack[] mats = new ItemStack[]{this.containerItemStacks[0], this.containerItemStacks[1]};
                int[] perm = this.getCraftPermutation(mats);
                if (perm == null) {
                    throw new RuntimeException("Invalid recipe reference : The permutation variable is null!");
                }

                ItemStack[] itemstacks = new ItemStack[perm.length];

                for (int i = 0; i < perm.length; i++) {
                    itemstacks[i] = mats[perm[i]];
                }

                this.machineTimeToCraft = (long)((float)this.recipe.getTime(itemstacks, method, this.baseTier) * this.multCraftTime);
                int[] consumedStackSize = this.recipe.getConsumedStackSize(itemstacks, method, this.baseTier);

                for (int i = 0; i < perm.length; i++) {
                    this.containerItemStacks[i + 4] = this.containerItemStacks[perm[i]].func_77979_a(consumedStackSize[i]);
                    if (this.containerItemStacks[perm[i]].field_77994_a <= 0) {
                        this.containerItemStacks[perm[i]] = null;
                    }
                }
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack[] mats = new ItemStack[]{this.containerItemStacks[4], this.containerItemStacks[5]};
                int[] perm = this.getCraftPermutation(mats);
                if (perm == null) {
                    throw new RuntimeException("Invalid recipe reference : The permutation variable is null!");
                }

                ItemStack[] itemstacks = new ItemStack[perm.length];

                for (int ix = 0; ix < perm.length; ix++) {
                    itemstacks[ix] = mats[perm[ix]];
                }

                ItemStack[] results = this.recipe.getResult(itemstacks, method, this.baseTier);
                int[] consumedStackSize = this.recipe.getConsumedStackSize(itemstacks, method, this.baseTier);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;

                for (int ix = 0; ix < Math.min(this.resultSlotNum, results.length); ix++) {
                    if (this.containerItemStacks[ix + 2] == null) {
                        this.containerItemStacks[ix + 2] = results[ix].func_77946_l();
                    } else if (this.containerItemStacks[ix + 2].func_77973_b() == results[ix].func_77973_b()) {
                        this.containerItemStacks[ix + 2].field_77994_a = this.containerItemStacks[ix + 2].field_77994_a + results[ix].field_77994_a;
                    }
                }

                for (int ixx = 0; ixx < perm.length; ixx++) {
                    if ((this.containerItemStacks[ixx + 4].field_77994_a = this.containerItemStacks[ixx + 4].field_77994_a - consumedStackSize[ixx]) <= 0) {
                        this.containerItemStacks[ixx + 4] = null;
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
