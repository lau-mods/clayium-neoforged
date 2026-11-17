package net.claustra01.clayium.machine;

import net.claustra01.clayium.registry.MachineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ClayMachineMenu extends AbstractContainerMenu {
    private final BlockPos machinePos;
    private final ClayMachineBlockEntity machine;
    private final ContainerData data;

    public ClayMachineMenu(int containerId, Inventory playerInventory, BlockPos machinePos) {
        this(containerId, playerInventory, machineAt(playerInventory, machinePos), new SimpleContainerData(14));
    }

    public ClayMachineMenu(
            int containerId,
            Inventory playerInventory,
            ClayMachineBlockEntity machine,
            ContainerData data) {
        super(MachineRegistration.MACHINE_MENU.get(), containerId);
        this.machine = machine;
        this.machinePos = machine == null ? BlockPos.ZERO : machine.getBlockPos();
        this.data = data;
        if (machine != null) {
            for (int input = 0; input < machine.inputSlots(); input++) {
                addSlot(new SlotItemHandler(machine.inventory(), input, inputX(machine.inputSlots(), input), 35));
            }
            addSlot(new SlotItemHandler(machine.inventory(), machine.outputSlot(), 116, 35));
            for (int input = 0; input < machine.inputSlots(); input++) {
                addSlot(new LockedSlot(machine, machine.reservedSlot(input), -1000, -1000));
            }
            addSlot(new SlotItemHandler(machine.inventory(), machine.fuelSlot(), 44, 57));
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    private static ClayMachineBlockEntity machineAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof ClayMachineBlockEntity machine ? machine : null;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public static int inputX(int inputSlots, int input) {
        return inputSlots == 1 ? 44 : 35 + input * 18;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        int machineSlots = machine == null ? 0 : machine.inventory().getSlots();
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = machine != null
                    && machine.inventory().isItemValid(machine.fuelSlot(), moving)
                    && moveItemStackTo(moving, machine.fuelSlot(), machine.fuelSlot() + 1, false);
            if (!moved && (machine == null || !moveItemStackTo(moving, 0, machine.inputSlots(), false))) {
                return ItemStack.EMPTY;
            }
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
        return machine != null
                && !machine.isRemoved()
                && player.level().getBlockEntity(machinePos) == machine
                && player.distanceToSqr(machinePos.getX() + 0.5D, machinePos.getY() + 0.5D, machinePos.getZ() + 0.5D) <= 64.0D;
    }

    public BlockPos machinePos() {
        return machinePos;
    }

    public ClayMachineBlockEntity machine() {
        return machine;
    }

    public long progress() {
        return readLong(0);
    }

    public long requiredProgress() {
        return readLong(4);
    }

    public long storedEnergy() {
        return readLong(8);
    }

    private long readLong(int first) {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(data.get(first + index) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    public ClayMachineBlockEntity.MachineState state() {
        int ordinal = data.get(12);
        ClayMachineBlockEntity.MachineState[] values = ClayMachineBlockEntity.MachineState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ClayMachineBlockEntity.MachineState.IDLE;
    }

    public int tier() {
        return data.get(13);
    }

    private static final class LockedSlot extends SlotItemHandler {
        private LockedSlot(ClayMachineBlockEntity machine, int slot, int x, int y) {
            super(machine.inventory(), slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
