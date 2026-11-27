package net.claustra01.clayium.block;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;

/**
 * Simple tiered block storing texture and optional tooltip metadata for DataGen and tooltips.
 */
public class TieredBlock extends Block implements ITieredBlock {
    private final int tier;
    private final String textureName;
    @Nullable
    private final String tooltipKey;

    public TieredBlock(Properties properties, int tier, String textureName) {
        this(properties, tier, textureName, null);
    }

    public TieredBlock(Properties properties, int tier, String textureName, @Nullable String tooltipKey) {
        super(properties);
        this.tier = tier;
        this.textureName = textureName;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public int getTier() {
        return this.tier;
    }

    public String getTextureName() {
        return this.textureName;
    }

    @Nullable
    public String getTooltipKey() {
        return this.tooltipKey;
    }
}
