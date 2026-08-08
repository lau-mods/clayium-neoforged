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
import net.claustra01.clayium.client.gui.screens.inventory.AutoClayCondenserScreen;
import net.claustra01.clayium.client.gui.screens.inventory.AutoCrafterScreen;
import net.claustra01.clayium.client.gui.screens.inventory.ChemicalMetalSeparatorScreen;
import net.claustra01.clayium.client.gui.screens.inventory.ClayEnergyLaserScreen;
import net.claustra01.clayium.client.gui.screens.inventory.ClayEnergyConverterScreen;
import net.claustra01.clayium.client.gui.screens.inventory.ResonatingCollectorScreen;
import net.claustra01.clayium.client.gui.screens.inventory.PanAdapterScreen;
import net.claustra01.clayium.client.gui.screens.inventory.PanCoreScreen;
import net.claustra01.clayium.client.gui.screens.inventory.PanDuplicatorScreen;
import net.claustra01.clayium.client.renderer.blockentity.ClayEnergyLaserRenderer;
import net.claustra01.clayium.client.renderer.blockentity.LaserReflectorRenderer;
import net.claustra01.clayium.client.renderer.blockentity.IoOverlayRenderer;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.ColoredSiliconeBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.world.item.DyeColor;

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
        event.register(ClayiumRegistries.AUTO_CLAY_CONDENSER_MENU.get(), AutoClayCondenserScreen::new);
        event.register(ClayiumRegistries.AUTO_CRAFTER_MENU.get(), AutoCrafterScreen::new);
        event.register(ClayiumRegistries.CHEMICAL_METAL_SEPARATOR_MENU.get(), ChemicalMetalSeparatorScreen::new);
        event.register(ClayiumRegistries.CLAY_ENERGY_LASER_MENU.get(), ClayEnergyLaserScreen::new);
        event.register(ClayiumRegistries.CLAY_ENERGY_CONVERTER_MENU.get(), ClayEnergyConverterScreen::new);
        event.register(ClayiumRegistries.RESONATING_COLLECTOR_MENU.get(), ResonatingCollectorScreen::new);
        event.register(ClayiumRegistries.PAN_ADAPTER_MENU.get(), PanAdapterScreen::new);
        event.register(ClayiumRegistries.PAN_CORE_MENU.get(), PanCoreScreen::new);
        event.register(ClayiumRegistries.PAN_DUPLICATOR_MENU.get(), PanDuplicatorScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ClayiumRegistries.MACHINE_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.LOGISTICS_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.MACHINE_INTERFACE_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.FLUID_BUFFER_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.SALT_EXTRACTOR_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.AUTO_CLAY_CONDENSER_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.AUTO_CRAFTER_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.CHEMICAL_METAL_SEPARATOR_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.CLAY_ENERGY_LASER_BLOCK_ENTITY.get(), context -> new ClayEnergyLaserRenderer());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.CLAY_ENERGY_CONVERTER_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.PAN_DUPLICATOR_BLOCK_ENTITY.get(), context -> new IoOverlayRenderer<>());
        event.registerBlockEntityRenderer(
                ClayiumRegistries.LASER_REFLECTOR_BLOCK_ENTITY.get(), context -> new LaserReflectorRenderer());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        dust(event, "impure_silicon_dust", colors(151,143,152, 83,55,100, 169,165,165));
        material(event, colors(151,143,152, 83,55,100, 169,165,165), "impure_silicon_large_plate");
        material(event, colors(210,210,210, 180,180,180, 240,240,240),
                "silicone_dust", "silicone_ingot", "silicone_plate", "silicone_large_plate");
        material(event, colors(40,28,40, 6,4,6, 255,255,255),
                "silicon_dust", "silicon_ingot", "silicon_large_plate");
        impureDust(event, "impure_aluminium_dust", 190,200,202);
        material(event, colors(190,200,202, 120,120,60, 220,220,220),
                "impure_aluminium_ingot", "impure_aluminium_plate", "impure_aluminium_large_plate");
        pureDust(event, "aluminium_dust", 190,200,202, 31,33,33);
        material(event, colors(190,200,202, 31,33,33, 255,255,255),
                "aluminium_ingot", "aluminium_plate", "aluminium_large_plate");
        impureDust(event, "impure_magnesium_dust", 150,220,150);
        pureDust(event, "magnesium_dust", 150,210,150, 120,120,120);
        material(event, colors(150,210,150, 120,120,120, 255,255,255), "magnesium_ingot");
        impureDust(event, "impure_sodium_dust", 170,170,230);
        pureDust(event, "sodium_dust", 170,170,222, 120,120,120);
        material(event, colors(170,170,222, 120,120,120, 255,255,255), "sodium_ingot");
        impureDust(event, "impure_lithium_dust", 220,220,150);
        pureDust(event, "lithium_dust", 210,210,150, 120,120,120);
        material(event, colors(210,210,150, 120,120,120, 255,255,255), "lithium_ingot");
        impureDust(event, "impure_zirconium_dust", 190,170,122);
        pureDust(event, "zirconium_dust", 190,170,122, 120,120,120);
        material(event, colors(190,170,122, 120,120,120, 255,255,255), "zirconium_ingot");
        impureDust(event, "impure_zinc_dust", 230,170,170);
        impureDust(event, "impure_manganese_dust", 190,240,240);
        impureDust(event, "impure_calcium_dust", 240,240,240);
        impureDust(event, "impure_potassium_dust", 240,240,190);
        impureDust(event, "impure_nickel_dust", 210,210,240);
        impureDust(event, "impure_iron_dust", 216,216,216);
        impureDust(event, "impure_beryllium_dust", 210,240,210);
        impureDust(event, "impure_lead_dust", 190,240,210);
        impureDust(event, "impure_hafnium_dust", 240,210,170);
        impureDust(event, "impure_chrome_dust", 240,210,210);
        impureDust(event, "impure_titanium_dust", 210,240,240);
        impureDust(event, "impure_strontium_dust", 210,170,242);
        impureDust(event, "impure_barium_dust", 150,80,120);
        impureDust(event, "impure_copper_dust", 160,90,10);
        pureDust(event, "manganese_dust", 190,240,240, 120,120,120);
        material(event, colors(190,240,240, 120,120,120, 255,255,255), "manganese_ingot");
        pureDust(event, "calcium_dust", 240,240,240, 120,120,120);
        material(event, colors(240,240,240, 120,120,120, 255,255,255), "calcium_ingot");
        pureDust(event, "potassium_dust", 240,240,190, 120,120,120);
        material(event, colors(240,240,190, 120,120,120, 255,255,255), "potassium_ingot");
        pureDust(event, "hafnium_dust", 240,210,170, 120,120,120);
        material(event, colors(240,210,170, 120,120,120, 255,255,255), "hafnium_ingot");
        pureDust(event, "strontium_dust", 210,170,242, 120,120,120);
        material(event, colors(210,170,242, 120,120,120, 255,255,255), "strontium_ingot");
        pureDust(event, "barium_dust", 150,80,120, 120,20,80);
        material(event, colors(150,80,120, 120,20,80, 255,255,255), "barium_ingot");
        pureDust(event, "beryllium_dust", 210,240,210, 120,120,120);
        material(event, colors(210,240,210, 120,120,120, 255,255,255), "beryllium_ingot");
        pureDust(event, "iron_dust", 216,216,216, 53,53,53);
        material(event, colors(136,144,173, 255,255,255, 255,255,255),
                "clay_steel_dust", "clay_steel_ingot", "clay_steel_plate", "clay_steel_large_plate");
        material(event, colors(216,216,216, 53,53,53, 255,255,255), "steel_dust", "steel_ingot");
        pureDust(event, "zinc_dust", 230,170,170, 120,120,120);
        material(event, colors(230,170,170, 120,120,120, 255,255,255), "zinc_ingot");
        material(event, colors(240,190,220, 160,0,0, 255,255,255),
                "zincalminium_dust", "zincalminium_ingot");
        material(event, colors(230,170,140, 120,0,0, 255,255,255),
                "zinconium_dust", "zinconium_ingot");
        material(event, colors(130,140,135, 10,40,10, 255,255,255),
                "az91d_dust", "az91d_ingot", "az91d_plate", "az91d_large_plate");
        material(event, colors(75,85,80, 10,40,10, 255,255,255),
                "zk60a_dust", "zk60a_ingot", "zk60a_plate", "zk60a_large_plate");
        material(event, colors(245,245,245, 235,0,0, 255,255,255), "rubidium_ingot");
        material(event, colors(245,245,245, 150,150,0, 255,255,255), "caesium_ingot");
        material(event, colors(245,245,245, 0,235,0, 255,255,255), "francium_ingot");
        material(event, colors(245,245,245, 0,150,150, 255,255,255), "radium_ingot");
        material(event, colors(245,245,245, 0,0,235, 255,255,255), "actinium_ingot");
        material(event, colors(50,50,50, 120,120,120, 200,50,50), "thorium_ingot");
        material(event, colors(50,50,50, 120,120,120, 50,50,100), "protactinium_ingot");
        material(event, colors(50,255,50, 50,155,50, 50,255,50), "uranium_ingot");
        material(event, colors(50,50,255, 50,50,155, 50,50,255), "neptunium_ingot");
        material(event, colors(145,145,145, 235,0,0, 255,255,255), "lanthanum_ingot");
        material(event, colors(145,145,145, 150,150,0, 255,255,255), "cerium_ingot");
        material(event, colors(145,145,145, 0,235,0, 255,255,255), "praseodymium_ingot");
        material(event, colors(145,145,145, 0,150,150, 255,255,255), "neodymium_ingot");
        material(event, colors(210,240,240, 120,120,120, 255,255,255), "titanium_ingot");
        material(event, colors(60,120,120, 120,120,120, 255,255,255), "vanadium_ingot");
        material(event, colors(30,30,230, 120,120,120, 255,255,255), "cobalt_ingot");
        material(event, colors(210,210,240, 120,120,120, 255,255,255), "nickel_ingot");
        material(event, colors(151,70,70, 120,120,120, 255,255,255), "palladium_ingot");
        material(event, colors(220,220,240, 120,120,120, 255,255,255), "silver_ingot");
        material(event, colors(255,220,40, 120,90,0, 255,255,255), "gold_ingot");
        material(event, colors(240,210,170, 40,35,28, 240,210,150), "tantalum_ingot");
        material(event, colors(30,30,30, 5,5,5, 60,60,60), "tungsten_ingot");
        material(event, colors(190,240,210, 31,40,35, 255,255,255), "lead_ingot");
        material(event, colors(230,230,240, 0,0,0, 255,255,255), "tin_ingot");
        material(event, colors(70,70,70, 11,11,11, 140,140,140), "antimony_ingot");
        material(event, colors(70,120,70, 11,20,11, 140,240,140), "bismuth_ingot");
        material(event, colors(230,160,40, 120,80,20, 255,255,255), "phosphorus_dust", "sulfur_dust");
        material(event, colors(10,10,10, 20,20,20, 30,30,30), "carbon_dust");
        material(event, colors(20,20,20, 50,50,50, 80,50,50), "charcoal_dust");
        material(event, colors(20,20,20, 50,50,50, 50,50,80), "coal_dust");
        material(event, colors(151,70,70, 120,40,40, 220,150,150), "impure_redstone_dust");
        material(event, colors(151,151,70, 120,120,40, 220,220,150), "impure_glowstone_dust");
        material(event, colors(90,240,210, 63,72,85, 255,205,200),
                "clayium_dust", "clayium_ingot", "clayium_plate", "clayium_large_plate");
        material(event, colors(85,205,85, 245,255,255, 245,160,255), "impure_ultimate_alloy_ingot");
        material(event, colors(85,205,85, 120,120,120, 245,160,255),
                "ultimate_alloy_dust", "ultimate_alloy_ingot", "ultimate_alloy_plate", "ultimate_alloy_large_plate");
        material(event, colors(0,0,235, 0,0,0, 255,255,255),
                "antimatter_dust", "antimatter", "antimatter_plate", "antimatter_large_plate");
        material(event, colors(255,50,255, 0,0,0, 255,255,255),
                "pure_antimatter_dust", "pure_antimatter", "pure_antimatter_plate", "pure_antimatter_large_plate");
        String[] compressed = {"compressed_pure_antimatter", "double_compressed_pure_antimatter",
                "triple_compressed_pure_antimatter", "quadruple_compressed_pure_antimatter",
                "quintuple_compressed_pure_antimatter", "sextuple_compressed_pure_antimatter",
                "septuple_compressed_pure_antimatter", "opa"};
        for (int index = 1; index <= compressed.length; index++) {
            double ratio = index / 8.0D;
            double luminosity = 1.0D - (ratio < 0.5D ? ratio : 1.0D - ratio) * 1.5D;
            int red = (int) (luminosity * (255.0D * (1.0D - ratio) + 150.0D * ratio));
            int green = (int) (luminosity * 50.0D * (1.0D - ratio));
            int blue = (int) (luminosity * 255.0D * (1.0D - ratio));
            material(event, colors(red, green, blue, (int)(200*ratio), (int)(200*ratio), 0, 255,255,255), compressed[index-1]);
        }
        material(event, colors(255,255,0, 140,140,140, 255,255,255), "oec_dust", "oec_plate", "oec_large_plate");
        material(event, colors(150,0,0, 200,200,0, 255,255,255), "opa_dust", "opa_plate", "opa_large_plate");
        pureDust(event, "lead_dust", 190,240,210, 31,40,35);
        pureDust(event, "copper_dust", 160,90,10, 40,22,2);
        pureDust(event, "nickel_dust", 210,210,240, 120,120,120);
        pureDust(event, "chrome_dust", 240,210,210, 120,120,120);
        pureDust(event, "titanium_dust", 210,240,240, 120,120,120);
        pureDust(event, "gold_dust", 255,220,40, 120,90,0);
        material(event, colors(240,210,210,120,120,120,255,255,255), "chrome_ingot");
        material(event, colors(225,180,80,80,55,20,255,255,255), "platinum_ingot");
        material(event, colors(220,220,235,70,70,90,255,255,255), "iridium_ingot");
        material(event, colors(110,150,220,30,40,80,255,255,255), "osmium_ingot");
        material(event, colors(180,190,210,50,55,70,255,255,255), "rhenium_ingot", "molybdenum_ingot");
        material(event, colors(145,145,145,235,0,0,255,255,255), "promethium_ingot");
        material(event, colors(145,145,145,0,150,150,255,255,255), "samarium_ingot");
        material(event, colors(145,145,145,0,0,235,255,255,255), "europium_ingot");
        material(event, colors(50,50,255,50,50,155,50,50,255), "curium_ingot");
        material(event, colors(70,70,70,50,120,50,100,255,100), "plutonium_ingot");
        material(event, colors(70,70,70,120,50,120,255,100,255), "americium_ingot");
        for (var entry : ClayiumRegistries.COLORED_SILICONE_ITEMS.entrySet()) {
            String colorName = entry.getKey().substring(0, entry.getKey().length() - "_silicone_block".length());
            DyeColor color = DyeColor.byName(colorName, DyeColor.WHITE);
            event.register((stack, tintIndex) -> tintIndex == 0 ? color.getTextureDiffuseColor() : 0xffffffff,
                    entry.getValue().get());
        }
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        for (var entry : ClayiumRegistries.COLORED_SILICONE_BLOCKS.entrySet()) {
            ColoredSiliconeBlock block = entry.getValue().get();
            event.register((state, level, pos, tintIndex) -> block.color().getTextureDiffuseColor(), block);
        }
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
                ClayiumRegistries.MATERIAL_ITEMS.get(id).get());
    }

    private static void material(RegisterColorHandlersEvent.Item event, int[] colors, String... ids) {
        for (String id : ids) dust(event, id, colors);
    }
}
