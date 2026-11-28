package net.claustra01.clayium.network;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.AutoTraderMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Server-to-client sync payload carrying nearby Merchant offers and selected recipe index for Auto Trader GUIs.
 */
public record AutoTraderOffersPayload(int containerId, int currentRecipeIndex, MerchantOffers offers)
        implements CustomPacketPayload {
    public static final Type<AutoTraderOffersPayload> TYPE = new Type<>(ClayiumMod.id("auto_trader_offers"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AutoTraderOffersPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AutoTraderOffersPayload::containerId,
            ByteBufCodecs.VAR_INT,
            AutoTraderOffersPayload::currentRecipeIndex,
            MerchantOffers.STREAM_CODEC,
            AutoTraderOffersPayload::offers,
            AutoTraderOffersPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AutoTraderOffersPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null
                    && context.player().containerMenu instanceof AutoTraderMenu menu
                    && context.player().containerMenu.containerId == payload.containerId()) {
                menu.applyClientOffers(payload.currentRecipeIndex(), payload.offers());
            }
        });
    }
}
