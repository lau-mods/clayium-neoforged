package net.claustra01.clayium;

import net.claustra01.clayium.network.ClayiumNetwork;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumCreativeTabs;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumEntities;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumLootFunctions;
import net.claustra01.clayium.registry.ClayiumMenus;
import net.claustra01.clayium.registry.ClayiumRecipeConditions;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(ClayiumMod.MOD_ID)
public class ClayiumMod {
    public static final String MOD_ID = "clayium_neoforged";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public ClayiumMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, ClayiumConfig.SPEC);

        ClayiumDataComponents.register(modEventBus);
        ClayiumBlocks.register(modEventBus);
        ClayiumItems.register(modEventBus);
        ClayiumBlockEntities.register(modEventBus);
        ClayiumEntities.register(modEventBus);
        ClayiumMenus.register(modEventBus);
        ClayiumRecipeTypes.register(modEventBus);
        ClayiumRecipeSerializers.register(modEventBus);
        ClayiumRecipeConditions.register(modEventBus);
        ClayiumLootFunctions.register(modEventBus);
        ClayiumCreativeTabs.register(modEventBus);
        modEventBus.addListener(ClayiumNetwork::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
