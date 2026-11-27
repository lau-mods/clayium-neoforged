package mods.clayium.block.tile;

import mods.clayium.gui.container.ContainerNormalInventory;
import net.minecraft.inventory.IInventory;

public class InventoryMultiPage extends InventoryOffsetted {
    public int pageNum = 1;
    public int sizePerPage = 0;

    public InventoryMultiPage(IInventory inventory, int offset, int sizePerPage, int pageNum) {
        super(inventory, offset, sizePerPage);
        this.pageNum = pageNum;
        this.sizePerPage = sizePerPage;
        this.addButton(ContainerNormalInventory.buttonIdPrevious, -this.sizePerPage);
        this.addButton(ContainerNormalInventory.buttonIdNext, this.sizePerPage);
        this.setOffsetBound(0, (this.pageNum - 1) * this.sizePerPage);
    }

    public InventoryMultiPage(INormalInventory normalInventory) {
        this(
            normalInventory,
            normalInventory.getInventoryStart(),
            normalInventory.getInventoryX() * normalInventory.getInventoryY(),
            normalInventory.getInventoryP()
        );
    }

    public int getPresentPage() {
        return this.getOffset() / this.sizePerPage;
    }

    public boolean isMultiPage() {
        return this.pageNum >= 2;
    }
}
