package net.claustra01.clayium.multiblock.careactor;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ProgressionRateRules;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class CaReactorCoreBlockEntity extends ClayMachineBlockEntity implements LaserReceiver {
    private static final String[] PURE_ANTIMATTER_OUTPUTS = {
            "pure_antimatter",
            "compressed_pure_antimatter",
            "double_compressed_pure_antimatter",
            "triple_compressed_pure_antimatter",
            "quadruple_compressed_pure_antimatter",
            "quintuple_compressed_pure_antimatter",
            "sextuple_compressed_pure_antimatter",
            "septuple_compressed_pure_antimatter",
            "octuple_compressed_pure_antimatter"
    };

    private boolean structureDirty = true;
    private CaReactorStructure structure = new CaReactorStructure(
            false, 0, 0, 0.0D, java.util.Set.of(), java.util.Optional.empty(), "not validated");
    private long structureValidationCount;
    private boolean notifierTracked;
    private LaserBeam receivedLaser = new LaserBeam(0L, 0L, 0L);
    private long receivedLaserTick = Long.MIN_VALUE;

    public CaReactorCoreBlockEntity(BlockPos pos, BlockState state) {
        super(CaReactorRegistration.CORE_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    protected void setState(MachineState newState) {
        if (state() != newState) {
            super.setState(newState);
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            CaReactorStructureNotifier.track(this);
            notifierTracked = true;
        }
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, CaReactorCoreBlockEntity core) {
        core.tickServer(level.random);
    }

    @Override
    public void tickServer(RandomSource random) {
        if (level instanceof ServerLevel serverLevel) {
            if (!notifierTracked) {
                CaReactorStructureNotifier.track(this);
                notifierTracked = true;
            }
            if (structureDirty) {
                validateStructure(serverLevel);
            }
        }
        super.tickServer(random);
    }

    private void validateStructure(ServerLevel level) {
        structureDirty = false;
        structureValidationCount++;
        CaReactorStructure previous = structure;
        structure = CaReactorStructure.inspect(level, worldPosition, tier());
        if (activeRecipe() != null && (!structure.formed()
                || previous.hullCount() != structure.hullCount()
                || Double.compare(previous.averageRank(), structure.averageRank()) != 0)) {
            abortProcess();
        }
        BlockState state = getBlockState();
        if (state.getValue(CaReactorCoreBlock.FORMED) != structure.formed()) {
            level.setBlock(worldPosition, state.setValue(CaReactorCoreBlock.FORMED, structure.formed()), 2);
        }
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public void markStructureDirty() {
        structureDirty = true;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            validateStructure(serverLevel);
        }
        return super.createMenu(containerId, inventory, player);
    }

    boolean observes(BlockPos changedPos) {
        if (Math.max(Math.max(Math.abs(worldPosition.getX() - changedPos.getX()),
                Math.abs(worldPosition.getY() - changedPos.getY())),
                Math.abs(worldPosition.getZ() - changedPos.getZ())) <= CaReactorStructure.SEARCH_RADIUS
                || structure.components().contains(changedPos)) {
            return true;
        }
        for (Direction direction : Direction.values()) {
            if (structure.components().contains(changedPos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean canProcess() {
        return structure.formed() && structure.formula().isPresent();
    }

    @Override
    protected TierManager.ProcessingParameters processingParameters(
            ResourceLocation recipeId, ClayiumMachineRecipe recipe) {
        CaReactorFormula formula = structure.formula().orElse(null);
        if (formula == null) {
            return null;
        }
        CaReactorFormula.ScaledProcess scaled = formula.scale(
                ProgressionRateRules.adjustedRecipeTicks(recipe),
                recipe.clayEnergy()).orElse(null);
        if (scaled == null) {
            return null;
        }
        long total = scaled.energyPerProgress() == 0L ? 0L
                : scaled.requiredProgress() * scaled.energyPerProgress();
        return new TierManager.ProcessingParameters(
                scaled.requiredProgress(),
                ClayEnergy.ofInternal(scaled.energyPerProgress()),
                ClayEnergy.ofInternal(total),
                Math.pow(9.0D, formula.compressionRank()) / formula.efficiency(),
                formula.energyMultiplier(),
                1.0D);
    }

    @Override
    protected List<ItemStack> fixedOutputs(ClayiumMachineRecipe recipe) {
        CaReactorFormula formula = structure.formula().orElse(null);
        if (formula == null) {
            return List.of();
        }
        List<ItemStack> outputs = new ArrayList<>();
        for (ItemStack base : recipe.itemOutputs()) {
            // TileCAReactor.getResultPureAntimatter: one item compressed by the structure rank.
            outputs.add(base.is(MaterialRegistration.MATERIAL_ITEMS.get("pure_antimatter").get())
                    ? new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get(
                            PURE_ANTIMATTER_OUTPUTS[formula.compressionRank()]).get(), base.getCount())
                    : base.copy());
        }
        return outputs;
    }

    @Override
    protected double processingProgressMultiplier() {
        if (level == null || receivedLaserTick < level.getGameTime() - 1L) {
            return 1.0D;
        }
        return 1.0D + receivedLaser.energy();
    }

    @Override
    public boolean receiveLaser(LaserBeam beam) {
        if (!structure.formed() || level == null || beam.isEmpty()) {
            return false;
        }
        long tick = level.getGameTime();
        receivedLaser = receivedLaserTick == tick ? LaserBeam.merge(receivedLaser, beam) : beam;
        receivedLaserTick = tick;
        return true;
    }

    public boolean isFormed() {
        return structure.formed();
    }

    public int hullCount() {
        return structure.hullCount();
    }

    public int coilCount() {
        return structure.coilCount();
    }

    public double averageRank() {
        return structure.averageRank();
    }

    public CaReactorFormula formula() {
        return structure.formula().orElse(null);
    }

    public long structureValidationCount() {
        return structureValidationCount;
    }

    public String structureFailure() {
        return structure.failure();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("reactor_formed", structure.formed());
        tag.putInt("reactor_coils", structure.coilCount());
        tag.putInt("reactor_hulls", structure.hullCount());
        tag.putDouble("reactor_average_rank", structure.averageRank());
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this,
                (blockEntity, registries) -> getUpdateTag(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (!tag.contains("reactor_formed")) {
            return;
        }
        boolean formed = tag.getBoolean("reactor_formed");
        int coils = tag.getInt("reactor_coils");
        int hulls = tag.getInt("reactor_hulls");
        double rank = tag.getDouble("reactor_average_rank");
        structure = new CaReactorStructure(formed, coils, hulls, rank, java.util.Set.of(),
                formed ? CaReactorFormula.calculate(hulls, rank) : java.util.Optional.empty(), "");
    }
}
