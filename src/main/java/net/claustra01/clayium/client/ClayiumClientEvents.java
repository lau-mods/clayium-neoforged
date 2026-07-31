/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.client;

import net.claustra01.clayium.Clayium;
import net.claustra01.clayium.client.gui.screens.inventory.ClayWorkTableScreen;
import net.claustra01.clayium.client.gui.screens.inventory.ClayCraftingTableScreen;
import net.claustra01.clayium.client.gui.screens.inventory.CobblestoneGeneratorScreen;
import net.claustra01.clayium.client.gui.screens.inventory.MachineScreen;
import net.claustra01.clayium.client.gui.screens.inventory.LogisticsScreen;
import net.claustra01.clayium.client.gui.screens.inventory.ItemFilterScreen;
import net.claustra01.clayium.client.gui.screens.inventory.FluidBufferScreen;
import net.claustra01.clayium.client.gui.screens.inventory.SaltExtractorScreen;
import net.claustra01.clayium.client.renderer.blockentity.IoOverlayRenderer;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Clayium.MODID, value = Dist.CLIENT)
public final class ClayiumClientEvents {
    private ClayiumClientEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ClayiumRegistries.CLAY_WORK_TABLE_MENU.get(), ClayWorkTableScreen::new);
        event.register(ClayiumRegistries.CLAY_CRAFTING_TABLE_MENU.get(), ClayCraftingTableScreen::new);
        event.register(ClayiumRegistries.COBBLESTONE_GENERATOR_MENU.get(), CobblestoneGeneratorScreen::new);
        event.register(ClayiumRegistries.MACHINE_MENU.get(), MachineScreen::new);
        event.register(ClayiumRegistries.ASSEMBLER_MACHINE_MENU.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CHEMICAL_MACHINE_MENU.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_1.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_2.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_3.get(), MachineScreen::new);
        event.register(ClayiumRegistries.CENTRIFUGE_MACHINE_MENU_4.get(), MachineScreen::new);
        event.register(ClayiumRegistries.LOGISTICS_MENU.get(), LogisticsScreen::new);
        event.register(ClayiumRegistries.ITEM_FILTER_MENU.get(), ItemFilterScreen::new);
        event.register(ClayiumRegistries.FLUID_BUFFER_MENU.get(), FluidBufferScreen::new);
        event.register(ClayiumRegistries.SALT_EXTRACTOR_MENU.get(), SaltExtractorScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ClayiumRegistries.MACHINE_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.LOGISTICS_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.FLUID_BUFFER_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.SALT_EXTRACTOR_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        dust(event, "impure_silicon_dust", colors(151,143,152, 83,55,100, 169,165,165));
        impureDust(event, "impure_aluminium_dust", 190,200,202);
        pureDust(event, "aluminium_dust", 190,200,202, 31,33,33);
        impureDust(event, "impure_magnesium_dust", 150,220,150);
        pureDust(event, "magnesium_dust", 150,210,150, 120,120,120);
        impureDust(event, "impure_sodium_dust", 170,170,230);
        pureDust(event, "sodium_dust", 170,170,222, 120,120,120);
        impureDust(event, "impure_lithium_dust", 220,220,150);
        pureDust(event, "lithium_dust", 210,210,150, 120,120,120);
        impureDust(event, "impure_zirconium_dust", 190,170,122);
        pureDust(event, "zirconium_dust", 190,170,122, 120,120,120);
        impureDust(event, "impure_zinc_dust", 230,170,170);
        pureDust(event, "zinc_dust", 230,170,170, 120,120,120);
    }

    private static void impureDust(RegisterColorHandlersEvent.Item event, String id, int r, int g, int b) {
        dust(event, id, colors(r,g,b, 120,120,60, 220,220,220));
    }

    private static void pureDust(RegisterColorHandlersEvent.Item event, String id,
                                 int r, int g, int b, int darkR, int darkG, int darkB) {
        dust(event, id, colors(r,g,b, darkR,darkG,darkB,
                Math.min(r*2,255),Math.min(g*2,255),Math.min(b*2,255)));
    }

    private static int[] colors(int... rgb) {
        return new int[]{argb(rgb[0],rgb[1],rgb[2]), argb(rgb[3],rgb[4],rgb[5]), argb(rgb[6],rgb[7],rgb[8])};
    }

    private static int argb(int r, int g, int b) { return 0xff000000 | r << 16 | g << 8 | b; }

    private static void dust(RegisterColorHandlersEvent.Item event, String id, int[] colors) {
        event.register((stack, tintIndex) -> tintIndex >= 0 && tintIndex < colors.length ? colors[tintIndex] : 0xffffffff,
                ClayiumRegistries.PHASE6_ITEMS.get(id).get());
    }
}
