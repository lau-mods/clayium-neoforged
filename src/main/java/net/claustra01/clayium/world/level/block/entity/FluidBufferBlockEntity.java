/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.EnumMap;
import net.claustra01.clayium.data.IoMemory;
import net.claustra01.clayium.logistics.ConfigurableItemDevice;
import net.claustra01.clayium.logistics.RelativeFace;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.FluidBufferMenu;
import net.claustra01.clayium.world.level.block.FluidBufferBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class FluidBufferBlockEntity extends BlockEntity implements MenuProvider, ConfigurableItemDevice {
    private int[] insertionRoutes = {-1, -1, -1, 0, -1, -1};
    private int[] extractionRoutes = {-1, -1, -1, -1, -1, -1};
    private final EnumMap<Direction, IFluidHandler> handlers = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, BlockCapabilityCache<IFluidHandler, Direction>> neighborCaches = new EnumMap<>(Direction.class);
    private int cooldown;
    private final FluidTank tank = new FluidTank(capacityForState()) {
        @Override protected void onContentsChanged() { configurationChanged(); }
    };

    public FluidBufferBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumRegistries.FLUID_BUFFER_BLOCK_ENTITY.get(), pos, state);
        for (Direction side : Direction.values()) handlers.put(side, new SidedFluidHandler(side));
    }

    private int capacityForState() {
        int tier = getBlockState().getBlock() instanceof FluidBufferBlock block ? block.tier().progressionIndex() : 4;
        return switch (tier) { case 4 -> 128_000; case 5 -> 384_000; default -> 768_000; };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FluidBufferBlockEntity be) {
        if (++be.cooldown >= 4) { be.cooldown = 0; be.transfer(); }
    }

    private void transfer() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        for (Direction side : Direction.values()) {
            BlockCapabilityCache<IFluidHandler, Direction> cache = neighborCaches.computeIfAbsent(side, key ->
                    BlockCapabilityCache.create(Capabilities.FluidHandler.BLOCK, server, worldPosition.relative(key), key.getOpposite(), () -> !isRemoved(), () -> {}));
            IFluidHandler neighbor = cache.getCapability();
            if (neighbor == null) continue;
            if (insertionRoute(side) >= 0) {
                FluidStack simulated = neighbor.drain(1_000, IFluidHandler.FluidAction.SIMULATE);
                if (!simulated.isEmpty()) {
                    int accepted = tank.fill(simulated, IFluidHandler.FluidAction.SIMULATE);
                    if (accepted > 0) tank.fill(neighbor.drain(accepted, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
                }
            }
            if (extractionRoute(side) >= 0 && !tank.getFluid().isEmpty()) {
                FluidStack offered = tank.drain(1_000, IFluidHandler.FluidAction.SIMULATE);
                int accepted = neighbor.fill(offered, IFluidHandler.FluidAction.SIMULATE);
                if (accepted > 0) neighbor.fill(tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public IFluidHandler fluidHandler(Direction side) { return handlers.get(side); }
    public IFluidHandler unrestrictedFluidHandler() { return tank; }
    public FluidStack fluid() { return tank.getFluid().copy(); }
    public int capacity() { return tank.getCapacity(); }
    public boolean pipeConnects(Direction side) {
        if (level == null) return false;
        boolean ownActive = insertionRoute(side) >= 0 || extractionRoute(side) >= 0;
        var neighbor = level.getBlockEntity(worldPosition.relative(side));
        if (neighbor instanceof ConfigurableItemDevice device) {
            boolean neighborActive = device.insertionRoute(side.getOpposite()) >= 0
                    || device.extractionRoute(side.getOpposite()) >= 0;
            boolean neighborPassive = neighbor instanceof FluidBufferBlockEntity;
            return ownActive && (neighborActive || neighborPassive) || neighborActive;
        }
        return ownActive && level.getCapability(
                Capabilities.FluidHandler.BLOCK,
                worldPosition.relative(side),
                side.getOpposite()) != null;
    }

    @Override public int cycleInsertRoute(Direction d) { int i=relativeIndex(d); insertionRoutes[i]=insertionRoutes[i] < 0 ? 0 : -1; configurationChanged(); return insertionRoutes[i]; }
    @Override public int cycleExtractRoute(Direction d) { int i=relativeIndex(d); extractionRoutes[i]=extractionRoutes[i] < 0 ? 0 : -1; configurationChanged(); return extractionRoutes[i]; }
    @Override public int insertionRoute(Direction d) { return insertionRoutes[relativeIndex(d)]; }
    @Override public int extractionRoute(Direction d) { return extractionRoutes[relativeIndex(d)]; }
    @Override public boolean hasFilter(Direction d) { return false; }
    @Override public ItemStack filter(Direction d) { return ItemStack.EMPTY; }
    @Override public void setFilter(Direction d, ItemStack filter) {}
    @Override public boolean togglePipe() { if (level == null) return false; boolean value=!getBlockState().getValue(FluidBufferBlock.PIPE); level.setBlock(worldPosition,getBlockState().setValue(FluidBufferBlock.PIPE,value),3); configurationChanged(); return value; }
    @Override public boolean rotate(Direction clicked) { if(level==null || !clicked.getAxis().isHorizontal()) return false; level.setBlock(worldPosition,getBlockState().setValue(FluidBufferBlock.FACING,clicked),3); configurationChanged(); return true; }
    @Override public IoMemory saveIoMemory() { return IoMemory.of(insertionRoutes,extractionRoutes,getBlockState().getValue(FluidBufferBlock.PIPE),getBlockState().getValue(FluidBufferBlock.FACING).getName()); }
    @Override public void loadIoMemory(IoMemory m) {
        insertionRoutes=m.insertionRoutesOrDefault(insertionRoutes);
        extractionRoutes=m.extractionRoutesOrDefault(extractionRoutes);
        if (level != null) {
            Direction facing=Direction.byName(m.facing());
            BlockState state=getBlockState().setValue(FluidBufferBlock.PIPE,m.pipe());
            if (facing != null && facing.getAxis().isHorizontal()) state=state.setValue(FluidBufferBlock.FACING,facing);
            level.setBlock(worldPosition,state,3);
        }
        configurationChanged();
    }

    private int relativeIndex(Direction side) {
        return RelativeFace.index(getBlockState().getValue(FluidBufferBlock.FACING), side);
    }

    private void configurationChanged() { setChanged(); if(level!=null){ level.invalidateCapabilities(worldPosition); level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); } }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider p) { super.saveAdditional(tag,p); tag.put("Tank",tank.writeToNBT(p,new CompoundTag())); tag.putIntArray("InsertionRoutes",insertionRoutes); tag.putIntArray("ExtractionRoutes",extractionRoutes); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider p) { super.loadAdditional(tag,p); tank.readFromNBT(p,tag.getCompound("Tank")); int[] in=tag.getIntArray("InsertionRoutes"); int[] out=tag.getIntArray("ExtractionRoutes"); if(in.length==6) insertionRoutes=in; if(out.length==6) extractionRoutes=out; }
    @Override public CompoundTag getUpdateTag(HolderLookup.Provider p) { CompoundTag tag=super.getUpdateTag(p); saveAdditional(tag,p); return tag; }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public Component getDisplayName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) { return new FluidBufferMenu(id,inv,this); }

    private final class SidedFluidHandler implements IFluidHandler {
        private final Direction side; private SidedFluidHandler(Direction side){this.side=side;}
        @Override public int getTanks(){return 1;} @Override public FluidStack getFluidInTank(int i){return tank.getFluidInTank(i);} @Override public int getTankCapacity(int i){return tank.getTankCapacity(i);}
        @Override public boolean isFluidValid(int i,FluidStack stack){return tank.isFluidValid(i,stack);}
        @Override public int fill(FluidStack stack,FluidAction action){return tank.fill(stack,action);}
        @Override public FluidStack drain(FluidStack stack,FluidAction action){return tank.drain(stack,action);}
        @Override public FluidStack drain(int amount,FluidAction action){return tank.drain(amount,action);}
    }
}
