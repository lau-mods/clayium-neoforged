package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.world.ClayiumChunkLoading;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Chunk Loader (Tier 6): keeps the 3x3 chunk area around its position force-loaded via {@link ClayiumChunkLoading}.
 */
public class ClayChunkLoaderBlockEntity extends TieredClayContainerBlockEntity {
    private boolean forcedLoaded = false;

    public ClayChunkLoaderBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CLAY_CHUNK_LOADER.get(), pos, state, 0);
        ensureTierInitialized();
    }

    @Override
    public boolean acceptEnergyClay() {
        return false;
    }

    @Override
    public void serverTick() {
        super.serverTick();
        if (!this.forcedLoaded && this.level instanceof ServerLevel serverLevel) {
            ClayiumChunkLoading.forceSquare3x3(serverLevel, this.worldPosition, true);
            this.forcedLoaded = true;
        }
    }

    @Override
    public void setRemoved() {
        if (this.forcedLoaded && this.level instanceof ServerLevel serverLevel) {
            ClayiumChunkLoading.forceSquare3x3(serverLevel, this.worldPosition, false);
            this.forcedLoaded = false;
        }
        super.setRemoved();
    }
}
