package net.claustra01.clayium.machine;

import net.claustra01.clayium.registry.MachineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.claustra01.clayium.multiblock.careactor.CaReactorCoreBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ClayMachineMenu extends AbstractContainerMenu {
    private final BlockPos machinePos;
    private final ClayMachineBlockEntity machine;
    private final ContainerData data;

    public ClayMachineMenu(int containerId, Inventory playerInventory, BlockPos machinePos) {
        this(containerId, playerInventory, machineAt(playerInventory, machinePos), new SimpleContainerData(22));
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
                addSlot(new SlotItemHandler(machine.inventory(), input, inputX(machine, input), inputY(machine, input)));
            }
            for (int output = 0; output < displayedOutputSlots(machine); output++) {
                addSlot(new SlotItemHandler(
                        machine.inventory(), machine.outputSlot(output), outputX(machine, output), outputY(machine, output)));
            }
            for (int input = 0; input < machine.inputSlots(); input++) {
                addSlot(new LockedSlot(machine, machine.reservedSlot(input), -1000, -1000));
            }
            addSlot(new SlotItemHandler(
                    machine.inventory(), machine.fuelSlot(), -12, machineAreaHeight() - 16));
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    private static ClayMachineBlockEntity machineAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof ClayMachineBlockEntity machine ? machine : null;
    }

    private void addPlayerInventory(Inventory inventory) {
        int inventoryY = machineAreaHeight() + 12;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, inventoryY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, inventoryY + 58));
        }
    }

    public static int inputX(int inputSlots, int input) {
        return inputSlots == 1 ? 44 : 32 + input * 18;
    }

    public static int inputX(ClayMachineBlockEntity machine, int input) {
        return isChemicalMetalSeparator(machine) ? 25 : inputX(machine.inputSlots(), input);
    }

    public static int inputY(ClayMachineBlockEntity machine, int input) {
        return isChemicalMetalSeparator(machine) ? 44 : 35;
    }

    public static int outputX(int outputSlots, int output) {
        if (outputSlots == 1) {
            return 116;
        }
        if (outputSlots == 2) {
            return 110 + output * 18;
        }
        return 132 + output % 2 * 18;
    }

    public static int outputY(int outputSlots, int output) {
        return outputSlots <= 2 ? 35 : 26 + output / 2 * 18;
    }

    public static int outputX(ClayMachineBlockEntity machine, int output) {
        if (isChemicalMetalSeparator(machine)) {
            return 85 + output % 4 * 18;
        }
        if (isCentrifuge(machine)) {
            return 116;
        }
        return outputX(Math.min(machine.outputSlots(), 4), output);
    }

    public static int outputY(ClayMachineBlockEntity machine, int output) {
        if (isChemicalMetalSeparator(machine)) {
            return 17 + output / 4 * 18;
        }
        if (isCentrifuge(machine)) {
            int outputs = Math.min(machine.outputSlots(), 4);
            return 35 + output * 18 - 9 * (outputs - 1);
        }
        return outputY(Math.min(machine.outputSlots(), 4), output);
    }

    public int machineAreaHeight() {
        if (machine instanceof CaReactorCoreBlockEntity) {
            return 102;
        }
        if (isChemicalMetalSeparator(machine)) {
            return 96;
        }
        if (isCentrifuge(machine)) {
            return (Math.min(machine.outputSlots(), 4) + 1) * 9 + 46;
        }
        return 72;
    }

    public static boolean isCentrifuge(ClayMachineBlockEntity machine) {
        return hasMethod(machine, "centrifuge");
    }

    public static boolean isChemicalMetalSeparator(ClayMachineBlockEntity machine) {
        return hasMethod(machine, "chemical_metal_separator");
    }

    public static int displayedOutputSlots(ClayMachineBlockEntity machine) {
        return isChemicalMetalSeparator(machine) ? 16 : Math.min(machine.outputSlots(), 4);
    }

    private static boolean hasMethod(ClayMachineBlockEntity machine, String id) {
        return machine != null && machine.series().recipeMethod().id().equals(id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        int machineSlots = machine == null ? 0 : machine.inputSlots() * 2
                + displayedOutputSlots(machine) + 1;
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = machine != null
                    && machine.inventory().isItemValid(machine.fuelSlot(), moving)
                    && moveItemStackTo(moving, machineSlots - 1, machineSlots, false);
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

    private long readLong(int first) {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(data.get(first + index) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    public ClayMachineBlockEntity.MachineState state() {
        int ordinal = data.get(8);
        ClayMachineBlockEntity.MachineState[] values = ClayMachineBlockEntity.MachineState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : ClayMachineBlockEntity.MachineState.IDLE;
    }

    public int tier() {
        return data.get(9);
    }

    public long requiredEnergy() {
        return readLong(14);
    }

    public boolean reactorFormed() {
        return data.get(18) != 0;
    }

    public int reactorCoils() {
        return data.get(19);
    }

    public int reactorHulls() {
        return data.get(20);
    }

    public double reactorAverageRank() {
        return data.get(21) / 100.0D;
    }

    public FluidStack inputFluid() {
        return fluid(10);
    }

    public FluidStack outputFluid() {
        return fluid(12);
    }

    private FluidStack fluid(int index) {
        int id = data.get(index) & 0xFFFF;
        int amount = data.get(index + 1) & 0xFFFF;
        return id == 0 || amount == 0 ? FluidStack.EMPTY
                : new FluidStack(BuiltInRegistries.FLUID.byId(id - 1), amount);
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
