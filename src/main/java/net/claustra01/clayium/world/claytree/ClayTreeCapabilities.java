package net.claustra01.clayium.world.claytree;

import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class ClayTreeCapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                InterfaceCapabilities.LASER_RECEIVER,
                (level, position, state, blockEntity, side) -> state.is(ClayTreeLaserConversion.LASER_TREE_SOURCES)
                        && level instanceof ServerLevel serverLevel
                        ? ClayTreeLaserConversion.receiver(serverLevel, position)
                        : null,
                BuiltInRegistries.BLOCK.stream().toArray(Block[]::new));
    }

    private ClayTreeCapabilities() {
    }
}
