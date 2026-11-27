package net.claustra01.clayium.blockentity;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Clay Interface (T5..T13): mirrors a linked core {@link ClayContainerBlockEntity}'s slots and fluid handler
 * while keeping independent side IO routes and filters.
 */
public class ClayInterfaceBlockEntity extends TieredClayContainerBlockEntity implements ISynchronizedInterface {
    protected int coreOffsetX = 0;
    protected int coreOffsetY = 0;
    protected int coreOffsetZ = 0;
    protected boolean synced = false;
    @Nullable
    protected ClayContainerBlockEntity core = null;
    private boolean syncLoopGuard = false;
    public boolean acceptCoordChanger = false;
    @Nullable
    protected ResourceKey<Level> coreDimension = null;

    public ClayInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CLAY_INTERFACE.get(), pos, state, 1);
        ensureTierInitialized();
    }

    @Override
    public boolean setCoreBlockOffset(int dx, int dy, int dz) {
        markForSync();
        this.coreOffsetX = dx;
        this.coreOffsetY = dy;
        this.coreOffsetZ = dz;
        return syncCoreBlock();
    }

    @Override
    public boolean setCoreBlockDimension(ResourceKey<Level> dimension) {
        if (this.level != null && this.level.dimension().equals(dimension)) {
            this.coreDimension = null;
            return true;
        }
        this.coreDimension = dimension;
        return getCoreLevel() != null;
    }

    @Override
    public BlockPos getCoreBlockOffset() {
        return new BlockPos(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ);
    }

    @Override
    public boolean isSynced() {
        return this.synced;
    }

    @Override
    public boolean acceptsCoordChanger() {
        return this.acceptCoordChanger;
    }

    @Nullable
    public ClayContainerBlockEntity getCore() {
        return this.core;
    }

    @Nullable
    protected Level getCoreLevel() {
        if (this.level == null) {
            return null;
        }
        if (this.coreDimension == null || this.level.dimension().equals(this.coreDimension)) {
            return this.level;
        }
        if (this.level.getServer() != null) {
            return this.level.getServer().getLevel(this.coreDimension);
        }
        return null;
    }

    public boolean syncCoreBlock() {
        Level targetLevel = getCoreLevel();
        if (targetLevel != null
                && (this.coreOffsetX != 0 || this.coreOffsetY != 0 || this.coreOffsetZ != 0 || this.coreDimension != null)) {
            BlockPos targetPos = this.worldPosition.offset(this.coreOffsetX, this.coreOffsetY, this.coreOffsetZ);
            BlockEntity te = targetLevel.getBlockEntity(targetPos);
            if (te instanceof ClayContainerBlockEntity cc && cc != this && cc.acceptInterfaceSync()) {
                this.core = cc;
                this.listSlotsInsert.clear();
                this.listSlotsInsert.addAll(cc.listSlotsInsert);
                this.listSlotsExtract.clear();
                this.listSlotsExtract.addAll(cc.listSlotsExtract);
                for (int i = 0; i < 6; i++) {
                    if (this.insertRoutes[i] >= this.listSlotsInsert.size()) {
                        this.insertRoutes[i] = ROUTE_DISABLED;
                    }
                    if (this.extractRoutes[i] >= this.listSlotsExtract.size()) {
                        this.extractRoutes[i] = ROUTE_DISABLED;
                    }
                }
                this.autoInsert = cc.autoInsert;
                this.autoExtract = cc.autoExtract;
                this.maxAutoInsert = cc.maxAutoInsert;
                this.maxAutoInsertDefault = cc.maxAutoInsertDefault;
                this.maxAutoExtract = cc.maxAutoExtract;
                this.maxAutoExtractDefault = cc.maxAutoExtractDefault;
                this.autoInsertInterval = cc.autoInsertInterval;
                this.autoExtractInterval = cc.autoExtractInterval;
                this.clayEnergySlot = cc.clayEnergySlot;
                this.slotsDrop = new int[0];
                this.synced = true;
                return true;
            }
        }
        this.core = null;
        this.synced = false;
        this.autoInsert = false;
        this.autoExtract = false;
        this.slotsDrop = new int[0];
        return false;
    }

    @Override
    public int getInventorySize() {
        syncCoreBlock();
        return this.core != null ? this.core.getInventorySize() : super.getInventorySize();
    }

    @Override
    public ItemStack getStack(int slot) {
        syncCoreBlock();
        return this.core != null ? this.core.getStack(slot) : super.getStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (syncCoreBlock() && this.core != null) {
            this.core.setStack(slot, stack);
        }
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return new InterfaceItemHandler(side);
    }

    @Nullable
    public IFluidHandler getDelegatedFluidHandler(@Nullable Direction side) {
        if (syncCoreBlock() && this.core instanceof FluidTranslatorBlockEntity ft) {
            return ft.getFluidHandler(side);
        }
        return null;
    }

    @Override
    public int getItemUseMode(ItemStack stack, Player player) {
        if (!stack.isEmpty()) {
            if (stack.is(ClayiumItems.byPath("synchronizer").get())) {
                return 90;
            }
            if (stack.is(ClayiumItems.byPath("synchronous_parts").get())) {
                return 91;
            }
        }
        return super.getItemUseMode(stack, player);
    }

    @Override
    public boolean isUsableTool(ItemStack stack, Player player, Direction hitFace) {
        int mode = getItemUseMode(stack, player);
        if (mode == 90 || mode == 91) {
            return true;
        }
        return syncCoreBlock() && super.isUsableTool(stack, player, hitFace);
    }

    @Override
    protected void useItemFromSide(ItemStack stack, Player player, RelativeSide relSide, int mode, Direction hitFace) {
        if (mode == 91 && !this.acceptCoordChanger) {
            this.acceptCoordChanger = true;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            player.sendSystemMessage(Component.literal("Interface coordinate linking unlocked"));
            return;
        }
        if (mode == 90 && this.acceptCoordChanger) {
            GlobalPos gp = stack.get(ClayiumDataComponents.SYNCHRONIZER_TARGET.get());
            if (gp != null) {
                BlockPos target = gp.pos();
                if (setCoreBlockDimension(gp.dimension())
                        && setCoreBlockOffset(
                                target.getX() - this.worldPosition.getX(),
                                target.getY() - this.worldPosition.getY(),
                                target.getZ() - this.worldPosition.getZ())) {
                    player.sendSystemMessage(Component.literal(
                            "Set Core block Coord (" + target.getX() + "," + target.getY() + "," + target.getZ()
                                    + ") in dim " + gp.dimension().location()
                    ));
                }
            }
            return;
        }
        if (syncCoreBlock()) {
            super.useItemFromSide(stack, player, relSide, mode, hitFace);
        }
    }

    @Override
    public void serverTick() {
        syncCoreBlock();
        super.serverTick();
    }

    @Override
    public void doAutoExtract() {
        if (syncCoreBlock()) {
            super.doAutoExtract();
        }
    }

    @Override
    public void doAutoInsert() {
        if (syncCoreBlock()) {
            super.doAutoInsert();
        }
    }

    @Override
    public boolean consumeClayEnergy(long energy) {
        return false;
    }

    @Override
    public boolean acceptEnergyClay() {
        return syncCoreBlock() && this.core != null && this.core.acceptEnergyClay();
    }

    @Override
    public void markForSync() {
        if (this.level != null && !this.level.isClientSide && !this.syncLoopGuard && this.core != null && this.core != this) {
            this.syncLoopGuard = true;
            this.core.markForSync();
            this.syncLoopGuard = false;
        }
        super.markForSync();
    }

    @Override
    public void markForInstantSync() {
        if (this.level != null && !this.level.isClientSide && !this.syncLoopGuard && this.core != null && this.core != this) {
            this.syncLoopGuard = true;
            this.core.markForInstantSync();
            this.syncLoopGuard = false;
        }
        super.markForInstantSync();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        syncCoreBlock();
        super.saveAdditional(tag, registries);
        tag.putInt("CoreBlockX", this.coreOffsetX);
        tag.putInt("CoreBlockY", this.coreOffsetY);
        tag.putInt("CoreBlockZ", this.coreOffsetZ);
        tag.putBoolean("AcceptCoordChanger", this.acceptCoordChanger);
        if (this.coreDimension != null) {
            tag.putString("CoreBlockDimension", this.coreDimension.location().toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.coreOffsetX = tag.getInt("CoreBlockX");
        this.coreOffsetY = tag.getInt("CoreBlockY");
        this.coreOffsetZ = tag.getInt("CoreBlockZ");
        this.acceptCoordChanger = tag.getBoolean("AcceptCoordChanger");
        if (tag.contains("CoreBlockDimension")) {
            ResourceLocation loc = ResourceLocation.tryParse(tag.getString("CoreBlockDimension"));
            this.coreDimension = loc != null ? ResourceKey.create(Registries.DIMENSION, loc) : null;
        } else {
            this.coreDimension = null;
        }
    }

    private final class InterfaceItemHandler implements IItemHandler {
        @Nullable
        private final Direction side;

        InterfaceItemHandler(@Nullable Direction side) {
            this.side = side;
        }

        @Nullable
        private IItemHandler delegate() {
            syncCoreBlock();
            return ClayInterfaceBlockEntity.this.core != null
                    ? ClayInterfaceBlockEntity.this.core.getItemHandler(null)
                    : null;
        }

        @Override
        public int getSlots() {
            IItemHandler d = delegate();
            return d != null ? d.getSlots() : 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            IItemHandler d = delegate();
            return d != null ? d.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            IItemHandler d = delegate();
            if (d == null) {
                return stack;
            }
            if (this.side != null) {
                RelativeSide rel = toRelativeSide(this.side);
                ItemStack filter = ClayInterfaceBlockEntity.this.filters.get(rel.id());
                if (ClayiumFilterMatcher.isFilter(filter) && !ClayiumFilterMatcher.match(filter, stack)) {
                    return stack;
                }
                List<int[]> insertRoutesList = ClayInterfaceBlockEntity.this.listSlotsInsert;
                int route = ClayInterfaceBlockEntity.this.insertRoutes[rel.id()];
                if (route < 0 || route >= insertRoutesList.size()) {
                    return stack;
                }
                boolean slotAllowed = false;
                for (int candidate : insertRoutesList.get(route)) {
                    if (candidate == slot) {
                        slotAllowed = true;
                        break;
                    }
                }
                if (!slotAllowed) {
                    return stack;
                }
            }
            return d.insertItem(slot, stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            IItemHandler d = delegate();
            if (d == null) {
                return ItemStack.EMPTY;
            }
            if (this.side != null) {
                RelativeSide rel = toRelativeSide(this.side);
                ItemStack cur = d.getStackInSlot(slot);
                ItemStack filter = ClayInterfaceBlockEntity.this.filters.get(rel.id());
                if (ClayiumFilterMatcher.isFilter(filter) && !ClayiumFilterMatcher.match(filter, cur)) {
                    return ItemStack.EMPTY;
                }
                List<int[]> extractRoutesList = ClayInterfaceBlockEntity.this.listSlotsExtract;
                int route = ClayInterfaceBlockEntity.this.extractRoutes[rel.id()];
                if (route < 0 || route >= extractRoutesList.size()) {
                    return ItemStack.EMPTY;
                }
                boolean slotAllowed = false;
                for (int candidate : extractRoutesList.get(route)) {
                    if (candidate == slot) {
                        slotAllowed = true;
                        break;
                    }
                }
                if (!slotAllowed) {
                    return ItemStack.EMPTY;
                }
            }
            return d.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            IItemHandler d = delegate();
            return d != null ? d.getSlotLimit(slot) : 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            IItemHandler d = delegate();
            return d != null && d.isItemValid(slot, stack);
        }
    }
}
