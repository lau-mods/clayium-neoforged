/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.network;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.world.inventory.ItemFilterMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public record SetFilterPatternPayload(String pattern) implements CustomPacketPayload {
    public static final Type<SetFilterPatternPayload> TYPE =
            new Type<>(Clayium.id("set_filter_pattern"));
    public static final StreamCodec<io.netty.buffer.ByteBuf, SetFilterPatternPayload> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(SetFilterPatternPayload::new, SetFilterPatternPayload::pattern);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @EventBusSubscriber(modid = Clayium.MODID)
    public static final class Registration {
        private Registration() {
        }

        @SubscribeEvent
        public static void register(RegisterPayloadHandlersEvent event) {
            event.registrar("1").playToServer(
                    TYPE,
                    STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(() -> {
                        if (context.player().containerMenu instanceof ItemFilterMenu menu) {
                            menu.setPattern(payload.pattern());
                        }
                    }));
        }
    }
}
