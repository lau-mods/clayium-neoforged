/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium;

import net.claustra01.clayium.config.ClayiumConfig;
import net.claustra01.clayium.data.ClayiumDataGenerators;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumCapabilities;
import net.claustra01.clayium.registry.ClayiumRecipes;
import net.claustra01.clayium.registry.ClayiumRegistries;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(Clayium.MODID)
public final class Clayium {
    public static final String MODID = "clayium_neoforged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Clayium(IEventBus modEventBus, ModContainer modContainer) {
        ClayiumRegistries.register(modEventBus);
        ClayiumDataComponents.register(modEventBus);
        ClayiumRecipes.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.COMMON, ClayiumConfig.SPEC);
        modEventBus.addListener(ClayiumDataGenerators::gatherData);
        modEventBus.addListener(this::addCreativeItems);
        modEventBus.addListener(ClayiumCapabilities::register);
    }

    private void addCreativeItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ClayiumRegistries.CLAY_WORK_TABLE_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ClayiumRegistries.CLAY_STICK);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        if (ClayiumConfig.LOG_REGISTRY_SUMMARY.getAsBoolean()) {
            LOGGER.info("Clayium core registries initialized for Minecraft 1.21.1 / NeoForge");
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
