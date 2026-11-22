package net.claustra01.clayium;

import net.claustra01.clayium.datagen.ClayiumDataGenerators;
import net.claustra01.clayium.energy.fe.EnergyConverterCapabilities;
import net.claustra01.clayium.fluid.FluidCapabilities;
import net.claustra01.clayium.fluid.FluidTransportControlPayload;
import net.claustra01.clayium.machine.MachineCapabilities;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterCapabilities;
import net.claustra01.clayium.machine.clayproduction.ClayProductionCapabilities;
import net.claustra01.clayium.machine.ca.CaCapabilities;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.machine.trade.TradeCapabilities;
import net.claustra01.clayium.machine.trade.TradeSelectPayload;
import net.claustra01.clayium.machine.trade.TradeSnapshotPayload;
import net.claustra01.clayium.multiblock.MultiblockCapabilities;
import net.claustra01.clayium.multiblock.careactor.CaReactorCapabilities;
import net.claustra01.clayium.machine.miner.MinerCapabilities;
import net.claustra01.clayium.network.MachineFaceModePayload;
import net.claustra01.clayium.logistics.BufferControlPayload;
import net.claustra01.clayium.logistics.LogisticsCapabilities;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.storage.StorageCapabilities;
import net.claustra01.clayium.world.chunkloading.ChunkLoaderTicketService;
import net.claustra01.clayium.world.area.AreaCapabilities;
import net.claustra01.clayium.world.claytree.ClayTreeCapabilities;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(ClayiumMod.MOD_ID)
public final class ClayiumMod {
    public static final String MOD_ID = "clayium";

    public ClayiumMod(IEventBus modEventBus, ModContainer modContainer) {
        ClayiumRegistries.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, ClayiumConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ClayiumConfig.SERVER_SPEC);
        modEventBus.addListener(ChunkLoaderTicketService::register);
        modEventBus.addListener(ClayiumDataGenerators::gatherData);
        modEventBus.addListener(MachineCapabilities::register);
        modEventBus.addListener(AutoCrafterCapabilities::register);
        modEventBus.addListener(ClayProductionCapabilities::register);
        modEventBus.addListener(CaCapabilities::register);
        modEventBus.addListener(EnergyConverterCapabilities::register);
        modEventBus.addListener(FluidCapabilities::register);
        modEventBus.addListener(LogisticsCapabilities::register);
        modEventBus.addListener(StorageCapabilities::register);
        modEventBus.addListener(InterfaceCapabilities::register);
        modEventBus.addListener(net.claustra01.clayium.laser.LaserCapabilities::register);
        modEventBus.addListener(MultiblockCapabilities::register);
        modEventBus.addListener(CaReactorCapabilities::register);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.claustra01.clayium.multiblock.MultiblockStructureNotifier::onNeighborNotify);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                net.claustra01.clayium.multiblock.careactor.CaReactorStructureNotifier::onNeighborNotify);
        modEventBus.addListener(ClayTreeCapabilities::register);
        modEventBus.addListener(TradeCapabilities::register);
        modEventBus.addListener(MinerCapabilities::register);
        modEventBus.addListener(AreaCapabilities::register);
        if (MOD_ID.equals(System.getProperty("neoforge.enabledGameTestNamespaces"))) {
            modEventBus.addListener(net.claustra01.clayium.gametest.iface.InterfaceGameTests::registerTestCapability);
            modEventBus.addListener(net.claustra01.clayium.gametest.logistics.LogisticsGameTests::registerTestCapability);
            modEventBus.addListener(net.claustra01.clayium.gametest.energyconverter.EnergyConverterGameTests::registerTestCapability);
            modEventBus.addListener(net.claustra01.clayium.gametest.fluid.FluidTransportGameTests::registerTestCapability);
            modEventBus.addListener(net.claustra01.clayium.gametest.laser.LaserGameTests::registerTestCapability);
            modEventBus.addListener(
                    net.claustra01.clayium.gametest.compat.StandardCompatibilityGameTests::registerTestCapabilities);
        }
        modEventBus.addListener(MachineFaceModePayload::register);
        modEventBus.addListener(BufferControlPayload::register);
        modEventBus.addListener(FluidTransportControlPayload::register);
        modEventBus.addListener(net.claustra01.clayium.logistics.multitrack.MultitrackControlPayload::register);
        modEventBus.addListener(net.claustra01.clayium.logistics.distributor.DistributorControlPayload::register);
        modEventBus.addListener(net.claustra01.clayium.logistics.filter.FilterControlPayload::register);
        modEventBus.addListener(TradeSelectPayload::register);
        modEventBus.addListener(TradeSnapshotPayload::register);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
