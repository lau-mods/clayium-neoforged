package mods.clayium.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class Inventories implements IInventory, IGeneralInterface {
    public IInventory[] inventories;
    public static int idOffset = 8;

    public Inventories(IInventory[] inventories) {
        super();
        this.inventories = inventories;
    }

    public int func_70302_i_() {
        int size = 0;

        for (IInventory inventory : this.inventories) {
            size += inventory.func_70302_i_();
        }

        return size;
    }

    public int invPos(int id, int pos) {
        if (id >= 0 && id < this.inventories.length && pos >= 0) {
            int size = 0;

            int i;
            for (i = 0; i < id; i++) {
                size += this.inventories[i].func_70302_i_();
            }

            return pos >= this.inventories[i].func_70302_i_() ? -1 : size + pos;
        } else {
            return -1;
        }
    }

    public int[] invIdPos(int pos) {
        int size = 0;

        for (int i = 0; i < this.inventories.length; i++) {
            if (size + this.inventories[i].func_70302_i_() > pos) {
                return new int[]{i, pos - size};
            }

            size += this.inventories[i].func_70302_i_();
        }

        return null;
    }

    public IInventory get(int id) {
        return this.inventories[id];
    }

    public int size() {
        return this.inventories.length;
    }

    public ItemStack func_70301_a(int pos) {
        int[] idPos = this.invIdPos(pos);
        return idPos == null ? null : this.inventories[idPos[0]].func_70301_a(idPos[1]);
    }

    public ItemStack func_70298_a(int pos, int stackSize) {
        int[] idPos = this.invIdPos(pos);
        return idPos == null ? null : this.inventories[idPos[0]].func_70298_a(idPos[1], stackSize);
    }

    public ItemStack func_70304_b(int pos) {
        int[] idPos = this.invIdPos(pos);
        return idPos == null ? null : this.inventories[idPos[0]].func_70304_b(idPos[1]);
    }

    public void func_70299_a(int pos, ItemStack p_70299_2_) {
        int[] idPos = this.invIdPos(pos);
        if (idPos != null) {
            this.inventories[idPos[0]].func_70299_a(idPos[1], p_70299_2_);
        }
    }

    public String func_145825_b() {
        return null;
    }

    public boolean func_145818_k_() {
        return false;
    }

    public int func_70297_j_() {
        return 64;
    }

    public void func_70296_d() {
        for (IInventory inventory : this.inventories) {
            inventory.func_70296_d();
        }
    }

    public boolean func_70300_a(EntityPlayer p_70300_1_) {
        return true;
    }

    public void func_70295_k_() {
        for (IInventory inventory : this.inventories) {
            inventory.func_70295_k_();
        }
    }

    public void func_70305_f() {
        for (IInventory inventory : this.inventories) {
            inventory.func_70305_f();
        }
    }

    public boolean func_94041_b(int pos, ItemStack p_94041_2_) {
        int[] idPos = this.invIdPos(pos);
        return idPos == null ? false : this.inventories[idPos[0]].func_94041_b(idPos[1], p_94041_2_);
    }

    public void markForStrongUpdate() {
        for (IInventory inventory : this.inventories) {
            if (inventory instanceof IGeneralInterface) {
                ((IGeneralInterface)inventory).markForStrongUpdate();
            }
        }
    }

    public void markForWeakUpdate() {
        for (IInventory inventory : this.inventories) {
            if (inventory instanceof IGeneralInterface) {
                ((IGeneralInterface)inventory).markForWeakUpdate();
            }
        }
    }

    public void setSyncFlag() {
        for (IInventory inventory : this.inventories) {
            if (inventory instanceof IGeneralInterface) {
                ((IGeneralInterface)inventory).setSyncFlag();
            }
        }
    }

    public void setInstantSyncFlag() {
        for (IInventory inventory : this.inventories) {
            if (inventory instanceof IGeneralInterface) {
                ((IGeneralInterface)inventory).setInstantSyncFlag();
            }
        }
    }

    public void setRenderSyncFlag() {
        for (IInventory inventory : this.inventories) {
            if (inventory instanceof IGeneralInterface) {
                ((IGeneralInterface)inventory).setRenderSyncFlag();
            }
        }
    }

    public void pushButton(EntityPlayer player, int action) {
        int inventoryId = getInventoryId(action);
        action = getInnerActionId(action);
        if (inventoryId >= 0 && inventoryId < this.inventories.length && this.inventories[inventoryId] instanceof IGeneralInterface) {
            ((IGeneralInterface)this.inventories[inventoryId]).pushButton(player, action);
        }
    }

    public void pushButton(EntityPlayer player, int action, int inventoryId) {
        this.pushButton(player, getGlobalActionId(action, inventoryId));
    }

    public static int getGlobalActionId(int action, int inventoryId) {
        return action + inventoryId * idOffset;
    }

    public static int getInnerActionId(int action) {
        return action % idOffset;
    }

    public static int getInventoryId(int action) {
        return action / idOffset;
    }
}
