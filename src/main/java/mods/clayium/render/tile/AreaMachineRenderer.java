package mods.clayium.render.tile;

import mods.clayium.block.tile.IAxisAlignedBBContainer;
import mods.clayium.util.UtilRender;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class AreaMachineRenderer extends TileEntitySpecialRenderer {
    public AreaMachineRenderer() {
        super();
    }

    public void func_147500_a(TileEntity tile, double x, double y, double z, float p_147500_8_) {
        if (tile instanceof IAxisAlignedBBContainer) {
            UtilRender.setLightValue(15, 15);
            IAxisAlignedBBContainer tile1 = (IAxisAlignedBBContainer)tile;
            UtilRender.renderAxisAlignedBB(tile1, 0.1F, 0.1F, 0.7F);
            Block block1 = tile.func_145831_w().func_147439_a(tile.field_145851_c, tile.field_145848_d, tile.field_145849_e);
            UtilRender.renderBox(
                (double)tile.field_145851_c + block1.func_149704_x(),
                (double)tile.field_145848_d + block1.func_149665_z(),
                (double)tile.field_145849_e + block1.func_149706_B(),
                (double)tile.field_145851_c + block1.func_149753_y(),
                (double)tile.field_145848_d + block1.func_149669_A(),
                (double)tile.field_145849_e + block1.func_149693_C(),
                tile1.getBoxAppearance(),
                1.0F,
                0.0F,
                0.0F
            );
        }
    }
}
