package mods.clayium.block.tile;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilTransfer;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class TileAutoTrader extends TileClayContainerTiered {
    public IMerchant merchant;
    public MerchantRecipeList merchantRecipeList;
    public MerchantRecipe currentRecipe;
    protected PacketBuffer oldRecipeListBuffer;
    public int currentRecipeIndex = 0;
    private int toSellSlotIndex = 2;
    public static long energyPerTrade = 10000000L;

    public TileAutoTrader() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.containerItemStacks = new ItemStack[4];
        this.clayEnergySlot = 3;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{2});
        this.insertRoutes = new int[]{-1, -1, -1, 3, 0, 1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{-1, -1, 0, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 3};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    public void initParamsByTier(int tier) {
        this.setDefaultTransportationParamsByTier(tier, TileClayContainerTiered.ParamMode.MACHINE);
    }

    public void func_145845_h() {
        super.func_145845_h();
        if (!this.field_145850_b.field_72995_K) {
            List list = this.field_145850_b
                .func_82733_a(
                    IMerchant.class,
                    AxisAlignedBB.func_72330_a(
                        (double)this.field_145851_c,
                        (double)(this.field_145848_d + 1),
                        (double)this.field_145849_e,
                        (double)(this.field_145851_c + 1),
                        (double)(this.field_145848_d + 3),
                        (double)(this.field_145849_e + 1)
                    ),
                    IEntitySelector.field_94557_a
                );
            if (!list.isEmpty()) {
                this.merchant = (IMerchant)list.get(0);
                this.merchantRecipeList = this.merchant.func_70934_b(null);
                PacketBuffer newRecipeListBuffer = new PacketBuffer(Unpooled.buffer());

                try {
                    this.merchantRecipeList.func_151391_a(newRecipeListBuffer);
                } catch (IOException var4) {
                    ClayiumCore.logger.catching(var4);
                }

                if (this.oldRecipeListBuffer == null || !newRecipeListBuffer.equals(this.oldRecipeListBuffer)) {
                    this.setSyncFlag();
                }

                this.oldRecipeListBuffer = newRecipeListBuffer;
                this.currentRecipeIndex = Math.max(Math.min(this.currentRecipeIndex, this.merchantRecipeList.size() - 1), 0);
            } else {
                this.merchant = null;
                this.merchantRecipeList = null;
                if (this.oldRecipeListBuffer != null) {
                    this.setSyncFlag();
                }

                this.oldRecipeListBuffer = null;
            }

            this.resetRecipeAndSlots();
            if (this.currentRecipe != null) {
                ItemStack itemToSell = this.currentRecipe.func_77397_d().func_77946_l();
                if (UtilTransfer.canProduceItemStack(itemToSell, this.containerItemStacks, this.toSellSlotIndex, this.func_70297_j_())
                        >= itemToSell.field_77994_a
                    && this.consumeClayEnergy(energyPerTrade)) {
                    UtilTransfer.produceItemStack(itemToSell, this.containerItemStacks, this.toSellSlotIndex, this.func_70297_j_());
                    this.onPickupFromMerchantSlot(this.currentRecipe);
                    this.setSyncFlag();
                    this.merchant.func_110297_a_(this.func_70301_a(this.toSellSlotIndex));
                }
            }
        }
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        super.func_70299_a(slot, itemstack);
        if (this.inventoryResetNeededOnSlotChange(slot)) {
            this.resetRecipeAndSlots();
        }
    }

    public void resetRecipeAndSlots() {
        this.resetRecipeAndSlots(this.currentRecipeIndex, this.containerItemStacks[0], this.containerItemStacks[1]);
    }

    public void resetRecipeAndSlots(int currentRecipeIndex, ItemStack itemstack, ItemStack itemstack1) {
        if (this.merchantRecipeList != null) {
            this.currentRecipe = null;
            if (itemstack == null) {
                itemstack = itemstack1;
                itemstack1 = null;
            }

            if (itemstack != null && this.merchantRecipeList != null) {
                MerchantRecipe merchantrecipe = this.merchantRecipeList.func_77203_a(itemstack, itemstack1, currentRecipeIndex);
                if (merchantrecipe == null) {
                }

                if (merchantrecipe != null && !merchantrecipe.func_82784_g()) {
                    this.currentRecipe = merchantrecipe;
                } else if (itemstack1 != null) {
                    merchantrecipe = this.merchantRecipeList.func_77203_a(itemstack1, itemstack, currentRecipeIndex);
                    if (merchantrecipe != null && !merchantrecipe.func_82784_g()) {
                        this.currentRecipe = merchantrecipe;
                    }
                }
            }
        } else {
            this.currentRecipe = null;
        }
    }

    public void setCurrentRecipeIndex(int p_70471_1_) {
        this.currentRecipeIndex = p_70471_1_;
        if (this.merchantRecipeList != null) {
            this.currentRecipeIndex = Math.max(Math.min(this.currentRecipeIndex, this.merchantRecipeList.size() - 1), 0);
        }

        this.resetRecipeAndSlots();
    }

    public boolean inventoryResetNeededOnSlotChange(int par1) {
        return par1 == 0 || par1 == 1;
    }

    public void onPickupFromMerchantSlot(MerchantRecipe currentRecipe) {
        if (this.merchant != null) {
            if (currentRecipe != null) {
                ItemStack itemstack1 = this.containerItemStacks[0];
                ItemStack itemstack2 = this.containerItemStacks[1];
                if (this.checkAndConsumeIngredents(currentRecipe, itemstack1, itemstack2)
                    || this.checkAndConsumeIngredents(currentRecipe, itemstack2, itemstack1)) {
                    this.merchant.func_70933_a(currentRecipe);
                    if (itemstack1 != null && itemstack1.field_77994_a <= 0) {
                        itemstack1 = null;
                    }

                    if (itemstack2 != null && itemstack2.field_77994_a <= 0) {
                        itemstack2 = null;
                    }

                    this.func_70299_a(0, itemstack1);
                    this.func_70299_a(1, itemstack2);
                }
            }

            this.func_70296_d();
            this.field_145850_b.func_147471_g(this.field_145851_c, this.field_145848_d, this.field_145849_e);
        }
    }

    private boolean checkAndConsumeIngredents(MerchantRecipe p_75230_1_, ItemStack p_75230_2_, ItemStack p_75230_3_) {
        ItemStack itemstack2 = p_75230_1_.func_77394_a();
        ItemStack itemstack3 = p_75230_1_.func_77396_b();
        if (p_75230_2_ != null && p_75230_2_.func_77973_b() == itemstack2.func_77973_b()) {
            if (itemstack3 != null && p_75230_3_ != null && itemstack3.func_77973_b() == p_75230_3_.func_77973_b()) {
                p_75230_2_.field_77994_a = p_75230_2_.field_77994_a - itemstack2.field_77994_a;
                p_75230_3_.field_77994_a = p_75230_3_.field_77994_a - itemstack3.field_77994_a;
                return true;
            }

            if (itemstack3 == null && p_75230_3_ == null) {
                p_75230_2_.field_77994_a = p_75230_2_.field_77994_a - itemstack2.field_77994_a;
                return true;
            }
        }

        return false;
    }

    public void pushButton(EntityPlayer player, int action) {
        switch (action) {
            case 1:
                this.setCurrentRecipeIndex(this.currentRecipeIndex + 1);
                break;
            case 2:
                this.setCurrentRecipeIndex(this.currentRecipeIndex - 1);
        }

        this.setInstantSyncFlag();
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        if (tagCompound.func_74767_n("HasMerchantRecipeList")) {
            this.merchantRecipeList = new MerchantRecipeList(tagCompound.func_74775_l("MerchantRecipeList"));
        } else {
            this.merchantRecipeList = null;
        }

        this.setCurrentRecipeIndex(tagCompound.func_74762_e("MerchantRecipeIndex"));
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74757_a("HasMerchantRecipeList", this.merchantRecipeList != null);
        if (this.merchantRecipeList != null) {
            tagCompound.func_74782_a("MerchantRecipeList", this.merchantRecipeList.func_77202_a());
        }

        tagCompound.func_74768_a("MerchantRecipeIndex", this.currentRecipeIndex);
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }
}
