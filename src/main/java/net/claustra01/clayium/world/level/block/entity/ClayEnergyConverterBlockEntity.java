/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.level.block.entity;

import java.util.EnumMap;
import net.claustra01.clayium.config.ClayiumConfig;
import net.claustra01.clayium.energy.EnergeticClayFuel;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.claustra01.clayium.world.inventory.ClayEnergyConverterMenu;
import net.claustra01.clayium.world.level.block.AbstractTieredIoMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class ClayEnergyConverterBlockEntity extends AbstractConfigurableMachineBlockEntity {
    private int storedFe;
    private long extractionBudgetTick = Long.MIN_VALUE;
    private int extractedThisTick;
    private final EnumMap<Direction, BlockCapabilityCache<IEnergyStorage, Direction>> neighborEnergyCaches =
            new EnumMap<>(Direction.class);
    private final IEnergyStorage unrestrictedFe = new OutputEnergyStorage(null);
    public ClayEnergyConverterBlockEntity(BlockPos pos, BlockState state) { super(ClayiumRegistries.CLAY_ENERGY_CONVERTER_BLOCK_ENTITY.get(), pos, state, 1, true); }
    @Override protected void tickMachine() {
        if (level == null) return;
        resetExtractionBudget();
        long ce = energyPerTick();
        int produced = productionPerTick();
        if (storedFe <= capacity() - produced) {
            if (energy.extract(ce, true) != ce) consumeFuel(0);
            if (energy.extract(ce, true) == ce) {
                energy.extract(ce, false);
                storedFe += produced;
                setChanged();
            }
        }
        // Match the original generator: redstone pauses FE output, while conversion may continue
        // until the internal FE store fills.
        if (!level.hasNeighborSignal(worldPosition)) pushForgeEnergy();
    }
    public IEnergyStorage feStorage(Direction side) { return side == null ? unrestrictedFe : new OutputEnergyStorage(side); }
    public int productionPerTick() { return scaled((int)Math.max(1, Math.round(switch(tierIndex()) { case 4 -> 10; case 5 -> 30; case 6 -> 90; case 7 -> 270; case 8 -> 810; default -> 2430; } * overclockFactor()))); }
    public int outputPerTick() { return productionPerTick(); }
    public int capacity() {
        int base = switch(tierIndex()) { case 4 -> 10_000; case 5 -> 30_000; case 6 -> 90_000;
            case 7 -> 270_000; case 8 -> 810_000; default -> 2_430_000; };
        return (int)Math.min(Integer.MAX_VALUE, (long)base * energySlotLimit());
    }
    private static int scaled(int value) { return (int)Math.min(Integer.MAX_VALUE, Math.max(1D, value * ClayiumConfig.CE_FE_CONVERSION_MULTIPLIER.get())); }
    @Override protected boolean acceptsClayEnergy() { return true; }
    @Override protected boolean isExternalInput(int slot, ItemStack stack) { return slot == 0 && EnergeticClayFuel.isFuel(stack); }
    @Override protected boolean isExternalOutput(int slot) { return false; }
    @Override protected boolean isNormalInputSlot(int slot) { return false; }
    @Override protected boolean isEnergySlot(int slot) { return slot == 0; }
    @Override public int totalProgress() { return 1; }
    @Override public int tierIndex() { return getBlockState().getBlock() instanceof AbstractTieredIoMachineBlock block ? block.tier().progressionIndex() : 4; }
    @Override public long energyPerTick() { return Math.max(1L, Math.round(switch(tierIndex()) { case 4 -> 100L; case 5 -> 1_000L; case 6 -> 10_000L; case 7 -> 100_000L; case 8 -> 1_000_000L; default -> 10_000_000L; } * ClayiumConfig.CE_FE_CONVERSION_MULTIPLIER.get() * overclockFactor())); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 0 && EnergeticClayFuel.isFuel(stack); }
    @Override protected int menuDataSize() { return 10; }
    @Override protected int additionalMenuData(int index) { return switch(index) {
        case 4 -> storedFe;
        case 5 -> capacity();
        case 6 -> productionPerTick();
        case 7 -> outputPerTick();
        case 8 -> (int) energyPerTick();
        case 9 -> (int) (energyPerTick() >>> 32);
        default -> 0;
    }; }
    @Override protected Component getDefaultName() { return Component.translatable(getBlockState().getBlock().getDescriptionId()); }
    @Override protected AbstractContainerMenu createMenu(int id, Inventory inventory) { return new ClayEnergyConverterMenu(id, inventory, this, menuData()); }
    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.saveAdditional(tag, registries); tag.putInt("ForgeEnergy", storedFe); }
    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) { super.loadAdditional(tag, registries); storedFe = Math.max(0, Math.min(capacity(), tag.getInt("ForgeEnergy"))); }

    private void pushForgeEnergy() {
        if (!(level instanceof ServerLevel serverLevel) || storedFe <= 0) return;
        for (Direction side : Direction.values()) {
            if (extractionRoute(side) < 0 || remainingExtractionBudget() <= 0) continue;
            IEnergyStorage target = neighborEnergyCaches.computeIfAbsent(side, direction ->
                    BlockCapabilityCache.create(
                            Capabilities.EnergyStorage.BLOCK,
                            serverLevel,
                            worldPosition.relative(direction),
                            direction.getOpposite(),
                            () -> !isRemoved(),
                            () -> {})).getCapability();
            if (target == null || !target.canReceive()) continue;
            int offered = Math.min(storedFe, remainingExtractionBudget());
            int accepted = Math.min(offered, Math.max(0, target.receiveEnergy(offered, false)));
            if (accepted > 0) {
                storedFe -= accepted;
                extractedThisTick += accepted;
                setChanged();
            }
        }
    }

    private void resetExtractionBudget() {
        long gameTime = level == null ? Long.MIN_VALUE : level.getGameTime();
        if (extractionBudgetTick != gameTime) {
            extractionBudgetTick = gameTime;
            extractedThisTick = 0;
        }
    }

    private int remainingExtractionBudget() {
        resetExtractionBudget();
        return Math.max(0, outputPerTick() - extractedThisTick);
    }

    private final class OutputEnergyStorage implements IEnergyStorage {
        private final Direction side;
        private OutputEnergyStorage(Direction side) { this.side = side; }
        private boolean enabled() {
            return (side == null || extractionRoute(side) >= 0)
                    && level != null && !level.hasNeighborSignal(worldPosition);
        }
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            if (!enabled() || maxExtract <= 0) return 0;
            int extracted = Math.min(Math.min(maxExtract, remainingExtractionBudget()), storedFe);
            if (!simulate && extracted > 0) {
                storedFe -= extracted;
                extractedThisTick += extracted;
                setChanged();
            }
            return extracted;
        }
        @Override public int getEnergyStored() { return storedFe; }
        @Override public int getMaxEnergyStored() { return capacity(); }
        @Override public boolean canExtract() { return enabled(); }
        @Override public boolean canReceive() { return false; }
    }
}
