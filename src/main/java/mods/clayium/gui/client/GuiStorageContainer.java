package mods.clayium.gui.client;

import mods.clayium.block.tile.TileStorageContainer;
import mods.clayium.gui.container.ContainerTemp;

public class GuiStorageContainer extends GuiTemp {
    protected TileStorageContainer tile;

    public GuiStorageContainer(ContainerTemp container, TileStorageContainer tile) {
        super(container);
        this.tile = tile;
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        int size = this.tile.containerItemStacks[0] == null ? 0 : this.tile.containerItemStacks[0].field_77994_a;
        int width = this.field_146289_q.func_78256_a("" + size);
        this.field_146289_q
            .func_78276_b("" + size + " / " + this.tile.maxStorageSize, container.machineGuiSizeX / 2 - 8 - width, container.machineGuiSizeY - 12, 4210752);
    }
}
