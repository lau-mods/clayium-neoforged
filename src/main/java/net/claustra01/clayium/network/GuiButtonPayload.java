package net.claustra01.clayium.network;

import io.netty.buffer.ByteBuf;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server button click payload used by Clayium interactive GUIs
 * (Work Table buttons, manual machine button, Chest page navigation, Auto Trader navigation).
 */
public record GuiButtonPayload(int containerId, int buttonId) implements CustomPacketPayload {
    public static final Type<GuiButtonPayload> TYPE = new Type<>(ClayiumMod.id("gui_button"));
    public static final StreamCodec<ByteBuf, GuiButtonPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            GuiButtonPayload::containerId,
            ByteBufCodecs.VAR_INT,
            GuiButtonPayload::buttonId,
            GuiButtonPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(GuiButtonPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof IClayButtonMenu menu
                    && serverPlayer.containerMenu.containerId == payload.containerId()) {
                menu.handleGuiButton(serverPlayer, payload.buttonId());
            }
        });
    }
}
