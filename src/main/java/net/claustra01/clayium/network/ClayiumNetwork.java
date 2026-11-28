package net.claustra01.clayium.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers all Clayium NeoForge CustomPacketPayloads.
 */
public final class ClayiumNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private ClayiumNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(
                GuiButtonPayload.TYPE,
                GuiButtonPayload.STREAM_CODEC,
                GuiButtonPayload::handle
        );
        registrar.playToServer(
                FilterTextPayload.TYPE,
                FilterTextPayload.STREAM_CODEC,
                FilterTextPayload::handle
        );
        registrar.playToClient(
                AutoTraderOffersPayload.TYPE,
                AutoTraderOffersPayload.STREAM_CODEC,
                AutoTraderOffersPayload::handle
        );
        registrar.playToClient(
                PANCoreListPayload.TYPE,
                PANCoreListPayload.STREAM_CODEC,
                PANCoreListPayload::handle
        );
    }
}
