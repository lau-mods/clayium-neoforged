package mods.clayium.gui.client;

import mods.clayium.block.tile.TileClayReactor;
import mods.clayium.gui.container.ContainerTemp;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;

public class GuiClayReactor extends GuiClayMachines {
    public GuiClayReactor(ContainerTemp container, TileClayReactor tile, Block block) {
        super(container, tile, block);
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        long energy = ((TileClayReactor)this.tile).irradiatedLaser == null ? 0L : (long)((TileClayReactor)this.tile).irradiatedLaser.getEnergy();
        this.field_146289_q
            .func_78276_b(
                UtilLocale.tierGui(((TileClayReactor)this.tile).recipeTier) + "  " + UtilLocale.laserGui(energy), 64, container.machineGuiSizeY - 12, 4210752
            );
    }

    public void addButton() {
    }

    public void drawButton() {
    }
}
