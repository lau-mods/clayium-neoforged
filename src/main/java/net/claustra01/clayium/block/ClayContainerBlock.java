package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Base block for Clayium machines, buffers, interfaces, and special containers (metaMode=0: no facing property).
 */
public class ClayContainerBlock extends Block implements ITieredBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public enum FacingMode {
        NONE,
        HORIZONTAL,
        ALL
    }

    public enum OverlayMode {
        FRONT_ONLY,
        ALL_FACES,
        TOP_ONLY,
        FRONT_AND_BACK_MINER,
        PAN_DUPLICATOR,
        AUTO_CRAFTER,
        AUTO_TRADER,
        STORAGE_CONTAINER,
        VACUUM_CONTAINER
    }

    private final int tier;
    private final String baseTexture;
    @Nullable
    private final String overlayTexture;
    @Nullable
    private final String categoryKey;
    @Nullable
    private final String tooltipKey;
    private final OverlayMode overlayMode;

    public ClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey) {
        this(properties, tier, baseTexture, overlayTexture, categoryKey, tooltipKey, OverlayMode.FRONT_ONLY);
    }

    public ClayContainerBlock(
            Properties properties,
            int tier,
            String baseTexture,
            @Nullable String overlayTexture,
            @Nullable String categoryKey,
            @Nullable String tooltipKey,
            OverlayMode overlayMode) {
        super(properties);
        this.tier = tier;
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
        this.categoryKey = categoryKey;
        this.tooltipKey = tooltipKey;
        this.overlayMode = overlayMode;
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

    public boolean hasActiveOverlay() {
        return false;
    }

    public OverlayMode getOverlayMode() {
        return this.overlayMode;
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
