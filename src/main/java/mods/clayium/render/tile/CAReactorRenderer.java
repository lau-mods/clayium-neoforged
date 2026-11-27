package mods.clayium.render.tile;

import mods.clayium.block.tile.TileCAReactor;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilRender;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class CAReactorRenderer extends TileEntitySpecialRenderer {
    public CAReactorRenderer() {
        super();
    }

    public void func_147500_a(TileEntity tile, double x, double y, double z, float p_147500_8_) {
        if (tile instanceof TileCAReactor && ClayiumCore.cfgCAReactorGlittering) {
            TileCAReactor tile1 = (TileCAReactor)tile;
            if (tile1.isDoingWork() && tile1.hullCoords != null) {
                UtilRender.setLightValue(15, 15);

                for (int j = 1; (double)j < tile1.reactorRank + 1.01; j++) {
                    for (int[] coord : tile1.hullCoords) {
                        int[] rel = tile1.getRelativeCoord(coord[0], coord[1], coord[2]);
                        float s = 1.0F;
                        float i = (float)tile1.reactorRank + 1.0F - (float)j;
                        float r = (float)j / ((float)tile1.reactorRank + 1.0F);
                        float f = 0.01F * ((float)Math.pow((double)i, 1.6) + 1.0F);
                        UtilRender.renderBox(
                            (double)((float)rel[0] - f),
                            (double)((float)rel[1] - f),
                            (double)((float)rel[2] - f),
                            (double)((float)rel[0] + s + f),
                            (double)((float)rel[1] + s + f),
                            (double)((float)rel[2] + s + f),
                            1,
                            1.0F,
                            1.0F,
                            0.3F + 0.05F * (2.0F * r - r * r) * (float)j,
                            0.11F,
                            false
                        );
                    }
                }
            }
        }
    }
}
