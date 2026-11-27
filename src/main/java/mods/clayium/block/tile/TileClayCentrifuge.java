package mods.clayium.block.tile;

import mods.clayium.util.crafting.Recipes;
import net.minecraft.item.ItemStack;

public class TileClayCentrifuge extends TileClayMachines {
    private Recipes recipe;
    public int resultSlotNum = 4;

    public TileClayCentrifuge() {
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
        this.listSlotsInsert.add(new int[]{6});
        this.listSlotsExtract.add(new int[]{1, 2, 3, 4});
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 6};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    public void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        if (tier <= 3) {
            this.resultSlotNum = 1;
        } else if (tier <= 4) {
            this.resultSlotNum = 2;
        } else if (tier <= 5) {
            this.resultSlotNum = 3;
        } else {
            this.resultSlotNum = 4;
        }
    }

    protected boolean canCraft(ItemStack material) {
        int method = 0;
        if (material != null && this.recipe != null) {
            ItemStack[] itemstacks = this.recipe.getResult(new ItemStack[]{material}, method, this.baseTier);
            if (itemstacks == null) {
                return false;
            } else {
                for (int i = 0; i < Math.min(this.resultSlotNum, itemstacks.length); i++) {
                    if (this.containerItemStacks[i + 1] != null && itemstacks[i] != null) {
                        if (!this.containerItemStacks[i + 1].func_77969_a(itemstacks[i])) {
                            return false;
                        }

                        int result = this.containerItemStacks[i + 1].field_77994_a + itemstacks[i].field_77994_a;
                        if (result > this.func_70297_j_() || result > this.containerItemStacks[i + 1].func_77976_d()) {
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

    public boolean canProceedCraft() {
        if (this.containerItemStacks[5] != null) {
            ItemStack itemstack = this.containerItemStacks[5];
            return this.canCraft(itemstack);
        } else {
            ItemStack itemstack = this.containerItemStacks[0];
            return this.canCraft(itemstack);
        }
    }

    public void proceedCraft() {
        int method = 0;
        if (this.containerItemStacks[5] == null) {
            this.machineConsumingEnergy = (long)(
                (float)this.recipe.getEnergy(new ItemStack[]{this.containerItemStacks[0]}, method, this.baseTier) * this.multConsumingEnergy
            );
        }

        if (this.consumeClayEnergy(this.machineConsumingEnergy)) {
            if (this.containerItemStacks[5] == null) {
                this.machineTimeToCraft = (long)(
                    (float)this.recipe.getTime(new ItemStack[]{this.containerItemStacks[0]}, method, this.baseTier) * this.multCraftTime
                );
                this.containerItemStacks[5] = this.containerItemStacks[0]
                    .func_77979_a(this.recipe.getConsumedStackSize(new ItemStack[]{this.containerItemStacks[0]}, method, this.baseTier)[0]);
                if (this.containerItemStacks[0].field_77994_a <= 0) {
                    this.containerItemStacks[0] = null;
                }
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack[] itemstacks = this.recipe.getResult(new ItemStack[]{this.containerItemStacks[5]}, method, this.baseTier);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;

                for (int i = 0; i < Math.min(this.resultSlotNum, itemstacks.length); i++) {
                    if (this.containerItemStacks[i + 1] == null) {
                        this.containerItemStacks[i + 1] = itemstacks[i].func_77946_l();
                    } else if (this.containerItemStacks[i + 1].func_77973_b() == itemstacks[i].func_77973_b()) {
                        this.containerItemStacks[i + 1].field_77994_a = this.containerItemStacks[i + 1].field_77994_a + itemstacks[i].field_77994_a;
                    }
                }

                if ((
                        this.containerItemStacks[5].field_77994_a = this.containerItemStacks[5].field_77994_a
                            - this.recipe.getConsumedStackSize(new ItemStack[]{this.containerItemStacks[5]}, method, this.baseTier)[0]
                    )
                    <= 0) {
                    this.containerItemStacks[5] = null;
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
