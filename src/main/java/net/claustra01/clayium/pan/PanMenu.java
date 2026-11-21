package net.claustra01.clayium.pan;

import net.claustra01.clayium.registry.PanRegistration;
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

public final class PanMenu extends AbstractContainerMenu {
    private final PanBlockEntity pan;
    private final BlockPos position;
    private final ContainerData data;
    private final int machineSlots;

    public PanMenu(int containerId, Inventory inventory, BlockPos position) {
        this(containerId, inventory, panAt(inventory, position), new SimpleContainerData(14));
    }

    public PanMenu(int containerId, Inventory inventory, PanBlockEntity pan, ContainerData data) {
        super(PanRegistration.MENU.get(), containerId);
        this.pan = pan;
        this.position = pan.getBlockPos();
        this.data = data;
        if (pan.kind() == PanBlock.Kind.DUPLICATOR) {
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.SAMPLE_SLOT, 35, 35));
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.ANTIMATTER_SLOT, 35, 57));
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.OUTPUT_SLOT, 125, 35));
            addSlot(new SlotItemHandler(pan.inventory(), PanBlockEntity.FUEL_SLOT, 35, 79));
        }
        machineSlots = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 105 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 163));
        }
        addDataSlots(data);
    }

    private static PanBlockEntity panAt(Inventory inventory, BlockPos position) {
        BlockEntity entity = inventory.player.level().getBlockEntity(position);
        return (PanBlockEntity)entity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem().copy();
        ItemStack moving = slot.getItem();
        if (index < machineSlots) {
            if (!moveItemStackTo(moving, machineSlots, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            for (int target = 0; target < machineSlots; target++) {
                if (slots.get(target).mayPlace(moving)
                        && moveItemStackTo(moving, target, target + 1, false)) {
                    moved = true;
                    break;
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
        return !pan.isRemoved() && player.level().getBlockEntity(position) == pan
                && player.distanceToSqr(position.getX() + 0.5D, position.getY() + 0.5D,
                        position.getZ() + 0.5D) <= 64.0D;
    }

    public long progress() {
        return read(0, 2);
    }

    public long requiredProgress() {
        return read(2, 2);
    }

    public long requiredEnergy() {
        return read(4, 4);
    }

    public int candidates() {
        return data.get(8);
    }

    public int nodes() {
        return data.get(9);
    }

    public int adapters() {
        return data.get(10);
    }

    public PanBlock.Kind kind() {
        return PanBlock.Kind.values()[data.get(11)];
    }

    public int tier() {
        return data.get(12);
    }

    public boolean sampleReachable() {
        return data.get(13) != 0;
    }

    private long read(int start, int length) {
        long value = 0L;
        for (int index = 0; index < length; index++) {
            value |= (long)(data.get(start + index) & 0xFFFF) << index * 16;
        }
        return value;
    }
}
