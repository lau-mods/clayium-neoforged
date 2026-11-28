package net.claustra01.clayium.network;

import net.minecraft.server.level.ServerPlayer;

/**
 * Implemented by menus that handle custom client-to-server GUI button clicks.
 */
public interface IClayButtonMenu {
    void handleGuiButton(ServerPlayer player, int buttonId);
}
