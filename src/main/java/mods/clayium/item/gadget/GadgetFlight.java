package mods.clayium.item.gadget;

import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class GadgetFlight extends GadgetOrdinal {
    public GadgetFlight() {
        super("Flight0", "Flight1", "Flight2");
    }

    public void update(int itemIndex, Entity entity, boolean isRemote) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)entity;
            int oldMode = -1;
            if (UtilPlayer.getPlayerInstantData(player, "GadgetFlight") != null) {
                oldMode = (Integer)UtilPlayer.getPlayerInstantData(player, "GadgetFlight");
            }

            if (itemIndex == -1) {
                if (oldMode != itemIndex && !ClayiumCore.proxy.isCreative(player)) {
                    player.field_71075_bZ.field_75101_c = false;
                    player.field_71075_bZ.field_75100_b = false;
                }
            } else {
                player.field_71075_bZ.field_75101_c = true;
                if (entity.field_70181_x >= 0.0) {
                    entity.field_70143_R = 0.0F;
                } else {
                    int t = (int)(-entity.field_70181_x / 0.05);
                    entity.field_70143_R = Math.min(entity.field_70143_R, (float)(t * (t - 1)) * 0.025F);
                }
            }

            if (isRemote && ClayiumCore.proxy.getClientPlayer() == entity) {
                ClayiumCore.proxy.updateFlightStatus(itemIndex);
            }

            UtilPlayer.setPlayerInstantData(player, "GadgetFlight", itemIndex);
        }
    }
}
