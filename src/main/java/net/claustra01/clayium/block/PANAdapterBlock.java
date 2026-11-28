package net.claustra01.clayium.block;

import net.claustra01.clayium.blockentity.PANAdapterBlockEntity;
import net.claustra01.clayium.pan.IPANConductor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * PAN Adapter (metaMode=2, all-face panadapter overlay, visual hull uses machineTier while item Tier >= 11).
 */
public class PANAdapterBlock extends DirectionalClayContainerBlock implements IPANConductor {
    public PANAdapterBlock(Properties properties, int tier, String baseTexture) {
        super(properties, tier, baseTexture, "panadapter", "pan", null, OverlayMode.ALL_FACES);
    }

    @Override
    public int getTier() {
        return Math.max(super.getTier(), 11);
    }

    @Override
    public int getTier(ItemStack stack) {
        return getTier();
    }

    public int getMachineTier() {
        return super.getTier();
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof PANAdapterBlockEntity adapter
                && !(level.getBlockEntity(neighborPos) instanceof PANAdapterBlockEntity)) {
            adapter.onNeighborChange();
        }
    }
}
