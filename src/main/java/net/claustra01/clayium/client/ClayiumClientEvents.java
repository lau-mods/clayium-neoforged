/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.client;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.client.gui.screens.inventory.ClayWorkTableScreen;
import net.claustra01.clayium.client.gui.screens.inventory.MachineScreen;
import net.claustra01.clayium.client.gui.screens.inventory.LogisticsScreen;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Clayium.MODID, value = Dist.CLIENT)
public final class ClayiumClientEvents {
    private ClayiumClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ClayiumRegistries.CLAY_WORK_TABLE_MENU.get(), ClayWorkTableScreen::new);
        event.register(ClayiumRegistries.MACHINE_MENU.get(), MachineScreen::new);
        event.register(ClayiumRegistries.ASSEMBLER_MACHINE_MENU.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_1.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_2.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_3.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_4.get(), MachineScreen::new);
        event.register(ClayiumRegistries.LOGISTICS_MENU.get(), LogisticsScreen::new);
    }
}
