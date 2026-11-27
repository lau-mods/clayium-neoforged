package mods.clayium.pan;

import mods.clayium.block.IPANConductor;
import mods.clayium.core.ClayiumCore;
import mods.clayium.plugin.multipart.UtilMultipart;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public class UtilPAN {
    public UtilPAN() {
        super();
    }

    public static boolean isPANConductor(IBlockAccess world, int x, int y, int z) {
        if (world == null) {
            return false;
        } else if (y >= 0 && y < 255) {
            Block b = world.func_147439_a(x, y, z);
            return isPANConductor(b, x, y, z) ? true : ClayiumCore.IntegrationID.MULTI_PART.loaded() && UtilMultipart.containsPANConductor(world, x, y, z);
        } else {
            return false;
        }
    }

    public static boolean isPANConductor(Block block, int x, int y, int z) {
        return block instanceof IPANConductor;
    }
}
