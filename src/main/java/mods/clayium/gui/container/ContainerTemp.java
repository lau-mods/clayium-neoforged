package mods.clayium.gui.container;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import mods.clayium.block.tile.IGeneralInterface;
import mods.clayium.core.ClayiumCore;
import mods.clayium.gui.SlotMemory;
import mods.clayium.network.GuiTextFieldPacket;
import mods.clayium.util.UtilItemStack;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class ContainerTemp extends Container {
    protected int lastCraftTime;
    protected int lastTimeToCraft;
    protected IInventory tile;
    public ArrayList<Slot> machineInventorySlots = new ArrayList<>();
    protected int playerSlotIndex;
    public int machineGuiSizeX = 176;
    public int machineGuiSizeY = 72;
    public int playerSlotOffsetX;
    public int playerSlotOffsetY;
    public Object[] additionalParams;
    private int field_94536_g;
    private int field_94535_f;
    private final Set field_94537_h = new HashSet();

    public ContainerTemp(InventoryPlayer player, IInventory tile, Block block, Object... additionalParams) {
        super();
        this.additionalParams = additionalParams;
        this.setTileEntity(tile);
        if (this.tile != null) {
            this.tile.func_70295_k_();
        }

        this.initParameters(player);
        this.setMachineInventorySlots(player);
        this.addMachineInventorySlots(player);
        this.addPlayerInventorySlots(player);
    }

    protected void initParameters(InventoryPlayer player) {
        this.initParametersDefault(player);
    }

    protected void initParametersDefault(InventoryPlayer player) {
        this.machineGuiSizeX = Math.max(this.machineGuiSizeX, 176);
        this.playerSlotOffsetX = (this.machineGuiSizeX - 176) / 2;
        this.playerSlotOffsetY = this.machineGuiSizeY;
    }

    public void setTileEntity(IInventory tile) {
        this.tile = tile;
    }

    public abstract void setMachineInventorySlots(InventoryPlayer var1);

    public int addMachineSlotToContainer(Slot slot) {
        this.machineInventorySlots.add(slot);
        return this.machineInventorySlots.size() - 1;
    }

    public void addMachineInventorySlots(InventoryPlayer player) {
        for (int i = 0; i < this.machineInventorySlots.size(); i++) {
            this.func_75146_a(this.machineInventorySlots.get(i));
        }

        this.playerSlotIndex = this.machineInventorySlots.size();
    }

    public void addPlayerInventorySlots(InventoryPlayer player) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.func_75146_a(new Slot(player, j + i * 9 + 9, this.playerSlotOffsetX + 8 + j * 18, this.playerSlotOffsetY + 12 + i * 18));
            }
        }

        for (int var4 = 0; var4 < 9; var4++) {
            this.func_75146_a(new Slot(player, var4, this.playerSlotOffsetX + 8 + var4 * 18, this.playerSlotOffsetY + 70));
        }
    }

    public boolean func_75145_c(EntityPlayer player) {
        return this.tile == null ? true : this.tile.func_70300_a(player);
    }

    public String getInventoryName() {
        return this.tile != null ? this.tile.func_145825_b() : "";
    }

    public abstract boolean canTransferToMachineInventory(ItemStack var1);

    public abstract boolean transferStackToMachineInventory(ItemStack var1);

    public abstract boolean transferStackFromMachineInventory(ItemStack var1, int var2);

    public String getTextFieldString(EntityPlayer player, int id) {
        return null;
    }

    public void setTextFieldString(EntityPlayer player, String string, int id) {
    }

    public void sendTextFieldStringToClient(EntityPlayer player, String string, int id) {
        if (!player.func_130014_f_().field_72995_K && player instanceof EntityPlayerMP) {
            ClayiumCore.packetDispatcher.sendTo(new GuiTextFieldPacket(string, id), (EntityPlayerMP)player);
        }
    }

    public ItemStack func_82846_b(EntityPlayer player, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.field_75151_b.get(par2);
        if (slot != null && slot.func_75216_d()) {
            ItemStack itemstack1 = slot.func_75211_c();
            itemstack = itemstack1.func_77946_l();
            if (par2 < this.playerSlotIndex) {
                if (!this.transferStackFromMachineInventory(itemstack1, par2)) {
                    return null;
                }

                slot.func_75220_a(itemstack1, itemstack);
            } else if (!this.transferStackFromPlayerInventory(itemstack1, par2)) {
                return null;
            }

            if (itemstack1.field_77994_a == 0) {
                slot.func_75215_d((ItemStack)null);
            } else {
                slot.func_75218_e();
            }

            if (itemstack1.field_77994_a == itemstack.field_77994_a) {
                return null;
            }

            slot.func_82870_a(player, itemstack1);
        }

        return itemstack;
    }

    public boolean transferStackToPlayerInventory(ItemStack itemstack1, boolean flag) {
        return this.func_75135_a(itemstack1, this.playerSlotIndex, this.playerSlotIndex + 36, flag);
    }

    public boolean transferStackFromPlayerInventory(ItemStack itemstack1, int par2) {
        if (this.canTransferToMachineInventory(itemstack1)) {
            if (!this.transferStackToMachineInventory(itemstack1)) {
                return false;
            }
        } else if (par2 >= this.playerSlotIndex && par2 < this.playerSlotIndex + 27) {
            if (!this.func_75135_a(itemstack1, this.playerSlotIndex + 27, this.playerSlotIndex + 36, false)) {
                return false;
            }
        } else if (par2 >= this.playerSlotIndex + 27
            && par2 < this.playerSlotIndex + 36
            && !this.func_75135_a(itemstack1, this.playerSlotIndex, this.playerSlotIndex + 27, false)) {
            return false;
        }

        return true;
    }

    protected int canMergeItemStack(ItemStack p_75135_1_, int p_75135_2_, int p_75135_3_, boolean p_75135_4_) {
        int stackSize = p_75135_1_.field_77994_a;
        boolean flag1 = false;
        int k = p_75135_2_;
        if (p_75135_4_) {
            k = p_75135_3_ - 1;
        }

        if (p_75135_1_.func_77985_e()) {
            while (stackSize > 0 && (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_)) {
                Slot slot = (Slot)this.field_75151_b.get(k);
                ItemStack itemstack1 = slot.func_75211_c();
                if (itemstack1 != null
                    && itemstack1.func_77973_b() == p_75135_1_.func_77973_b()
                    && (!p_75135_1_.func_77981_g() || p_75135_1_.func_77960_j() == itemstack1.func_77960_j())
                    && ItemStack.func_77970_a(p_75135_1_, itemstack1)) {
                    int l = itemstack1.field_77994_a + stackSize;
                    if (l <= p_75135_1_.func_77976_d()) {
                        return 0;
                    }

                    if (itemstack1.field_77994_a < p_75135_1_.func_77976_d()) {
                        stackSize -= p_75135_1_.func_77976_d() - itemstack1.field_77994_a;
                    }
                }

                if (p_75135_4_) {
                    k--;
                } else {
                    k++;
                }
            }
        }

        if (stackSize > 0) {
            if (p_75135_4_) {
                k = p_75135_3_ - 1;
            } else {
                k = p_75135_2_;
            }

            while (!p_75135_4_ && k < p_75135_3_ || p_75135_4_ && k >= p_75135_2_) {
                Slot slotx = (Slot)this.field_75151_b.get(k);
                ItemStack itemstack1x = slotx.func_75211_c();
                if (itemstack1x == null) {
                    return 0;
                }

                if (p_75135_4_) {
                    k--;
                } else {
                    k++;
                }
            }
        }

        return stackSize;
    }

    public boolean func_75140_a(EntityPlayer player, int action) {
        if (this.tile instanceof IGeneralInterface) {
            ((IGeneralInterface)this.tile).pushButton(player, action);
        }

        return true;
    }

    public boolean pushClientButton(EntityPlayer player, int action) {
        return true;
    }

    public void func_75142_b() {
        for (int i = 0; i < this.field_75151_b.size(); i++) {
            ItemStack itemstack = ((Slot)this.field_75151_b.get(i)).func_75211_c();
            ItemStack itemstack1 = (ItemStack)this.field_75153_a.get(i);
            if (!UtilItemStack.areStackEqual(itemstack1, itemstack)) {
                if (this.tile instanceof IGeneralInterface) {
                    ((IGeneralInterface)this.tile).setSyncFlag();
                }

                itemstack1 = itemstack == null ? null : itemstack.func_77946_l();
                this.field_75153_a.set(i, itemstack1);
                if (i >= this.playerSlotIndex && i < this.playerSlotIndex + 36) {
                    for (int j = 0; j < this.field_75149_d.size(); j++) {
                        ((ICrafting)this.field_75149_d.get(j)).func_71111_a(this, i, itemstack1);
                    }
                }
            }
        }

        if (this.tile instanceof IGeneralInterface) {
            ((IGeneralInterface)this.tile).markForWeakUpdate();
        }
    }

    public void func_75134_a(EntityPlayer p_75134_1_) {
        super.func_75134_a(p_75134_1_);
        if (this.tile != null) {
            this.tile.func_70305_f();
        }
    }

    public boolean drawInventoryName() {
        return true;
    }

    public boolean drawPlayerInventoryName() {
        return true;
    }

    public ItemStack func_75144_a(int p_75144_1_, int p_75144_2_, int p_75144_3_, EntityPlayer p_75144_4_) {
        ItemStack itemstack = null;
        InventoryPlayer inventoryplayer = p_75144_4_.field_71071_by;
        if (p_75144_3_ == 5) {
            int l = this.field_94536_g;
            this.field_94536_g = func_94532_c(p_75144_2_);
            if ((l != 1 || this.field_94536_g != 2) && l != this.field_94536_g) {
                this.func_94533_d();
            } else if (inventoryplayer.func_70445_o() == null) {
                this.func_94533_d();
            } else if (this.field_94536_g == 0) {
                this.field_94535_f = func_94529_b(p_75144_2_);
                if (func_94528_d(this.field_94535_f)) {
                    this.field_94536_g = 1;
                    this.field_94537_h.clear();
                } else {
                    this.func_94533_d();
                }
            } else if (this.field_94536_g == 1) {
                Slot slot = (Slot)this.field_75151_b.get(p_75144_1_);
                if (slot != null
                    && func_94527_a(slot, inventoryplayer.func_70445_o(), true)
                    && slot.func_75214_a(inventoryplayer.func_70445_o())
                    && inventoryplayer.func_70445_o().field_77994_a > this.field_94537_h.size()
                    && this.func_94531_b(slot)) {
                    this.field_94537_h.add(slot);
                }
            } else if (this.field_94536_g == 2) {
                if (!this.field_94537_h.isEmpty()) {
                    ItemStack itemstack3 = inventoryplayer.func_70445_o().func_77946_l();
                    int i1 = inventoryplayer.func_70445_o().field_77994_a;

                    for (Slot slot1 : this.field_94537_h) {
                        if (slot1 != null
                            && func_94527_a(slot1, inventoryplayer.func_70445_o(), true)
                            && slot1.func_75214_a(inventoryplayer.func_70445_o())
                            && inventoryplayer.func_70445_o().field_77994_a >= this.field_94537_h.size()
                            && this.func_94531_b(slot1)) {
                            ItemStack itemstack1 = itemstack3.func_77946_l();
                            int j1 = slot1.func_75216_d() ? slot1.func_75211_c().field_77994_a : 0;
                            func_94525_a(this.field_94537_h, this.field_94535_f, itemstack1, j1);
                            if (itemstack1.field_77994_a > itemstack1.func_77976_d()) {
                                itemstack1.field_77994_a = itemstack1.func_77976_d();
                            }

                            if (itemstack1.field_77994_a > slot1.func_75219_a()) {
                                itemstack1.field_77994_a = slot1.func_75219_a();
                            }

                            if (!(slot1 instanceof SlotMemory)) {
                                i1 -= itemstack1.field_77994_a - j1;
                            }

                            slot1.func_75215_d(itemstack1);
                        }
                    }

                    itemstack3.field_77994_a = i1;
                    if (itemstack3.field_77994_a <= 0) {
                        itemstack3 = null;
                    }

                    inventoryplayer.func_70437_b(itemstack3);
                }

                this.func_94533_d();
            } else {
                this.func_94533_d();
            }
        } else if (this.field_94536_g != 0) {
            this.func_94533_d();
        } else if ((p_75144_3_ == 0 || p_75144_3_ == 1) && (p_75144_2_ == 0 || p_75144_2_ == 1)) {
            if (p_75144_1_ == -999) {
                if (inventoryplayer.func_70445_o() != null && p_75144_1_ == -999) {
                    if (p_75144_2_ == 0) {
                        p_75144_4_.func_71019_a(inventoryplayer.func_70445_o(), true);
                        inventoryplayer.func_70437_b((ItemStack)null);
                    }

                    if (p_75144_2_ == 1) {
                        p_75144_4_.func_71019_a(inventoryplayer.func_70445_o().func_77979_a(1), true);
                        if (inventoryplayer.func_70445_o().field_77994_a == 0) {
                            inventoryplayer.func_70437_b((ItemStack)null);
                        }
                    }
                }
            } else if (p_75144_3_ == 1) {
                if (p_75144_1_ < 0) {
                    return null;
                }

                Slot slot2 = (Slot)this.field_75151_b.get(p_75144_1_);
                if (slot2 != null && slot2.func_82869_a(p_75144_4_)) {
                    ItemStack itemstack3 = this.func_82846_b(p_75144_4_, p_75144_1_);
                    if (itemstack3 != null) {
                        Item item = itemstack3.func_77973_b();
                        itemstack = itemstack3.func_77946_l();
                        if (slot2.func_75211_c() != null && slot2.func_75211_c().func_77973_b() == item) {
                            this.func_75133_b(p_75144_1_, p_75144_2_, true, p_75144_4_);
                        }
                    }
                }
            } else {
                if (p_75144_1_ < 0) {
                    return null;
                }

                Slot slot2 = (Slot)this.field_75151_b.get(p_75144_1_);
                if (slot2 != null) {
                    ItemStack itemstack3 = slot2.func_75211_c();
                    ItemStack itemstack4 = inventoryplayer.func_70445_o();
                    if (slot2 instanceof SlotMemory && itemstack4 != null) {
                        itemstack4 = itemstack4.func_77946_l();
                    }

                    if (itemstack3 != null) {
                        itemstack = itemstack3.func_77946_l();
                    }

                    if (itemstack3 == null) {
                        if (itemstack4 != null && slot2.func_75214_a(itemstack4)) {
                            int l1 = p_75144_2_ == 0 ? itemstack4.field_77994_a : 1;
                            if (l1 > slot2.func_75219_a()) {
                                l1 = slot2.func_75219_a();
                            }

                            if (itemstack4.field_77994_a >= l1) {
                                slot2.func_75215_d(itemstack4.func_77979_a(l1));
                            }

                            if (itemstack4.field_77994_a == 0 && !(slot2 instanceof SlotMemory)) {
                                inventoryplayer.func_70437_b((ItemStack)null);
                            }
                        }
                    } else if (slot2.func_82869_a(p_75144_4_)) {
                        if (itemstack4 == null) {
                            int l1x = p_75144_2_ == 0 ? itemstack3.field_77994_a : (itemstack3.field_77994_a + 1) / 2;
                            ItemStack itemstack5 = slot2.func_75209_a(l1x);
                            inventoryplayer.func_70437_b(itemstack5);
                            if (itemstack3.field_77994_a == 0) {
                                slot2.func_75215_d((ItemStack)null);
                            }

                            slot2.func_82870_a(p_75144_4_, inventoryplayer.func_70445_o());
                        } else if (slot2.func_75214_a(itemstack4)) {
                            if (itemstack3.func_77973_b() == itemstack4.func_77973_b()
                                && itemstack3.func_77960_j() == itemstack4.func_77960_j()
                                && ItemStack.func_77970_a(itemstack3, itemstack4)) {
                                int l1x = p_75144_2_ == 0 ? itemstack4.field_77994_a : 1;
                                if (l1x > slot2.func_75219_a() - itemstack3.field_77994_a) {
                                    l1x = slot2.func_75219_a() - itemstack3.field_77994_a;
                                }

                                if (l1x > itemstack4.func_77976_d() - itemstack3.field_77994_a) {
                                    l1x = itemstack4.func_77976_d() - itemstack3.field_77994_a;
                                }

                                itemstack4.func_77979_a(l1x);
                                if (itemstack4.field_77994_a == 0) {
                                    inventoryplayer.func_70437_b((ItemStack)null);
                                }

                                itemstack3.field_77994_a += l1x;
                            } else if (itemstack4.field_77994_a <= slot2.func_75219_a()) {
                                slot2.func_75215_d(itemstack4);
                                inventoryplayer.func_70437_b(itemstack3);
                            }
                        } else if (itemstack3.func_77973_b() == itemstack4.func_77973_b()
                            && itemstack4.func_77976_d() > 1
                            && (!itemstack3.func_77981_g() || itemstack3.func_77960_j() == itemstack4.func_77960_j())
                            && ItemStack.func_77970_a(itemstack3, itemstack4)) {
                            int l1xx = itemstack3.field_77994_a;
                            if (l1xx > 0 && l1xx + itemstack4.field_77994_a <= itemstack4.func_77976_d()) {
                                itemstack4.field_77994_a += l1xx;
                                itemstack3 = slot2.func_75209_a(l1xx);
                                if (itemstack3.field_77994_a == 0) {
                                    slot2.func_75215_d((ItemStack)null);
                                }

                                slot2.func_82870_a(p_75144_4_, inventoryplayer.func_70445_o());
                            }
                        }
                    }

                    slot2.func_75218_e();
                }
            }
        } else if (p_75144_3_ == 2 && p_75144_2_ >= 0 && p_75144_2_ < 9) {
            Slot slot2 = (Slot)this.field_75151_b.get(p_75144_1_);
            if (slot2.func_82869_a(p_75144_4_)) {
                ItemStack itemstack3x = inventoryplayer.func_70301_a(p_75144_2_);
                boolean flag = itemstack3x == null || slot2.field_75224_c == inventoryplayer && slot2.func_75214_a(itemstack3x);
                int l1xx = -1;
                if (!flag) {
                    l1xx = inventoryplayer.func_70447_i();
                    flag |= l1xx > -1;
                }

                if (slot2.func_75216_d() && flag) {
                    ItemStack itemstack5 = slot2.func_75211_c();
                    inventoryplayer.func_70299_a(p_75144_2_, itemstack5.func_77946_l());
                    if ((slot2.field_75224_c != inventoryplayer || !slot2.func_75214_a(itemstack3x)) && itemstack3x != null) {
                        if (l1xx > -1) {
                            inventoryplayer.func_70441_a(itemstack3x);
                            slot2.func_75209_a(itemstack5.field_77994_a);
                            slot2.func_75215_d((ItemStack)null);
                            slot2.func_82870_a(p_75144_4_, itemstack5);
                        }
                    } else {
                        slot2.func_75209_a(itemstack5.field_77994_a);
                        slot2.func_75215_d(itemstack3x);
                        slot2.func_82870_a(p_75144_4_, itemstack5);
                    }
                } else if (!slot2.func_75216_d() && itemstack3x != null && slot2.func_75214_a(itemstack3x)) {
                    inventoryplayer.func_70299_a(p_75144_2_, (ItemStack)null);
                    slot2.func_75215_d(itemstack3x);
                }
            }
        } else if (p_75144_3_ == 3 && p_75144_4_.field_71075_bZ.field_75098_d && inventoryplayer.func_70445_o() == null && p_75144_1_ >= 0) {
            Slot slot2 = (Slot)this.field_75151_b.get(p_75144_1_);
            if (slot2 != null && slot2.func_75216_d()) {
                ItemStack itemstack3xx = slot2.func_75211_c().func_77946_l();
                itemstack3xx.field_77994_a = itemstack3xx.func_77976_d();
                inventoryplayer.func_70437_b(itemstack3xx);
            }
        } else if (p_75144_3_ == 4 && inventoryplayer.func_70445_o() == null && p_75144_1_ >= 0) {
            Slot slot2 = (Slot)this.field_75151_b.get(p_75144_1_);
            if (slot2 != null && slot2.func_75216_d() && slot2.func_82869_a(p_75144_4_)) {
                ItemStack itemstack3xx = slot2.func_75209_a(p_75144_2_ == 0 ? 1 : slot2.func_75211_c().field_77994_a);
                slot2.func_82870_a(p_75144_4_, itemstack3xx);
                p_75144_4_.func_71019_a(itemstack3xx, true);
            }
        } else if (p_75144_3_ == 6 && p_75144_1_ >= 0) {
            Slot slot2 = (Slot)this.field_75151_b.get(p_75144_1_);
            ItemStack itemstack3xx = inventoryplayer.func_70445_o();
            if (itemstack3xx != null && (slot2 == null || !slot2.func_75216_d() || !slot2.func_82869_a(p_75144_4_))) {
                int i1 = p_75144_2_ == 0 ? 0 : this.field_75151_b.size() - 1;
                int l1xxx = p_75144_2_ == 0 ? 1 : -1;

                for (int i2 = 0; i2 < 2; i2++) {
                    for (int j2 = i1; j2 >= 0 && j2 < this.field_75151_b.size() && itemstack3xx.field_77994_a < itemstack3xx.func_77976_d(); j2 += l1xxx) {
                        Slot slot3 = (Slot)this.field_75151_b.get(j2);
                        if (slot3.func_75216_d()
                            && func_94527_a(slot3, itemstack3xx, true)
                            && slot3.func_82869_a(p_75144_4_)
                            && this.func_94530_a(itemstack3xx, slot3)
                            && (i2 != 0 || slot3.func_75211_c().field_77994_a != slot3.func_75211_c().func_77976_d())) {
                            int k1 = Math.min(itemstack3xx.func_77976_d() - itemstack3xx.field_77994_a, slot3.func_75211_c().field_77994_a);
                            ItemStack itemstack2 = slot3.func_75209_a(k1);
                            itemstack3xx.field_77994_a += k1;
                            if (itemstack2.field_77994_a <= 0) {
                                slot3.func_75215_d((ItemStack)null);
                            }

                            slot3.func_82870_a(p_75144_4_, itemstack2);
                        }
                    }
                }
            }

            this.func_75142_b();
        }

        return itemstack;
    }

    protected void func_94533_d() {
        this.field_94536_g = 0;
        this.field_94537_h.clear();
    }
}
