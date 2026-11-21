package net.claustra01.clayium.world.chunkloading;

import net.claustra01.clayium.registry.ChunkLoaderRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ChunkLoaderMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final ChunkLoaderBlockEntity blockEntity;
    private final ContainerData data;

    public ChunkLoaderMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, chunkLoaderAt(inventory, pos), new SimpleContainerData(1));
    }

    public ChunkLoaderMenu(
            int containerId, Inventory inventory, ChunkLoaderBlockEntity blockEntity, ContainerData data) {
        super(ChunkLoaderRegistration.MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = blockEntity == null ? BlockPos.ZERO : blockEntity.getBlockPos();
        this.data = data;
        addDataSlots(data);
    }

    private static ChunkLoaderBlockEntity chunkLoaderAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof ChunkLoaderBlockEntity chunkLoader ? chunkLoader : null;
    }

    public ChunkLoaderBlockEntity.TicketStatus ticketStatus() {
        return ChunkLoaderBlockEntity.TicketStatus.values()[data.get(0)];
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
