package mods.clayium.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import mods.clayium.util.UtilKeyInput;
import net.minecraft.entity.player.EntityPlayer;

public class KeyInputEventPacketHandler implements IMessageHandler<KeyInputEventPacket, IMessage> {
    public KeyInputEventPacketHandler() {
        super();
    }

    public IMessage onMessage(KeyInputEventPacket message, MessageContext ctx) {
        EntityPlayer entityPlayer = ctx.getServerHandler().field_147369_b;
        UtilKeyInput.setKeyInput(ctx.getServerHandler().field_147369_b, message.key, message.keystate);
        return null;
    }
}
