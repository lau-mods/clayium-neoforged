package net.claustra01.clayium.logistics;

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

public record BufferControlPayload(BlockPos pos, int action, int value) implements CustomPacketPayload {
    public static final int REDSTONE = 6;
    public static final int FILTER_MODE = 7;
    public static final int RULE_TYPE = 8;
    public static final int ADD_RULE = 9;
    public static final int REMOVE_RULE = 10;
    public static final int TRANSFER_LIMIT = 11;
    public static final int NEXT_TAG = 12;

    public static final Type<BufferControlPayload> TYPE = new Type<>(ClayiumMod.id("buffer_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BufferControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BufferControlPayload::pos,
            ByteBufCodecs.VAR_INT, BufferControlPayload::action,
            ByteBufCodecs.VAR_INT, BufferControlPayload::value,
            BufferControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, BufferControlPayload::handle);
    }

    private static void handle(BufferControlPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            apply(player, payload);
        }
    }

    public static void apply(Player player, BufferControlPayload payload) {
        if (!(player.containerMenu instanceof BufferMenu menu)
                || !menu.bufferPos().equals(payload.pos)
                || !menu.stillValid(player)) {
            return;
        }
        BufferBlockEntity buffer = menu.buffer();
        if (payload.action >= 0 && payload.action < Direction.values().length) {
            if (payload.value >= 0 && payload.value < BufferBlockEntity.FaceMode.values().length) {
                buffer.setFaceMode(
                        Direction.from3DDataValue(payload.action), BufferBlockEntity.FaceMode.values()[payload.value]);
            }
            return;
        }
        switch (payload.action) {
            case REDSTONE -> {
                if (payload.value >= 0 && payload.value < BufferBlockEntity.RedstoneCondition.values().length) {
                    buffer.setRedstoneCondition(BufferBlockEntity.RedstoneCondition.values()[payload.value]);
                }
            }
            case FILTER_MODE -> buffer.cycleFilterMode();
            case RULE_TYPE -> buffer.cycleSelectedRuleType();
            case ADD_RULE -> buffer.addSelectedRule();
            case REMOVE_RULE -> buffer.removeLastRule();
            case TRANSFER_LIMIT -> buffer.setTransferLimit(payload.value);
            case NEXT_TAG -> buffer.cycleSelectedTag();
            default -> {
            }
        }
    }
}
