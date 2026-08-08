/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.inventory;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.pan.PanCoreEntry;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.level.block.entity.PanCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Read-only PAN conversion browser. The potentially large table is sent only when opened. */
public final class PanCoreMenu extends AbstractContainerMenu {
    public static final int MACHINE_HEIGHT=160;
    private static final int MAX_ENTRIES=4_096;
    private final BlockPos corePos;
    private final List<PanCoreEntry> entries;
    private final int networkSize;
    private final int conversionCount;

    public PanCoreMenu(int id,Inventory inventory,RegistryFriendlyByteBuf buffer){
        this(id,inventory,buffer.readBlockPos(),readEntries(buffer),buffer.readVarInt(),buffer.readVarInt());
    }

    public PanCoreMenu(int id,Inventory inventory,PanCoreBlockEntity core){
        this(id,inventory,core.getBlockPos(),core.entries(),core.networkSize(),core.conversionCount());
    }

    private PanCoreMenu(int id,Inventory inventory,BlockPos pos,List<PanCoreEntry> entries,int networkSize,int conversionCount){
        super(ClayiumRegistries.PAN_CORE_MENU.get(),id);
        this.corePos=pos.immutable();this.entries=List.copyOf(entries);
        this.networkSize=Math.max(0,networkSize);this.conversionCount=Math.max(0,conversionCount);
        for(int row=0;row<3;row++)for(int column=0;column<9;column++)
            addSlot(new Slot(inventory,column+row*9+9,8+column*18,MACHINE_HEIGHT+12+row*18));
        for(int column=0;column<9;column++)addSlot(new Slot(inventory,column,8+column*18,MACHINE_HEIGHT+70));
    }

    public static void writeOpeningData(RegistryFriendlyByteBuf buffer,PanCoreBlockEntity core){
        core.refreshForMenu();
        buffer.writeBlockPos(core.getBlockPos());
        List<PanCoreEntry> entries=core.entries();buffer.writeVarInt(Math.min(entries.size(),MAX_ENTRIES));
        for(int index=0;index<Math.min(entries.size(),MAX_ENTRIES);index++)PanCoreEntry.encode(buffer,entries.get(index));
        buffer.writeVarInt(core.networkSize());buffer.writeVarInt(core.conversionCount());
    }

    private static List<PanCoreEntry> readEntries(RegistryFriendlyByteBuf buffer){
        int size=Math.min(buffer.readVarInt(),MAX_ENTRIES);List<PanCoreEntry> result=new ArrayList<>(size);
        for(int index=0;index<size;index++)result.add(PanCoreEntry.decode(buffer));
        return result;
    }

    public List<PanCoreEntry> entries(){return entries;}
    public int networkSize(){return networkSize;}
    public int conversionCount(){return conversionCount;}
    @Override public boolean stillValid(Player player){return player.distanceToSqr(corePos.getCenter())<=64.0D;}
    @Override public ItemStack quickMoveStack(Player player,int index){return ItemStack.EMPTY;}
}
