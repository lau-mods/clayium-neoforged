package mods.clayium.block.tile;

import java.util.List;
import java.util.Random;
import mods.clayium.item.CMaterials;
import mods.clayium.util.UtilTransfer;
import mods.clayium.util.crafting.SimpleMachinesRecipes;
import mods.clayium.util.crafting.WeightedList;
import net.minecraft.item.ItemStack;

public class TileChemicalMetalSeparator extends TileClayMachines {
    public static int baseConsumingEnergy = 5000;
    public static int baseCraftTime = 40;
    public static WeightedList<ItemStack> results = new WeightedList<>();
    private Random random = new Random();

    public TileChemicalMetalSeparator() {
        super();
    }

    public void initParams() {
        this.containerItemStacks = new ItemStack[19];
        this.clayEnergySlot = 18;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{18});
        this.listSlotsExtract.add(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18};
        this.autoInsert = true;
        this.autoExtract = true;
        this.recipeId = "";
    }

    public void setRecipe(String recipeId_) {
    }

    public void refreshRecipe() {
    }

    public SimpleMachinesRecipes getRecipe() {
        return null;
    }

    public String getNEIOutputId() {
        return "ChemicalMetalSeparator";
    }

    protected boolean canCraft(ItemStack material) {
        if (material != null && material.func_77969_a(CMaterials.get(CMaterials.IND_CLAY, CMaterials.DUST))) {
            List<ItemStack> resultlist = results.getResultList();

            for (int i = 0; i < resultlist.size(); i++) {
                if (UtilTransfer.canProduceItemStack(resultlist.get(i), this.containerItemStacks, 1, 17, this.func_70297_j_())
                    < resultlist.get(i).field_77994_a) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean canProceedCraft() {
        if (this.containerItemStacks[17] != null) {
            ItemStack itemstack = this.containerItemStacks[17];
            return this.canCraft(itemstack);
        } else {
            ItemStack itemstack = this.containerItemStacks[0];
            return this.canCraft(itemstack);
        }
    }

    public void proceedCraft() {
        if (this.containerItemStacks[17] == null) {
            this.machineConsumingEnergy = (long)((float)baseConsumingEnergy * this.multConsumingEnergy);
        }

        if (this.consumeClayEnergy(this.machineConsumingEnergy)) {
            if (this.containerItemStacks[17] == null) {
                this.machineTimeToCraft = (long)((float)baseCraftTime * this.multCraftTime);
                this.containerItemStacks[17] = this.containerItemStacks[0].func_77979_a(1);
                if (this.containerItemStacks[0].field_77994_a <= 0) {
                    this.containerItemStacks[0] = null;
                }
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack itemstack = results.put(this.random);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                this.containerItemStacks[17] = null;
                if (!this.field_145850_b.field_72995_K) {
                    UtilTransfer.produceItemStack(itemstack, this.containerItemStacks, 1, 17, this.func_70297_j_());
                }

                this.syncFlag = true;
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
