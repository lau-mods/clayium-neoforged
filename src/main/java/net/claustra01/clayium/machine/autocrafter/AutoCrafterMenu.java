package net.claustra01.clayium.machine.autocrafter;

import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class AutoCrafterMenu extends AbstractContainerMenu {
    private static final int PATTERN_START = 0;
    private static final int PREVIEW_SLOT = PATTERN_START + AutoCrafterBlockEntity.PATTERN_SLOTS;
    private static final int INPUT_START = PREVIEW_SLOT + 1;
    private static final int OUTPUT_START = INPUT_START + AutoCrafterBlockEntity.INPUT_SLOTS;
    private static final int FUEL_SLOT = OUTPUT_START + AutoCrafterBlockEntity.OUTPUT_SLOTS;
    private static final int MACHINE_SLOT_COUNT = FUEL_SLOT + 1;

    private final AutoCrafterBlockEntity autoCrafter;
    private final BlockPos autoCrafterPos;
    private final ContainerData data;

    public AutoCrafterMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, autoCrafterAt(inventory, pos), new SimpleContainerData(7));
    }

    public AutoCrafterMenu(
            int containerId, Inventory inventory, AutoCrafterBlockEntity autoCrafter, ContainerData data) {
        super(AutoCrafterRegistration.MENU.get(), containerId);
        this.autoCrafter = autoCrafter;
        autoCrafterPos = autoCrafter.getBlockPos();
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new GhostSlot(autoCrafter, column + row * 3, 8 + column * 18, 34 + row * 18));
            }
        }
        addSlot(new LockedSlot(autoCrafter.preview(), 0, 75, 52, true));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(autoCrafter.inventory(), column + row * 3,
                        107 + column * 18, 34 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 2; column++) {
                addSlot(new LockedSlot(autoCrafter.inventory(),
                        AutoCrafterBlockEntity.OUTPUT_START + column + row * 2,
                        174 + column * 18, 34 + row * 18, false));
            }
        }
        addSlot(new SlotItemHandler(autoCrafter.inventory(), AutoCrafterBlockEntity.FUEL_SLOT, 75, 76));
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static AutoCrafterBlockEntity autoCrafterAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof AutoCrafterBlockEntity autoCrafter) {
            return autoCrafter;
        }
        throw new IllegalStateException("Auto crafter menu opened without its block entity");
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 34 + column * 18, 139 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 34 + column * 18, 197));
        }
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= PATTERN_START && slotId < PREVIEW_SLOT && clickType == ClickType.PICKUP) {
            ItemStack carried = getCarried();
            autoCrafter.setPatternStack(slotId - PATTERN_START, carried);
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || index < INPUT_START) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem() || !slot.mayPickup(player)) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(moving, MACHINE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (ClayEnergyStorage.fuelValue(moving).internalValue() > 0L
                && moveItemStackTo(moving, FUEL_SLOT, FUEL_SLOT + 1, false)) {
            // Fuel has a dedicated slot.
        } else if (!moveItemStackTo(moving, INPUT_START, OUTPUT_START, false)) {
            return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return !autoCrafter.isRemoved()
                && player.level().getBlockEntity(autoCrafterPos) == autoCrafter
                && player.distanceToSqr(autoCrafterPos.getX() + 0.5D, autoCrafterPos.getY() + 0.5D,
                        autoCrafterPos.getZ() + 0.5D) <= 64.0D;
    }

    public int tier() {
        return data.get(0);
    }

    public int intervalTicks() {
        return Math.max(1, data.get(1));
    }

    public int craftsPerCycle() {
        return data.get(2);
    }

    public long storedEnergy() {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(data.get(index + 3) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    private static final class GhostSlot extends SlotItemHandler {
        private GhostSlot(AutoCrafterBlockEntity autoCrafter, int slot, int x, int y) {
            super(autoCrafter.pattern(), slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }

    private static final class LockedSlot extends SlotItemHandler {
        private final boolean lockPickup;

        private LockedSlot(net.neoforged.neoforge.items.IItemHandler handler, int slot, int x, int y, boolean lockPickup) {
            super(handler, slot, x, y);
            this.lockPickup = lockPickup;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return !lockPickup;
        }
    }
}
