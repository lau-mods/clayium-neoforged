package net.claustra01.clayium.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Original ContainerNormalInventory for single-page hosts (cobblestone generator, CA collector). */
public final class NormalInventoryMenu extends AbstractContainerMenu {
    private final NormalInventoryHost host;
    private final BlockPos pos;
    private final NormalInventoryLayout layout;
    private final int extraHeight;
    private final int storageSlots;
    private final ContainerData data;

    public NormalInventoryMenu(MenuType<?> type, int id, Inventory inventory, BlockPos pos) {
        this(type, id, inventory, inventory.player.level().getBlockEntity(pos) instanceof NormalInventoryHost host
                ? host : null);
    }

    public NormalInventoryMenu(MenuType<?> type, int id, Inventory playerInventory, NormalInventoryHost host) {
        super(type, id);
        this.host = host;
        this.pos = host == null ? BlockPos.ZERO : host.getBlockPos();
        this.layout = host == null ? new NormalInventoryLayout(1, 1, 1) : host.normalLayout();
        this.extraHeight = host == null ? 0 : host.extraMachineHeight();
        this.storageSlots = host == null ? 0 : layout.columns() * layout.rows();
        this.data = playerInventory.player.level().isClientSide ? new SimpleContainerData(2) : new ContainerData() {
            @Override public int get(int index) {
                int bits = Float.floatToIntBits((float)host.displayedResonance());
                return index == 0 ? bits & 0xFFFF : bits >>> 16 & 0xFFFF;
            }
            @Override public void set(int index, int value) { }
            @Override public int getCount() { return 2; }
        };
        addDataSlots(data);
        for (int row = 0; row < layout.rows() && host != null; row++) {
            for (int column = 0; column < layout.columns(); column++) {
                addSlot(new SlotItemHandler(host.normalInventory(), column + row * layout.columns(),
                        layout.slotX(column), layout.slotY(row)));
            }
        }
        int playerX = layout.playerX();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9,
                        playerX + 8 + column * 18, machineHeight() + 12 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, playerX + 8 + column * 18, machineHeight() + 70));
        }
    }

    public NormalInventoryLayout layout() {
        return layout;
    }

    public int machineHeight() {
        return layout.machineHeight() + extraHeight;
    }

    public double resonance() {
        return Float.intBitsToFloat((data.get(0) & 0xFFFF) | (data.get(1) & 0xFFFF) << 16);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < storageSlots) {
            if (!moveItemStackTo(moving, storageSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(moving, 0, storageSlots, false)) {
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
        return host != null && !host.isRemoved() && player.level().getBlockEntity(pos) == host
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}
