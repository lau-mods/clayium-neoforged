/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.FluidBufferBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FluidBufferMenu extends AbstractContainerMenu {
    private final FluidBufferBlockEntity buffer;
    private int amount;
    private int capacity;

    public FluidBufferMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof FluidBufferBlockEntity value ? value : null);
    }

    public FluidBufferMenu(int id, Inventory inventory, FluidBufferBlockEntity buffer) {
        super(ClayiumRegistries.FLUID_BUFFER_MENU.get(), id);
        this.buffer = buffer;
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,142));
        addDataSlot(new DataSlot(){public int get(){return buffer==null?0:buffer.fluid().getAmount();} public void set(int v){amount=v;}});
        addDataSlot(new DataSlot(){public int get(){return buffer==null?0:buffer.capacity();} public void set(int v){capacity=v;}});
    }

    public int amount(){return amount;} public int capacity(){return capacity;}
    @Override public boolean stillValid(Player player){return buffer==null || player.distanceToSqr(buffer.getBlockPos().getCenter())<=64;}
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
}
