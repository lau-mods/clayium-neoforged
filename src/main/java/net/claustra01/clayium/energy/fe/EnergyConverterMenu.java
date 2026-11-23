package net.claustra01.clayium.energy.fe;

import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public final class EnergyConverterMenu extends AbstractContainerMenu {
    private final EnergyConverterBlockEntity converter;
    private final ContainerData data;

    public EnergyConverterMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, (EnergyConverterBlockEntity)inventory.player.level().getBlockEntity(pos),
                new SimpleContainerData(12));
    }

    public EnergyConverterMenu(int id, Inventory inventory, EnergyConverterBlockEntity converter, ContainerData data) {
        super(EnergyConverterRegistration.MENU.get(), id);
        this.converter = converter;
        this.data = data;
        addSlot(new SlotItemHandler(converter.fuelHandler(null), 0, 35, 53));
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

    public long storedCe() {
        long value = 0;
        for (int index = 0; index < 4; index++) value |= (long)(data.get(index) & 0xFFFF) << index * 16;
        return value;
    }

    public int storedFe() { return (data.get(4) & 0xFFFF) | (data.get(5) & 0xFFFF) << 16; }
    public int generatedFe() { return (data.get(6) & 0xFFFF) | (data.get(7) & 0xFFFF) << 16; }
    public int outputLimit() { return (data.get(8) & 0xFFFF) | (data.get(9) & 0xFFFF) << 16; }
    public int capacity() { return (data.get(10) & 0xFFFF) | (data.get(11) & 0xFFFF) << 16; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0 ? !moveItemStackTo(stack, 1, slots.size(), true)
                : !moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = converter.getBlockPos();
        return !converter.isRemoved() && player.level().getBlockEntity(pos) == converter
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
}
