package net.claustra01.clayium.machine.trade;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TradeSnapshotPayload(int containerId, MerchantOffers offers, int selectedOffer,
                                   long storedEnergy, long energyPerTrade)
        implements CustomPacketPayload {
    public static final Type<TradeSnapshotPayload> TYPE = new Type<>(ClayiumMod.id("trade_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TradeSnapshotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, TradeSnapshotPayload::containerId,
            MerchantOffers.STREAM_CODEC, TradeSnapshotPayload::offers,
            ByteBufCodecs.VAR_INT, TradeSnapshotPayload::selectedOffer,
            ByteBufCodecs.VAR_LONG, TradeSnapshotPayload::storedEnergy,
            ByteBufCodecs.VAR_LONG, TradeSnapshotPayload::energyPerTrade,
            TradeSnapshotPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, TradeSnapshotPayload::handle);
    }

    private static void handle(TradeSnapshotPayload payload, IPayloadContext context) {
        if (context.player().containerMenu instanceof AutoTraderMenu menu) {
            menu.applySnapshot(payload);
        }
    }

    public long contentHash() {
        long hash = selectedOffer;
        hash = 31L * hash + storedEnergy;
        for (MerchantOffer offer : offers) {
            hash = 31L * hash + ItemStack.hashItemAndComponents(offer.getCostA());
            hash = 31L * hash + offer.getCostA().getCount();
            hash = 31L * hash + ItemStack.hashItemAndComponents(offer.getCostB());
            hash = 31L * hash + offer.getCostB().getCount();
            hash = 31L * hash + ItemStack.hashItemAndComponents(offer.getResult());
            hash = 31L * hash + offer.getResult().getCount();
            hash = 31L * hash + offer.getUses();
            hash = 31L * hash + offer.getMaxUses();
        }
        return hash;
    }
}
