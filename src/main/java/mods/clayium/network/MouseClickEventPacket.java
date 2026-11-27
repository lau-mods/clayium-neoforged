package mods.clayium.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MouseClickEventPacket implements IMessage {
    public int button;
    public boolean buttonstate;

    public MouseClickEventPacket() {
        super();
    }

    public MouseClickEventPacket(int button, boolean buttonstate) {
        super();
        this.button = button;
        this.buttonstate = buttonstate;
    }

    public void fromBytes(ByteBuf buf) {
        this.button = buf.readInt();
        this.buttonstate = buf.readBoolean();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.button);
        buf.writeBoolean(this.buttonstate);
    }
}
