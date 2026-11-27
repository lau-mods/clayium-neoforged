package mods.clayium.block.tile;

import java.util.ArrayList;
import mods.clayium.block.StorageContainer;
import mods.clayium.item.CItems;
import mods.clayium.util.UtilItemStack;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class TileStorageContainer extends TileClayContainer {
    public int extractSlotNum = 4;
    public int insertSlotNum = 4;
    public int maxStorageSize = 65536;
    protected int alternativeStackSize = 0;
    protected ItemStack[] alternativeItemStacks;
    protected ItemStack[] alternativeItemStacksLastTick;

    public TileStorageContainer() {
        super();
        this.insertRoutes = new int[]{-1, 0, -1, -1, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.containerItemStacks = new ItemStack[2];
        this.maxAutoInsertDefault = this.maxAutoExtractDefault = 64;
        this.autoExtractInterval = this.autoInsertInterval = 1;
        this.maxStorageSize = 65536;
        this.alternativeItemStacks = new ItemStack[this.extractSlotNum + this.insertSlotNum + 1];
        this.alternativeItemStacksLastTick = new ItemStack[this.extractSlotNum + this.insertSlotNum + 1];
        this.listSlotsInsert = new ArrayList<>();
        this.listSlotsExtract = new ArrayList<>();
        int[] slots = new int[this.extractSlotNum + this.insertSlotNum];
        int[] slots2 = new int[this.extractSlotNum + this.insertSlotNum];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
            slots2[i] = slots.length - i - 1;
        }

        this.listSlotsInsert.add(slots);
        this.listSlotsExtract.add(slots2);
        this.slotsDrop = new int[0];
    }

    public void func_145845_h() {
        super.func_145845_h();

        for (int i = 0; i < this.alternativeItemStacks.length; i++) {
            this.manageAlternativeStorage(i);
        }

        if (this.containerItemStacks[0] != null && this.containerItemStacks[0].field_77994_a == 0) {
            this.containerItemStacks[0] = null;
        }
    }

    public int getItemUseMode(ItemStack itemStack, EntityPlayer player) {
        return UtilItemStack.areItemDamageEqual(itemStack, CItems.itemMisc.get("ClayCore")) ? 99 : super.getItemUseMode(itemStack, player);
    }

    public void useItemFromSide(ItemStack itemStack, EntityPlayer player, int side, int mode) {
        super.useItemFromSide(itemStack, player, side, mode);
        if (mode == 99 && this.maxStorageSize < Integer.MAX_VALUE) {
            this.setSyncFlag();
            this.maxStorageSize = Integer.MAX_VALUE;
            if (--player.func_71045_bC().field_77994_a == 0) {
                player.field_71071_by.field_70462_a[player.field_71071_by.field_70461_c] = null;
            }
        }
    }

    public int func_70302_i_() {
        return this.insertSlotNum + this.extractSlotNum;
    }

    public int getStackSizeInSlot(int slot) {
        if (this.containerItemStacks[0] == null) {
            return 0;
        } else if (slot >= -1 && slot <= this.extractSlotNum + this.insertSlotNum) {
            int stackSize = this.containerItemStacks[0].field_77994_a;
            int maxStackSize = this.containerItemStacks[0].func_77976_d();
            int containerMaxStorageSize = this.maxStorageSize - this.alternativeStackSize;
            int extractSlotNum = this.extractSlotNum;
            int insertSlotNum = this.insertSlotNum;
            if (slot == this.extractSlotNum + this.insertSlotNum) {
                return Math.min(stackSize, maxStackSize);
            } else {
                int[] map = new int[this.extractSlotNum + this.insertSlotNum];
                int p = 0;

                for (int i = 0; i < this.extractSlotNum + this.insertSlotNum; i++) {
                    if (this.alternativeItemStacksLastTick[i] != null) {
                        stackSize -= this.alternativeItemStacksLastTick[i].field_77994_a;
                        containerMaxStorageSize -= maxStackSize;
                        if (i == 0) {
                            containerMaxStorageSize -= this.maxStorageSize - maxStackSize * (this.extractSlotNum + this.insertSlotNum);
                        }

                        if (i < this.extractSlotNum) {
                            extractSlotNum--;
                        } else {
                            insertSlotNum--;
                        }

                        map[i] = -1;
                    } else {
                        map[i] = p++;
                    }
                }

                return this.getStackSizeInSlot(map[slot], stackSize, containerMaxStorageSize, maxStackSize, extractSlotNum, insertSlotNum);
            }
        } else {
            return 0;
        }
    }

    public int getStackSizeInSlot(int slot, int containerStackSize, int containerMaxStorageSize, int itemMaxStackSize, int extractSlotNum, int insertSlotNum) {
        if (this.containerItemStacks[0] == null) {
            return 0;
        } else if (slot >= -1 && slot < extractSlotNum + insertSlotNum) {
            int restStackSize = containerMaxStorageSize - containerStackSize;
            int filledSlots = containerStackSize / itemMaxStackSize;
            int insertAreaStackSize = insertSlotNum * itemMaxStackSize > restStackSize ? insertSlotNum * itemMaxStackSize - restStackSize : 0;
            if (slot == 0 && extractSlotNum > 0 && filledSlots >= extractSlotNum) {
                return containerStackSize - insertAreaStackSize - itemMaxStackSize * (extractSlotNum - 1);
            } else if (slot >= extractSlotNum && slot < extractSlotNum + insertSlotNum) {
                int slot0 = slot - extractSlotNum;
                filledSlots = insertAreaStackSize / itemMaxStackSize;
                return slot0 < filledSlots ? itemMaxStackSize : (slot0 == filledSlots ? insertAreaStackSize - filledSlots * itemMaxStackSize : 0);
            } else {
                return slot < filledSlots ? itemMaxStackSize : (slot == filledSlots ? containerStackSize - filledSlots * itemMaxStackSize : 0);
            }
        } else {
            return 0;
        }
    }

    private void debug(String str) {
    }

    public ItemStack func_70301_a(int slot) {
        this.debug("getStackInSlot(" + slot + ")");
        if (slot >= this.extractSlotNum + this.insertSlotNum + 1) {
            return super.func_70301_a(1);
        } else {
            if (this.alternativeItemStacks[slot] == null) {
                int size = this.getStackSizeInSlot(slot);
                if (size == 0) {
                    return null;
                }

                ItemStack res = this.containerItemStacks[0].func_77946_l();
                res.field_77994_a = size;
                if (this.field_145850_b.field_72995_K) {
                    return res;
                }

                this.alternativeItemStacksLastTick[slot] = res.func_77946_l();
                this.alternativeItemStacks[slot] = res.func_77946_l();
            }

            return this.alternativeItemStacks[slot];
        }
    }

    public ItemStack func_70298_a(int slot, int size) {
        this.setSyncFlag();
        this.debug("decrStackSize(" + slot + "," + size + ")");
        if (slot >= this.extractSlotNum + this.insertSlotNum + 1) {
            return super.func_70298_a(1, size);
        } else if (this.alternativeItemStacks[slot] == null) {
            int stackSize = this.getStackSizeInSlot(slot);
            int resSize = size >= stackSize ? stackSize : size;
            if (this.containerItemStacks[0] != null && resSize != 0) {
                ItemStack res = this.containerItemStacks[0].func_77979_a(resSize);
                if (this.containerItemStacks[0].field_77994_a == 0) {
                    this.containerItemStacks[0] = null;
                }

                return res;
            } else {
                return null;
            }
        } else {
            int stackSize = this.alternativeItemStacks[slot].field_77994_a;
            int resSize = size >= stackSize ? stackSize : size;
            return this.alternativeItemStacks[slot].func_77979_a(resSize);
        }
    }

    public ItemStack func_70304_b(int slot) {
        return slot >= this.extractSlotNum + this.insertSlotNum + 1 ? super.func_70304_b(1) : this.func_70298_a(slot, this.getStackSizeInSlot(slot));
    }

    public void func_70299_a(int slot, ItemStack itemstack) {
        this.debug("setInventorySlotContents(" + slot + "," + itemstack + "@" + (itemstack == null ? "n" : itemstack.hashCode()) + ")");
        if (slot >= this.extractSlotNum + this.insertSlotNum + 1) {
            super.func_70299_a(1, itemstack);
        } else if (this.alternativeItemStacks[slot] == null || this.alternativeItemStacks[slot] != itemstack) {
            this.setInventorySlotContentsUnsafe(slot, itemstack);
            this.initializealternativeStorage(slot);
        }
    }

    public void manageAlternativeStorage(int slot) {
        if (slot == 0 && this.alternativeStackSize > 0) {
            this.addToStorage(this.alternativeStackSize);
            this.alternativeStackSize = 0;
        }

        if (this.alternativeItemStacks[slot] != null) {
            if (this.containerItemStacks[0] == null) {
                if (this.alternativeItemStacks[slot].field_77994_a != this.alternativeItemStacksLastTick[slot].field_77994_a) {
                    this.setInventorySlotContentsUnsafe(slot, this.alternativeItemStacks[slot]);
                }
            } else {
                this.addToStorage(this.alternativeItemStacks[slot].field_77994_a - this.alternativeItemStacksLastTick[slot].field_77994_a);
            }

            this.initializealternativeStorage(slot);
        }
    }

    public void initializealternativeStorage(int slot) {
        if (this.alternativeItemStacks[slot] != null) {
            this.alternativeItemStacks[slot].field_77994_a = 0;
            this.alternativeItemStacks[slot] = null;
            this.alternativeItemStacksLastTick[slot] = null;
        }

        if (slot == 0 && this.alternativeStackSize > 0) {
            this.addToStorage(this.alternativeStackSize);
            this.alternativeStackSize = 0;
        }
    }

    public void setInventorySlotContentsUnsafe(int slot, ItemStack itemstack) {
        this.setSyncFlag();
        if (this.containerItemStacks[0] == null) {
            if (itemstack != null) {
                this.containerItemStacks[0] = itemstack.func_77946_l();
            }
        } else {
            int setsize = itemstack == null ? 0 : itemstack.field_77994_a;
            int dif = setsize - this.getStackSizeInSlot(slot);
            if (this.alternativeItemStacksLastTick[slot] != null) {
                dif = setsize - this.alternativeItemStacksLastTick[slot].field_77994_a;
                this.alternativeItemStacks[slot].field_77994_a = this.alternativeItemStacksLastTick[slot].field_77994_a;
            }

            if (this.alternativeItemStacks[0] != null && dif > 0) {
                this.alternativeStackSize += dif;
            } else {
                this.addToStorage(dif);
            }
        }

        this.debug("done setInventorySlotContentsUnsafe(" + slot + "," + itemstack + "@" + (itemstack == null ? "n" : itemstack.hashCode()) + ")");
    }

    public void addToStorage(int stackSize) {
        if (this.maxStorageSize - this.containerItemStacks[0].field_77994_a < stackSize) {
            this.containerItemStacks[0].field_77994_a = this.maxStorageSize;
        } else {
            this.containerItemStacks[0].field_77994_a += stackSize;
        }
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        return slot >= this.insertSlotNum + this.extractSlotNum + 1
            || (this.containerItemStacks[0] == null || UtilItemStack.areTypeEqual(itemstack, this.containerItemStacks[0]))
                && (this.containerItemStacks[1] == null || this.checkFilterSlot(itemstack, this.containerItemStacks[1]));
    }

    public int[] func_94128_d(int side) {
        int[] slots = new int[this.insertSlotNum + this.extractSlotNum];
        int i = 0;

        while (i < slots.length) {
            slots[i] = i++;
        }

        return slots;
    }

    public boolean canInsertItemUnsafe(int slot, ItemStack itemstack, int route) {
        ItemStack itemInSlot = this.func_70301_a(slot);
        return itemInSlot != null && itemInSlot.field_77994_a > itemInSlot.func_77976_d() ? false : this.func_94041_b(slot, itemstack);
    }

    public boolean canExtractItemUnsafe(int slot, ItemStack itemstack, int route) {
        return true;
    }

    public boolean checkFilterSlot(ItemStack itemstack, ItemStack filter) {
        return UtilItemStack.areTypeEqual(itemstack, filter);
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        if (this.containerItemStacks[0] != null && tagCompound.func_74764_b("ItemStackSize")) {
            this.containerItemStacks[0].field_77994_a = tagCompound.func_74762_e("ItemStackSize");
        }

        if (tagCompound.func_74764_b("MaxStorageSize")) {
            this.maxStorageSize = tagCompound.func_74762_e("MaxStorageSize");
        }
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);

        for (int i = 0; i < this.alternativeItemStacks.length; i++) {
            this.manageAlternativeStorage(i);
        }

        if (this.containerItemStacks[0] != null) {
            tagCompound.func_74768_a("ItemStackSize", this.containerItemStacks[0].field_77994_a);
        } else {
            tagCompound.func_74768_a("ItemStackSize", 0);
        }

        tagCompound.func_74768_a("MaxStorageSize", this.maxStorageSize);
    }

    public int[] getSlotsDrop() {
        return new int[0];
    }

    public boolean shouldRefresh() {
        return false;
    }

    public boolean hasSpecialDrops() {
        return true;
    }

    public void setSyncFlag() {
        super.setSyncFlag();
        this.markForWeakUpdate();
    }

    public boolean acceptInterfaceSync() {
        return false;
    }

    public boolean shouldRenderInPass(int pass) {
        return true;
    }

    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, Block block, int metadata, int fortune) {
        ArrayList<ItemStack> ret = super.getDrops(world, x, y, z, block, metadata, fortune);
        if (ret != null) {
            for (int i = 0; i < ret.size(); i++) {
                ItemStack item = ret.get(i);
                ret.set(i, StorageContainer.expandStorage(item, StorageContainer.getStorageSize(item)));
            }
        }

        return ret;
    }
}
