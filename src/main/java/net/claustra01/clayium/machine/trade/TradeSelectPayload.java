package net.claustra01.clayium.machine.trade;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TradeSelectPayload(BlockPos pos, int offerIndex) implements CustomPacketPayload {
    public static final Type<TradeSelectPayload> TYPE = new Type<>(ClayiumMod.id("trade_select"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeSelectPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TradeSelectPayload::pos,
            ByteBufCodecs.VAR_INT, TradeSelectPayload::offerIndex,
            TradeSelectPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, TradeSelectPayload::handle);
    }

    private static void handle(TradeSelectPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            apply(player, payload);
        }
    }

    public static void apply(Player player, TradeSelectPayload payload) {
        if (!(player.containerMenu instanceof AutoTraderMenu menu)
                || !menu.pos().equals(payload.pos)
                || !menu.stillValid(player)
                || payload.offerIndex < 0) {
            return;
        }
        menu.autoTrader().selectOffer(payload.offerIndex);
    }
}
