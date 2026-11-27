package mods.clayium.gui.client;

import cpw.mods.fml.client.config.GuiButtonExt;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.gui.container.ContainerNormalInventory;
import mods.clayium.gui.container.ContainerTemp;
import net.minecraft.block.Block;

public class GuiMultipageContainer extends GuiClayContainerTemp {
    public GuiMultipageContainer(ContainerTemp container, TileClayContainer tile, Block block) {
        super(container, tile, block);
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        if (this.field_147002_h instanceof ContainerNormalInventory) {
            ContainerNormalInventory container = (ContainerNormalInventory)this.field_147002_h;
            if (container.isMultipage()) {
                this.field_146289_q
                    .func_78276_b(
                        container.getPresentPageNum() + "/" + container.getMaxPageNum(),
                        container.playerSlotOffsetX + 8 + 162,
                        container.playerSlotOffsetY + 32,
                        4210752
                    );
            }
        }
    }

    public void addButton() {
        if (this.field_147002_h instanceof ContainerNormalInventory) {
            ContainerNormalInventory container = (ContainerNormalInventory)this.field_147002_h;
            if (container.isMultipage()) {
                int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
                int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
                this.field_146292_n
                    .add(
                        new GuiButtonExt(
                            ContainerNormalInventory.buttonIdPrevious,
                            offsetX + container.playerSlotOffsetX + 8 + 162,
                            offsetY + container.playerSlotOffsetY + 12,
                            16,
                            16,
                            "<"
                        )
                    );
                this.field_146292_n
                    .add(
                        new GuiButtonExt(
                            ContainerNormalInventory.buttonIdNext,
                            offsetX + container.playerSlotOffsetX + 8 + 180,
                            offsetY + container.playerSlotOffsetY + 12,
                            16,
                            16,
                            ">"
                        )
                    );
            }
        }
    }
}
