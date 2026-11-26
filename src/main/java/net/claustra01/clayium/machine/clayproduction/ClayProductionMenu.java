package net.claustra01.clayium.machine.clayproduction;

import net.claustra01.clayium.registry.ClayProductionRegistration;
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

public final class ClayProductionMenu extends AbstractContainerMenu {
    private final BlockPos machinePos;
    private final ClayProductionBlockEntity machine;
    private final ContainerData data;
    private final int machineSlotCount;

    public ClayProductionMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, machineAt(inventory, pos), new SimpleContainerData(19));
    }

    public ClayProductionMenu(
            int containerId, Inventory inventory, ClayProductionBlockEntity machine, ContainerData data) {
        super(ClayProductionRegistration.MENU.get(), containerId);
        this.machine = machine;
        machinePos = machine.getBlockPos();
        this.data = data;
        if (machine.type().kind() == ClayProductionType.Kind.AUTOMATIC_CONDENSER) {
            // ContainerAutoClayCondenser: 5x4 grid centred at y=18, SlotMemory limit at 108 + grid x.
            for (int row = 0; row < AUTO_ROWS; row++) {
                for (int column = 0; column < AUTO_COLUMNS; column++) {
                    addSlot(new SlotItemHandler(machine.inventory(),
                            ClayProductionBlockEntity.AUTO_GRID_START + column + row * AUTO_COLUMNS,
                            column * 18 + AUTO_GRID_X, row * 18 + 18));
                }
            }
            addSlot(new LimitSlot(machine, 108 + AUTO_GRID_X, 18));
        } else {
            // ContainerClayMachines: large input (44,35) and output (116,35).
            addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.INPUT_SLOT, 44, 35));
            addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.OUTPUT_SLOT, 116, 35));
            if (machine.type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER) {
                addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.FUEL_SLOT,
                        -12, machineHeight() - 16));
            }
        }
        machineSlotCount = slots.size();
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static ClayProductionBlockEntity machineAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return (ClayProductionBlockEntity)blockEntity;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, machineHeight() + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, machineHeight() + 70));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < machineSlotCount) {
            if (!moveItemStackTo(moving, machineSlotCount, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int end = automatic() ? AUTO_ROWS * AUTO_COLUMNS : machineSlotCount;
            boolean moved = false;
            if (machine.type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER
                    && slots.get(2).mayPlace(moving)) {
                moved = moveItemStackTo(moving, 2, 3, false);
            }
            for (int target = 0; target < end && !moved; target++) {
                if (slots.get(target).mayPlace(moving)) {
                    moved = moveItemStackTo(moving, target, target + 1, false);
                }
            }
            if (!moved) {
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
        return !machine.isRemoved() && player.level().getBlockEntity(machinePos) == machine
                && player.distanceToSqr(machinePos.getX() + 0.5D, machinePos.getY() + 0.5D,
                        machinePos.getZ() + 0.5D) <= 64.0D;
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

    /** Original tile.clayEnergy shown by GuiClayEnergyTemp. */
    public long displayedEnergy() {
        return readLong(12);
    }

    public boolean automatic() {
        return machine.type().kind() == ClayProductionType.Kind.AUTOMATIC_CONDENSER;
    }

    public int machineHeight() {
        return automatic() ? AUTO_ROWS * 18 + 32 : 72;
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        if (automatic() && slotId == AUTO_ROWS * AUTO_COLUMNS) {
            if (clickType == net.minecraft.world.inventory.ClickType.PICKUP
                    || clickType == net.minecraft.world.inventory.ClickType.QUICK_MOVE) {
                ItemStack carried = getCarried();
                machine.inventory().setStackInSlot(ClayProductionBlockEntity.LIMIT_SLOT,
                        carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    public ClayProductionBlockEntity.State state() {
        return ClayProductionBlockEntity.State.values()[data.get(16)];
    }

    public int tier() {
        return data.get(17);
    }

    public ClayProductionType type() {
        return machine.type();
    }

    public int productionLevel() {
        return data.get(18);
    }

    private long readLong(int first) {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(data.get(first + index) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    public static final int AUTO_COLUMNS = 5;
    public static final int AUTO_ROWS = 4;
    public static final int AUTO_GRID_X = (176 - 18 * AUTO_COLUMNS) / 2;

    private static final class LimitSlot extends SlotItemHandler {
        private LimitSlot(ClayProductionBlockEntity machine, int x, int y) {
            super(machine.inventory(), ClayProductionBlockEntity.LIMIT_SLOT, x, y);
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
