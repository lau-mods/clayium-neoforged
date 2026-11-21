package net.claustra01.clayium.machine.iface;

import net.claustra01.clayium.registry.InterfaceRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class InterfaceMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final InterfaceBlockEntity blockEntity;
    private final ContainerData data;

    public InterfaceMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, interfaceAt(inventory, pos), new SimpleContainerData(2));
    }

    public InterfaceMenu(int containerId, Inventory inventory, InterfaceBlockEntity blockEntity, ContainerData data) {
        super(InterfaceRegistration.MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = blockEntity == null ? BlockPos.ZERO : blockEntity.getBlockPos();
        this.data = data;
        addDataSlots(data);
    }

    private static InterfaceBlockEntity interfaceAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof InterfaceBlockEntity interfaceBlockEntity ? interfaceBlockEntity : null;
    }

    public InterfaceBlockEntity blockEntity() {
        return blockEntity;
    }

    public int tier() {
        return data.get(0);
    }

    public InterfaceBlockEntity.ConnectionStatus status() {
        int ordinal = data.get(1);
        return ordinal >= 0 && ordinal < InterfaceBlockEntity.ConnectionStatus.values().length
                ? InterfaceBlockEntity.ConnectionStatus.values()[ordinal]
                : InterfaceBlockEntity.ConnectionStatus.DISCONNECTED;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved()
                && player.level().getBlockEntity(pos) == blockEntity
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
