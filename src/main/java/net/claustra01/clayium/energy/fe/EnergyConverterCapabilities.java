package net.claustra01.clayium.energy.fe;

import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class EnergyConverterCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                EnergyConverterRegistration.BLOCK_ENTITY.get(),
                EnergyConverterBlockEntity::fuelHandler);
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                EnergyConverterRegistration.BLOCK_ENTITY.get(),
                EnergyConverterBlockEntity::energyStorage);
    }

    private EnergyConverterCapabilities() {
    }
}
