package mods.clayium.gui.client;

import mods.clayium.block.tile.TileClayBlastFurnace;
import mods.clayium.gui.container.ContainerTemp;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;

public class GuiClayBlastFurnace extends GuiClayMachines {
    public GuiClayBlastFurnace(ContainerTemp container, TileClayBlastFurnace tile, Block block) {
        super(container, tile, block);
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        this.field_146289_q.func_78276_b(UtilLocale.tierGui(((TileClayBlastFurnace)this.tile).recipeTier), 64, container.machineGuiSizeY - 12, 4210752);
    }

    public void addButton() {
    }

    public void drawButton() {
    }
}
