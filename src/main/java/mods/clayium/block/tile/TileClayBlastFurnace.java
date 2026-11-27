package mods.clayium.block.tile;

import java.util.ArrayList;
import mods.clayium.block.CBlocks;
import mods.clayium.util.crafting.Recipes;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class TileClayBlastFurnace extends TileMultiblockMachines {
    public int recipeTier = 0;
    private Recipes recipe;
    protected int resultSlotNum = 2;

    public TileClayBlastFurnace() {
        super();
    }

    public boolean isConstructed() {
        int sum = 0;
        int num = 0;
        boolean flag = true;

        for (int yy = 0; yy < 2; yy++) {
            for (int xx = -1; xx < 2; xx++) {
                for (int zz = 0; zz < 3; zz++) {
                    if (xx != 0 || yy != 0 || zz != 0) {
                        num++;
                        int atier = this.getMachineTier(xx, yy, zz);
                        if (atier <= 4) {
                            flag = false;
                        }

                        sum = (int)((double)sum + Math.pow(2.0, (double)(16 - atier)));
                        TileEntity te = this.getTileEntity(xx, yy, zz);
                        if (te != null && (te instanceof TileClayContainerInterface || te instanceof TileRedstoneInterface)) {
                            int[] coord = this.getRelativeCoord(0, 0, 0);
                            int[] coord1 = this.getRelativeCoord(xx, yy, zz);
                            ((ISynchronizedInterface)te).setCoreBlockCoord(coord[0] - coord1[0], coord[1] - coord1[1], coord[2] - coord1[2]);
                        }
                    }
                }
            }
        }

        this.recipeTier = Math.max((int)(16.0 - Math.floor(Math.log((double)(sum / num)) / Math.log(2.0) + 0.5)), 0);
        return flag;
    }

    public boolean acceptEnergyClay() {
        return true;
    }

    protected void onConstruction() {
        this.setRenderSyncFlag();
    }

    protected void onDestruction() {
        this.setRenderSyncFlag();
        this.machineCraftTime = 0L;
        this.containerItemStacks[4] = this.containerItemStacks[5] = null;
    }

    protected int getMachineTier(int xx, int yy, int zz) {
        Block block = this.getBlock(xx, yy, zz);
        if (block == CBlocks.blockMachineHull) {
            return this.getBlockMetadata(xx, yy, zz) + 1;
        } else if (block == CBlocks.blockOverclocker) {
            int[] coord = this.getRelativeCoord(xx, yy, zz);
            return CBlocks.blockOverclocker.getTier(this.field_145850_b, coord[0], coord[1], coord[2]);
        } else {
            TileEntity te = this.getTileEntity(xx, yy, zz);
            if (te != null && te instanceof TileClayContainerInterface) {
                return ((TileClayContainerInterface)te).getTier();
            } else {
                return te != null && te instanceof TileRedstoneInterface ? this.getBlockTier(xx, yy, zz) : -1;
            }
        }
    }

    public void initParamsByTier(int tier) {
    }

    public int getRecipeTier() {
        return this.recipeTier;
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
        this.listSlotsInsert = new ArrayList<>();
        this.listSlotsExtract = new ArrayList<>();
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{6});
        this.listSlotsExtract.add(new int[]{2});
        this.listSlotsExtract.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{2, 3});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{64, 64, 64, 1};
        this.extractRoutes = new int[]{2, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{64, 64, 64};
        this.autoExtractInterval = this.autoInsertInterval = 1;
        this.slotsDrop = new int[]{0, 1, 2, 3, 6};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    protected boolean canCraft(ItemStack[] materials) {
        int method = 0;
        if (materials != null && this.recipe != null) {
            ItemStack[] itemstacks = this.recipe.getResult(materials, method, this.recipeTier);
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

    public boolean canProceedCraftWhenConstructed() {
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

            this.machineConsumingEnergy = (long)((float)this.recipe.getEnergy(itemstacks, method, this.recipeTier) * this.multConsumingEnergy);
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

                this.machineTimeToCraft = (long)((float)this.recipe.getTime(itemstacks, method, this.recipeTier) * this.multCraftTime);
                int[] consumedStackSize = this.recipe.getConsumedStackSize(itemstacks, method, this.recipeTier);

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

                ItemStack[] results = this.recipe.getResult(itemstacks, method, this.recipeTier);
                int[] consumedStackSize = this.recipe.getConsumedStackSize(itemstacks, method, this.recipeTier);
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
