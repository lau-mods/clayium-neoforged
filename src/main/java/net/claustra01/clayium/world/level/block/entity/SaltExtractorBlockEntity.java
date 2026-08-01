/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.EnumMap;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.energy.ClayEnergyReceiver;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.SaltExtractorMenu;
import net.claustra01.clayium.world.level.block.SaltExtractorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/** Server-authoritative implementation of the original adjacent-water Salt Extractor. */
public final class SaltExtractorBlockEntity extends BaseContainerBlockEntity
        implements net.claustra01.clayium.machine.ConfigurableClayEnergyMachine {
    public static final int MAX_SLOTS = 13;
    private static final int PROGRESS_MAX = 100;
    private static final int ENERGY_PER_WORK = 30;
    private net.minecraft.core.NonNullList<ItemStack> items = net.minecraft.core.NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private final ClayEnergyStorage energy = new ClayEnergyStorage(1_000_000_000L, 1_000_000_000L, 1_000_000_000L);
    private final EnumMap<Direction,IItemHandler> handlers = new EnumMap<>(Direction.class);
    private final int[] insertionRoutes = {-1,-1,-1,0,-1,-1};
    private final int[] extractionRoutes = {-1,-1,0,-1,-1,-1};
    private final net.claustra01.clayium.logistics.SideConfiguration sideConfiguration;
    private int progress;
    private int waterCount;
    private long activeEnergy;
    private int stopReason;

    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int index) { return switch(index) {
            case 0 -> progress; case 1 -> PROGRESS_MAX;
            case 2 -> (int) energy.energyStored(); case 3 -> (int)(energy.energyStored() >>> 32);
            case 4 -> stopReason; case 5 -> tier();
            case 6 -> (int) activeEnergy; case 7 -> (int)(activeEnergy >>> 32); default -> 0; }; }
        @Override public void set(int index,int value) {}
        @Override public int getCount() { return 8; }
    };

    public SaltExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.SALT_EXTRACTOR_BLOCK_ENTITY.get(),pos,state);
        sideConfiguration = new net.claustra01.clayium.logistics.SideConfiguration(
                insertionRoutes, extractionRoutes, null, () -> 1, () -> 1);
        for(Direction side:Direction.values()) handlers.put(side,new SidedHandler(side));
    }

    @Override public net.claustra01.clayium.logistics.SideConfiguration sideConfiguration(){return sideConfiguration;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity ioOwner(){return this;}
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty(){return SaltExtractorBlock.PIPE;}
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty(){return SaltExtractorBlock.FACING;}
    @Override public void ioConfigurationChanged(){changed();}
    @Override public String insertionIcon(Direction side){return insertionRoute(side)>=0?"import_energy":"";}

    public static void serverTick(Level level, BlockPos pos, BlockState state, SaltExtractorBlockEntity be) { be.tickServer(); }
    private void tickServer() {
        waterCount=0;
        for(Direction side:Direction.values()) if(level.getFluidState(worldPosition.relative(side)).is(net.minecraft.tags.FluidTags.WATER)) waterCount++;
        activeEnergy=(long) efficiency()*ENERGY_PER_WORK;
        if(waterCount==0){stopReason=1;return;}
        if(!hasOutputSpace()){stopReason=2;return;}
        if(energy.extract(activeEnergy,true)!=activeEnergy && !consumeFuel()){stopReason=3;return;}
        energy.extract(activeEnergy,false);
        progress += efficiency()*waterCount;
        while(progress>=PROGRESS_MAX && hasOutputSpace()) { progress-=PROGRESS_MAX; insertSalt(); }
        stopReason=0;
        setChanged();
    }
    private boolean consumeFuel() {
        ItemStack fuel=getItem(energySlot()); long value=EnergeticClayFuel.value(fuel);
        if(value<=0 || energy.receive(value,true)!=value) return false;
        energy.receive(value,false); fuel.shrink(1); return true;
    }
    private boolean hasOutputSpace() {
        for(int slot=0;slot<outputSlots();slot++){ItemStack s=getItem(slot); if(s.isEmpty() || s.is(ClayiumRegistries.MATERIAL_ITEMS.get("salt_dust").get()) && s.getCount()<s.getMaxStackSize()) return true;}
        return false;
    }
    private void insertSalt() {
        for(int slot=0;slot<outputSlots();slot++){ItemStack s=getItem(slot); if(s.isEmpty()){setItem(slot,new ItemStack(ClayiumRegistries.MATERIAL_ITEMS.get("salt_dust").get()));return;} if(s.is(ClayiumRegistries.MATERIAL_ITEMS.get("salt_dust").get())&&s.getCount()<s.getMaxStackSize()){s.grow(1);return;}}
    }
    public int tier(){return getBlockState().getBlock() instanceof SaltExtractorBlock block?block.tier().progressionIndex():4;}
    public int outputSlots(){return switch(tier()){case 4->2;case 5->6;default->12;};}
    public int columns(){return switch(tier()){case 4->2;case 5->3;default->4;};}
    public int rows(){return outputSlots()/columns();}
    public int energySlot(){return outputSlots();}
    private int efficiency(){return switch(tier()){case 4->50;case 5->200;default->1000;};}
    public int waterCount(){return waterCount;}
    public IItemHandler itemHandler(Direction side){return handlers.get(side);}
    @Override public long receiveClayEnergy(long amount,boolean simulate){return energy.receive(amount,simulate);}

    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override protected AbstractContainerMenu createMenu(int id,Inventory inventory){return new SaltExtractorMenu(id,inventory,this,menuData);}
    @Override protected net.minecraft.core.NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(net.minecraft.core.NonNullList<ItemStack> stacks){items=stacks;}
    @Override public int getContainerSize(){return MAX_SLOTS;}
    @Override public boolean isEmpty(){return items.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int slot){return items.get(slot);}
    @Override public ItemStack removeItem(int slot,int count){ItemStack result=net.minecraft.world.ContainerHelper.removeItem(items,slot,count);if(!result.isEmpty())setChanged();return result;}
    @Override public ItemStack removeItemNoUpdate(int slot){return net.minecraft.world.ContainerHelper.takeItem(items,slot);}
    @Override public void setItem(int slot,ItemStack stack){items.set(slot,stack);stack.limitSize(getMaxStackSize(stack));setChanged();}
    @Override public void clearContent(){items.clear();}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return slot==energySlot()&&EnergeticClayFuel.isFuel(stack);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider p){super.saveAdditional(tag,p);net.minecraft.world.ContainerHelper.saveAllItems(tag,items,p);energy.save(tag);tag.putInt("Progress",progress);tag.putIntArray("InsertionRoutes",insertionRoutes);tag.putIntArray("ExtractionRoutes",extractionRoutes);}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider p){super.loadAdditional(tag,p);items.clear();net.minecraft.world.ContainerHelper.loadAllItems(tag,items,p);energy.load(tag);progress=Math.max(0,Math.min(PROGRESS_MAX-1,tag.getInt("Progress")));int[] in=tag.getIntArray("InsertionRoutes"),out=tag.getIntArray("ExtractionRoutes");sideConfiguration.replaceRoutes(in.length==6?in:insertionRoutes,out.length==6?out:extractionRoutes);}

    private void changed(){setChanged();if(level!=null){level.invalidateCapabilities(worldPosition);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}}

    private final class SidedHandler implements IItemHandler {
        private final Direction side; private SidedHandler(Direction side){this.side=side;}
        @Override public int getSlots(){return MAX_SLOTS;}
        @Override public ItemStack getStackInSlot(int slot){return getItem(slot);}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){if(insertionRoute(side)<0||slot!=energySlot()||!EnergeticClayFuel.isFuel(stack))return stack;ItemStack current=getItem(slot);if(!current.isEmpty()&&!ItemStack.isSameItemSameComponents(current,stack))return stack;int accepted=Math.min(stack.getCount(),stack.getMaxStackSize()-current.getCount());if(!simulate&&accepted>0){if(current.isEmpty())setItem(slot,stack.copyWithCount(accepted));else current.grow(accepted);}return accepted==stack.getCount()?ItemStack.EMPTY:stack.copyWithCount(stack.getCount()-accepted);}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){if(extractionRoute(side)<0||slot<0||slot>=outputSlots()||amount<=0)return ItemStack.EMPTY;ItemStack current=getItem(slot);int count=Math.min(amount,current.getCount());if(count<=0)return ItemStack.EMPTY;ItemStack result=current.copyWithCount(count);if(!simulate)removeItem(slot,count);return result;}
        @Override public int getSlotLimit(int slot){return 64;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return slot==energySlot()&&EnergeticClayFuel.isFuel(stack);}
    }
}
