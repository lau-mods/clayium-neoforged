package net.claustra01.clayium.fluid;

import net.claustra01.clayium.registry.FluidRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class FluidTransportMenu extends AbstractContainerMenu {
    private final BlockPos transportPos;
    private final FluidTransportBlockEntity transport;
    private final ContainerData data;

    public FluidTransportMenu(int containerId, Inventory inventory, BlockPos pos) {
        this(containerId, inventory, transportAt(inventory, pos), new SimpleContainerData(19));
    }

    public FluidTransportMenu(
            int containerId,
            Inventory inventory,
            FluidTransportBlockEntity transport,
            ContainerData data) {
        super(FluidRegistration.MENU.get(), containerId);
        this.transport = transport;
        transportPos = transport == null ? BlockPos.ZERO : transport.getBlockPos();
        this.data = data;
        if (transport != null) {
            addSlot(new SlotItemHandler(transport.filterInventory(), 0, 152, 54));
        }
        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    private static FluidTransportBlockEntity transportAt(Inventory inventory, BlockPos pos) {
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        return blockEntity instanceof FluidTransportBlockEntity transport ? transport : null;
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 113 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 171));
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
        if (index == 0) {
            if (!moveItemStackTo(moving, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (moving.is(FluidRegistration.FLUID_FILTER.get())) {
            if (!moveItemStackTo(moving, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
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
        return transport != null
                && !transport.isRemoved()
                && player.level().getBlockEntity(transportPos) == transport
                && player.distanceToSqr(
                        transportPos.getX() + 0.5D,
                        transportPos.getY() + 0.5D,
                        transportPos.getZ() + 0.5D) <= 64.0D;
    }

    public BlockPos transportPos() {
        return transportPos;
    }

    public FluidTransportBlockEntity transport() {
        return transport;
    }

    public int tier() {
        return data.get(0);
    }

    public FluidStack fluid() {
        int id = data.get(1);
        return id == 0 || amount() == 0
                ? FluidStack.EMPTY : new FluidStack(BuiltInRegistries.FLUID.byId(id - 1), amount());
    }

    public int amount() {
        return combined(2);
    }

    public int capacity() {
        return combined(4);
    }

    public int pendingAmount() {
        return combined(6);
    }

    public int transferPerTick() {
        return combined(8);
    }

    public FluidTransportBlockEntity.RedstoneCondition redstoneCondition() {
        return enumValue(FluidTransportBlockEntity.RedstoneCondition.values(), data.get(10));
    }

    public FluidTransportBlockEntity.FaceMode faceMode(Direction direction) {
        return enumValue(FluidTransportBlockEntity.FaceMode.values(), data.get(11 + direction.get3DDataValue()));
    }

    public boolean filterBlacklist() {
        return data.get(17) != 0;
    }

    public String filteredFluidName() {
        int id = data.get(18);
        return id == 0 ? "*" : new FluidStack(BuiltInRegistries.FLUID.byId(id - 1), 1).getHoverName().getString();
    }

    private static <T> T enumValue(T[] values, int ordinal) {
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : values[0];
    }

    private int combined(int index) {
        return (data.get(index) & 0xFFFF) | ((data.get(index + 1) & 0xFFFF) << 16);
    }
}
