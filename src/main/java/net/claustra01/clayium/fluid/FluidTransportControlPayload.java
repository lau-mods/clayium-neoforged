package net.claustra01.clayium.fluid;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidTransportControlPayload(BlockPos pos, int action, int value) implements CustomPacketPayload {
    public static final int REDSTONE = 6;
    public static final int FILTER_MODE = 7;
    public static final int FILTER_CURRENT = 8;
    public static final int FILTER_CLEAR = 9;

    public static final Type<FluidTransportControlPayload> TYPE =
            new Type<>(ClayiumMod.id("fluid_transport_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidTransportControlPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, FluidTransportControlPayload::pos,
                    ByteBufCodecs.VAR_INT, FluidTransportControlPayload::action,
                    ByteBufCodecs.VAR_INT, FluidTransportControlPayload::value,
                    FluidTransportControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, FluidTransportControlPayload::handle);
    }

    private static void handle(FluidTransportControlPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            apply(player, payload);
        }
    }

    public static void apply(Player player, FluidTransportControlPayload payload) {
        if (!(player.containerMenu instanceof FluidTransportMenu menu)
                || !menu.transportPos().equals(payload.pos())
                || !menu.stillValid(player)) {
            return;
        }
        FluidTransportBlockEntity transport = menu.transport();
        if (payload.action() >= 0 && payload.action() < Direction.values().length) {
            if (payload.value() >= 0 && payload.value() < FluidTransportBlockEntity.FaceMode.values().length) {
                transport.setFaceMode(
                        Direction.from3DDataValue(payload.action()),
                        FluidTransportBlockEntity.FaceMode.values()[payload.value()]);
            }
            return;
        }
        switch (payload.action()) {
            case REDSTONE -> {
                if (payload.value() >= 0
                        && payload.value() < FluidTransportBlockEntity.RedstoneCondition.values().length) {
                    transport.setRedstoneCondition(
                            FluidTransportBlockEntity.RedstoneCondition.values()[payload.value()]);
                }
            }
            case FILTER_MODE -> transport.toggleFilterMode();
            case FILTER_CURRENT -> transport.filterCurrentFluid();
            case FILTER_CLEAR -> transport.clearFilterFluid();
            default -> {
            }
        }
    }
}
