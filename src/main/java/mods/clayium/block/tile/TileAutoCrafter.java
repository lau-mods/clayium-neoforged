package mods.clayium.block.tile;

import mods.clayium.item.filter.ItemFilterTemp;
import mods.clayium.util.UtilItemStack;
import mods.clayium.util.UtilTransfer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;

public class TileAutoCrafter extends TileClayContainerTiered {
    public int machineCraftTime;
    public int machineTimeToCraft = 20;
    public long machineConsumingEnergy = 10L;
    public float multCraftTime = 1.0F;
    public float multConsumingEnergy = 1.0F;
    public int numAutomation = 1;
    private boolean succeedConsumingCE = false;
    private ItemStack[] oldContainer = new ItemStack[9];
    private ItemStack oldResult;

    public TileAutoCrafter() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.containerItemStacks = new ItemStack[43];
        this.clayEnergySlot = 33;
        this.listSlotsInsert.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        this.listSlotsExtract.add(new int[]{9, 10, 11, 12, 13, 14});
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42
        };
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    public void initParamsByTier(int tier) {
        switch (tier) {
            case 4:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 1;
                this.autoExtractInterval = this.autoInsertInterval = 8;
                break;
            case 5:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 4;
                this.autoExtractInterval = this.autoInsertInterval = 4;
                this.multConsumingEnergy = 0.0F;
                break;
            case 6:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 16;
                this.autoExtractInterval = this.autoInsertInterval = 2;
                this.multCraftTime = 0.05F;
                break;
            case 7:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 1;
                this.autoExtractInterval = this.autoInsertInterval = 1;
                this.multCraftTime = 0.05F;
                this.multConsumingEnergy = 4.0F;
                this.numAutomation = 16;
                break;
            case 8:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 16;
                this.autoExtractInterval = this.autoInsertInterval = 1;
                this.multCraftTime = 0.05F;
                this.multConsumingEnergy = 16.0F;
                this.numAutomation = 64;
                break;
            case 9:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 576;
                this.autoExtractInterval = this.autoInsertInterval = 1;
                this.multCraftTime = 0.05F;
                this.multConsumingEnergy = 64.0F;
                this.numAutomation = 256;
                break;
            default:
                this.maxAutoInsertDefault = this.maxAutoExtractDefault = 1;
                this.autoExtractInterval = this.autoInsertInterval = 8;
        }

        if ((float)this.machineConsumingEnergy * this.multConsumingEnergy > 0.0F) {
            this.listSlotsInsert.add(new int[]{33});
        } else {
            for (int i = 0; i < 6; i++) {
                if (this.insertRoutes[i] == 1) {
                    this.insertRoutes[i] = -1;
                }
            }
        }
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        if (slot >= 15 && slot < 24) {
            return true;
        } else {
            boolean res = super.func_94041_b(slot, itemstack);
            if (res && slot >= 0 && slot < 9) {
                ItemStack item = this.containerItemStacks[slot + 15];
                if (item == null) {
                    return false;
                } else {
                    return ItemFilterTemp.isFilter(item) ? ItemFilterTemp.match(item, itemstack) : UtilItemStack.areTypeEqual(item, itemstack);
                }
            } else {
                return res;
            }
        }
    }

    public boolean acceptEnergyClay() {
        return (float)this.machineConsumingEnergy * this.multConsumingEnergy > 0.0F;
    }

    public void func_145845_h() {
        boolean breakflag = false;
        boolean update = true;

        for (int i = 0; i < this.numAutomation; i++) {
            if (update) {
                super.func_145845_h();
            }

            if (!this.field_145850_b.field_72995_K) {
                if (this.canProceedCraft()) {
                    this.proceedCraft();
                    if (!this.succeedConsumingCE) {
                        breakflag = true;
                    }

                    update = false;
                } else if (update) {
                    breakflag = true;
                } else {
                    update = true;
                }

                for (int k = 0; k < 9; k++) {
                    if (this.containerItemStacks[k] != null && this.containerItemStacks[k].field_77994_a > 1) {
                        for (int l = 0; l < 9; l++) {
                            if (this.containerItemStacks[l] == null && this.func_94041_b(l, this.containerItemStacks[k])) {
                                this.containerItemStacks[l] = this.containerItemStacks[k].func_77979_a(1);
                                if (this.containerItemStacks[k].field_77994_a == 1) {
                                    break;
                                }
                            }
                        }
                    }
                }

                for (int kx = 0; kx < 9; kx++) {
                    if (this.containerItemStacks[34 + kx] != null
                        && UtilTransfer.canProduceItemStack(this.containerItemStacks[34 + kx], this.containerItemStacks, 9, 15, 64)
                            >= this.containerItemStacks[34 + kx].field_77994_a) {
                        UtilTransfer.produceItemStack(this.containerItemStacks[34 + kx], this.containerItemStacks, 9, 15, 64);
                        this.containerItemStacks[34 + kx] = null;
                    }
                }
            } else {
                breakflag = true;
            }

            if (breakflag && i >= 1) {
                break;
            }
        }
    }

    public boolean canProceedCraft() {
        ItemStack result = this.getResult();
        if (result == null) {
            return false;
        } else {
            for (int i = 0; i < 9; i++) {
                if (this.containerItemStacks[34 + i] != null) {
                    return false;
                }
            }

            return UtilTransfer.canProduceItemStack(result, this.containerItemStacks, 9, 15, 64) >= result.field_77994_a;
        }
    }

    public void proceedCraft() {
        int consumingEnergy = (int)((float)this.machineConsumingEnergy * this.multConsumingEnergy);
        int timeToCraft = (int)((float)this.machineTimeToCraft * this.multCraftTime);
        this.succeedConsumingCE = this.consumeClayEnergy((long)consumingEnergy);
        if (this.succeedConsumingCE) {
            boolean flag = true;

            for (int i = 0; i < 9; i++) {
                if (this.containerItemStacks[24 + i] != null) {
                    flag = false;
                }
            }

            if (flag) {
                for (int ix = 0; ix < 9; ix++) {
                    if (this.containerItemStacks[ix] != null && this.containerItemStacks[15 + ix] != null) {
                        this.containerItemStacks[24 + ix] = this.containerItemStacks[ix].func_77979_a(1);
                        if (this.containerItemStacks[ix].field_77994_a <= 0) {
                            this.containerItemStacks[ix] = null;
                        }
                    }
                }
            }

            this.machineCraftTime++;
            if (this.machineCraftTime >= timeToCraft) {
                ItemStack itemstack = this.getResult();
                UtilTransfer.produceItemStack(itemstack, this.containerItemStacks, 9, 15, 64);

                for (int ixx = 0; ixx < 9; ixx++) {
                    if (this.containerItemStacks[24 + ixx] != null) {
                        ItemStack container = this.containerItemStacks[24 + ixx].func_77973_b().getContainerItem(this.containerItemStacks[24 + ixx]);
                        if (container != null) {
                            UtilTransfer.produceItemStack(container, this.containerItemStacks, 34, 43, 64);
                        }

                        this.containerItemStacks[24 + ixx].field_77994_a--;
                        if (this.containerItemStacks[24 + ixx].field_77994_a <= 0) {
                            this.containerItemStacks[24 + ixx] = null;
                        }
                    }
                }

                this.machineCraftTime = 0;
            }

            this.setSyncFlag();
        }
    }

    protected ItemStack getResult() {
        for (int i = 0; i < 9; i++) {
            if (this.containerItemStacks[i + 24] != null) {
                return this.getResult(this.containerItemStacks, 24);
            }
        }

        for (int ix = 0; ix < 9; ix++) {
            if (this.containerItemStacks[ix + 15] != null && this.containerItemStacks[ix] == null
                || this.containerItemStacks[ix + 15] == null && this.containerItemStacks[ix] != null) {
                return null;
            }
        }

        return this.getResult(this.containerItemStacks, 0);
    }

    protected ItemStack getResult(ItemStack[] container, int s) {
        boolean flag = false;

        for (int i = 0; i < 9; i++) {
            if (!UtilItemStack.areTypeEqual(this.oldContainer[i], container[i + s])) {
                flag = true;
                this.oldContainer[i] = container[i + s] == null ? null : container[i + s].func_77946_l();
            }
        }

        if (flag) {
            this.oldResult = CraftingManager.func_77594_a()
                .func_82787_a(InventoryCraftingInTile.getNormalCraftingGrid(this.containerItemStacks, s), this.field_145850_b);
        }

        return this.oldResult;
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.machineCraftTime = tagCompound.func_74762_e("CraftTime");
        this.machineTimeToCraft = tagCompound.func_74762_e("TimeToCraft");
        this.machineConsumingEnergy = tagCompound.func_74763_f("ConsumingEnergy");
        this.multCraftTime = tagCompound.func_74760_g("CraftTimeMultiplier");
        this.multConsumingEnergy = tagCompound.func_74760_g("ConsumingEnergyMultiplier");
        this.numAutomation = tagCompound.func_74762_e("NumAutoMation");
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74768_a("CraftTime", this.machineCraftTime);
        tagCompound.func_74768_a("TimeToCraft", this.machineTimeToCraft);
        tagCompound.func_74772_a("ConsumingEnergy", this.machineConsumingEnergy);
        tagCompound.func_74776_a("CraftTimeMultiplier", this.multCraftTime);
        tagCompound.func_74776_a("ConsumingEnergyMultiplier", this.multConsumingEnergy);
        tagCompound.func_74768_a("NumAutoMation", this.numAutomation);
    }
}
