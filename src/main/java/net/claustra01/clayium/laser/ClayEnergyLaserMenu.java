package net.claustra01.clayium.laser;

import net.claustra01.clayium.registry.LaserRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Original GUI 13: ContainerNothing (machine area 32) with the energy item outside the window. */
public final class ClayEnergyLaserMenu extends AbstractContainerMenu {
    public static final int MACHINE_HEIGHT = 32;

    private final ClayEnergyLaserBlockEntity laser;
    private final BlockPos pos;
    private final ContainerData data;

    public ClayEnergyLaserMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, inventory.player.level().getBlockEntity(pos) instanceof ClayEnergyLaserBlockEntity laser
                ? laser : null);
    }

    public ClayEnergyLaserMenu(int id, Inventory playerInventory, ClayEnergyLaserBlockEntity laser) {
        super(LaserRegistration.LASER_MENU.get(), id);
        this.laser = laser;
        this.pos = laser == null ? BlockPos.ZERO : laser.getBlockPos();
        this.data = playerInventory.player.level().isClientSide ? new SimpleContainerData(4) : new ContainerData() {
            @Override public int get(int index) {
                return (int)(laser.energyStorage().stored().internalValue() >>> (index * 16)) & 0xFFFF;
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 4; }
        };
        addDataSlots(data);
        if (laser != null) {
            addSlot(new SlotItemHandler(laser.fuelInventory(), 0, -12, MACHINE_HEIGHT - 16));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, MACHINE_HEIGHT + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, MACHINE_HEIGHT + 70));
        }
    }

    public long storedEnergy() {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(data.get(index) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || laser == null) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index == 0 ? !moveItemStackTo(moving, 1, slots.size(), true) : !moveItemStackTo(moving, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return laser != null && !laser.isRemoved() && player.level().getBlockEntity(pos) == laser
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
