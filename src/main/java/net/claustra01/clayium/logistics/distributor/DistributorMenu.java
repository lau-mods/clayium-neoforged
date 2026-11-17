package net.claustra01.clayium.logistics.distributor;

import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class DistributorMenu extends AbstractContainerMenu {
    private final DistributorBlockEntity distributor;
    private final BlockPos pos;
    private final ContainerData data;

    public DistributorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, at(inventory, pos), new SimpleContainerData(7));
    }
    public DistributorMenu(int id, Inventory playerInventory, DistributorBlockEntity distributor, ContainerData data) {
        super(MultitrackRegistration.DISTRIBUTOR_MENU.get(), id);
        this.distributor = distributor;
        this.pos = distributor == null ? BlockPos.ZERO : distributor.getBlockPos();
        this.data = data;
        if (distributor != null) {
            for (int slot = 0; slot < DistributorBlockEntity.SLOTS; slot++) {
                addSlot(new SlotItemHandler(distributor.inventory(), slot, 62 + slot % 3 * 18, 54 + slot / 3 * 18));
            }
        }
        for (int row = 0; row < 3; row++) for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column + row * 9 + 9, 35 + column * 18, 130 + row * 18));
        for (int column = 0; column < 9; column++)
            addSlot(new Slot(playerInventory, column, 35 + column * 18, 188));
        addDataSlots(data);
    }
    private static DistributorBlockEntity at(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof DistributorBlockEntity distributor ? distributor : null;
    }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (!(index < DistributorBlockEntity.SLOTS
                ? moveItemStackTo(moving, DistributorBlockEntity.SLOTS, slots.size(), true)
                : moveItemStackTo(moving, 0, DistributorBlockEntity.SLOTS, false))) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
    @Override public boolean stillValid(Player player) {
        return distributor != null && !distributor.isRemoved() && player.level().getBlockEntity(pos) == distributor
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
    public DistributorBlockEntity distributor() { return distributor; }
    public BlockPos pos() { return pos; }
    public int tier() { return data.get(0); }
    public BufferBlockEntity.FaceMode faceMode(Direction direction) {
        return BufferBlockEntity.FaceMode.values()[data.get(1 + direction.get3DDataValue())];
    }
}
