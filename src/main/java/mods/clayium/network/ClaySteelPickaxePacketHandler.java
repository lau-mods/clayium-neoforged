package mods.clayium.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import java.util.HashMap;
import mods.clayium.util.UtilAdvancedTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.DimensionManager;

public class ClaySteelPickaxePacketHandler implements IMessageHandler<ClaySteelPickaxePacket, IMessage> {
    public ClaySteelPickaxePacketHandler() {
        super();
    }

    public IMessage onMessage(ClaySteelPickaxePacket message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().field_147369_b;
        if (message.side != -2) {
            UtilAdvancedTools.sideList.put(entityPlayer, message.side);
        } else {
            HashMap<String, Integer> map = new HashMap<>();
            map.put("x", message.x);
            map.put("y", message.y);
            map.put("z", message.z);
            map.put("d", message.dimid);
            DimensionManager.getWorld(message.dimid).func_147471_g(message.x, message.y, message.z);
            UtilAdvancedTools.forceList.put(entityPlayer, map);
        }

        return null;
    }
}
