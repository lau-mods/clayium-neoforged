package net.claustra01.clayium.machine.iface;

import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class InterfaceCapabilities {
    public static final BlockCapability<RedstoneControlReceiver, Direction> REDSTONE_RECEIVER =
            BlockCapability.createSided(ClayiumMod.id("redstone_control_receiver"), RedstoneControlReceiver.class);
    public static final BlockCapability<LaserReceiver, Direction> LASER_RECEIVER =
            BlockCapability.createSided(ClayiumMod.id("laser_receiver"), LaserReceiver.class);

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                InterfaceRegistration.BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.interfaceType() == InterfaceType.ITEM
                        ? blockEntity.itemHandler(direction) : null);
        event.registerBlockEntity(
                LASER_RECEIVER, InterfaceRegistration.BLOCK_ENTITY.get(),
                (blockEntity, direction) -> blockEntity.interfaceType() == InterfaceType.LASER
                        ? blockEntity : null);
    }

    private InterfaceCapabilities() {
    }
}
