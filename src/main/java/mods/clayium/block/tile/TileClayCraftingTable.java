package mods.clayium.block.tile;

import net.minecraft.item.ItemStack;

public class TileClayCraftingTable extends TileClayContainerTiered {
    public TileClayCraftingTable() {
        super();
    }

    protected void initParams() {
        this.containerItemStacks = new ItemStack[9];
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }
}
