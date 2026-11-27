package net.claustra01.clayium.blockentity;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe.WeightedOutput;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.claustra01.clayium.transfer.ClayiumTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chemical Metal Separator (T6): weighted random metal extraction from Industrial Clay Dust matching 1.7.10 {@code TileChemicalMetalSeparator}.
 */
public class ChemicalMetalSeparatorBlockEntity extends AbstractClayMachineBlockEntity {
    private final RandomSource fallbackRandom = RandomSource.create();
    @Nullable
    private Collection<ChemicalMetalSeparatorRecipe> overrideRecipesForTest;

    public ChemicalMetalSeparatorBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CHEMICAL_METAL_SEPARATOR.get(), pos, state, 19);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(19, ItemStack.EMPTY);
        this.clayEnergySlot = 18;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{18});
        this.listSlotsExtract.add(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
    }

    public void setOverrideRecipesForTest(@Nullable Collection<ChemicalMetalSeparatorRecipe> recipes) {
        this.overrideRecipesForTest = recipes;
    }

    @Nullable
    protected ChemicalMetalSeparatorRecipe findRecipe(ItemStack material) {
        if (material.isEmpty()) {
            return null;
        }
        if (this.overrideRecipesForTest != null) {
            for (ChemicalMetalSeparatorRecipe r : this.overrideRecipesForTest) {
                if (getRecipeTier() >= r.tier() && r.input().test(material)) {
                    return r;
                }
            }
            return null;
        }
        if (this.level == null) {
            return null;
        }
        List<RecipeHolder<ChemicalMetalSeparatorRecipe>> holders =
                this.level.getRecipeManager().getAllRecipesFor(ClayiumRecipeTypes.CHEMICAL_METAL_SEPARATOR.get());
        for (RecipeHolder<ChemicalMetalSeparatorRecipe> holder : holders) {
            ChemicalMetalSeparatorRecipe r = holder.value();
            if (getRecipeTier() >= r.tier() && r.input().test(material)) {
                return r;
            }
        }
        return null;
    }

    protected boolean canCraft(ItemStack material) {
        ChemicalMetalSeparatorRecipe recipe = findRecipe(material);
        if (recipe == null || recipe.outputs().isEmpty()) {
            return false;
        }
        int limit = getInventoryStackLimit(1);
        for (WeightedOutput out : recipe.outputs()) {
            if (ClayiumTransfer.canProduceItemStack(out.stack(), this.items, 1, 17, limit) < out.stack().getCount()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canProceedCraft() {
        ItemStack buf = getStack(17);
        return !buf.isEmpty() ? canCraft(buf) : canCraft(getStack(0));
    }

    @Override
    public void proceedCraft() {
        ChemicalMetalSeparatorRecipe recipe = !getStack(17).isEmpty() ? findRecipe(getStack(17)) : findRecipe(getStack(0));
        if (recipe == null) {
            return;
        }

        if (getStack(17).isEmpty()) {
            this.machineConsumingEnergy = (long) ((float) recipe.energyPerTick() * this.multConsumingEnergy);
        }

        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(17).isEmpty()) {
                this.machineTimeToCraft = (long) ((float) recipe.durationTicks() * this.multCraftTime);
                ItemStack input = getStack(0);
                ItemStack moved = input.split(recipe.input().count());
                if (input.isEmpty()) {
                    setStack(0, ItemStack.EMPTY);
                }
                setStack(17, moved);
            }

            this.machineCraftTime++;
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                RandomSource rng = this.level != null ? this.level.random : this.fallbackRandom;
                ItemStack selected = recipe.selectRandom(rng);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                setStack(17, ItemStack.EMPTY);
                if (!selected.isEmpty()) {
                    ClayiumTransfer.produceItemStack(selected, this.items, 1, 17, getInventoryStackLimit(1));
                }
                markForSync();
                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
            }
        }
    }
}
