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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidBufferMenu extends AbstractContainerMenu {
    private final FluidBufferBlockEntity buffer;
    private int amountLow;
    private int amountHigh;
    private int capacityLow;
    private int capacityHigh;
    private int fluidId = BuiltInRegistries.FLUID.getId(Fluids.EMPTY);

    public FluidBufferMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof FluidBufferBlockEntity value ? value : null);
    }

    public FluidBufferMenu(int id, Inventory inventory, FluidBufferBlockEntity buffer) {
        super(ClayiumRegistries.FLUID_BUFFER_MENU.get(), id);
        this.buffer = buffer;
        for(int row=0;row<3;row++) for(int col=0;col<9;col++) addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++) addSlot(new Slot(inventory,col,8+col*18,142));
        addDataSlot(lowWord(() -> buffer == null ? 0 : buffer.fluid().getAmount(), value -> amountLow = value));
        addDataSlot(highWord(() -> buffer == null ? 0 : buffer.fluid().getAmount(), value -> amountHigh = value));
        addDataSlot(lowWord(() -> buffer == null ? 0 : buffer.capacity(), value -> capacityLow = value));
        addDataSlot(highWord(() -> buffer == null ? 0 : buffer.capacity(), value -> capacityHigh = value));
        addDataSlot(new DataSlot(){public int get(){return buffer==null?BuiltInRegistries.FLUID.getId(Fluids.EMPTY):BuiltInRegistries.FLUID.getId(buffer.fluid().getFluid());} public void set(int v){fluidId=v;}});
    }

    private static DataSlot lowWord(java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
        return new DataSlot() {
            @Override public int get() { return getter.getAsInt() & 0xffff; }
            @Override public void set(int value) { setter.accept(value & 0xffff); }
        };
    }

    private static DataSlot highWord(java.util.function.IntSupplier getter, java.util.function.IntConsumer setter) {
        return new DataSlot() {
            @Override public int get() { return getter.getAsInt() >>> 16 & 0xffff; }
            @Override public void set(int value) { setter.accept(value & 0xffff); }
        };
    }

    public int amount(){return amountLow | amountHigh << 16;} public int capacity(){return capacityLow | capacityHigh << 16;}
    public FluidStack fluid(){int amount=amount();return amount <= 0 ? FluidStack.EMPTY : new FluidStack(BuiltInRegistries.FLUID.byId(fluidId), amount);}
    @Override public boolean stillValid(Player player){return buffer==null || player.distanceToSqr(buffer.getBlockPos().getCenter())<=64;}
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
}
