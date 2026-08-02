/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.SaltExtractorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SaltExtractorMenu extends AbstractContainerMenu {
    private final Container container; private final ContainerData data;
    private final int tier,outputs,columns,rows,machineHeight,machineSlots;
    public SaltExtractorMenu(int id,Inventory inv,RegistryFriendlyByteBuf buf){this(id,inv,inv.player.level().getBlockEntity(buf.readBlockPos()) instanceof SaltExtractorBlockEntity be?be:new SimpleContainer(SaltExtractorBlockEntity.MAX_SLOTS),new SimpleContainerData(8));}
    public SaltExtractorMenu(int id,Inventory inv,Container container,ContainerData data){
        super(ClayiumRegistries.SALT_EXTRACTOR_MENU.get(),id);this.container=container;this.data=data;container.startOpen(inv.player);
        SaltExtractorBlockEntity be=container instanceof SaltExtractorBlockEntity value?value:null;tier=be==null?4:be.tier();outputs=be==null?2:be.outputSlots();columns=be==null?2:be.columns();rows=outputs/columns;machineHeight=rows*18+42;
        int x=(176-columns*18)/2+1;for(int slot=0;slot<outputs;slot++)addSlot(new Slot(container,slot,x+slot%columns*18,18+slot/columns*18){@Override public boolean mayPlace(ItemStack stack){return false;}});
        int energySlot=outputs;addSlot(new Slot(container,energySlot,151,machineHeight-22){
            @Override public boolean mayPlace(ItemStack stack){return EnergeticClayFuel.isFuel(stack);}
            @Override public int getMaxStackSize(){return container instanceof SaltExtractorBlockEntity value?value.energySlotLimit():1;}
            @Override public int getMaxStackSize(ItemStack stack){return Math.min(stack.getMaxStackSize(),getMaxStackSize());}
        });machineSlots=slots.size();
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(inv,col+row*9+9,8+col*18,machineHeight+12+row*18));for(int col=0;col<9;col++)addSlot(new Slot(inv,col,8+col*18,machineHeight+70));addDataSlots(data);
    }
    public int tier(){return tier;}public int outputs(){return outputs;}public int columns(){return columns;}public int rows(){return rows;}public int machineHeight(){return machineHeight;}public int progress(){return data.get(0);}public int totalProgress(){return data.get(1);}public long energy(){return Integer.toUnsignedLong(data.get(2))|(Integer.toUnsignedLong(data.get(3))<<32);}public int stopReason(){return data.get(4);}public long energyPerTick(){return Integer.toUnsignedLong(data.get(6))|(Integer.toUnsignedLong(data.get(7))<<32);}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size()||!slots.get(index).hasItem())return ItemStack.EMPTY;Slot slot=slots.get(index);ItemStack stack=slot.getItem(),copy=stack.copy();if(index<machineSlots){if(!moveItemStackTo(stack,machineSlots,slots.size(),true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,outputs,outputs+1,false))return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
    @Override public void removed(Player player){super.removed(player);container.stopOpen(player);}
}
