package net.claustra01.clayium.network;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
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

public record MachineFaceModePayload(BlockPos pos, int direction, int mode) implements CustomPacketPayload {
    public static final Type<MachineFaceModePayload> TYPE = new Type<>(ClayiumMod.id("machine_face_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MachineFaceModePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, MachineFaceModePayload::pos,
            ByteBufCodecs.VAR_INT, MachineFaceModePayload::direction,
            ByteBufCodecs.VAR_INT, MachineFaceModePayload::mode,
            MachineFaceModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, MachineFaceModePayload::handle);
    }

    private static void handle(MachineFaceModePayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            apply(player, payload);
        }
    }

    public static void apply(Player player, MachineFaceModePayload payload) {
        if (!(player.containerMenu instanceof ClayMachineMenu menu)
                || !menu.machinePos().equals(payload.pos)
                || !menu.stillValid(player)
                || payload.direction < 0
                || payload.direction >= Direction.values().length
                || payload.mode < 0
                || payload.mode >= ClayMachineBlockEntity.FaceMode.values().length) {
            return;
        }
        menu.machine().setFaceMode(
                Direction.from3DDataValue(payload.direction),
                ClayMachineBlockEntity.FaceMode.values()[payload.mode]);
    }
}
