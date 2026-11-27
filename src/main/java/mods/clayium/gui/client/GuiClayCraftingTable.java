package mods.clayium.gui.client;

import codechicken.nei.recipe.GuiCraftingRecipe;
import java.util.ArrayList;
import java.util.List;
import mods.clayium.block.tile.TileClayContainer;
import mods.clayium.core.ClayiumCore;
import mods.clayium.gui.container.ContainerTemp;
import net.minecraft.block.Block;

public class GuiClayCraftingTable extends GuiClayContainerTemp {
    public GuiClayCraftingTable(ContainerTemp container, TileClayContainer tile, Block block) {
        super(container, tile, block);
    }

    public void func_73864_a(int mousex, int mousey, int button) {
        if (ClayiumCore.IntegrationID.NEI.loaded()) {
            int progressBarSizeX = 24;
            int progressBarSizeY = 17;
            int progressBarPosX = 90;
            int progressBarPosY = 34;
            int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
            int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
            if (offsetX + progressBarPosX <= mousex
                && offsetY + progressBarPosY <= mousey
                && offsetX + progressBarPosX + progressBarSizeX > mousex
                && offsetY + progressBarPosY + progressBarSizeY > mousey) {
                GuiCraftingRecipe.openRecipeGui("crafting", new Object[0]);
                return;
            }
        }

        super.func_73864_a(mousex, mousey, button);
    }

    public void func_73863_a(int mousex, int mousey, float p_73863_3_) {
        super.func_73863_a(mousex, mousey, p_73863_3_);
        if (ClayiumCore.IntegrationID.NEI.loaded()) {
            int progressBarSizeX = 24;
            int progressBarSizeY = 17;
            int progressBarPosX = 90;
            int progressBarPosY = 34;
            int offsetX = (this.field_146294_l - this.field_146999_f) / 2;
            int offsetY = (this.field_146295_m - this.field_147000_g) / 2;
            if (offsetX + progressBarPosX <= mousex
                && offsetY + progressBarPosY <= mousey
                && offsetX + progressBarPosX + progressBarSizeX > mousex
                && offsetY + progressBarPosY + progressBarSizeY > mousey) {
                List list = new ArrayList();
                list.add("Recipes");
                this.func_146283_a(list, mousex, mousey);
            }
        }
    }
}
