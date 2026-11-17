package net.claustra01.clayium.logistics.filter;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FilterControlPayload(int action) implements CustomPacketPayload {
    public static final Type<FilterControlPayload> TYPE = new Type<>(ClayiumMod.id("filter_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterControlPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FilterControlPayload::action, FilterControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, (payload, context) -> apply(payload, context));
    }

    private static void apply(FilterControlPayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof FilterMenu menu && menu.stillValid(context.player())
                && payload.action >= 0 && payload.action <= 4) {
            menu.apply(payload.action);
        }
    }
}
