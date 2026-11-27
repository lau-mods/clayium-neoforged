package mods.clayium.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import mods.clayium.util.UtilKeyInput;
import net.minecraft.entity.player.EntityPlayer;

public class MouseClickEventPacketHandler implements IMessageHandler<MouseClickEventPacket, IMessage> {
    public MouseClickEventPacketHandler() {
        super();
    }

    public IMessage onMessage(MouseClickEventPacket message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().field_147369_b;
        UtilKeyInput.setMouseInput(ctx.getServerHandler().field_147369_b, message.button, message.buttonstate);
        return null;
    }
}
