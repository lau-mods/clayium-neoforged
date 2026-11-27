package mods.clayium.block.tile;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class InventoryOffsetted implements INormalInventory, IGeneralInterface {
    private IInventory inventory;
    protected int offset = 0;
    protected int offsetmin = Integer.MIN_VALUE;
    protected int offsetmax = Integer.MAX_VALUE;
    protected int inventorySize = 0;
    protected Map<Integer, Integer> buttonMap = new HashMap<>();

    public InventoryOffsetted(IInventory inventory) {
        this(inventory, 0);
    }

    public InventoryOffsetted(IInventory inventory, int initialOffset) {
        this(inventory, initialOffset, inventory.func_70302_i_());
    }

    public InventoryOffsetted(IInventory inventory, int initialOffset, int inventorySize) {
        super();
        this.inventory = inventory;
        this.setOffset(initialOffset);
        this.inventorySize = inventorySize;
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setOffsetBound(int min, int max) {
        this.offsetmin = min;
        this.offsetmax = max;
    }

    public void addButton(int buttonid, int offset) {
        this.buttonMap.put(buttonid, offset);
    }

    public void markForStrongUpdate() {
        if (this.inventory instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventory).markForStrongUpdate();
        }
    }

    public void markForWeakUpdate() {
        if (this.inventory instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventory).markForWeakUpdate();
        }
    }

    public void setSyncFlag() {
        if (this.inventory instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventory).setSyncFlag();
        }
    }

    public void setInstantSyncFlag() {
        if (this.inventory instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventory).setInstantSyncFlag();
        }
    }

    public void setRenderSyncFlag() {
        if (this.inventory instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventory).setRenderSyncFlag();
        }
    }

    public void pushButton(EntityPlayer player, int action) {
        if (this.buttonMap.containsKey(action)) {
            this.offset = this.offset + this.buttonMap.get(action);
            this.offset = Math.max(this.offsetmin, Math.min(this.offset, this.offsetmax));
        } else {
            if (this.inventory instanceof IGeneralInterface) {
                ((IGeneralInterface)this.inventory).setRenderSyncFlag();
            }
        }
    }

    public int func_70302_i_() {
        return this.inventorySize;
    }

    public ItemStack func_70301_a(int slot) {
        return this.offset + slot >= 0 && this.offset + slot < this.inventory.func_70302_i_() ? this.inventory.func_70301_a(this.offset + slot) : null;
    }

    public ItemStack func_70298_a(int slot, int p_70298_2_) {
        return this.offset + slot >= 0 && this.offset + slot < this.inventory.func_70302_i_()
            ? this.inventory.func_70298_a(this.offset + slot, p_70298_2_)
            : null;
    }

    public ItemStack func_70304_b(int slot) {
        return this.offset + slot >= 0 && this.offset + slot < this.inventory.func_70302_i_() ? this.inventory.func_70304_b(this.offset + slot) : null;
    }

    public void func_70299_a(int slot, ItemStack p_70299_2_) {
        if (this.offset + slot >= 0 && this.offset + slot < this.inventory.func_70302_i_()) {
            this.inventory.func_70299_a(this.offset + slot, p_70299_2_);
        }
    }

    public String func_145825_b() {
        return this.inventory.func_145825_b();
    }

    public boolean func_145818_k_() {
        return this.inventory.func_145818_k_();
    }

    public int func_70297_j_() {
        return this.inventory.func_70297_j_();
    }

    public void func_70296_d() {
        this.inventory.func_70296_d();
    }

    public boolean func_70300_a(EntityPlayer p_70300_1_) {
        return this.inventory.func_70300_a(p_70300_1_);
    }

    public void func_70295_k_() {
        this.inventory.func_70295_k_();
    }

    public void func_70305_f() {
        this.inventory.func_70305_f();
    }

    public boolean func_94041_b(int slot, ItemStack p_94041_2_) {
        return this.offset + slot >= 0 && this.offset + slot < this.inventory.func_70302_i_()
            ? this.inventory.func_94041_b(this.offset + slot, p_94041_2_)
            : false;
    }

    public int getInventoryX() {
        return this.inventory instanceof INormalInventory ? ((INormalInventory)this.inventory).getInventoryX() : 1;
    }

    public int getInventoryY() {
        return this.inventory instanceof INormalInventory ? ((INormalInventory)this.inventory).getInventoryY() : 1;
    }

    public int getInventoryP() {
        return this.inventory instanceof INormalInventory ? ((INormalInventory)this.inventory).getInventoryP() : 1;
    }

    public int getInventoryStart() {
        return this.inventory instanceof INormalInventory ? ((INormalInventory)this.inventory).getInventoryStart() : 0;
    }
}
