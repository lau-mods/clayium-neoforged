package net.claustra01.clayium.logistics.distributor;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DistributorControlPayload(BlockPos pos, int direction, int mode) implements CustomPacketPayload {
    public static final Type<DistributorControlPayload> TYPE = new Type<>(ClayiumMod.id("distributor_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DistributorControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, DistributorControlPayload::pos,
            ByteBufCodecs.VAR_INT, DistributorControlPayload::direction,
            ByteBufCodecs.VAR_INT, DistributorControlPayload::mode,
            DistributorControlPayload::new);
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, DistributorControlPayload::handle);
    }
    private static void handle(DistributorControlPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player
                && player.containerMenu instanceof DistributorMenu menu
                && menu.pos().equals(payload.pos) && menu.stillValid(player)
                && payload.direction >= 0 && payload.direction < Direction.values().length
                && payload.mode >= 0 && payload.mode < BufferBlockEntity.FaceMode.values().length) {
            menu.distributor().setFaceMode(Direction.from3DDataValue(payload.direction),
                    BufferBlockEntity.FaceMode.values()[payload.mode]);
        }
    }
}
