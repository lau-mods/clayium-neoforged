package net.claustra01.clayium.network;

import io.netty.buffer.ByteBuf;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.ItemFilterStringMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-to-server text payload used by String Item Filter GUI.
 */
public record FilterTextPayload(int containerId, String filterText) implements CustomPacketPayload {
    public static final Type<FilterTextPayload> TYPE = new Type<>(ClayiumMod.id("filter_text"));
    public static final StreamCodec<ByteBuf, FilterTextPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            FilterTextPayload::containerId,
            ByteBufCodecs.STRING_UTF8,
            FilterTextPayload::filterText,
            FilterTextPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FilterTextPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer
                    && serverPlayer.containerMenu instanceof ItemFilterStringMenu menu
                    && serverPlayer.containerMenu.containerId == payload.containerId()) {
                menu.applyFilterText(payload.filterText());
            }
        });
    }
}
