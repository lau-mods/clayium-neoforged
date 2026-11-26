package net.claustra01.clayium.logistics.distributor;

import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Original ContainerClayDistributor: 2x2 colonies separated by 2 px, first row at y=18. */
public final class DistributorMenu extends AbstractContainerMenu {
    private final DistributorBlockEntity distributor;
    private final BlockPos pos;
    private final int colonyColumns;
    private final int colonyRows;
    private final int storageSlots;

    public DistributorMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, at(inventory, pos));
    }

    public DistributorMenu(int id, Inventory playerInventory, DistributorBlockEntity distributor) {
        super(MultitrackRegistration.DISTRIBUTOR_MENU.get(), id);
        this.distributor = distributor;
        this.pos = distributor == null ? BlockPos.ZERO : distributor.getBlockPos();
        int tier = distributor == null ? 7 : distributor.tier();
        this.colonyColumns = DistributorBlockEntity.colonyColumns(tier);
        this.colonyRows = DistributorBlockEntity.colonyRows(tier);
        this.storageSlots = distributor == null ? 0 : distributor.inventory().getSlots();
        for (int colonyRow = 0; colonyRow < colonyRows && distributor != null; colonyRow++) {
            for (int colonyColumn = 0; colonyColumn < colonyColumns; colonyColumn++) {
                for (int row = 0; row < DistributorBlockEntity.COLONY_ROWS; row++) {
                    for (int column = 0; column < DistributorBlockEntity.COLONY_COLUMNS; column++) {
                        int index = column + DistributorBlockEntity.COLONY_COLUMNS
                                * (row + DistributorBlockEntity.COLONY_ROWS * (colonyColumn + colonyColumns * colonyRow));
                        addSlot(new SlotItemHandler(distributor.inventory(), index,
                                slotX(colonyColumn, column), slotY(colonyRow, row)) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return distributor.acceptsSlot(getSlotIndex());
                            }
                        });
                    }
                }
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, machineHeight() + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, machineHeight() + 70));
        }
    }

    private static DistributorBlockEntity at(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof DistributorBlockEntity distributor ? distributor : null;
    }

    public int slotX(int colonyColumn, int column) {
        int width = DistributorBlockEntity.COLONY_COLUMNS * colonyColumns * 18 + (colonyColumns - 1) * 2;
        return column * 18 + colonyColumn * (DistributorBlockEntity.COLONY_COLUMNS * 18 + 2) + (176 - width) / 2 + 1;
    }

    public int slotY(int colonyRow, int row) {
        return row * 18 + colonyRow * (DistributorBlockEntity.COLONY_ROWS * 18 + 2) + 18;
    }

    public int machineHeight() {
        return DistributorBlockEntity.COLONY_ROWS * colonyRows * 18 + (colonyRows - 1) * 2 + 18;
    }

    public int storageSlots() {
        return storageSlots;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (!(index < storageSlots
                ? moveItemStackTo(moving, storageSlots, slots.size(), true)
                : moveItemStackTo(moving, 0, storageSlots, false))) return ItemStack.EMPTY;
        if (moving.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return distributor != null && !distributor.isRemoved() && player.level().getBlockEntity(pos) == distributor
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    public DistributorBlockEntity distributor() {
        return distributor;
    }

    public BlockPos pos() {
        return pos;
    }
}
