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
        addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.INPUT_SLOT, 44, 46));
        addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.OUTPUT_SLOT, 116, 46));
        addSlot(new LockedSlot(machine, ClayProductionBlockEntity.RESERVED_SLOT));
        if (machine.type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER) {
            addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.FUEL_SLOT, 44, 68));
        }
        if (machine.type().kind() == ClayProductionType.Kind.AUTOMATIC_CONDENSER) {
            addSlot(new SlotItemHandler(machine.inventory(), ClayProductionBlockEntity.LIMIT_SLOT, 66, 68));
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
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 100 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 158));
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
            boolean moved = false;
            int[] targets = machine.type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER
                    || machine.type().kind() == ClayProductionType.Kind.AUTOMATIC_CONDENSER
                            && !machine.inventory().getStackInSlot(ClayProductionBlockEntity.INPUT_SLOT).isEmpty()
                    ? new int[] {3, 0} : new int[] {0, 3};
            for (int target : targets) {
                if (target >= machineSlotCount) {
                    continue;
                }
                if (slots.get(target).mayPlace(moving)) {
                    moved = moveItemStackTo(moving, target, target + 1, false);
                    if (moved) {
                        break;
                    }
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

    public long cePerTick() {
        return readLong(12);
    }

    public ClayProductionBlockEntity.State state() {
        return ClayProductionBlockEntity.State.values()[data.get(16)];
    }

    public boolean hasFuelSlot() {
        return machine.type().kind() == ClayProductionType.Kind.ENERGETIC_CONDENSER;
    }

    public boolean hasLimitSlot() {
        return machine.type().kind() == ClayProductionType.Kind.AUTOMATIC_CONDENSER;
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

    private static final class LockedSlot extends SlotItemHandler {
        private LockedSlot(ClayProductionBlockEntity machine, int slot) {
            super(machine.inventory(), slot, -1000, -1000);
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
