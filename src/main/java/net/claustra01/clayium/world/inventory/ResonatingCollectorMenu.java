/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.ResonatingCollectorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public final class ResonatingCollectorMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    public ResonatingCollectorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, inventory.player.level().getBlockEntity(data.readBlockPos())
                instanceof ResonatingCollectorBlockEntity value ? value
                : new SimpleContainer(ResonatingCollectorBlockEntity.SLOT_COUNT),new SimpleContainerData(2));
    }
    public ResonatingCollectorMenu(int id, Inventory inventory, Container container,ContainerData data) {
        super(ClayiumRegistries.RESONATING_COLLECTOR_MENU.get(), id);
        this.container = container;
        this.data=data;
        checkContainerSize(container, 9);
        checkContainerDataCount(data,2);
        container.startOpen(inventory.player);
        for (int row=0; row<3; row++) for (int column=0; column<3; column++) {
            addSlot(new Slot(container, column + row*3, 62 + column*18, 18 + row*18) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
            });
        }
        for (int row=0; row<3; row++) for (int column=0; column<9; column++)
            addSlot(new Slot(inventory, column+row*9+9, 8+column*18, 98+row*18));
        for (int column=0; column<9; column++) addSlot(new Slot(inventory,column,8+column*18,156));
        addDataSlots(data);
    }
    public double resonance(){return data.get(1)/1_000.0D;}
    public double progressRatio(){return data.get(0)/10_000.0D;}
    @Override public boolean stillValid(Player player) { return container.stillValid(player); }
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !slots.get(index).hasItem()) return ItemStack.EMPTY;
        Slot slot=slots.get(index); ItemStack stack=slot.getItem(); ItemStack copy=stack.copy();
        if (index < 9) { if (!moveItemStackTo(stack,9,slots.size(),true)) return ItemStack.EMPTY; }
        else return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }
    @Override public void removed(Player player) { super.removed(player); container.stopOpen(player); }
}
