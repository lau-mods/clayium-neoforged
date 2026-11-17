package net.claustra01.clayium.logistics;

import net.claustra01.clayium.registry.LogisticsRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class BufferMenu extends AbstractContainerMenu {
    public static final int STORAGE_SLOTS = BufferBlockEntity.STORAGE_SLOTS;
    public static final int PAGE_SIZE = 30;

    private final BlockPos bufferPos;
    private final BufferBlockEntity buffer;
    private final ContainerData data;
    private int page;

    public BufferMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        this(containerId, playerInventory, bufferAt(playerInventory, pos), new SimpleContainerData(12));
    }

    public BufferMenu(int containerId, Inventory playerInventory, BufferBlockEntity buffer, ContainerData data) {
        super(LogisticsRegistration.BUFFER_MENU.get(), containerId);
        this.buffer = buffer;
        this.bufferPos = buffer == null ? BlockPos.ZERO : buffer.getBlockPos();
        this.data = data;
        if (buffer != null) {
            for (int index = 0; index < STORAGE_SLOTS; index++) {
                int slotPage = index / PAGE_SIZE;
                int slotIndex = index % PAGE_SIZE;
                addSlot(new SlotItemHandler(buffer.inventory(), index,
                        29 + slotIndex % 10 * 18, 69 + slotIndex / 10 * 18) {
                    @Override
                    public boolean isActive() {
                        return page == slotPage;
                    }
                });
            }
            addSlot(new SlotItemHandler(buffer.filterInventory(), 0, 7, 77));
            addSlot(new SlotItemHandler(buffer.exampleInventory(), 0, 7, 100));
        }
        addPlayerInventory(playerInventory);
        addDataSlots(data);
    }

    private static BufferBlockEntity bufferAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof BufferBlockEntity buffer ? buffer : null;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 29 + column * 18, 141 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 29 + column * 18, 199));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < STORAGE_SLOTS + 2) {
            if (!moveItemStackTo(moving, STORAGE_SLOTS + 2, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (moving.is(LogisticsRegistration.ITEM_FILTER.get())) {
            if (!moveItemStackTo(moving, STORAGE_SLOTS, STORAGE_SLOTS + 2, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(moving, STORAGE_SLOTS + 1, STORAGE_SLOTS + 2, false)
                && !moveItemStackTo(moving, 0, STORAGE_SLOTS, false)) {
            return ItemStack.EMPTY;
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
        return buffer != null
                && !buffer.isRemoved()
                && player.level().getBlockEntity(bufferPos) == buffer
                && player.distanceToSqr(bufferPos.getX() + 0.5D, bufferPos.getY() + 0.5D, bufferPos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id > 3) {
            return false;
        }
        page = id;
        return true;
    }

    public int page() {
        return page;
    }

    public BlockPos bufferPos() {
        return bufferPos;
    }

    public BufferBlockEntity buffer() {
        return buffer;
    }

    public int tier() {
        return data.get(0);
    }

    public BufferBlockEntity.RedstoneCondition redstoneCondition() {
        return enumValue(BufferBlockEntity.RedstoneCondition.values(), data.get(1));
    }

    public int transferLimit() {
        return data.get(2);
    }

    public net.claustra01.clayium.logistics.filter.ItemFilterSettings.Mode filterMode() {
        return enumValue(net.claustra01.clayium.logistics.filter.ItemFilterSettings.Mode.values(), data.get(3));
    }

    public BufferBlockEntity.FaceMode faceMode(Direction direction) {
        return enumValue(BufferBlockEntity.FaceMode.values(), data.get(4 + direction.get3DDataValue()));
    }

    public int ruleCount() {
        return data.get(10);
    }

    public net.claustra01.clayium.logistics.filter.ItemFilterRule.MatchType selectedRuleType() {
        return enumValue(net.claustra01.clayium.logistics.filter.ItemFilterRule.MatchType.values(), data.get(11));
    }

    private static <T> T enumValue(T[] values, int ordinal) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : values[0];
    }
}
