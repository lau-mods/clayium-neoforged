package net.claustra01.clayium.blockentity;

import com.mojang.authlib.GameProfile;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.DirectionMemoryData;
import net.claustra01.clayium.filter.ClayiumFilterMatcher;
import net.claustra01.clayium.laser.ClayLaser;
import net.claustra01.clayium.laser.IClayLaserMachine;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

/**
 * Area Activator (T6 single-block {@code activator}, T8 {@code area_activator}):
 * walks bottom-up across an AABB and triggers right-click / block placement / entity interaction via a NeoForge {@link FakePlayer},
 * optionally directed by {@link DirectionMemoryData}.
 */
public class AreaActivatorBlockEntity extends TieredClayContainerBlockEntity
        implements IClayLaserMachine, IExternalControl, IRayTracer {
    private static final GameProfile FAKE_PROFILE =
            new GameProfile(UUID.fromString("92f0b4d1-96bb-4d61-8c80-3f567289ab01"), "[ClayiumActivator]");

    public static final int STATE_ONCE_START = 0;
    public static final int STATE_ONCE_RUNNING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_REPEAT_START = 3;
    public static final int STATE_REPEAT_RUNNING = 4;

    public static final int CLAY_ENERGY_SLOT = 49;
    public static final int FILTER_SLOT = 50;

    public boolean areaMode = true;
    @Nullable
    public AABB axisAlignedBB;
    public int boxState = 1;
    public int state = STATE_REPEAT_START;
    public long progress = 0L;
    public long progressPerTick = 100L;
    public long energyPerTick = 1000L;

    public int target = 0; // 0=Block, 1=Entity, 2=Both
    public boolean enableRayTrace = false;
    public boolean sneak = false;
    @Nullable
    public DirectionMemoryData rayTraceMemory;

    public int activeX = 0;
    public int activeY = 0;
    public int activeZ = 0;
    public long laserEnergy = 0L;
    public long lastLaserEnergy = 0L;
    @Nullable
    private Direction lastFixedFacing;

    public AreaActivatorBlockEntity(BlockPos pos, BlockState blockState) {
        super(ClayiumBlockEntities.AREA_ACTIVATOR.get(), pos, blockState, 53);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(53, ItemStack.EMPTY);
        this.clayEnergySlot = CLAY_ENERGY_SLOT;
        this.energyPerTick = 1000L;
        this.progressPerTick = 100L;
        this.autoInsert = true;
        this.autoExtract = true;
        this.listSlotsInsert.clear();
        this.listSlotsExtract.clear();
        this.listSlotsInsert.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        this.listSlotsInsert.add(new int[]{CLAY_ENERGY_SLOT});
        this.listSlotsExtract.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        this.insertRoutes = new int[]{ROUTE_DISABLED, 0, ROUTE_DISABLED, 1, ROUTE_DISABLED, ROUTE_DISABLED};
        this.extractRoutes = new int[]{ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, 0, 0};
    }

    @Override
    protected void initParamsByTier(int tier) {
        this.areaMode = tier >= 7;
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        this.state = STATE_REPEAT_START;
        if (!this.areaMode) {
            this.energyPerTick = 0L;
            this.insertRoutes = new int[]{ROUTE_DISABLED, 0, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED, ROUTE_DISABLED};
            this.listSlotsInsert.clear();
            this.listSlotsInsert.add(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
            this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
        } else {
            this.maxAutoExtract = new int[]{-1, 1};
            this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, CLAY_ENERGY_SLOT};
        }
    }

    @Override
    public int[] getSlotsToDrop() {
        int extra = 0;
        for (int i = 9; i < 49; i++) {
            if (!getStack(i).isEmpty()) {
                extra++;
            }
        }
        if (extra == 0) {
            return this.slotsDrop;
        }
        int[] combined = new int[this.slotsDrop.length + extra];
        System.arraycopy(this.slotsDrop, 0, combined, 0, this.slotsDrop.length);
        int idx = this.slotsDrop.length;
        for (int i = 9; i < 49; i++) {
            if (!getStack(i).isEmpty()) {
                combined[idx++] = i;
            }
        }
        return combined;
    }

    @Override
    public boolean acceptEnergyClay() {
        return this.areaMode;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == FILTER_SLOT) {
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
        this.activeX = (int) Math.floor(aabb.minX);
        this.activeY = (int) Math.floor(aabb.minY);
        this.activeZ = (int) Math.floor(aabb.minZ);
        markForSync();
    }

    public void resetCoordinates() {
        if (this.axisAlignedBB == null) {
            return;
        }
        this.activeX = (int) Math.floor(this.axisAlignedBB.minX);
        this.activeY = (int) Math.floor(this.axisAlignedBB.minY);
        this.activeZ = (int) Math.floor(this.axisAlignedBB.minZ);
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

        this.activeX++;
        if (this.activeX >= maxX) {
            this.activeX = minX;
            this.activeZ++;
            if (this.activeZ >= maxZ) {
                this.activeZ = minZ;
                this.activeY++;
                if (this.activeY >= maxY) {
                    this.activeY = minY;
                    return true;
                }
            }
        }
        return false;
    }

    public void searchAABBProvider() {
        if (this.level == null) {
            return;
        }
        if (!this.areaMode) {
            Direction front = getFrontDirection();
            BlockPos targetPos = this.worldPosition.relative(front.getOpposite());
            if (this.axisAlignedBB == null || this.lastFixedFacing != front) {
                this.lastFixedFacing = front;
                setAxisAlignedBB(new AABB(targetPos));
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

    @Override
    public boolean acceptRayTraceMemory(DirectionMemoryData data) {
        return this.areaMode && data != null;
    }

    @Override
    public void applyRayTraceMemory(DirectionMemoryData data) {
        this.rayTraceMemory = data;
        markForSync();
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

    public void cycleTarget() {
        this.target = (this.target + 1) % 3;
        markForSync();
    }

    public void toggleRayTrace() {
        this.enableRayTrace = !this.enableRayTrace;
        markForSync();
    }

    public void toggleSneak() {
        this.sneak = !this.sneak;
        markForSync();
    }

    private boolean isCrowded() {
        boolean stillOverflow = false;
        for (int i = 9; i < 49; i++) {
            ItemStack extra = getStack(i);
            if (extra.isEmpty()) {
                continue;
            }
            for (int dst = 0; dst < 9 && !extra.isEmpty(); dst++) {
                ItemStack existing = getStack(dst);
                if (existing.isEmpty()) {
                    setStack(dst, extra.copy());
                    setStack(i, ItemStack.EMPTY);
                    extra = ItemStack.EMPTY;
                } else if (ItemStack.isSameItemSameComponents(existing, extra)) {
                    int cap = Math.min(64, existing.getMaxStackSize()) - existing.getCount();
                    if (cap > 0) {
                        int move = Math.min(cap, extra.getCount());
                        existing.grow(move);
                        extra.shrink(move);
                        markForSync();
                        if (extra.isEmpty()) {
                            setStack(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
            if (!getStack(i).isEmpty()) {
                stillOverflow = true;
            }
        }
        return stillOverflow;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        searchAABBProvider();
        if (!this.areaMode) {
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

            if (isDoingWork() && !isCrowded()) {
                double mult = AreaMinerBlockEntity.computeLaserSpeedMultiplier(this.laserEnergy);
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

    private void doWork() {
        if (!(this.level instanceof ServerLevel serverLevel) || this.axisAlignedBB == null) {
            return;
        }
        int maxSteps = this.state == STATE_ONCE_RUNNING ? 1000 : 10;
        for (int step = 0; step < maxSteps; step++) {
            BlockPos pos = new BlockPos(this.activeX, this.activeY, this.activeZ);
            if (!serverLevel.isInWorldBounds(pos)) {
                if (advanceCoordinate() && this.state == STATE_ONCE_RUNNING) {
                    setState(STATE_STOPPED);
                    break;
                }
                continue;
            }

            boolean shouldWait = shouldActAt(serverLevel, pos);
            if (shouldWait && this.progress < 400L) {
                break;
            }
            if (shouldWait) {
                this.progress -= 400L;
                performActivation(serverLevel, pos);
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

    private boolean shouldActAt(ServerLevel level, BlockPos pos) {
        ItemStack filter = getStack(FILTER_SLOT);
        if (this.target != 1) {
            BlockState bs = level.getBlockState(pos);
            if (ClayiumFilterMatcher.isFilter(filter)) {
                if (!bs.isAir() && ClayiumFilterMatcher.matchBlock(filter, level, pos)) {
                    return true;
                }
            } else if (!bs.isAir() || findActiveItemSlot() >= 0) {
                return true;
            }
        }
        if (this.target != 0) {
            List<Entity> ents = level.getEntities((Entity) null, new AABB(pos), Entity::isAlive);
            if (!ents.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private int findActiveItemSlot() {
        for (int i = 0; i < 9; i++) {
            if (!getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void performActivation(ServerLevel level, BlockPos pos) {
        FakePlayer fake = FakePlayerFactory.get(level, FAKE_PROFILE);
        int activeSlot = findActiveItemSlot();
        ItemStack held = activeSlot >= 0 ? getStack(activeSlot) : ItemStack.EMPTY;

        for (int i = 0; i < 9; i++) {
            fake.getInventory().setItem(i, getStack(i));
        }
        for (int i = 9; i < 36; i++) {
            fake.getInventory().setItem(i, getStack(i));
        }
        fake.getInventory().selected = Math.max(0, activeSlot);
        fake.setShiftKeyDown(this.sneak);

        Vec3 eyeOffset;
        Vec3 lookVec;
        if (this.rayTraceMemory != null) {
            BlockPos recordedBlock = BlockPos.containing(
                    this.rayTraceMemory.hitX(), this.rayTraceMemory.hitY(), this.rayTraceMemory.hitZ());
            eyeOffset = new Vec3(
                    this.rayTraceMemory.startX() - recordedBlock.getX(),
                    this.rayTraceMemory.startY() - recordedBlock.getY(),
                    this.rayTraceMemory.startZ() - recordedBlock.getZ());
            lookVec = new Vec3(
                    this.rayTraceMemory.hitX() - this.rayTraceMemory.startX(),
                    this.rayTraceMemory.hitY() - this.rayTraceMemory.startY(),
                    this.rayTraceMemory.hitZ() - this.rayTraceMemory.startZ());
        } else {
            eyeOffset = new Vec3(0.5, 0.5, 0.5);
            lookVec = new Vec3(0.0, -1.0, 0.0);
        }

        Vec3 origin = Vec3.atLowerCornerOf(pos).add(eyeOffset);
        fake.setPos(origin.x, origin.y, origin.z);

        boolean acted = false;
        if (this.target != 1) {
            BlockHitResult hit;
            if (this.enableRayTrace && this.rayTraceMemory != null) {
                Vec3 end = origin.add(lookVec.normalize().scale(Math.max(lookVec.length(), 3.0)));
                hit = level.clip(new ClipContext(origin, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, fake));
            } else {
                Direction dir = this.rayTraceMemory != null
                        ? this.rayTraceMemory.side()
                        : (this.areaMode ? Direction.DOWN : getFrontDirection().getOpposite());
                hit = new BlockHitResult(Vec3.atCenterOf(pos), dir, pos, false);
            }
            if (hit.getType() == HitResult.Type.BLOCK) {
                ItemStack filter = getStack(FILTER_SLOT);
                if (!ClayiumFilterMatcher.isFilter(filter) || ClayiumFilterMatcher.matchBlock(filter, level, hit.getBlockPos())) {
                    UseOnContext ctx = new UseOnContext(fake, InteractionHand.MAIN_HAND, hit);
                    InteractionResult res = fake.gameMode.useItemOn(fake, level, held, InteractionHand.MAIN_HAND, hit);
                    acted = res.consumesAction() || held.useOn(ctx).consumesAction();
                }
            }
        }
        if (!acted && this.target != 0) {
            List<Entity> ents = level.getEntities((Entity) null, new AABB(pos), Entity::isAlive);
            if (!ents.isEmpty()) {
                Entity victim = ents.getFirst();
                fake.interactOn(victim, InteractionHand.MAIN_HAND);
                acted = true;
            }
        }
        if (!acted && !held.isEmpty()) {
            fake.gameMode.useItem(fake, level, held, InteractionHand.MAIN_HAND);
        }

        for (int i = 0; i < 36; i++) {
            setStack(i, fake.getInventory().getItem(i));
            fake.getInventory().setItem(i, ItemStack.EMPTY);
        }
        fake.setShiftKeyDown(false);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putByte("State", (byte) this.state);
        tag.putByte("BoxState", (byte) this.boxState);
        tag.putLong("Progress", this.progress);
        tag.putByte("Target", (byte) this.target);
        tag.putBoolean("EnableRayTrace", this.enableRayTrace);
        tag.putBoolean("Sneak", this.sneak);
        tag.putInt("ActiveX", this.activeX);
        tag.putInt("ActiveY", this.activeY);
        tag.putInt("ActiveZ", this.activeZ);
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
        if (this.rayTraceMemory != null) {
            CompoundTag mem = new CompoundTag();
            mem.putDouble("StartX", this.rayTraceMemory.startX());
            mem.putDouble("StartY", this.rayTraceMemory.startY());
            mem.putDouble("StartZ", this.rayTraceMemory.startZ());
            mem.putDouble("HitX", this.rayTraceMemory.hitX());
            mem.putDouble("HitY", this.rayTraceMemory.hitY());
            mem.putDouble("HitZ", this.rayTraceMemory.hitZ());
            mem.putInt("SideHit", this.rayTraceMemory.side().get3DDataValue());
            tag.put("RayTraceMemory", mem);
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
        this.target = tag.getByte("Target");
        this.enableRayTrace = tag.getBoolean("EnableRayTrace");
        this.sneak = tag.getBoolean("Sneak");
        this.activeX = tag.getInt("ActiveX");
        this.activeY = tag.getInt("ActiveY");
        this.activeZ = tag.getInt("ActiveZ");
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
        if (tag.contains("RayTraceMemory", Tag.TAG_COMPOUND)) {
            CompoundTag mem = tag.getCompound("RayTraceMemory");
            this.rayTraceMemory = new DirectionMemoryData(
                    mem.getDouble("StartX"),
                    mem.getDouble("StartY"),
                    mem.getDouble("StartZ"),
                    mem.getDouble("HitX"),
                    mem.getDouble("HitY"),
                    mem.getDouble("HitZ"),
                    Direction.from3DDataValue(mem.getInt("SideHit"))
            );
        } else {
            this.rayTraceMemory = null;
        }
    }
}
