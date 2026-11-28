package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Registers ItemHandler, FluidHandler, and EnergyStorage capabilities for Clayium BlockEntities.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClayiumCapabilities {
    private ClayiumCapabilities() {}

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerContainerItemHandler(event, ClayiumBlockEntities.BUFFER);
        registerContainerItemHandler(event, ClayiumBlockEntities.MULTITRACK_BUFFER);
        registerContainerItemHandler(event, ClayiumBlockEntities.FLUID_TRANSLATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.STORAGE_CONTAINER);
        registerContainerItemHandler(event, ClayiumBlockEntities.VACUUM_CONTAINER);
        registerContainerItemHandler(event, ClayiumBlockEntities.METAL_CHEST);
        registerContainerItemHandler(event, ClayiumBlockEntities.DISTRIBUTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CLAY_INTERFACE);
        registerContainerItemHandler(event, ClayiumBlockEntities.SIMPLE_MACHINE);
        registerContainerItemHandler(event, ClayiumBlockEntities.ASSEMBLER);
        registerContainerItemHandler(event, ClayiumBlockEntities.CENTRIFUGE);
        registerContainerItemHandler(event, ClayiumBlockEntities.CHEMICAL_REACTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CHEMICAL_METAL_SEPARATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CA_INJECTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CA_CONDENSER);
        registerContainerItemHandler(event, ClayiumBlockEntities.CA_COLLECTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.BLAST_FURNACE);
        registerContainerItemHandler(event, ClayiumBlockEntities.REACTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CA_REACTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.AUTO_CLAY_CONDENSER);
        registerContainerItemHandler(event, ClayiumBlockEntities.SOLAR_CLAY_FABRICATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CLAY_FABRICATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.COBBLESTONE_GENERATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.SALT_EXTRACTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.WATER_WHEEL);
        registerContainerItemHandler(event, ClayiumBlockEntities.CREATIVE_CE_SOURCE);
        registerContainerItemHandler(event, ClayiumBlockEntities.AUTO_CRAFTER);
        registerContainerItemHandler(event, ClayiumBlockEntities.AUTO_TRADER);
        registerContainerItemHandler(event, ClayiumBlockEntities.PAN_CORE);
        registerContainerItemHandler(event, ClayiumBlockEntities.PAN_ADAPTER);
        registerContainerItemHandler(event, ClayiumBlockEntities.PAN_DUPLICATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.CLAY_CRAFTING_TABLE);
        registerContainerItemHandler(event, ClayiumBlockEntities.LASER_INTERFACE);
        registerContainerItemHandler(event, ClayiumBlockEntities.CLAY_ENERGY_LASER);
        registerContainerItemHandler(event, ClayiumBlockEntities.AREA_MINER);
        registerContainerItemHandler(event, ClayiumBlockEntities.AREA_COLLECTOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.AREA_ACTIVATOR);
        registerContainerItemHandler(event, ClayiumBlockEntities.FE_GENERATOR);

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ClayiumBlockEntities.FE_GENERATOR.get(),
                (be, side) -> be.getEnergyStorage(side)
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumBlockEntities.CLAY_WORK_TABLE.get(),
                (be, side) -> be.getItemHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ClayiumBlockEntities.FLUID_TRANSLATOR.get(),
                (be, side) -> be.getFluidHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ClayiumBlockEntities.CLAY_INTERFACE.get(),
                (be, side) -> be.getDelegatedFluidHandler(side)
        );
    }

    private static <T extends net.claustra01.clayium.blockentity.ClayContainerBlockEntity> void registerContainerItemHandler(
            RegisterCapabilitiesEvent event,
            net.neoforged.neoforge.registries.DeferredHolder<
                    net.minecraft.world.level.block.entity.BlockEntityType<?>,
                    net.minecraft.world.level.block.entity.BlockEntityType<T>> type) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                type.get(),
                (be, side) -> be.getItemHandler(side)
        );
    }
}
