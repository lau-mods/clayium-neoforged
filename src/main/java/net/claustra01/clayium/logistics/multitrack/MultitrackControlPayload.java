package net.claustra01.clayium.logistics.multitrack;

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

public record MultitrackControlPayload(BlockPos pos, int action, int value) implements CustomPacketPayload {
    public static final int SELECT_TRACK = 6;
    public static final int SELECT_FACE_TRACK = 7;
    public static final Type<MultitrackControlPayload> TYPE = new Type<>(ClayiumMod.id("multitrack_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MultitrackControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MultitrackControlPayload::pos,
            ByteBufCodecs.VAR_INT, MultitrackControlPayload::action,
            ByteBufCodecs.VAR_INT, MultitrackControlPayload::value,
            MultitrackControlPayload::new);

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, MultitrackControlPayload::handle);
    }

    private static void handle(MultitrackControlPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)
                || !(player.containerMenu instanceof MultitrackBufferMenu menu)
                || !menu.bufferPos().equals(payload.pos) || !menu.stillValid(player)) {
            return;
        }
        MultitrackBufferBlockEntity buffer = menu.buffer();
        if (payload.action >= 0 && payload.action < Direction.values().length
                && payload.value >= 0 && payload.value < BufferBlockEntity.FaceMode.values().length) {
            buffer.setFaceMode(menu.selectedTrack(), Direction.from3DDataValue(payload.action),
                    BufferBlockEntity.FaceMode.values()[payload.value]);
        } else if (payload.action == SELECT_TRACK) {
            if (payload.value >= 0 && payload.value < buffer.trackCount()) {
                menu.setSelectedTrack(payload.value);
            }
        } else if (payload.action == SELECT_FACE_TRACK) {
            int direction = Math.floorDiv(payload.value, MultitrackBufferBlockEntity.TRACKS);
            int track = Math.floorMod(payload.value, MultitrackBufferBlockEntity.TRACKS);
            if (direction >= 0 && direction < Direction.values().length) {
                if (track < buffer.trackCount()) buffer.setFaceTrack(Direction.from3DDataValue(direction), track);
            }
        }
    }
}
