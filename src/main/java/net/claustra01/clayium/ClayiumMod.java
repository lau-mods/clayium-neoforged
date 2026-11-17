package net.claustra01.clayium;

import net.claustra01.clayium.datagen.ClayiumDataGenerators;
import net.claustra01.clayium.machine.MachineCapabilities;
import net.claustra01.clayium.network.MachineFaceModePayload;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ClayiumMod.MOD_ID)
public final class ClayiumMod {
    public static final String MOD_ID = "clayium";

    public ClayiumMod(IEventBus modEventBus) {
        ClayiumRegistries.register(modEventBus);
        modEventBus.addListener(ClayiumDataGenerators::gatherData);
        modEventBus.addListener(MachineCapabilities::register);
        modEventBus.addListener(MachineFaceModePayload::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
