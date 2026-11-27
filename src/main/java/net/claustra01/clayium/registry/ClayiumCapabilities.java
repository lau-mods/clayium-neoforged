package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers ItemHandler and FluidHandler capabilities for Clayium BlockEntities.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClayiumCapabilities {
    private ClayiumCapabilities() {}

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.BUFFER.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.MULTITRACK_BUFFER.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.FLUID_TRANSLATOR.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ClayiumBlockEntities.FLUID_TRANSLATOR.get(),
                (be, side) -> be.getFluidHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.STORAGE_CONTAINER.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.VACUUM_CONTAINER.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.METAL_CHEST.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.DISTRIBUTOR.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.CLAY_INTERFACE.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ClayiumBlockEntities.CLAY_INTERFACE.get(),
                (be, side) -> be.getDelegatedFluidHandler(side)
        );
    }
}
