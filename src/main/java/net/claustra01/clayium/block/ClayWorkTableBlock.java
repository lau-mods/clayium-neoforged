package net.claustra01.clayium.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Clay Work Table (Tier 0 manual crafting block; does not enter piping mode).
 */
public class ClayWorkTableBlock extends ClayContainerBlock {
    public ClayWorkTableBlock(Properties properties) {
        super(properties, 0, "clayworktable", "clayworktable", "crafting", null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // No PIPE_MODE property
    }

    @Override
    public boolean canBePipe() {
        return false;
    }
}
