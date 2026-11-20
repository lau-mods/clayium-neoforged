package net.claustra01.clayium;

import net.claustra01.clayium.datagen.ClayiumDataGenerators;
import net.claustra01.clayium.energy.fe.EnergyConverterCapabilities;
import net.claustra01.clayium.fluid.FluidCapabilities;
import net.claustra01.clayium.fluid.FluidTransportControlPayload;
import net.claustra01.clayium.machine.MachineCapabilities;
import net.claustra01.clayium.machine.clayproduction.ClayProductionCapabilities;
import net.claustra01.clayium.network.MachineFaceModePayload;
import net.claustra01.clayium.logistics.BufferControlPayload;
import net.claustra01.clayium.logistics.LogisticsCapabilities;
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
        modEventBus.addListener(ClayProductionCapabilities::register);
        modEventBus.addListener(EnergyConverterCapabilities::register);
        modEventBus.addListener(FluidCapabilities::register);
        modEventBus.addListener(LogisticsCapabilities::register);
        if (MOD_ID.equals(System.getProperty("neoforge.enabledGameTestNamespaces"))) {
            modEventBus.addListener(net.claustra01.clayium.gametest.logistics.LogisticsGameTests::registerTestCapability);
            modEventBus.addListener(net.claustra01.clayium.gametest.energyconverter.EnergyConverterGameTests::registerTestCapability);
            modEventBus.addListener(net.claustra01.clayium.gametest.fluid.FluidTransportGameTests::registerTestCapability);
        }
        modEventBus.addListener(MachineFaceModePayload::register);
        modEventBus.addListener(BufferControlPayload::register);
        modEventBus.addListener(FluidTransportControlPayload::register);
        modEventBus.addListener(net.claustra01.clayium.logistics.multitrack.MultitrackControlPayload::register);
        modEventBus.addListener(net.claustra01.clayium.logistics.distributor.DistributorControlPayload::register);
        modEventBus.addListener(net.claustra01.clayium.logistics.filter.FilterControlPayload::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
