package net.claustra01.clayium.world.area;

import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Original ContainerAreaMiner / ContainerAreaActivator. The default branch (Tier 6-8) centres a
 * 3x3 grid with the harvest filter four pixels to its right; Tier 9 shifts the grid left by 16
 * and adds the fortune and silk-touch filters; Tier 10 stacks two 4x2 grids two pixels apart.
 */
public final class AreaMachineMenu extends AbstractContainerMenu {
    private static final int DATA_COUNT = 13;

    private final AreaGuiHost host;
    private final BlockPos position;
    private final ContainerData data;
    private final int gridSlots;
    private final int filterSlots;
    private final boolean hasFuel;

    public AreaMachineMenu(int containerId, Inventory playerInventory, BlockPos position) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(position)
                instanceof AreaGuiHost areaHost ? areaHost : null);
    }

    public AreaMachineMenu(int containerId, Inventory playerInventory, AreaGuiHost host) {
        super(AreaMachineRegistration.MENU.get(), containerId);
        this.host = host;
        position = host == null ? BlockPos.ZERO : host.getBlockPos();
        data = playerInventory.player.level().isClientSide ? new SimpleContainerData(DATA_COUNT) : new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> host.activatorTarget().ordinal();
                    case 1 -> host.activatorRayTrace() ? 1 : 0;
                    case 2 -> host.activatorSneaking() ? 1 : 0;
                    case 3, 4, 5, 6 -> (int)(host.storedEnergy() >>> ((index - 3) * 16)) & 0xFFFF;
                    case 7, 8, 9, 10 -> (int)(host.laserEnergy() >>> ((index - 7) * 16)) & 0xFFFF;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return DATA_COUNT; }
        };
        addDataSlots(data);
        int columns = host == null ? 3 : host.gridColumns();
        int rows = host == null ? 3 : host.gridRows();
        int gridCount = 0;
        int filterCount = 0;
        if (host != null) {
            int base = gridBaseX();
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    addSlot(new SlotItemHandler(host.inventory(), column + row * columns,
                            column * 18 + base, row * 18 + 18));
                    gridCount++;
                }
            }
            if (host.hasReplacementGrid()) {
                for (int row = 0; row < rows; row++) {
                    for (int column = 0; column < columns; column++) {
                        addSlot(new SlotItemHandler(host.inventory(), column + row * columns + columns * rows,
                                column * 18 + base, (row + rows) * 18 + 18 + 2));
                        gridCount++;
                    }
                }
            }
            int filterX = columns * 18 + (host.guiTier() == 10 ? 2 : 4) + base;
            int[] filters = {host.harvestFilterSlot(), host.fortuneFilterSlot(), host.silkTouchFilterSlot()};
            for (int index = 0; index < filters.length; index++) {
                if (filters[index] >= 0) {
                    addSlot(new MemorySlot(host, filters[index], filterX, 18 + index * 18));
                    filterCount++;
                }
            }
            if (host.fuelSlot() >= 0) {
                // Original GuiClayEnergyTemp draws the energy item outside the window.
                addSlot(new SlotItemHandler(host.inventory(), host.fuelSlot(), -12, machineHeight() - 16));
            }
        }
        gridSlots = gridCount;
        filterSlots = filterCount;
        hasFuel = host != null && host.fuelSlot() >= 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, 9 + row * 9 + column, 8 + column * 18,
                        machineHeight() + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, machineHeight() + 70));
        }
    }

    public int gridBaseX() {
        int columns = host == null ? 3 : host.gridColumns();
        return host != null && host.guiTier() >= 9 && !host.isActivator()
                ? (176 - 32 - 18 * columns) / 2 : (176 - 18 * columns) / 2;
    }

    public int machineHeight() {
        int rows = host == null ? 3 : host.gridRows();
        return host != null && host.hasReplacementGrid() ? rows * 18 * 2 + 2 + 18 + 18 : rows * 18 + 18 + 18;
    }

    public int gridSlots() {
        return gridSlots;
    }

    public int filterSlots() {
        return filterSlots;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= gridSlots && slotId < gridSlots + filterSlots) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                ((MemorySlot)slots.get(slotId)).remember(getCarried());
                broadcastChanges();
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return stillValid(player) && host.applyControl(id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (host == null || index >= slots.size() || !slots.get(index).hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack moving = slots.get(index).getItem();
        ItemStack original = moving.copy();
        int machineSlots = gridSlots + filterSlots + (hasFuel ? 1 : 0);
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int fuelIndex = gridSlots + filterSlots;
            boolean moved = hasFuel && host.inventory().isItemValid(host.fuelSlot(), moving)
                    && moveItemStackTo(moving, fuelIndex, fuelIndex + 1, false);
            if (!moved && !moveItemStackTo(moving, 0, gridSlots, false)) {
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

    public AreaGuiHost host() {
        return host;
    }

    public boolean isActivator() {
        return host != null && host.isActivator();
    }

    public int guiTier() {
        return host == null ? 8 : host.guiTier();
    }

    public int activatorTarget() {
        return data.get(0);
    }

    public boolean activatorRayTrace() {
        return data.get(1) != 0;
    }

    public boolean activatorSneaking() {
        return data.get(2) != 0;
    }

    public long storedEnergy() {
        return readLong(3);
    }

    public long laserEnergy() {
        return readLong(7);
    }

    private long readLong(int start) {
        long value = 0L;
        for (int index = 0; index < 4; index++) {
            value |= (long)(data.get(start + index) & 0xFFFF) << (index * 16);
        }
        return value;
    }

    @Override
    public boolean stillValid(Player player) {
        return host != null && !host.isRemoved()
                && player.level().getBlockEntity(position) == host
                && player.distanceToSqr(position.getX() + 0.5D,
                        position.getY() + 0.5D, position.getZ() + 0.5D) <= 64.0D;
    }

    /** Original SlotMemory: a remembered one-item copy that is never consumed. */
    private static final class MemorySlot extends SlotItemHandler {
        private final AreaGuiHost host;

        private MemorySlot(AreaGuiHost host, int index, int x, int y) {
            super(host.inventory(), index, x, y);
            this.host = host;
        }

        private void remember(ItemStack carried) {
            host.inventory().setStackInSlot(getSlotIndex(), carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
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
}
