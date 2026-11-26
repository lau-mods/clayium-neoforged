package net.claustra01.clayium.machine.waterwheel;

import net.claustra01.clayium.registry.WaterWheelRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class WaterWheelMenu extends AbstractContainerMenu {
    private final WaterWheelBlockEntity wheel;
    private final ContainerData data;

    public WaterWheelMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, (WaterWheelBlockEntity)inventory.player.level().getBlockEntity(pos),
                new SimpleContainerData(3));
    }

    public WaterWheelMenu(int id, Inventory inventory, WaterWheelBlockEntity wheel, ContainerData data) {
        super(WaterWheelRegistration.MENU.get(), id);
        this.wheel = wheel;
        this.data = data;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 44 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 102));
        }
        addDataSlots(data);
    }

    public int progress() { return data.get(0); }
    public int efficiency() { return data.get(1); }
    public int flowingWater() { return data.get(2); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        BlockPos pos = wheel.getBlockPos();
        return !wheel.isRemoved() && player.level().getBlockEntity(pos) == wheel
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
}
