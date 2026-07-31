/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.EnumMap;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.CobblestoneGeneratorMenu;
import net.claustra01.clayium.world.level.block.CobblestoneGeneratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.items.IItemHandler;

/** Water/lava environmental generator with original tier rates and output dimensions. */
public final class CobblestoneGeneratorBlockEntity extends BaseContainerBlockEntity implements ConfigurableItemDevice {
    public static final int MAX_SLOTS = 12;
    private static final int PROGRESS_MAX = 100;
    private NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
    private int progress;
    private final int[] insertionRoutes = {-1,-1,-1,-1,-1,-1};
    private final int[] extractionRoutes = {-1,-1,0,-1,-1,-1};
    private final net.claustra01.clayium.logistics.SideConfiguration sideConfiguration;
    private final EnumMap<Direction, IItemHandler> handlers = new EnumMap<>(Direction.class);

    public CobblestoneGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.COBBLESTONE_GENERATOR_BLOCK_ENTITY.get(), pos, state);
        sideConfiguration = new net.claustra01.clayium.logistics.SideConfiguration(
                insertionRoutes, extractionRoutes, null, () -> 0, () -> 1);
        for (Direction direction : Direction.values()) handlers.put(direction, new OutputHandler(direction));
    }

    @Override public net.claustra01.clayium.logistics.SideConfiguration sideConfiguration(){return sideConfiguration;}
    @Override public net.minecraft.world.level.block.entity.BlockEntity ioOwner(){return this;}
    @Override public net.minecraft.world.level.block.state.properties.BooleanProperty ioPipeProperty(){return CobblestoneGeneratorBlock.PIPE;}
    @Override public net.minecraft.world.level.block.state.properties.DirectionProperty ioFacingProperty(){return CobblestoneGeneratorBlock.FACING;}
    @Override public void ioConfigurationChanged(){configurationChanged();}

    public static void serverTick(Level level, BlockPos pos, BlockState state, CobblestoneGeneratorBlockEntity generator) {
        if (!generator.hasWaterAndLava() || !generator.canInsertCobblestone()) return;
        generator.progress += generator.efficiency();
        boolean changed = false;
        while (generator.progress >= PROGRESS_MAX && generator.canInsertCobblestone()) {
            generator.progress -= PROGRESS_MAX;
            generator.insertCobblestone();
            changed = true;
        }
        if (changed || generator.progress > 0) generator.setChanged();
    }

    private boolean hasWaterAndLava() {
        if (level == null) return false;
        boolean water = false, lava = false;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            var fluid = level.getFluidState(worldPosition.relative(side));
            water |= fluid.is(Fluids.WATER);
            lava |= fluid.is(Fluids.LAVA);
        }
        return water && lava;
    }

    private int efficiency() { return switch (tier()) { case 1 -> 2; case 2 -> 5; case 3 -> 15; case 4 -> 50; case 5 -> 200; default -> 1000; }; }
    public int tier() { return getBlockState().getBlock() instanceof CobblestoneGeneratorBlock block ? block.tier().progressionIndex() : 1; }
    public int activeSlots() { return switch (tier()) { case 4 -> 2; case 5 -> 6; case 6 -> 12; default -> 1; }; }
    public int columns() { return switch (tier()) { case 4 -> 2; case 5 -> 3; case 6 -> 4; default -> 1; }; }
    public int rows() { return Math.max(1, activeSlots() / columns()); }
    public int progress() { return progress; }

    private boolean canInsertCobblestone() {
        for (int slot = 0; slot < activeSlots(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty() || stack.is(Items.COBBLESTONE) && stack.getCount() < stack.getMaxStackSize()) return true;
        }
        return false;
    }

    private void insertCobblestone() {
        for (int slot = 0; slot < activeSlots(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) { items.set(slot, new ItemStack(Items.COBBLESTONE)); return; }
            if (stack.is(Items.COBBLESTONE) && stack.getCount() < stack.getMaxStackSize()) { stack.grow(1); return; }
        }
    }

    public IItemHandler itemHandler(Direction side) { return handlers.get(side); }
    private void configurationChanged(){setChanged();if(level!=null){level.invalidateCapabilities(worldPosition);level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}}

    @Override protected Component getDefaultName(){return Component.translatable(getBlockState().getBlock().getDescriptionId());}
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory){return new CobblestoneGeneratorMenu(id,inventory,this);}
    @Override protected NonNullList<ItemStack> getItems(){return items;}
    @Override protected void setItems(NonNullList<ItemStack> stacks){items=stacks;}
    @Override public int getContainerSize(){return MAX_SLOTS;}
    @Override public boolean canPlaceItem(int slot,ItemStack stack){return false;}
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){super.loadAdditional(tag,registries);items=NonNullList.withSize(MAX_SLOTS,ItemStack.EMPTY);ContainerHelper.loadAllItems(tag,items,registries);progress=Math.max(0,Math.min(PROGRESS_MAX-1,tag.getInt("Progress")));int[] saved=tag.getIntArray("ExtractionRoutes");sideConfiguration.replaceRoutes(insertionRoutes,saved.length==6?saved:extractionRoutes);}
    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){super.saveAdditional(tag,registries);ContainerHelper.saveAllItems(tag,items,registries);tag.putInt("Progress",progress);tag.putIntArray("ExtractionRoutes",extractionRoutes);}
    @Override public Packet<ClientGamePacketListener> getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider registries){return saveCustomOnly(registries);}

    private final class OutputHandler implements IItemHandler {
        private final Direction side; private OutputHandler(Direction side){this.side=side;}
        @Override public int getSlots(){return activeSlots();}
        @Override public ItemStack getStackInSlot(int slot){return slot>=0&&slot<activeSlots()?getItem(slot):ItemStack.EMPTY;}
        @Override public ItemStack insertItem(int slot,ItemStack stack,boolean simulate){return stack;}
        @Override public ItemStack extractItem(int slot,int amount,boolean simulate){if(extractionRoute(side)<0||slot<0||slot>=activeSlots()||amount<=0)return ItemStack.EMPTY;ItemStack stack=getItem(slot);int count=Math.min(amount,stack.getCount());if(count<=0)return ItemStack.EMPTY;ItemStack result=stack.copyWithCount(count);if(!simulate)removeItem(slot,count);return result;}
        @Override public int getSlotLimit(int slot){return 64;}
        @Override public boolean isItemValid(int slot,ItemStack stack){return false;}
    }
}
