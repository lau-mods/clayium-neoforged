package net.claustra01.clayium.world.area;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AreaMachineControlPayload(BlockPos position, int action) implements CustomPacketPayload {
    public static final Type<AreaMachineControlPayload> TYPE =
            new Type<>(ClayiumMod.id("area_machine_control"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AreaMachineControlPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, AreaMachineControlPayload::position,
                    ByteBufCodecs.VAR_INT, AreaMachineControlPayload::action,
                    AreaMachineControlPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, AreaMachineControlPayload::handle);
    }

    private static void handle(AreaMachineControlPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> apply(context.player(), payload));
    }

    public static void apply(Player player, AreaMachineControlPayload payload) {
        if (payload.action < 0 || payload.action > 3
                || !(player.containerMenu instanceof AreaMachineMenu menu)
                || !menu.stillValid(player)
                || !menu.position().equals(payload.position)
                || !(player.level().getBlockEntity(payload.position) instanceof AreaMachineBlockEntity machine)) {
            return;
        }
        machine.applyActivatorControl(payload.action, player);
    }
}
