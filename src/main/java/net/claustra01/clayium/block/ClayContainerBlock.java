package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base block for Clayium machines, buffers, interfaces, and special containers (metaMode=0: no facing property).
 */
public class ClayContainerBlock extends Block implements ITieredBlock {
    public enum FacingMode {
        NONE,
        HORIZONTAL,
        ALL
    }

    private final int tier;
    private final String baseTexture;
    @Nullable
    private final String overlayTexture;
    @Nullable
    private final String categoryKey;
    @Nullable
    private final String tooltipKey;

    public ClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey) {
        super(properties);
        this.tier = tier;
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
        this.categoryKey = categoryKey;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public FacingMode getFacingMode() {
        return FacingMode.NONE;
    }

    public Direction getFacing(BlockState state) {
        return Direction.NORTH;
    }

    public String getBaseTexture() {
        return this.baseTexture;
    }

    @Nullable
    public String getOverlayTexture() {
        return this.overlayTexture;
    }

    @Nullable
    public String getCategoryKey() {
        return this.categoryKey;
    }

    @Nullable
    public String getTooltipKey() {
        return this.tooltipKey;
    }
}
