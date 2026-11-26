package net.claustra01.clayium.world.area;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.logistics.filter.ItemFilter;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.world.operation.BlockOperationResult;
import net.claustra01.clayium.world.operation.BlockOperationService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class AreaMachineBlockEntity extends BlockEntity implements LaserReceiver, MenuProvider, AreaGuiHost {
    /**
     * Original TileAreaMiner/TileAreaActivator inventory: an X*Y grid (3x3; the Tier 10 replacer
     * has 4x2 outputs followed by 4x2 replacement blocks), then SlotMemory filters and the
     * energy slot shown outside the window.
     */
    public static final int GRID_CAPACITY = 16;
    public static final int REPLACEMENT_START = 8;
    public static final int HARVEST_FILTER_SLOT = 16;
    public static final int FORTUNE_FILTER_SLOT = 17;
    public static final int SILK_TOUCH_FILTER_SLOT = 18;
    public static final int FUEL_SLOT = 19;
    public static final int MAX_OPERATIONS_PER_TICK = 1000;

    private final ItemStackHandler inventory = new ItemStackHandler(FUEL_SLOT + 1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case HARVEST_FILTER_SLOT, FORTUNE_FILTER_SLOT, SILK_TOUCH_FILTER_SLOT -> false;
                case FUEL_SLOT -> ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                default -> true;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= HARVEST_FILTER_SLOT && slot <= SILK_TOUCH_FILTER_SLOT
                    ? 1 : super.getSlotLimit(slot);
        }
    };
    private final IItemHandler itemHandler = new AreaItemHandler();
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private AreaBounds workArea;
    private long cursor;
    private String targetDimension = "";
    @Nullable private UUID owner;
    private LaserBeam pendingLaser = new LaserBeam(0L, 0L, 0L);
    private long progress;
    private OperationStatus status = OperationStatus.IDLE;
    private RayTraceMemory rayTraceMemory;
    private ActivatorTarget activatorTarget = ActivatorTarget.BLOCK;
    private boolean activatorRayTrace;
    private boolean activatorSneaking;
    private RunMode runMode = RunMode.ONCE;
    private int boxState;
    private long displayedLaser;

    public AreaMachineBlockEntity(BlockPos pos, BlockState state) {
        super(AreaMachineRegistration.MACHINE_BLOCK_ENTITY.get(), pos, state);
        workArea = new AreaBounds(pos, pos);
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, AreaMachineBlockEntity machine) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        if (machine.owner == null) {
            machine.status = OperationStatus.NO_OWNER;
            machine.updateActiveState();
            return;
        }
        if (!machine.targetDimension.isEmpty()
                && !machine.targetDimension.equals(level.dimension().location().toString())) {
            machine.status = OperationStatus.WRONG_DIMENSION;
            machine.updateActiveState();
            return;
        }
        if (machine.runMode == RunMode.STOPPED) {
            machine.status = OperationStatus.IDLE;
            machine.pendingLaser = new LaserBeam(0L, 0L, 0L);
            machine.updateActiveState();
            return;
        }
        machine.displayedLaser = (long)machine.pendingLaser.energy();
        double factor = 1.0D + 4.0D * Math.log10(1.0D + (long)machine.pendingLaser.energy() / 1000L);
        machine.pendingLaser = new LaserBeam(0L, 0L, 0L);
        long cost = (long)(machine.machineType().energyPerTick() * factor);
        if (!machine.energy.consume(ClayEnergy.ofInternal(cost), machine.inventory.getStackInSlot(FUEL_SLOT))) {
            machine.status = OperationStatus.NO_ENERGY;
            machine.updateActiveState();
            return;
        }
        machine.status = OperationStatus.WORKING;
        machine.progress += (long)(100.0D * factor);
        int operations = (int)Math.min(MAX_OPERATIONS_PER_TICK,
                machine.progress / machine.machineType().progressPerJob());
        if (operations > 0) {
            machine.runOperationsNow(operations);
            machine.progress %= machine.machineType().progressPerJob();
        }
        if (machine.status == OperationStatus.IDLE) {
            machine.status = OperationStatus.WORKING;
        }
        machine.setChanged();
        machine.updateActiveState();
    }

    public int runOperationsNow(int maximumOperations) {
        status = OperationStatus.IDLE;
        if (!(level instanceof ServerLevel serverLevel) || owner == null) {
            status = OperationStatus.NO_OWNER;
            updateActiveState();
            return 0;
        }
        if (!targetDimension.isEmpty() && !targetDimension.equals(serverLevel.dimension().location().toString())) {
            status = OperationStatus.WRONG_DIMENSION;
            updateActiveState();
            return 0;
        }
        workArea = workArea.clampHeight(serverLevel);
        int budget = Math.max(0, Math.min(maximumOperations, MAX_OPERATIONS_PER_TICK));
        int completed;
        if (machineType() == AreaMachineType.ITEM_COLLECTOR) {
            completed = collectItems(serverLevel, budget);
            if (runMode == RunMode.ONCE && status != OperationStatus.UNLOADED
                    && status != OperationStatus.OUTPUT_FULL) {
                runMode = RunMode.STOPPED;
            }
        } else {
            completed = scanBlocks(serverLevel, budget);
        }
        if (completed > 0 && status == OperationStatus.IDLE) {
            status = OperationStatus.WORKING;
        }
        setChanged();
        updateActiveState();
        return completed;
    }

    private int scanBlocks(ServerLevel serverLevel, int budget) {
        int completed = 0;
        for (int inspected = 0; inspected < budget && runMode != RunMode.STOPPED; inspected++) {
            BlockPos target = workArea.positionAt(cursor);
            if (!serverLevel.isLoaded(target)) {
                status = OperationStatus.UNLOADED;
                break;
            }
            BlockState targetState = serverLevel.getBlockState(target);
            if (machineType() != AreaMachineType.ACTIVATOR || activatorTarget == ActivatorTarget.BLOCK) {
                if (targetState.isAir()
                        || !acceptsHarvest(new ItemStack(targetState.getBlock().asItem()))) {
                    advanceCursor();
                    continue;
                }
            }
            BlockOperationResult result = switch (machineType()) {
                case MINER, ADVANCED_MINER -> breakTarget(serverLevel, target);
                case REPLACER -> replaceTarget(serverLevel, target);
                case ACTIVATOR -> activateTarget(serverLevel, target);
                case ITEM_COLLECTOR -> BlockOperationResult.FAILED;
            };
            if (result == BlockOperationResult.SUCCESS) {
                completed++;
                advanceCursor();
            } else if (result == BlockOperationResult.NO_TARGET) {
                advanceCursor();
            } else if (result == BlockOperationResult.UNLOADED) {
                status = OperationStatus.UNLOADED;
                break;
            } else if (result == BlockOperationResult.DENIED) {
                status = OperationStatus.PROTECTED;
                break;
            } else if (result == BlockOperationResult.NO_OUTPUT_SPACE) {
                status = OperationStatus.OUTPUT_FULL;
                break;
            } else {
                status = machineType() == AreaMachineType.REPLACER
                        ? OperationStatus.NO_MATERIAL : OperationStatus.PROTECTED;
                break;
            }
        }
        setChanged();
        return completed;
    }

    /** States 1 (single pass) stop at the end of the area; state 4 (loop) wraps. */
    private void advanceCursor() {
        cursor = (cursor + 1L) % workArea.volume();
        if (cursor == 0L && runMode == RunMode.ONCE) {
            runMode = RunMode.STOPPED;
        }
    }

    private BlockOperationResult breakTarget(ServerLevel serverLevel, BlockPos target) {
        ItemStack targetItem = new ItemStack(serverLevel.getBlockState(target).getBlock().asItem());
        // Original UtilBuilder.getDropsFromBlock needs no tool; fortune and silk touch come from filters.
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        if (matchesFilter(FORTUNE_FILTER_SLOT, targetItem)) {
            tool.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            tool.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.FORTUNE), 3);
        }
        if (matchesFilter(SILK_TOUCH_FILTER_SLOT, targetItem)) {
            tool.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            tool.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.SILK_TOUCH), 1);
        }
        return BlockOperationService.breakBlock(
                serverLevel,
                worldPosition,
                target,
                owner,
                tool,
                stack -> { },
                stacks -> insertOutputs(stacks, true).isEmpty(),
                stacks -> insertOutputs(stacks, false));
    }

    private BlockOperationResult replaceTarget(ServerLevel serverLevel, BlockPos target) {
        int replacementSlot = replacementSlot();
        if (replacementSlot < 0) {
            return BlockOperationResult.FAILED;
        }
        ItemStack replacement = inventory.getStackInSlot(replacementSlot);
        BlockItem blockItem = (BlockItem)replacement.getItem();
        BlockState replacementState = blockItem.getBlock().defaultBlockState();
        if (serverLevel.getBlockState(target).is(replacementState.getBlock())) {
            return BlockOperationResult.NO_TARGET;
        }
        FakePlayer placer = FakePlayerFactory.get(serverLevel, new GameProfile(owner, "[Clayium]"));
        placer.setPos(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D);
        placer.setItemInHand(InteractionHand.MAIN_HAND, replacement.copyWithCount(1));
        BlockPos against = target.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atBottomCenterOf(target), Direction.UP, against, false);
        BlockPlaceContext context = new BlockPlaceContext(placer, InteractionHand.MAIN_HAND,
                placer.getMainHandItem(), hit);
        BlockState placement = blockItem.getBlock().getStateForPlacement(context);
        if (placement == null || !placement.canSurvive(serverLevel, target)
                || !serverLevel.mayInteract(placer, target)
                || EventHooks.onBlockPlace(placer,
                        BlockSnapshot.create(serverLevel.dimension(), serverLevel, target), Direction.UP)) {
            placer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return BlockOperationResult.DENIED;
        }
        placer.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        BlockOperationResult result = breakTarget(serverLevel, target);
        if (result != BlockOperationResult.SUCCESS) {
            return result;
        }
        serverLevel.setBlockAndUpdate(target, placement);
        ItemStack storedReplacement = inventory.getStackInSlot(replacementSlot).copy();
        storedReplacement.shrink(1);
        inventory.setStackInSlot(replacementSlot, storedReplacement);
        return BlockOperationResult.SUCCESS;
    }

    private BlockOperationResult activateTarget(ServerLevel serverLevel, BlockPos target) {
        if (rayTraceMemory == null) {
            rayTraceMemory = RayTraceMemory.standard(
                    getBlockState().getValue(AreaMachineBlock.FACING));
        }
        if (activatorTarget != ActivatorTarget.BLOCK) {
            BlockOperationService.UseResult entityUse = BlockOperationService.useEntity(
                    serverLevel, worldPosition, target, owner,
                    useStack().copyWithCount(1),
                    rayTraceMemory.startAt(target), rayTraceMemory.look(), activatorRayTrace, activatorSneaking);
            if (entityUse.result() == BlockOperationResult.SUCCESS) {
                return finishActivation(serverLevel, entityUse);
            }
            if (entityUse.result() == BlockOperationResult.DENIED) {
                return BlockOperationResult.DENIED;
            }
            if (activatorTarget == ActivatorTarget.ENTITY) {
                return entityUse.result();
            }
        }
        BlockState targetState = serverLevel.getBlockState(target);
        if (targetState.isAir() || !acceptsHarvest(new ItemStack(targetState.getBlock().asItem()))) {
            return BlockOperationResult.NO_TARGET;
        }
        ItemStack input = useStack();
        ItemStack used = input.isEmpty() ? ItemStack.EMPTY : input.copyWithCount(1);
        BlockHitResult hit = rayTraceMemory.directHit(target);
        if (activatorRayTrace) {
            Vec3 start = rayTraceMemory.startAt(target);
            Vec3 end = start.add(rayTraceMemory.look().scale(Math.max(3.0D, rayTraceMemory.relativeEye().length())));
            var clipped = serverLevel.clip(new net.minecraft.world.level.ClipContext(
                    start, end, net.minecraft.world.level.ClipContext.Block.OUTLINE,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    FakePlayerFactory.get(serverLevel, new GameProfile(owner, "[Clayium]"))));
            if (clipped.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
                return BlockOperationResult.NO_TARGET;
            }
            hit = clipped;
        }
        BlockOperationService.UseResult use = BlockOperationService.useBlock(
                serverLevel, worldPosition, hit, owner, used, activatorSneaking);
        return finishActivation(serverLevel, use);
    }

    private BlockOperationResult finishActivation(
            ServerLevel serverLevel, BlockOperationService.UseResult use) {
        if (use.result() != BlockOperationResult.SUCCESS) {
            return use.result();
        }

        int useSlot = useSlot();
        ItemStack input = useSlot < 0 ? ItemStack.EMPTY : inventory.getStackInSlot(useSlot);
        List<ItemStack> returned = new ArrayList<>(use.returnedItems());
        if (!input.isEmpty()) {
            ItemStack remaining = input.copy();
            remaining.shrink(1);
            ItemStack held = use.heldStack();
            if (!held.isEmpty() && input.is(held.getItem()) && held.getCount() == 1) {
                if (remaining.isEmpty()) {
                    remaining = held.copy();
                } else if (ItemStack.isSameItemSameComponents(remaining, held)) {
                    remaining.grow(1);
                } else {
                    returned.add(held.copy());
                }
            } else if (!held.isEmpty()) {
                returned.add(held.copy());
            }
            inventory.setStackInSlot(useSlot, remaining);
        }
        returned.forEach(stack -> insertOutputs(List.of(stack), false).forEach(remainder ->
                Containers.dropItemStack(serverLevel,
                        worldPosition.getX() + 0.5D,
                        worldPosition.getY() + 1.0D,
                        worldPosition.getZ() + 0.5D,
                        remainder)));
        return BlockOperationResult.SUCCESS;
    }

    private int collectItems(ServerLevel serverLevel, int budget) {
        for (int chunkX = workArea.minimum().getX() >> 4;
                chunkX <= workArea.maximum().getX() >> 4; chunkX++) {
            for (int chunkZ = workArea.minimum().getZ() >> 4;
                    chunkZ <= workArea.maximum().getZ() >> 4; chunkZ++) {
                if (!serverLevel.isLoaded(new BlockPos(chunkX << 4, workArea.minimum().getY(), chunkZ << 4))) {
                    status = OperationStatus.UNLOADED;
                    return 0;
                }
            }
        }
        int collected = 0;
        List<ItemEntity> entities = serverLevel.getEntitiesOfClass(
                ItemEntity.class, workArea.box(), entity -> entity.isAlive() && acceptsHarvest(entity.getItem()));
        for (ItemEntity entity : entities) {
            if (collected >= budget) {
                break;
            }
            ItemStack stack = entity.getItem();
            if (!insertOutputs(List.of(stack), true).isEmpty()) {
                status = OperationStatus.OUTPUT_FULL;
                break;
            }
            insertOutputs(List.of(stack), false);
            entity.discard();
            collected++;
        }
        return collected;
    }

    private boolean acceptsHarvest(ItemStack candidate) {
        return !candidate.isEmpty() && ItemFilter.accepts(
                inventory.getStackInSlot(HARVEST_FILTER_SLOT), candidate);
    }

    /** Original filterFflag/filterSflag: only a remembered filter item enables fortune or silk touch. */
    private boolean matchesFilter(int slot, ItemStack candidate) {
        ItemStack filter = inventory.getStackInSlot(slot);
        return ItemFilter.isFilter(filter) && ItemFilter.accepts(filter, candidate);
    }

    /** Output grid size: 3x3, or 4x2 for the Tier 10 replacer. */
    public int outputSlots() {
        return machineType() == AreaMachineType.REPLACER ? REPLACEMENT_START : 9;
    }

    /** Original replacer: the first ItemBlock in the second 4x2 grid. */
    private int replacementSlot() {
        for (int slot = REPLACEMENT_START; slot < GRID_CAPACITY; slot++) {
            if (inventory.getStackInSlot(slot).getItem() instanceof BlockItem) {
                return slot;
            }
        }
        return -1;
    }

    /** Original TileAreaActivator: the first non-empty grid slot is used. */
    private int useSlot() {
        for (int slot = 0; slot < outputSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                return slot;
            }
        }
        return -1;
    }

    private ItemStack useStack() {
        int slot = useSlot();
        return slot < 0 ? ItemStack.EMPTY : inventory.getStackInSlot(slot);
    }

    private List<ItemStack> insertOutputs(List<ItemStack> stacks, boolean simulate) {
        List<ItemStack> remainingStacks = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack remaining = stack.copy();
            for (int slot = 0; slot < outputSlots() && !remaining.isEmpty(); slot++) {
                remaining = inventory.insertItem(slot, remaining, simulate);
            }
            if (!remaining.isEmpty()) {
                remainingStacks.add(remaining);
            }
        }
        return remainingStacks;
    }

    public void configureArea(AreaBounds area) {
        workArea = level == null ? area : area.clampHeight(level);
        targetDimension = level == null ? targetDimension : level.dimension().location().toString();
        cursor = 0L;
        // Applying a marker area re-arms a single pass (original state 0).
        runMode = RunMode.ONCE;
        setChangedAndSync();
    }

    public AreaBounds workArea() {
        return workArea;
    }

    public long cursor() {
        return cursor;
    }

    public OperationStatus status() {
        return status;
    }

    @Override
    public long storedEnergy() {
        return energy.stored().internalValue();
    }

    public long progress() {
        return progress;
    }

    public AreaMachineType machineType() {
        return ((AreaMachineBlock)getBlockState().getBlock()).machineType();
    }

    @Override
    public boolean receiveLaser(LaserBeam beam) {
        if (beam.isEmpty()) {
            return false;
        }
        pendingLaser = LaserBeam.merge(pendingLaser, beam);
        return true;
    }

    @Override
    public ItemStackHandler inventory() {
        return inventory;
    }

    /**
     * Original GuiAreaMiner/GuiAreaActivator buttons: 0 start (single pass), 1 stop, 2 loop,
     * 3 box appearance 0-2, 4 target, 5 ray trace, 6 sneak.
     */
    @Override
    public boolean applyControl(int action) {
        switch (action) {
            case 0 -> { runMode = RunMode.ONCE; cursor = 0L; }
            case 1 -> runMode = RunMode.STOPPED;
            case 2 -> { runMode = RunMode.LOOP; cursor = 0L; }
            case 3 -> boxState = (boxState + 1) % 3;
            case 4, 5, 6 -> {
                if (machineType() != AreaMachineType.ACTIVATOR) {
                    return false;
                }
                switch (action) {
                    case 4 -> activatorTarget = activatorTarget.next();
                    case 5 -> activatorRayTrace = !activatorRayTrace;
                    default -> activatorSneaking = !activatorSneaking;
                }
            }
            default -> {
                return false;
            }
        }
        setChangedAndSync();
        return true;
    }

    public void setRayTraceMemory(RayTraceMemory memory) {
        if (machineType() == AreaMachineType.ACTIVATOR) {
            rayTraceMemory = memory;
            setChangedAndSync();
        }
    }

    @Override public int guiTier() { return machineType().tier(); }
    @Override public int gridColumns() { return machineType() == AreaMachineType.REPLACER ? 4 : 3; }
    @Override public int gridRows() { return machineType() == AreaMachineType.REPLACER ? 2 : 3; }
    @Override public boolean hasReplacementGrid() { return machineType() == AreaMachineType.REPLACER; }
    @Override public int harvestFilterSlot() { return HARVEST_FILTER_SLOT; }
    @Override public int fortuneFilterSlot() { return guiTier() >= 9 ? FORTUNE_FILTER_SLOT : -1; }
    @Override public int silkTouchFilterSlot() { return guiTier() >= 9 ? SILK_TOUCH_FILTER_SLOT : -1; }
    @Override public int fuelSlot() { return FUEL_SLOT; }
    @Override public boolean isActivator() { return machineType() == AreaMachineType.ACTIVATOR; }
    @Override public long laserEnergy() { return displayedLaser; }

    public RunMode runMode() {
        return runMode;
    }

    public int boxState() {
        return boxState;
    }

    @Override
    public ActivatorTarget activatorTarget() {
        return activatorTarget;
    }

    @Override
    public boolean activatorRayTrace() {
        return activatorRayTrace;
    }

    @Override
    public boolean activatorSneaking() {
        return activatorSneaking;
    }

    public RayTraceMemory rayTraceMemory() {
        return rayTraceMemory;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AreaMachineMenu(containerId, playerInventory, this);
    }

    public IItemHandler itemHandler(@Nullable Direction direction) {
        return itemHandler;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public Container dropInventory() {
        SimpleContainer drops = new SimpleContainer(GRID_CAPACITY + 1);
        for (int slot = 0; slot < GRID_CAPACITY; slot++) {
            drops.setItem(slot, inventory.getStackInSlot(slot).copy());
        }
        drops.setItem(GRID_CAPACITY, inventory.getStackInSlot(FUEL_SLOT).copy());
        return drops;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putLong("progress", progress);
        tag.putLong("area_min", workArea.minimum().asLong());
        tag.putLong("area_max", workArea.maximum().asLong());
        tag.putLong("cursor", cursor);
        tag.putString("target_dimension", targetDimension);
        tag.putString("status", status.name());
        tag.putString("activator_target", activatorTarget.name());
        tag.putBoolean("activator_ray_trace", activatorRayTrace);
        tag.putBoolean("activator_sneaking", activatorSneaking);
        tag.putString("run_mode", runMode.name());
        tag.putInt("box_state", boxState);
        if (rayTraceMemory != null) {
            tag.put("ray_trace_memory", rayTraceMemory.save());
        }
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.getCompound("energy"));
        }
        progress = tag.getLong("progress");
        if (tag.contains("area_min") && tag.contains("area_max")) {
            workArea = new AreaBounds(BlockPos.of(tag.getLong("area_min")), BlockPos.of(tag.getLong("area_max")));
        }
        cursor = Math.floorMod(tag.getLong("cursor"), workArea.volume());
        targetDimension = tag.getString("target_dimension");
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        status = tag.contains("status") ? OperationStatus.valueOf(tag.getString("status")) : OperationStatus.IDLE;
        activatorTarget = tag.contains("activator_target")
                ? ActivatorTarget.valueOf(tag.getString("activator_target")) : ActivatorTarget.BLOCK;
        activatorRayTrace = tag.getBoolean("activator_ray_trace");
        activatorSneaking = tag.getBoolean("activator_sneaking");
        runMode = tag.contains("run_mode") ? RunMode.valueOf(tag.getString("run_mode")) : RunMode.ONCE;
        boxState = Math.floorMod(tag.getInt("box_state"), 3);
        rayTraceMemory = tag.contains("ray_trace_memory")
                ? RayTraceMemory.load(tag.getCompound("ray_trace_memory")) : null;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("area_min", workArea.minimum().asLong());
        tag.putLong("area_max", workArea.maximum().asLong());
        tag.putInt("box_state", boxState);
        return tag;
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void updateActiveState() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        boolean active = owner != null && status == OperationStatus.WORKING;
        if (state.getValue(AreaMachineBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(AreaMachineBlock.ACTIVE, active), Block.UPDATE_CLIENTS);
        }
    }

    public enum OperationStatus {
        IDLE,
        WORKING,
        NO_OWNER,
        WRONG_DIMENSION,
        UNLOADED,
        PROTECTED,
        OUTPUT_FULL,
        NO_MATERIAL,
        NO_ENERGY
    }

    public enum RunMode {
        ONCE,
        STOPPED,
        LOOP
    }

    public enum ActivatorTarget {
        BLOCK,
        ENTITY,
        BOTH;

        public ActivatorTarget next() {
            return values()[(ordinal() + 1) % values().length];
        }
    }

    /** Automation sees the grid (and the energy slot); filters stay GUI-only memories. */
    private final class AreaItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return GRID_CAPACITY + 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(exposedSlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            int actual = exposedSlot(slot);
            boolean accepted = actual == FUEL_SLOT
                    || machineType() == AreaMachineType.ACTIVATOR && actual < outputSlots()
                    || machineType() == AreaMachineType.REPLACER && actual >= REPLACEMENT_START
                            && actual < GRID_CAPACITY;
            return accepted ? inventory.insertItem(actual, stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            int actual = exposedSlot(slot);
            return actual < outputSlots() ? inventory.extractItem(actual, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return inventory.getSlotLimit(exposedSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return inventory.isItemValid(exposedSlot(slot), stack);
        }

        private int exposedSlot(int slot) {
            return slot < GRID_CAPACITY ? slot : FUEL_SLOT;
        }
    }
}
