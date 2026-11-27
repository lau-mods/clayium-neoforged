package net.claustra01.clayium.blockentity;

import net.claustra01.clayium.energy.ClayEnergyUpgradeMath;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * CA Condenser (T9..T11): {@link UtilTier#CA_CONDENSER} scaling plus {@code ln(resonance) + 1} output yield multiplier.
 */
public class CACondenserBlockEntity extends SimpleClayMachineBlockEntity {
    public CACondenserBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CA_CONDENSER.get(), pos, state);
        ensureTierInitialized();
    }

    @Override
    protected void initParamsByTier(int tier) {
        super.initParamsByTier(tier);
        this.recipeCategoryId = "ca_condenser";
        this.initCraftTime = UtilTier.CA_CONDENSER.multCraftTime(tier);
        this.initConsumingEnergy = UtilTier.CA_CONDENSER.multConsumingEnergy(tier);
        this.multCraftTime = this.initCraftTime;
        this.multConsumingEnergy = this.initConsumingEnergy;
    }

    public double getResonance() {
        return this.level == null ? 1.0 : ClayEnergyUpgradeMath.scanResonance(this.level, this.worldPosition);
    }

    @Override
    public void proceedCraft() {
        if (getStack(2).isEmpty()) {
            ClayMachineRecipe recipe = findRecipe(getStack(0));
            if (recipe == null) {
                return;
            }
            this.machineConsumingEnergy = (long) ((float) recipe.energyPerTick() * this.multConsumingEnergy);
        }

        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(2).isEmpty()) {
                ClayMachineRecipe recipe = findRecipe(getStack(0));
                if (recipe == null) {
                    return;
                }
                this.machineTimeToCraft = (long) ((float) recipe.durationTicks() * this.multCraftTime);
                ItemStack input = getStack(0);
                ItemStack taken = input.split(recipe.inputs().get(0).count());
                if (input.isEmpty()) {
                    setStack(0, ItemStack.EMPTY);
                }
                setStack(2, taken);
            }

            this.machineCraftTime++;
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ClayMachineRecipe recipe = findRecipe(getStack(2));
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (recipe != null && !recipe.results().isEmpty()) {
                    ItemStack result = recipe.results().getFirst().copy();
                    int multiplied = (int) ((double) result.getCount() * (Math.log(Math.max(1.0, getResonance())) + 1.0));
                    result.setCount(Math.min(result.getMaxStackSize(), Math.max(1, multiplied)));
                    ItemStack existing = getStack(1);
                    if (existing.isEmpty()) {
                        setStack(1, result);
                    } else if (ItemStack.isSameItemSameComponents(existing, result)) {
                        existing.setCount(Math.min(existing.getMaxStackSize(), existing.getCount() + result.getCount()));
                        markForSync();
                    }
                    ItemStack buf = getStack(2);
                    buf.shrink(recipe.inputs().get(0).count());
                    if (buf.isEmpty()) {
                        setStack(2, ItemStack.EMPTY);
                    }
                } else {
                    setStack(2, ItemStack.EMPTY);
                }

                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
                markForSync();
            }
        }
    }
}
