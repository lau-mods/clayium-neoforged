package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.block.ITieredBlock;
import net.claustra01.clayium.block.PANAdapterBlock;
import net.claustra01.clayium.block.PANDuplicatorBlock;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tier-aware ClayContainerBlockEntity applying {@link UtilTier} transport intervals and transfer limits.
 */
public abstract class TieredClayContainerBlockEntity extends ClayContainerBlockEntity {
    public enum ParamMode {
        MACHINE,
        BUFFER
    }

    protected int baseTier = -1;
    private boolean initialized = false;

    protected TieredClayContainerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            int inventorySize) {
        super(type, pos, state, inventorySize);
    }

    /**
     * Subclass constructors must call this after their own field initializers have run
     * so {@link #initParamsByTier(int)} can safely populate subclass fields.
     */
    protected final void ensureTierInitialized() {
        if (this.baseTier == -1) {
            resolveTierFromState(getBlockState());
        } else if (!this.initialized) {
            initByTier(this.baseTier);
        }
    }

    private void resolveTierFromState(BlockState state) {
        Block block = state.getBlock();
        int resolved = -1;
        if (block instanceof PANAdapterBlock adapter) {
            resolved = adapter.getMachineTier();
        } else if (block instanceof PANDuplicatorBlock dup) {
            resolved = dup.getDuplicatorRank();
        } else if (block instanceof ITieredBlock tiered) {
            resolved = tiered.getTier();
        }
        if (resolved >= 0) {
            setBaseTier(resolved);
        }
    }

    public void setBaseTier(int tier) {
        this.baseTier = tier;
        if (!this.initialized && this.baseTier != -1) {
            initByTier(this.baseTier);
        }
    }

    public int getTier() {
        return this.baseTier;
    }

    public int getRecipeTier() {
        return getTier();
    }

    public final void initByTier(int tier) {
        this.initialized = true;
        initParamsByTier(tier);
    }

    protected void initParamsByTier(int tier) {}

    protected void setDefaultTransportationParamsByTier(int tier, ParamMode mode) {
        switch (mode) {
            case MACHINE -> {
                UtilTier.TransportTierTable t = UtilTier.MACHINE_TRANSPORT;
                this.autoInsertInterval = t.autoInsertInterval(tier);
                this.autoExtractInterval = t.autoExtractInterval(tier);
                this.maxAutoInsertDefault = t.maxAutoInsert(tier);
                this.maxAutoExtractDefault = t.maxAutoExtract(tier);
                if (!UtilTier.canAutoTransfer(this.baseTier)) {
                    this.autoExtract = false;
                    this.autoInsert = false;
                }
            }
            case BUFFER -> {
                UtilTier.TransportTierTable t = UtilTier.BUFFER_TRANSPORT;
                this.autoInsertInterval = t.autoInsertInterval(tier);
                this.autoExtractInterval = t.autoExtractInterval(tier);
                this.maxAutoInsertDefault = t.maxAutoInsert(tier);
                this.maxAutoExtractDefault = t.maxAutoExtract(tier);
            }
        }
    }

    @Override
    public boolean acceptEnergyClay() {
        return UtilTier.acceptEnergyClay(this.baseTier);
    }

    @Override
    public void serverTick() {
        if (this.baseTier == -1) {
            resolveTierFromState(getBlockState());
        }
        if (!this.initialized && this.baseTier != -1) {
            initByTier(this.baseTier);
            markForSync();
        }
        super.serverTick();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("BaseTier", (short) this.baseTier);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("BaseTier") && this.baseTier == -1) {
            int loaded = tag.getShort("BaseTier");
            if (loaded != -1) {
                setBaseTier(loaded);
            }
        }
    }
}
