package net.claustra01.clayium.world.area;

import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AreaMachineMenu extends AbstractContainerMenu {
    private final AreaMachineBlockEntity machine;
    private final BlockPos position;
    private final ContainerData activatorData;

    public AreaMachineMenu(int containerId, Inventory playerInventory, BlockPos position) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(position)
                instanceof AreaMachineBlockEntity areaMachine ? areaMachine : null);
    }

    public AreaMachineMenu(int containerId, Inventory playerInventory, AreaMachineBlockEntity machine) {
        super(AreaMachineRegistration.MENU.get(), containerId);
        this.machine = machine;
        position = machine == null ? BlockPos.ZERO : machine.getBlockPos();
        activatorData = playerInventory.player.level().isClientSide ? new SimpleContainerData(12) : new ContainerData() {
            @Override public int get(int index) {
                if (AreaMachineMenu.this.machine == null) return 0;
                return switch (index) {
                    case 0 -> AreaMachineMenu.this.machine.activatorTarget().ordinal();
                    case 1 -> AreaMachineMenu.this.machine.activatorRayTrace() ? 1 : 0;
                    case 2 -> AreaMachineMenu.this.machine.activatorSneaking() ? 1 : 0;
                    case 3 -> AreaMachineMenu.this.machine.status().ordinal();
                    case 4, 5, 6, 7 -> (int)(AreaMachineMenu.this.machine.storedEnergy()
                            >>> ((index - 4) * 16)) & 0xFFFF;
                    case 8, 9, 10, 11 -> (int)(AreaMachineMenu.this.machine.progress()
                            >>> ((index - 8) * 16)) & 0xFFFF;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 12; }
        };
        addDataSlots(activatorData);
        if (machine != null) {
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.OPERATION_SLOT, 8, 35));
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.HARVEST_FILTER_SLOT, 8, 58));
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.FORTUNE_FILTER_SLOT, 26, 58));
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.SILK_TOUCH_FILTER_SLOT, 44, 58));
            for (int index = 0; index < AreaMachineBlockEntity.OUTPUT_SLOTS; index++) {
                addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.OUTPUT_START + index,
                        68 + index % 3 * 18, 26 + index / 3 * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
            addSlot(new SlotItemHandler(machine.inventory(), AreaMachineBlockEntity.FUEL_SLOT, 35, 35));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18, 133 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 191));
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
            int start = AreaMachineBlockEntity.OPERATION_SLOT;
            int end = start + 1;
            if (moving.is(LogisticsRegistration.ITEM_FILTER.get())) {
                start = AreaMachineBlockEntity.HARVEST_FILTER_SLOT;
                end = AreaMachineBlockEntity.SILK_TOUCH_FILTER_SLOT + 1;
            } else if (machine.inventory().isItemValid(AreaMachineBlockEntity.FUEL_SLOT, moving)) {
                start = AreaMachineBlockEntity.FUEL_SLOT;
                end = start + 1;
            }
            if (!moveItemStackTo(moving, start, end, false)) {
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

    public BlockPos position() {
        return position;
    }

    public boolean isActivator() {
        return machine != null && machine.machineType() == AreaMachineType.ACTIVATOR;
    }

    public int activatorTarget() {
        return activatorData.get(0);
    }

    public boolean activatorRayTrace() {
        return activatorData.get(1) != 0;
    }

    public boolean activatorSneaking() {
        return activatorData.get(2) != 0;
    }

    public AreaMachineBlockEntity.OperationStatus status() {
        return AreaMachineBlockEntity.OperationStatus.values()[activatorData.get(3)];
    }

    public long storedEnergy() {
        return readLong(4);
    }

    public long progress() {
        return readLong(8);
    }

    private long readLong(int start) {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(activatorData.get(start + index) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    public int requiredProgress() {
        return machine == null ? 1 : machine.machineType().progressPerJob();
    }

    public AreaMachineType machineType() {
        return machine == null ? AreaMachineType.MINER : machine.machineType();
    }

    @Override
    public boolean stillValid(Player player) {
        return machine != null && !machine.isRemoved()
                && player.level().getBlockEntity(position) == machine
                && player.distanceToSqr(position.getX() + 0.5D,
                        position.getY() + 0.5D, position.getZ() + 0.5D) <= 64.0D;
    }
}
