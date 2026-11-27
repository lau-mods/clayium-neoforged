package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * 6-way directional Clayium machine whose front overlay swaps to {@code <overlay>_1} when active.
 */
public class ActiveDirectionalClayContainerBlock extends DirectionalClayContainerBlock {
    public ActiveDirectionalClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey) {
        this(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, OverlayMode.FRONT_ONLY);
    }

    public ActiveDirectionalClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey,
            OverlayMode overlayMode) {
        super(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, overlayMode);
        this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, Boolean.FALSE));
    }

    @Override
    public boolean hasActiveOverlay() {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }
}
