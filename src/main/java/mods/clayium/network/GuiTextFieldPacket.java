package mods.clayium.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import mods.clayium.core.ClayiumCore;
import net.minecraft.network.PacketBuffer;

public class GuiTextFieldPacket implements IMessage {
    public int id;
    public String string;

    public GuiTextFieldPacket() {
        super();
    }

    public GuiTextFieldPacket(String string, int id) {
        super();
        this.id = id;
        this.string = string;
    }

    public void fromBytes(ByteBuf buf) {
        PacketBuffer b = new PacketBuffer(buf);
        this.id = b.readInt();

        try {
            this.string = b.func_150789_c(128);
        } catch (IOException var4) {
            var4.printStackTrace();
        }
    }

    public void toBytes(ByteBuf buf) {
        PacketBuffer b = new PacketBuffer(buf);
        b.writeInt(this.id);

        try {
            b.func_150785_a(this.string);
        } catch (IOException var4) {
            ClayiumCore.logger.catching(var4);
        }
    }
}
