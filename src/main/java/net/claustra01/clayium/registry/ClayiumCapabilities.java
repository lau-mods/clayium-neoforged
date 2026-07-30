/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.registry;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Registers standard NeoForge capabilities exposed by Clayium devices. */
public final class ClayiumCapabilities {
    private ClayiumCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ClayiumRegistries.CLAY_WORK_TABLE_BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.externalItemHandler());
    }
}
