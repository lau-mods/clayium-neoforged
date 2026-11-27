package mods.clayium.gui.client;

import mods.clayium.block.tile.TileAreaActivator;
import mods.clayium.gui.GuiPictureButton;
import mods.clayium.gui.container.ContainerAreaActivator;
import mods.clayium.gui.container.ContainerTemp;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;

public class GuiAreaActivator extends GuiClayEnergyTemp {
    public GuiAreaActivator(ContainerAreaActivator container, TileAreaActivator tile, Block block) {
        super(container, tile, block);
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        long energy = ((TileAreaActivator)this.tile).laserEnergy;
        this.field_146289_q.func_78276_b(UtilLocale.laserGui(energy), 64, container.machineGuiSizeY - 12, 4210752);
        TileAreaActivator tile = (TileAreaActivator)this.tile;
        ((GuiPictureButton)this.field_146292_n.get(4)).setTexture(GuiTemp.ButtonTexture, 16 * (5 + tile.target), 0);
        ((GuiPictureButton)this.field_146292_n.get(5)).setTexture(GuiTemp.ButtonTexture, 16 * (8 + (tile.enableRayTrace ? 1 : 0)), 0);
        ((GuiPictureButton)this.field_146292_n.get(6)).setTexture(GuiTemp.ButtonTexture, 16 * (10 + (tile.sneak ? 1 : 0)), 0);
    }

    public void addButton() {
        int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
        int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
        this.field_146292_n.add(new GuiPictureButton(0, offsetX + 16, offsetY + 16, 16, 16, GuiTemp.ButtonTexture, 16, 0));
        this.field_146292_n.add(new GuiPictureButton(1, offsetX + 16 + 18, offsetY + 16, 16, 16, GuiTemp.ButtonTexture, 32, 0));
        this.field_146292_n.add(new GuiPictureButton(2, offsetX + 16, offsetY + 16 + 18, 16, 16, GuiTemp.ButtonTexture, 64, 0));
        this.field_146292_n.add(new GuiPictureButton(3, offsetX + 16 + 18, offsetY + 16 + 18, 16, 16, GuiTemp.ButtonTexture, 48, 0));
        this.field_146292_n.add(new GuiPictureButton(4, offsetX + this.field_146999_f - 16 - 16, offsetY + 16, 16, 16, GuiTemp.ButtonTexture, 16, 0));
        this.field_146292_n.add(new GuiPictureButton(5, offsetX + this.field_146999_f - 16 - 16, offsetY + 16 + 18, 16, 16, GuiTemp.ButtonTexture, 16, 0));
        this.field_146292_n.add(new GuiPictureButton(6, offsetX + this.field_146999_f - 16 - 16, offsetY + 16 + 36, 16, 16, GuiTemp.ButtonTexture, 16, 0));
    }
}
