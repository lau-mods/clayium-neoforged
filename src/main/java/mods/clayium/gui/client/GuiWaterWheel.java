package mods.clayium.gui.client;

import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.block.tile.TileWaterWheel;
import mods.clayium.gui.container.ContainerTemp;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;

public class GuiWaterWheel extends GuiClayContainerTemp {
    public GuiWaterWheel(ContainerTemp container, TileClayContainer tile, Block block) {
        super(container, tile, block);
    }

    protected void func_146979_b(int mouseX, int mouseZ) {
        super.func_146979_b(mouseX, mouseZ);
        ContainerTemp container = (ContainerTemp)this.field_147002_h;
        TileWaterWheel tile = (TileWaterWheel)this.tile;
        this.field_146289_q
            .func_78276_b(I18n.func_135052_a("gui.WaterWheel.progress", new Object[]{tile.progress}), 6, container.machineGuiSizeY - 12, 4210752);
        this.field_146289_q
            .func_78276_b(I18n.func_135052_a("gui.WaterWheel.durability", new Object[]{tile.progressEfficiency}), 90, container.machineGuiSizeY - 12, 4210752);
        this.field_146289_q
            .func_78276_b(
                I18n.func_135052_a("gui.WaterWheel.surroundingWater", new Object[]{tile.countSurroundingWater()}), 90, container.machineGuiSizeY - 0, 4210752
            );
    }
}
