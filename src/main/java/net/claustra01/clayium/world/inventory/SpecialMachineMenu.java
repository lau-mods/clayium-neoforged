/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import net.claustra01.clayium.machine.SpecialMachineKind;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.SpecialMachineBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Original slot layouts for the three special machines available through Precision tier. */
public final class SpecialMachineMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;
    private final SpecialMachineKind kind;
    private final int tier;
    private final int machineSlots;
    private final int machineHeight;

    public SpecialMachineMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory,
                inventory.player.level().getBlockEntity(buffer.readBlockPos()) instanceof SpecialMachineBlockEntity machine
                        ? machine : new SimpleContainer(SpecialMachineBlockEntity.MAX_SLOTS),
                new SimpleContainerData(4));
    }

    public SpecialMachineMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ClayiumRegistries.SPECIAL_MACHINE_MENU.get(), id);
        this.container = container;
        this.data = data;
        SpecialMachineBlockEntity machine = container instanceof SpecialMachineBlockEntity value ? value : null;
        this.kind = machine == null ? SpecialMachineKind.AUTO_CLAY_CONDENSER : machine.kind();
        this.tier = machine == null ? 5 : machine.tier();
        this.machineHeight = kind == SpecialMachineKind.AUTO_CLAY_CONDENSER ? 104 : 84;
        container.startOpen(inventory.player);
        switch (kind) {
            case AUTO_CLAY_CONDENSER -> condenserSlots();
            case AUTO_CRAFTER -> crafterSlots();
            case CHEMICAL_METAL_SEPARATOR -> separatorSlots();
        }
        machineSlots = slots.size();
        for (int row=0;row<3;row++) for(int column=0;column<9;column++)
            addSlot(new Slot(inventory,column+row*9+9,8+column*18,machineHeight+12+row*18));
        for(int column=0;column<9;column++) addSlot(new Slot(inventory,column,8+column*18,machineHeight+70));
        addDataSlots(data);
    }

    private void condenserSlots(){
        for(int row=0;row<4;row++)for(int column=0;column<5;column++)
            addSlot(new RestrictedSlot(container,column+row*5,44+column*18,18+row*18));
        addSlot(new GhostSlot(container,21,152,18));
    }
    private void crafterSlots(){
        for(int row=0;row<3;row++)for(int column=0;column<3;column++)
            addSlot(new RestrictedSlot(container,column+row*3,62+column*18,18+row*18));
        for(int row=0;row<3;row++)for(int column=0;column<3;column++)
            addSlot(new GhostSlot(container,15+column+row*3,5+column*18,18+row*18));
        for(int row=0;row<3;row++)for(int column=0;column<2;column++)
            addSlot(new OutputSlot(container,9+column+row*2,135+column*18,18+row*18));
        if(tier>=6)addSlot(new RestrictedSlot(container,33,8,66));
    }
    private void separatorSlots(){
        addSlot(new RestrictedSlot(container,0,8,32));
        for(int row=0;row<2;row++)for(int column=0;column<8;column++)
            addSlot(new OutputSlot(container,1+column+row*8,26+column*18,18+row*18));
        addSlot(new RestrictedSlot(container,18,152,54));
    }

    @Override public boolean stillValid(Player player){return container.stillValid(player);}
    @Override public void clicked(int slotId, int button, ClickType clickType, Player player) {
        int target = -1;
        if (kind == SpecialMachineKind.AUTO_CLAY_CONDENSER && slotId == 20) target = 21;
        if (kind == SpecialMachineKind.AUTO_CRAFTER && slotId >= 9 && slotId < 18) target = 15 + slotId - 9;
        if (target >= 0) {
            ItemStack carried = getCarried();
            ItemStack value = carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1);
            if (kind == SpecialMachineKind.AUTO_CLAY_CONDENSER
                    && !value.isEmpty() && SpecialMachineBlockEntity.clayLevel(value) < 0) return;
            container.setItem(target, value);
            container.setChanged();
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size()||!slots.get(index).hasItem())return ItemStack.EMPTY;
        Slot slot=slots.get(index);ItemStack stack=slot.getItem();ItemStack copy=stack.copy();
        if(index<machineSlots){if(!moveItemStackTo(stack,machineSlots,slots.size(),true))return ItemStack.EMPTY;}
        else {boolean moved=false;for(int i=0;i<machineSlots&&!moved;i++)if(slots.get(i).mayPlace(stack))moved=moveItemStackTo(stack,i,i+1,false);if(!moved)return ItemStack.EMPTY;}
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        return copy;
    }
    @Override public void removed(Player player){super.removed(player);container.stopOpen(player);}
    public SpecialMachineKind kind(){return kind;} public int tier(){return tier;} public int machineHeight(){return machineHeight;}
    public int progress(){return data.get(0);} public int totalProgress(){return data.get(1);}

    private static final class RestrictedSlot extends Slot {
        RestrictedSlot(Container container,int index,int x,int y){super(container,index,x,y);}
        @Override public boolean mayPlace(ItemStack stack){return container.canPlaceItem(getContainerSlot(),stack);}
    }
    private static final class OutputSlot extends Slot {
        OutputSlot(Container container,int index,int x,int y){super(container,index,x,y);}
        @Override public boolean mayPlace(ItemStack stack){return false;}
    }
    private static final class GhostSlot extends Slot {
        GhostSlot(Container container,int index,int x,int y){super(container,index,x,y);}
        @Override public boolean mayPlace(ItemStack stack){return false;}
        @Override public boolean mayPickup(Player player){return false;}
    }
}
