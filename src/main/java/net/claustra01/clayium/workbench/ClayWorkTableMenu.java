package net.claustra01.clayium.workbench;

import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class ClayWorkTableMenu extends AbstractContainerMenu {
    private static final int TABLE_SLOT_COUNT = 4;
    private final ClayWorkTableBlockEntity workTable;
    private final ContainerData data;

    public ClayWorkTableMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        this(containerId, playerInventory, blockEntity(playerInventory, buffer), new SimpleContainerData(2));
    }

    public ClayWorkTableMenu(int containerId, Inventory playerInventory, ClayWorkTableBlockEntity workTable) {
        this(containerId, playerInventory, workTable, workTable.menuData());
    }

    private ClayWorkTableMenu(
            int containerId,
            Inventory playerInventory,
            ClayWorkTableBlockEntity workTable,
            ContainerData data) {
        super(WorkbenchRegistration.CLAY_WORK_TABLE_MENU.get(), containerId);
        this.workTable = workTable;
        this.data = data;
        addSlot(new SlotItemHandler(workTable.inventory(), ClayWorkTableBlockEntity.INPUT_SLOT, 17, 30));
        addSlot(new SlotItemHandler(workTable.inventory(), ClayWorkTableBlockEntity.TOOL_SLOT, 80, 17));
        addSlot(outputSlot(workTable, ClayWorkTableBlockEntity.FIRST_OUTPUT_SLOT, 143, 30));
        addSlot(outputSlot(workTable, ClayWorkTableBlockEntity.SECOND_OUTPUT_SLOT, 143, 55));
        addPlayerInventory(playerInventory, 84);
        addDataSlots(data);
    }

    private static ClayWorkTableBlockEntity blockEntity(
            Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        if (playerInventory.player.level().getBlockEntity(buffer.readBlockPos())
                instanceof ClayWorkTableBlockEntity workTable) {
            return workTable;
        }
        throw new IllegalStateException("Clay work table menu opened without its block entity");
    }

    private Slot outputSlot(ClayWorkTableBlockEntity workTable, int slot, int x, int y) {
        return new SlotItemHandler(workTable.inventory(), slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    private void addPlayerInventory(Inventory inventory, int inventoryY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, inventoryY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, inventoryY + 58));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return id >= 1 && id <= 6 && stillValid(player) && workTable.press(id);
    }

    public int scaledProgress(int width) {
        int total = data.get(1);
        return total == 0 ? 0 : data.get(0) * width / total;
    }

    @Override
    public boolean stillValid(Player player) {
        return workTable.getLevel() != null
                && workTable.getLevel().getBlockEntity(workTable.getBlockPos()) == workTable
                && player.distanceToSqr(
                        workTable.getBlockPos().getX() + 0.5,
                        workTable.getBlockPos().getY() + 0.5,
                        workTable.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack result = original.copy();
        if (index < TABLE_SLOT_COUNT) {
            if (!moveItemStackTo(original, TABLE_SLOT_COUNT, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = slots.get(ClayWorkTableBlockEntity.TOOL_SLOT).mayPlace(original)
                    ? ClayWorkTableBlockEntity.TOOL_SLOT : ClayWorkTableBlockEntity.INPUT_SLOT;
            if (!moveItemStackTo(original, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
