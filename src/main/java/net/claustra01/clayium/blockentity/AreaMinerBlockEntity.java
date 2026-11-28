package net.claustra01.clayium.blockentity;

import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.IClayLaserMachine;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * Area Miner (T6 single-block {@code miner}, T8 {@code area_miner}, T9 {@code advanced_area_miner}, T10 {@code area_replacer}):
 * harvests blocks top-down within an {@link AABB} acquired from nearby {@link IAxisAlignedBBProvider} markers
 * (or 1x1x1 behind facing when {@code !areaMode}), with filter-driven harvest/fortune/silktouch and laser speedup.
 */
public class AreaMinerBlockEntity extends TieredClayContainerBlockEntity
        implements IClayLaserMachine, IExternalControl {
    public static final int STATE_ONCE_START = 0;
    public static final int STATE_ONCE_RUNNING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_REPEAT_START = 3;
    public static final int STATE_REPEAT_RUNNING = 4;

    public static final int FILTER_HARVEST_SLOT = 37;
    public static final int FILTER_FORTUNE_SLOT = 38;
    public static final int FILTER_SILKTOUCH_SLOT = 39;

    public boolean areaMode = true;
    public boolean replaceMode = false;
    public int inventoryX = 3;
    public int inventoryY = 3;
    public int slotNum = 9;

    @Nullable
    public AABB axisAlignedBB;
    public int boxState = 1;
    public int state = STATE_ONCE_START;
    public long progress = 0L;
    public long progressPerTick = 100L;
    public long progressPerJob = 400L;
    public long energyPerTick = 1000L;

    public int miningX = 0;
    public int miningY = 0;
    public int miningZ = 0;
    public boolean placeFlag = false;
    protected int maxJobCount = 1000;
    protected int maxJobCountInLoop = 10;

    public long laserEnergy = 0L;
    public long lastLaserEnergy = 0L;
    @Nullable
    private Direction lastFixedFacing;

    public AreaMinerBlockEntity(BlockPos pos, BlockState blockState) {
        this(ClayiumBlockEntities.AREA_MINER.get(), pos, blockState);
        ensureTierInitialized();
    }

    protected AreaMinerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, 40);
    }

    @Override
    protected void initParams() {
        this.areaMode = true;
        this.replaceMode = false;
        this.inventoryX = 3;
        this.inventoryY = 3;
        this.slotNum = 9;
        this.energyPerTick = 1000L;
        this.progressPerTick = 100L;
        this.progressPerJob = 400L;
        this.maxJobCount = 1000;
        this.maxJobCountInLoop = 10;
        this.items = NonNullList.withSize(40, ItemStack.EMPTY);
        this.clayEnergySlot = 36;
        this.autoInsert = true;
        this.autoExtract = true;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 0, 0};
    }

    @Override
    protected void initParamsByTier(int tier) {
        String path = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();
        this.replaceMode = "area_replacer".equals(path);
        this.areaMode = tier >= 7;
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        this.state = initState();

        if (!this.areaMode) {
            this.energyPerTick = 0L;
            this.insertRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
            this.listSlotsInsert.clear();
            this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        } else {
            this.listSlotsInsert.clear();
            this.listSlotsInsert.add(new int[]{36});
            this.insertRoutes = new int[]{ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
            this.maxAutoExtract = new int[]{1};
            if (this.replaceMode) {
                this.inventoryX = 4;
                this.inventoryY = 2;
                this.slotNum = 8;
                this.listSlotsInsert.add(new int[]{8, 9, 10, 11, 12, 13, 14, 15});
                this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 36};
            } else {
                this.inventoryX = 3;
                this.inventoryY = 3;
                this.slotNum = 9;
                this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 36};
            }
        }
        int[] extractSlots = new int[this.slotNum];
        for (int i = 0; i < this.slotNum; i++) {
            extractSlots[i] = i;
        }
        this.listSlotsExtract.clear();
        this.listSlotsExtract.add(extractSlots);
    }

    protected int initState() {
        return !this.areaMode ? STATE_REPEAT_START : STATE_ONCE_START;
    }

    @Override
    public boolean acceptEnergyClay() {
        return this.areaMode;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == FILTER_HARVEST_SLOT || slot == FILTER_FORTUNE_SLOT || slot == FILTER_SILKTOUCH_SLOT) {
            return ClayiumFilterMatcher.isFilter(stack);
        }
        return super.isItemValidForSlot(slot, stack);
    }

    public boolean hasAxisAlignedBB() {
        return this.axisAlignedBB != null;
    }

    @Nullable
    public AABB getAxisAlignedBB() {
        return this.axisAlignedBB;
    }

    public void setAxisAlignedBB(AABB aabb) {
        this.axisAlignedBB = aabb;
        this.miningX = (int) Math.floor(aabb.minX);
        this.miningY = (int) Math.floor(aabb.maxY) - 1;
        this.miningZ = (int) Math.floor(aabb.minZ);
        markForSync();
    }

    public void searchAABBProvider() {
        if (this.level == null) {
            return;
        }
        if (!this.areaMode) {
            Direction front = getFrontDirection();
            BlockPos target = this.worldPosition.relative(front.getOpposite());
            if (this.axisAlignedBB == null || this.lastFixedFacing != front) {
                this.lastFixedFacing = front;
                setAxisAlignedBB(new AABB(target));
            }
            return;
        }
        if (hasAxisAlignedBB()) {
            return;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos p = this.worldPosition.offset(dx, dy, dz);
                    BlockEntity be = this.level.getBlockEntity(p);
                    if (be instanceof IAxisAlignedBBProvider provider && provider.hasAxisAlignedBB()) {
                        AABB bb = provider.getAxisAlignedBB();
                        if (bb != null) {
                            setAxisAlignedBB(bb);
                            provider.setAxisAlignedBBToMachine();
                            return;
                        }
                    }
                }
            }
        }
    }

    public void resetCoordinates() {
        if (this.axisAlignedBB == null) {
            return;
        }
        this.miningX = (int) Math.floor(this.axisAlignedBB.minX);
        this.miningY = (int) Math.floor(this.axisAlignedBB.maxY) - 1;
        this.miningZ = (int) Math.floor(this.axisAlignedBB.minZ);
        this.placeFlag = false;
    }

    public boolean advanceCoordinate() {
        if (this.axisAlignedBB == null) {
            return true;
        }
        int minX = (int) Math.floor(this.axisAlignedBB.minX);
        int maxX = (int) Math.floor(this.axisAlignedBB.maxX);
        int minY = (int) Math.floor(this.axisAlignedBB.minY);
        int maxY = (int) Math.floor(this.axisAlignedBB.maxY);
        int minZ = (int) Math.floor(this.axisAlignedBB.minZ);
        int maxZ = (int) Math.floor(this.axisAlignedBB.maxZ);

        this.miningX++;
        if (this.miningX >= maxX) {
            this.miningX = minX;
            this.miningZ++;
            if (this.miningZ >= maxZ) {
                this.miningZ = minZ;
                this.miningY--;
                if (this.miningY < minY) {
                    this.miningY = maxY - 1;
                    return true;
                }
            }
        }
        return false;
    }

    public double getLaserSpeedMultiplier() {
        return computeLaserSpeedMultiplier(this.laserEnergy);
    }

    public static double computeLaserSpeedMultiplier(long laserEnergy) {
        return 1.0 + 4.0 * Math.log10((double) (laserEnergy / 1000L + 1L));
    }

    @Override
    public boolean irradiateClayLaser(ClayLaser laser, Direction incomingSide) {
        if (laser != null) {
            this.laserEnergy += (long) laser.getEnergy();
        }
        return true;
    }

    @Override
    public boolean isDoingWork() {
        return this.state == STATE_ONCE_RUNNING || this.state == STATE_REPEAT_RUNNING;
    }

    @Override
    public boolean isScheduledWork() {
        return true;
    }

    public void doWorkOnce() {
        setState(STATE_ONCE_START);
    }

    @Override
    public void startWork() {
        setState(STATE_REPEAT_START);
    }

    @Override
    public void stopWork() {
        setState(STATE_STOPPED);
    }

    public void setState(int nextState) {
        if (this.state != nextState) {
            this.state = nextState;
            markForSync();
        }
    }

    public void cycleBoxState() {
        this.boxState = (this.boxState + 1) % 3;
        markForSync();
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        searchAABBProvider();
        if (!this.areaMode) {
            // Re-sync fixed 1x1x1 coordinates if rotated while running
            Direction front = getFrontDirection();
            if (this.lastFixedFacing != front) {
                this.lastFixedFacing = front;
                BlockPos t = this.worldPosition.relative(front.getOpposite());
                setAxisAlignedBB(new AABB(t));
            }
        }

        if (hasAxisAlignedBB()) {
            if (this.state == STATE_ONCE_START) {
                resetCoordinates();
                this.state = STATE_ONCE_RUNNING;
            } else if (this.state == STATE_REPEAT_START) {
                resetCoordinates();
                this.state = STATE_REPEAT_RUNNING;
            }

            if (isDoingWork()) {
                double mult = getLaserSpeedMultiplier();
                long energy = this.areaMode ? (long) ((double) this.energyPerTick * mult) : 0L;
                if (energy <= 0L || consumeClayEnergy(energy)) {
                    this.progress += (long) ((double) this.progressPerTick * mult);
                    doWork();
                }
            } else {
                this.progress = 0L;
            }
        } else {
            this.progress = 0L;
        }

        if (this.lastLaserEnergy != this.laserEnergy) {
            this.lastLaserEnergy = this.laserEnergy;
            markForSync();
        } else {
            this.lastLaserEnergy = this.laserEnergy;
        }
        this.laserEnergy = 0L;

        BlockState blockState = getBlockState();
        if (blockState.hasProperty(ClayContainerBlock.ACTIVE)) {
            boolean active = isDoingWork();
            if (blockState.getValue(ClayContainerBlock.ACTIVE) != active) {
                this.level.setBlock(this.worldPosition, blockState.setValue(ClayContainerBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
            }
        }
    }

    protected void doWork() {
        if (!(this.level instanceof ServerLevel serverLevel) || this.axisAlignedBB == null) {
            return;
        }
        int maxSteps = this.state == STATE_ONCE_RUNNING ? this.maxJobCount : this.maxJobCountInLoop;
        for (int step = 0; step < maxSteps; step++) {
            BlockPos pos = new BlockPos(this.miningX, this.miningY, this.miningZ);
            if (!serverLevel.isInWorldBounds(pos)) {
                if (advanceCoordinate() && this.state == STATE_ONCE_RUNNING) {
                    setState(STATE_STOPPED);
                    break;
                }
                continue;
            }

            BlockState target = serverLevel.getBlockState(pos);
            FluidState fluidState = target.getFluidState();
            boolean isSourceFluid = !fluidState.isEmpty()
                    && fluidState.isSource()
                    && target.getBlock() instanceof LiquidBlock
                    && ClayiumConfig.ENABLE_FLUID_CAPSULE.get();

            float hardness = target.getDestroySpeed(serverLevel, pos);
            boolean canMine = !target.isAir() && (isSourceFluid || hardness >= 0.0F);
            long jobCost = target.isAir()
                    ? 0L
                    : (long) (400.0 * (0.1 + (isSourceFluid ? 1.0 : hardness))) + (this.replaceMode ? 1L : 0L);

            if (canMine) {
                ItemStack harvestFilter = getStack(FILTER_HARVEST_SLOT);
                if (ClayiumFilterMatcher.isFilter(harvestFilter)) {
                    canMine = ClayiumFilterMatcher.matchBlock(harvestFilter, serverLevel, pos);
                }
            }

            if (canMine) {
                if (jobCost > this.progress) {
                    break;
                }
                List<ItemStack> drops;
                if (isSourceFluid) {
                    ItemStack cap = new ItemStack(ClayiumItems.byPath("fluid_capsule_1000").get());
                    cap.set(
                            ClayiumDataComponents.FLUID_CONTENT.get(),
                            SimpleFluidContent.copyOf(new FluidStack(fluidState.getType(), 1000))
                    );
                    drops = List.of(cap);
                } else {
                    boolean silkTouch = getTier() >= 9
                            && ClayiumFilterMatcher.isFilter(getStack(FILTER_SILKTOUCH_SLOT))
                            && ClayiumFilterMatcher.matchBlock(getStack(FILTER_SILKTOUCH_SLOT), serverLevel, pos);
                    int fortune = !silkTouch
                            && getTier() >= 9
                            && ClayiumFilterMatcher.isFilter(getStack(FILTER_FORTUNE_SLOT))
                            && ClayiumFilterMatcher.matchBlock(getStack(FILTER_FORTUNE_SLOT), serverLevel, pos)
                            ? 3
                            : 0;
                    ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
                    var enchRegistry = serverLevel.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
                    if (silkTouch) {
                        tool.enchant(enchRegistry.getOrThrow(Enchantments.SILK_TOUCH), 1);
                    } else if (fortune > 0) {
                        tool.enchant(enchRegistry.getOrThrow(Enchantments.FORTUNE), fortune);
                    }
                    LootParams.Builder lootBuilder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.TOOL, tool)
                            .withOptionalParameter(LootContextParams.BLOCK_ENTITY, serverLevel.getBlockEntity(pos));
                    drops = target.getDrops(lootBuilder);
                }

                if (!canFitAllDrops(drops)) {
                    this.progress = Math.min(this.progress, jobCost);
                    break;
                }
                this.progress -= jobCost;
                serverLevel.levelEvent(2001, pos, Block.getId(target));
                serverLevel.removeBlock(pos, false);
                insertAllDrops(drops);
                if (this.replaceMode) {
                    this.placeFlag = true;
                }
            }

            if (this.replaceMode && this.placeFlag && serverLevel.getBlockState(pos).isAir()) {
                if (!tryPlaceReplacementBlock(serverLevel, pos)) {
                    this.progress = 0L;
                    break;
                }
                this.placeFlag = false;
            }

            boolean wrapped = advanceCoordinate();
            if (wrapped && this.state == STATE_ONCE_RUNNING) {
                setState(STATE_STOPPED);
                break;
            }
            if (step == maxSteps - 1) {
                this.progress = 0L;
            }
        }
    }

    private boolean canFitAllDrops(List<ItemStack> drops) {
        ItemStack[] sim = new ItemStack[this.slotNum];
        for (int i = 0; i < this.slotNum; i++) {
            sim[i] = getStack(i).copy();
        }
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            int remaining = drop.getCount();
            for (int i = 0; i < this.slotNum && remaining > 0; i++) {
                if (sim[i].isEmpty()) {
                    int move = Math.min(remaining, Math.min(64, drop.getMaxStackSize()));
                    sim[i] = drop.copyWithCount(move);
                    remaining -= move;
                } else if (ItemStack.isSameItemSameComponents(sim[i], drop)) {
                    int cap = Math.min(64, sim[i].getMaxStackSize()) - sim[i].getCount();
                    if (cap > 0) {
                        int move = Math.min(remaining, cap);
                        sim[i].grow(move);
                        remaining -= move;
                    }
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private void insertAllDrops(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (drop.isEmpty()) {
                continue;
            }
            ItemStack rem = drop.copy();
            for (int i = 0; i < this.slotNum && !rem.isEmpty(); i++) {
                ItemStack existing = getStack(i);
                if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, rem)) {
                    int cap = Math.min(64, existing.getMaxStackSize()) - existing.getCount();
                    if (cap > 0) {
                        int move = Math.min(cap, rem.getCount());
                        existing.grow(move);
                        rem.shrink(move);
                        markForSync();
                    }
                }
            }
            for (int i = 0; i < this.slotNum && !rem.isEmpty(); i++) {
                if (getStack(i).isEmpty()) {
                    int move = Math.min(Math.min(64, rem.getMaxStackSize()), rem.getCount());
                    setStack(i, rem.copyWithCount(move));
                    rem.shrink(move);
                }
            }
        }
    }

    private boolean tryPlaceReplacementBlock(ServerLevel level, BlockPos pos) {
        for (int s = 8; s < 16; s++) {
            ItemStack stack = getStack(s);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem) {
                BlockState placeState = blockItem.getBlock().defaultBlockState();
                if (level.setBlock(pos, placeState, Block.UPDATE_ALL)) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        setStack(s, ItemStack.EMPTY);
                    } else {
                        markForSync();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("State", (byte) this.state);
        tag.putByte("BoxState", (byte) this.boxState);
        tag.putLong("Progress", this.progress);
        tag.putInt("MiningX", this.miningX);
        tag.putInt("MiningY", this.miningY);
        tag.putInt("MiningZ", this.miningZ);
        tag.putBoolean("PlaceFlag", this.placeFlag);
        tag.putLong("LastLaserEnergy", this.lastLaserEnergy);
        tag.putBoolean("hasAABB", this.axisAlignedBB != null);
        if (this.axisAlignedBB != null) {
            tag.putDouble("AABBMinX", this.axisAlignedBB.minX);
            tag.putDouble("AABBMinY", this.axisAlignedBB.minY);
            tag.putDouble("AABBMinZ", this.axisAlignedBB.minZ);
            tag.putDouble("AABBMaxX", this.axisAlignedBB.maxX);
            tag.putDouble("AABBMaxY", this.axisAlignedBB.maxY);
            tag.putDouble("AABBMaxZ", this.axisAlignedBB.maxZ);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("State")) {
            this.state = tag.getByte("State");
        }
        if (tag.contains("BoxState")) {
            this.boxState = tag.getByte("BoxState");
        }
        this.progress = tag.getLong("Progress");
        this.miningX = tag.getInt("MiningX");
        this.miningY = tag.getInt("MiningY");
        this.miningZ = tag.getInt("MiningZ");
        this.placeFlag = tag.getBoolean("PlaceFlag");
        this.lastLaserEnergy = tag.getLong("LastLaserEnergy");
        if (tag.getBoolean("hasAABB")) {
            this.axisAlignedBB = new AABB(
                    tag.getDouble("AABBMinX"),
                    tag.getDouble("AABBMinY"),
                    tag.getDouble("AABBMinZ"),
                    tag.getDouble("AABBMaxX"),
                    tag.getDouble("AABBMaxY"),
                    tag.getDouble("AABBMaxZ")
            );
        } else {
            this.axisAlignedBB = null;
        }
    }
}
