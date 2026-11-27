package mods.clayium.item.gadget;

import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;

public class GadgetLongArm extends GadgetOrdinal {
    public static float defaultDist = 5.0F;

    public GadgetLongArm() {
        super("LongArm0", "LongArm1", "LongArm2");
    }

    public void update(int itemIndex, Entity entity, boolean isRemote) {
        if (entity instanceof EntityPlayer) {
            Integer c = (Integer)UtilPlayer.getPlayerInstantData((EntityPlayer)entity, "GadgetLongArm");
            int i = -1;
            if (c != null) {
                i = c;
            }

            UtilPlayer.setPlayerInstantData((EntityPlayer)entity, "GadgetLongArm", itemIndex);
            if (!isRemote && entity instanceof EntityPlayerMP && i != itemIndex) {
                ((EntityPlayerMP)entity).field_71134_c.setBlockReachDistance((double)hookBlockReachDistance(defaultDist, (EntityPlayer)entity));
            }
        }
    }

    public static float hookBlockReachDistance(float distance) {
        return ClayiumCore.proxy.hookBlockReachDistance(distance);
    }

    public static float hookBlockReachDistance(float dist, EntityPlayer player) {
        Integer c = (Integer)UtilPlayer.getPlayerInstantData(player, "GadgetLongArm");
        int i = -1;
        if (c != null) {
            i = c;
        }

        float d = 0.0F;
        switch (i) {
            case 0:
                d = 3.0F;
                break;
            case 1:
                d = 7.0F;
                break;
            case 2:
                d = 20.0F;
        }

        return d + dist;
    }

    public static float hookBlockReachDistanceSq(float distSq, EntityPlayer player) {
        float ret = hookBlockReachDistance(MathHelper.func_76129_c(distSq), player);
        return ret * ret;
    }
}
