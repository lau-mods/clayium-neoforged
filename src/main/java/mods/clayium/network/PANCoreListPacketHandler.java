package mods.clayium.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import mods.clayium.gui.client.GuiPANCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class PANCoreListPacketHandler implements IMessageHandler<PANCoreListPacket, IMessage> {
    public PANCoreListPacketHandler() {
        super();
    }

    public IMessage onMessage(PANCoreListPacket message, MessageContext ctx) {
        GuiScreen gui = Minecraft.func_71410_x().field_71462_r;
        if (gui != null && gui instanceof GuiPANCore) {
            GuiPANCore guiPanCore = (GuiPANCore)gui;
            if (message.windowId == guiPanCore.field_147002_h.field_75152_c) {
                guiPanCore.setItemList(message.ingreds, message.prohibiteds);
            }

            return null;
        } else {
            return null;
        }
    }
}
