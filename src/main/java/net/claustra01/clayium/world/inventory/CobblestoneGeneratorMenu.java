/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.CobblestoneGeneratorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class CobblestoneGeneratorMenu extends AbstractContainerMenu {
    private final Container container;
    private final int tier, columns, rows, machineHeight, machineSlots;
    private int progress;

    public CobblestoneGeneratorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        this(id, inventory, inventory.player.level().getBlockEntity(data.readBlockPos()) instanceof CobblestoneGeneratorBlockEntity generator
                ? generator : new SimpleContainer(CobblestoneGeneratorBlockEntity.MAX_SLOTS));
    }

    public CobblestoneGeneratorMenu(int id, Inventory inventory, Container container) {
        super(ClayiumRegistries.COBBLESTONE_GENERATOR_MENU.get(), id);
        this.container=container;
        CobblestoneGeneratorBlockEntity generator=container instanceof CobblestoneGeneratorBlockEntity value?value:null;
        tier=generator==null?1:generator.tier();columns=generator==null?1:generator.columns();rows=generator==null?1:generator.rows();
        machineHeight=rows*18+18;
        int offsetX=(176-columns*18)/2+1;
        for(int slot=0;slot<columns*rows;slot++)addSlot(new Slot(container,slot,offsetX+slot%columns*18,18+slot/columns*18){@Override public boolean mayPlace(ItemStack stack){return false;}});
        machineSlots=slots.size();
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)addSlot(new Slot(inventory,column+row*9+9,8+column*18,machineHeight+12+row*18));
        for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,8+column*18,machineHeight+70));
        addDataSlot(new DataSlot(){@Override public int get(){return generator==null?progress:generator.progress();}@Override public void set(int value){progress=value;}});
    }

    public int tier(){return tier;} public int columns(){return columns;} public int rows(){return rows;} public int machineHeight(){return machineHeight;} public int progress(){return progress;}
    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public ItemStack quickMoveStack(Player player,int index){if(index<0||index>=slots.size()||!slots.get(index).hasItem())return ItemStack.EMPTY;Slot slot=slots.get(index);ItemStack stack=slot.getItem(),copy=stack.copy();if(index<machineSlots){if(!moveItemStackTo(stack,machineSlots,slots.size(),true))return ItemStack.EMPTY;}else return ItemStack.EMPTY;if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();return copy;}
}
