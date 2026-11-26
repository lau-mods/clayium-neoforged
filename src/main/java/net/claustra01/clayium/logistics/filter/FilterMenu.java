package net.claustra01.clayium.logistics.filter;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Original ContainerItemFilterWhiteList (GUI 20): machine area 176x54, 5x2 SlotMemory at
 * {@code (43 + 18i, 18 + 18j)}, player inventory at y=54 with the held filter slot locked.
 */
public final class FilterMenu extends AbstractContainerMenu {
    public static final int MACHINE_HEIGHT = 54;
    public static final int COLUMNS = 5;
    public static final int ROWS = 2;
    public static final int BUTTON_TOGGLE_MODE = 0;

    private final Inventory inventory;
    private final int heldSlot;
    private final SimpleContainer memory = new SimpleContainer(ItemFilterSettings.SIZE);
    private final ContainerData data;

    public FilterMenu(int id, Inventory inventory) {
        super(LogisticsRegistration.FILTER_MENU.get(), id);
        this.inventory = inventory;
        this.heldSlot = inventory.selected;
        this.data = inventory.player.level().isClientSide ? new SimpleContainerData(1) : new ContainerData() {
            @Override
            public int get(int index) {
                return settings().mode().ordinal();
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
        addDataSlots(data);
        ItemFilterSettings settings = settings();
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int index = column + row * COLUMNS;
                memory.setItem(index, settings.item(index));
                addSlot(new MemorySlot(memory, index,
                        column * 18 + (176 - 18 * COLUMNS) / 2, row * 18 + 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, MACHINE_HEIGHT + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(column == heldSlot
                    ? new LockedSlot(inventory, column, 8 + column * 18, MACHINE_HEIGHT + 70)
                    : new Slot(inventory, column, 8 + column * 18, MACHINE_HEIGHT + 70));
        }
    }

    public ItemStack filterStack() {
        return inventory.getItem(heldSlot);
    }

    public ItemFilterSettings settings() {
        return filterStack().getOrDefault(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), ItemFilterSettings.EMPTY);
    }

    public ItemFilterSettings.Mode modeSelected() {
        return data.get(0) == ItemFilterSettings.Mode.BLACKLIST.ordinal()
                ? ItemFilterSettings.Mode.BLACKLIST : ItemFilterSettings.Mode.WHITELIST;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_TOGGLE_MODE || !stillValid(player)) {
            return false;
        }
        filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings().withMode(settings().mode().next()));
        broadcastChanges();
        return true;
    }

    /** SlotMemory: clicking stores a one-item copy of the carried stack without consuming it. */
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < ItemFilterSettings.SIZE) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                ItemStack carried = getCarried();
                ItemStack remembered = carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1);
                if (!player.level().isClientSide && stillValid(player)) {
                    ItemFilterSettings changed = settings().withItem(slotId, remembered);
                    if (FilterCopyValidator.valid(changed, 0, new int[] {0})) {
                        memory.setItem(slotId, remembered);
                        filterStack().set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), changed);
                    } else {
                        memory.setItem(slotId, settings().item(slotId));
                    }
                } else if (player.level().isClientSide) {
                    memory.setItem(slotId, remembered);
                }
                broadcastChanges();
            }
            return;
        }
        if (clickType == ClickType.SWAP && button == heldSlot) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return filterStack().is(LogisticsRegistration.ITEM_FILTER.get());
    }

    private static final class MemorySlot extends Slot {
        private MemorySlot(SimpleContainer container, int index, int x, int y) {
            super(container, index, x, y);
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

    private static final class LockedSlot extends Slot {
        private LockedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
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
