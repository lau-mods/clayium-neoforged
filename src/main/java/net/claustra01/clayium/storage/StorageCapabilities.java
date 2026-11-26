package net.claustra01.clayium.storage;

import net.claustra01.clayium.registry.StorageRegistration;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class StorageCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                StorageRegistration.STORAGE_CONTAINER_BLOCK_ENTITY.get(),
                StorageContainerBlockEntity::itemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                StorageRegistration.VOID_CONTAINER_BLOCK_ENTITY.get(),
                VoidContainerBlockEntity::itemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                StorageRegistration.METAL_CHEST_BLOCK_ENTITY.get(),
                (chest, side) -> chest.inventory());
    }

    private StorageCapabilities() {
    }
}
