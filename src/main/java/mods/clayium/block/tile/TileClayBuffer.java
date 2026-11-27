package mods.clayium.block.tile;

import net.minecraft.item.ItemStack;

public class TileClayBuffer extends TileClayContainerTiered implements INormalInventory {
    public int inventoryX = 0;
    public int inventoryY = 0;

    public TileClayBuffer() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.insertRoutes = new int[]{-1, -1, -1, 0, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.autoInsert = true;
        this.autoExtract = true;
        this.containerItemStacks = new ItemStack[54];
        this.clayEnergySlot = -1;
    }

    public void initParamsByTier(int tier) {
        this.setDefaultTransportationParamsByTier(tier, TileClayContainerTiered.ParamMode.BUFFER);
        switch (tier) {
            case 4:
                this.inventoryX = 1 + (this.inventoryY = 1);
                break;
            case 5:
                this.inventoryX = 1 + (this.inventoryY = 2);
                break;
            case 6:
                this.inventoryX = 1 + (this.inventoryY = 3);
                break;
            case 7:
                this.inventoryX = 1 + (this.inventoryY = 4);
                break;
            case 8:
                this.inventoryX = 5 + (this.inventoryY = 4);
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
                this.inventoryX = 3 + (this.inventoryY = 6);
                break;
            default:
                this.inventoryX = this.inventoryY = 1;
        }

        int slotNum = this.inventoryX * this.inventoryY;
        int[] slots = new int[slotNum];
        int[] slots2 = new int[slotNum];

        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
            slots2[i] = slots.length - i - 1;
        }

        this.listSlotsInsert.add(slots);
        this.listSlotsExtract.add(slots2);
        this.slotsDrop = slots;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public boolean func_94041_b(int slot, ItemStack itemstack) {
        return true;
    }

    public int[] func_94128_d(int side) {
        int slotNum = this.inventoryX * this.inventoryY;
        int[] slots = new int[slotNum];
        int i = 0;

        while (i < slots.length) {
            slots[i] = i++;
        }

        return slots;
    }

    public boolean canExtractItemUnsafe(int slot, ItemStack itemstack, int route) {
        return true;
    }

    public boolean canInsertItemUnsafe(int slot, ItemStack itemstack, int route) {
        return true;
    }

    public int getInventoryX() {
        return this.inventoryX;
    }

    public int getInventoryY() {
        return this.inventoryY;
    }

    public int getInventoryStart() {
        return 0;
    }

    public int getInventoryP() {
        return 1;
    }
}
