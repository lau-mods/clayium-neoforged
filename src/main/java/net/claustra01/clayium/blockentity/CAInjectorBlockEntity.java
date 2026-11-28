package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CA Injector (T9..T13): uses CA Resonator resonance inside a 5x5x5 region to exponentially reduce craft time.
 */
public class CAInjectorBlockEntity extends AssemblerBlockEntity {
    public double caFactorExponent;

    public CAInjectorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CA_INJECTOR.get(), pos, state);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.caFactorExponent = 1.0;
    }

    @Override
    protected void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        this.recipeCategoryId = "ca_injector";
        this.caFactorExponent = ClayEnergyUpgradeMath.caInjectorExponent(tier);
    }

    public double getResonance() {
        return this.level == null ? 1.0 : ClayEnergyUpgradeMath.scanResonance(this.level, this.worldPosition);
    }

    @Override
    public double getCraftTimeMultiplier() {
        return ClayEnergyUpgradeMath.caInjectorCraftTimeMultiplier(getResonance(), getTier());
    }
}
