package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.FEGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 6-way directional CE-FE Converter block.
 * Tracks redstone power signals to pause FE emission while preserving directional orientation and pipe mode states.
 */
public class FEGeneratorBlock extends DirectionalClayContainerBlock {
    private final String configBlockName;

    public FEGeneratorBlock(
            Properties properties,
            String configBlockName,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey,
            OverlayMode overlayMode) {
        super(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, overlayMode);
        this.configBlockName = configBlockName;
    }

    public String getConfigBlockName() {
        return this.configBlockName;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            updatePower(level, pos);
        }
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
        if (!level.isClientSide) {
            updatePower(level, pos);
        }
    }

    private static void updatePower(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FEGeneratorBlockEntity gen) {
            gen.setPowered(level.hasNeighborSignal(pos));
        }
    }
}
