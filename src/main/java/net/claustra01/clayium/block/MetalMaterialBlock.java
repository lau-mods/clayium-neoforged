package net.claustra01.clayium.block;

import net.claustra01.clayium.material.CMaterial;

/**
 * Material/metal storage block (either dedicated texture or 3-layer metalblock_base/dark/light tint).
 */
public class MetalMaterialBlock extends TieredBlock {
    private final CMaterial material;
    private final boolean layeredTint;

    public MetalMaterialBlock(Properties properties, CMaterial material, int tier, String textureName, boolean layeredTint) {
        super(properties, tier, textureName);
        this.material = material;
        this.layeredTint = layeredTint;
    }

    public CMaterial getMaterial() {
        return this.material;
    }

    public boolean isLayeredTint() {
        return this.layeredTint;
    }
}
