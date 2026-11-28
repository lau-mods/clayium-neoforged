package net.claustra01.clayium.network;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.PANCoreMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Synchronizes resolved PAN Core ingredient costs and prohibited exclusions to an open {@link PANCoreMenu}.
 */
public record PANCoreListPayload(
        int containerId,
        List<Entry> ingredients,
        List<ItemStack> prohibited) implements CustomPacketPayload {

    public record Entry(ItemStack stack, double cost, double consumption) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, Entry::stack,
                ByteBufCodecs.DOUBLE, Entry::cost,
                ByteBufCodecs.DOUBLE, Entry::consumption,
                Entry::new
        );
    }

    public static final Type<PANCoreListPayload> TYPE =
            new Type<>(ClayiumMod.id("pan_core_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PANCoreListPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PANCoreListPayload::containerId,
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), PANCoreListPayload::ingredients,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), PANCoreListPayload::prohibited,
            PANCoreListPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PANCoreListPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            if (player.containerMenu instanceof PANCoreMenu menu && menu.containerId == payload.containerId()) {
                menu.updateSyncedList(payload.ingredients(), payload.prohibited());
            }
        });
    }
}
