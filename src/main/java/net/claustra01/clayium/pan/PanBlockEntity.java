package net.claustra01.clayium.pan;

import java.util.Map;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.ClayEnergyStorage;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.BlastFurnaceBlock;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class PanBlockEntity extends BlockEntity implements MenuProvider {
    public static final int SAMPLE_SLOT = 0;
    public static final int ANTIMATTER_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int FUEL_SLOT = 3;

    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SAMPLE_SLOT -> PanRecipeClosure.isPlain(stack);
                case ANTIMATTER_SLOT -> stack.is(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get());
                case OUTPUT_SLOT -> false;
                case FUEL_SLOT -> ClayEnergyStorage.fuelValue(stack).internalValue() > 0L;
                default -> false;
            };
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot == SAMPLE_SLOT ? 1 : super.getSlotLimit(slot);
        }
    };
    private final ClayEnergyStorage energy = new ClayEnergyStorage();
    private PanNetwork.Scan scan;
    private PanRecipeClosure.Calculation closureCalculation;
    private PanRecipeClosure.Result closure = new PanRecipeClosure.Result(Map.of(), Map.of());
    private BlockPos connectedCore;
    private long progress;
    private long requiredProgress;
    private long requiredEnergy;
    private ResourceLocation activeSample;
    private long activeConsumption;
    private long knownNetworkVersion = -1L;
    private long knownRecipeVersion = -1L;
    private Set<BlockPos> knownAdapters = Set.of();
    private Set<RecipeType<?>> knownVanillaTypes = Set.of();
    private Map<ClayiumRecipeMethod, Integer> knownMachineTiers = Map.of();
    private boolean closureReady;
    private long lastScanTick = Long.MIN_VALUE;
    private int nodeCount;
    private int adapterCount;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int)(progress & 0xFFFFL);
                case 1 -> (int)((progress >>> 16) & 0xFFFFL);
                case 2 -> (int)(requiredProgress & 0xFFFFL);
                case 3 -> (int)((requiredProgress >>> 16) & 0xFFFFL);
                case 4 -> (int)(requiredEnergy & 0xFFFFL);
                case 5 -> (int)((requiredEnergy >>> 16) & 0xFFFFL);
                case 6 -> (int)((requiredEnergy >>> 32) & 0xFFFFL);
                case 7 -> (int)((requiredEnergy >>> 48) & 0xFFFFL);
                case 8 -> kind() == PanBlock.Kind.DUPLICATOR && connectedCore() != null
                        ? connectedCore().closure.costs().size() : closure.costs().size();
                case 9 -> nodeCount;
                case 10 -> adapterCount;
                case 11 -> kind().ordinal();
                case 12 -> tier();
                case 13 -> connectedCore() != null && connectedCore().closure.consumption(
                        inventory.getStackInSlot(SAMPLE_SLOT)) > 0L ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 14;
        }
    };

    public PanBlockEntity(BlockPos pos, BlockState state) {
        super(PanRegistration.BLOCK_ENTITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PanBlockEntity pan) {
        pan.tickServer();
    }

    private void tickServer() {
        if (level == null) {
            return;
        }
        long version = PanNetwork.changeVersion(level);
        if (scan == null && (knownNetworkVersion != version
                || kind() == PanBlock.Kind.CORE && knownRecipeVersion != PanNetwork.recipeVersion()
                || level.getGameTime() - lastScanTick >= PanNetwork.REFRESH_INTERVAL)) {
            scan = PanNetwork.begin(level, worldPosition);
            lastScanTick = level.getGameTime();
        }
        if (scan != null && scan.advance(PanNetwork.NODES_PER_TICK)) {
            finishScan(scan);
            scan = null;
            knownNetworkVersion = PanNetwork.changeVersion(level);
        }
        if (kind() == PanBlock.Kind.CORE && closureCalculation != null
                && closureCalculation.advance(PanNetwork.NODES_PER_TICK)) {
            closure = closureCalculation.result();
            closureCalculation = null;
            closureReady = true;
            setChanged();
        }
        if (kind() == PanBlock.Kind.DUPLICATOR) {
            tickDuplicator();
        }
    }

    private void finishScan(PanNetwork.Scan finished) {
        nodeCount = finished.nodeCount();
        adapterCount = finished.adapters().size();
        if (kind() == PanBlock.Kind.CORE) {
            beginClosure(finished);
        } else if (kind() == PanBlock.Kind.DUPLICATOR) {
            connectedCore = finished.cores().stream().findFirst().orElse(null);
        }
        setChanged();
    }

    private void beginClosure(PanNetwork.Scan finished) {
        Set<RecipeType<?>> vanillaTypes = new HashSet<>();
        Map<ClayiumRecipeMethod, Integer> machineTiers = new EnumMap<>(ClayiumRecipeMethod.class);
        for (BlockPos adapter : finished.adapters()) {
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                var block = level.getBlockState(adapter.relative(direction)).getBlock();
                if (block instanceof CraftingTableBlock) vanillaTypes.add(RecipeType.CRAFTING);
                if (block instanceof FurnaceBlock) vanillaTypes.add(RecipeType.SMELTING);
                if (block instanceof BlastFurnaceBlock) vanillaTypes.add(RecipeType.BLASTING);
                if (block instanceof SmokerBlock) vanillaTypes.add(RecipeType.SMOKING);
                if (block instanceof ClayMachineBlock machine) {
                    machineTiers.merge(machine.series().recipeMethod(), machine.tier(), Math::max);
                }
            }
        }
        Set<BlockPos> adapters = Set.copyOf(finished.adapters());
        long recipeVersion = PanNetwork.recipeVersion();
        if (adapters.equals(knownAdapters) && vanillaTypes.equals(knownVanillaTypes)
                && machineTiers.equals(knownMachineTiers) && recipeVersion == knownRecipeVersion) {
            return;
        }
        knownAdapters = adapters;
        knownVanillaTypes = Set.copyOf(vanillaTypes);
        knownMachineTiers = Map.copyOf(machineTiers);
        knownRecipeVersion = recipeVersion;
        closureCalculation = PanRecipeClosure.begin(level.getRecipeManager(), level.registryAccess(),
                vanillaTypes, machineTiers);
    }

    private void tickDuplicator() {
        ItemStack sample = inventory.getStackInSlot(SAMPLE_SLOT);
        PanBlockEntity core = connectedCore();
        long consumption = core == null || !core.closureReady ? 0L : core.closure.consumption(sample);
        ResourceLocation sampleId = sample.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(sample.getItem());
        if (!java.util.Objects.equals(activeSample, sampleId)
                || core != null && core.closureReady && activeConsumption != consumption) {
            progress = 0L;
            activeSample = sampleId;
            activeConsumption = consumption;
        }
        requiredEnergy = consumption;
        requiredProgress = processingTicks(tier(), consumption);
        if (consumption <= 0L || !canOutput(sample) || inventory.getStackInSlot(ANTIMATTER_SLOT).isEmpty()) {
            return;
        }
        ClayEnergy needed = ClayEnergy.ofInternal(consumption);
        if (!energy.stored().canAfford(needed)) {
            energy.produceClayEnergy(inventory.getStackInSlot(FUEL_SLOT));
        }
        if (!energy.stored().canAfford(needed)) {
            return;
        }
        progress++;
        if (progress < requiredProgress) {
            setChanged();
            return;
        }
        if (energy.consume(needed)) {
            inventory.extractItem(ANTIMATTER_SLOT, 1, false);
            ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
            if (output.isEmpty()) {
                inventory.setStackInSlot(OUTPUT_SLOT, sample.copyWithCount(1));
            } else {
                output.grow(1);
            }
        }
        progress = 0L;
        setChanged();
    }

    private boolean canOutput(ItemStack sample) {
        if (!PanRecipeClosure.isPlain(sample)) {
            return false;
        }
        ItemStack output = inventory.getStackInSlot(OUTPUT_SLOT);
        return output.isEmpty() || ItemStack.isSameItemSameComponents(output, sample)
                && output.getCount() < output.getMaxStackSize();
    }

    @Nullable
    private PanBlockEntity connectedCore() {
        if (level == null || connectedCore == null || !level.hasChunkAt(connectedCore)) {
            return null;
        }
        return level.getBlockEntity(connectedCore) instanceof PanBlockEntity pan
                && pan.kind() == PanBlock.Kind.CORE ? pan : null;
    }

    public static long processingTicks(int tier, long consumption) {
        long perTick = Math.max(1L, (long)(100_000L * TierManager.general(tier).energyMultiplier()));
        return Math.max(1L, consumption / perTick + (consumption % perTick == 0L ? 0L : 1L));
    }

    public PanBlock.Kind kind() {
        return ((PanBlock)getBlockState().getBlock()).kind();
    }

    public int tier() {
        return ((PanBlock)getBlockState().getBlock()).tier();
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    public ClayEnergyStorage energy() {
        return energy;
    }

    public PanRecipeClosure.Result closure() {
        return closure;
    }

    public int nodeCount() {
        return nodeCount;
    }

    public long lastScanTick() {
        return lastScanTick;
    }

    public int adapterCount() {
        return adapterCount;
    }

    public void receiveEnergy(long internal) {
        energy.add(ClayEnergy.ofInternal(Math.max(0L, internal)));
    }

    public Container dropInventory() {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            container.setItem(slot, inventory.getStackInSlot(slot).copy());
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return container;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PanMenu(containerId, playerInventory, this, data);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) PanNetwork.invalidate(level);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        tag.putLong("progress", progress);
        tag.putLong("active_consumption", activeConsumption);
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
        progress = Math.max(0L, tag.getLong("progress"));
        activeSample = inventory.getStackInSlot(SAMPLE_SLOT).isEmpty() ? null
                : BuiltInRegistries.ITEM.getKey(inventory.getStackInSlot(SAMPLE_SLOT).getItem());
        activeConsumption = Math.max(0L, tag.getLong("active_consumption"));
        knownNetworkVersion = -1L;
        knownRecipeVersion = -1L;
        knownAdapters = Set.of();
        scan = null;
        closureCalculation = null;
        connectedCore = null;
        nodeCount = 0;
        adapterCount = 0;
        closureReady = false;
        closure = new PanRecipeClosure.Result(Map.of(), Map.of());
    }
}
