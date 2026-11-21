package net.claustra01.clayium.world.area;

import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AreaMachineMenu extends AbstractContainerMenu {
    private final AreaMachineBlockEntity machine;
    private final BlockPos position;

    public AreaMachineMenu(int containerId, Inventory playerInventory, BlockPos position) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(position)
                instanceof AreaMachineBlockEntity areaMachine ? areaMachine : null);
    }

    public AreaMachineMenu(int containerId, Inventory playerInventory, AreaMachineBlockEntity machine) {
        super(AreaMachineRegistration.MENU.get(), containerId);
        this.machine = machine;
        position = machine == null ? BlockPos.ZERO : machine.getBlockPos();
        if (machine != null) {
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.OPERATION_SLOT, 8, 35));
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.FILTER_SLOT, 8, 58));
            for (int index = 0; index < AreaMachineBlockEntity.OUTPUT_SLOTS; index++) {
                addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.OUTPUT_START + index,
                        62 + index % 3 * 18, 26 + index / 3 * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.FUEL_SLOT, 35, 58));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 100 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 158));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (machine == null || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moving = slots.get(index).getItem();
        ItemStack original = moving.copy();
        int machineSlots = AreaMachineBlockEntity.FUEL_SLOT + 1;
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = moving.is(LogisticsRegistration.ITEM_FILTER.get())
                    ? AreaMachineBlockEntity.FILTER_SLOT
                    : machine.inventory().isItemValid(AreaMachineBlockEntity.FUEL_SLOT, moving)
                            ? AreaMachineBlockEntity.FUEL_SLOT : AreaMachineBlockEntity.OPERATION_SLOT;
            if (!moveItemStackTo(moving, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (moving.isEmpty()) {
            slots.get(index).set(ItemStack.EMPTY);
        } else {
            slots.get(index).setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return machine != null && !machine.isRemoved()
                && player.level().getBlockEntity(position) == machine
                && player.distanceToSqr(position.getX() + 0.5D,
                        position.getY() + 0.5D, position.getZ() + 0.5D) <= 64.0D;
    }
}
