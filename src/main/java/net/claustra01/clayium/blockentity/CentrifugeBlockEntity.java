package net.claustra01.clayium.blockentity;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tiered Centrifuge (T3..T6): 1 input, 1..4 outputs depending on tier, using {@link UtilTier#GENERIC} scaling.
 */
public class CentrifugeBlockEntity extends AbstractClayMachineBlockEntity {
    public int resultSlotNum = 1;
    @Nullable
    private Collection<ClayMachineRecipe> overrideRecipesForTest;

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ClayiumBlockEntities.CENTRIFUGE.get(), pos, state, 7);
        ensureTierInitialized();
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(7, ItemStack.EMPTY);
        this.clayEnergySlot = 6;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{6});
        this.listSlotsExtract.add(new int[]{1, 2, 3, 4});
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 4, 6};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        this.initCraftTime = UtilTier.GENERIC.multCraftTime(tier);
        this.initConsumingEnergy = UtilTier.GENERIC.multConsumingEnergy(tier);
        this.multCraftTime = this.initCraftTime;
        this.multConsumingEnergy = this.initConsumingEnergy;
        if (tier <= 3) {
            this.resultSlotNum = 1;
        } else if (tier == 4) {
            this.resultSlotNum = 2;
        } else if (tier == 5) {
            this.resultSlotNum = 3;
        } else {
            this.resultSlotNum = 4;
        }
    }

    @Override
    public String getRecipeCategoryId() {
        return "centrifuge";
    }

    public void setOverrideRecipesForTest(@Nullable Collection<ClayMachineRecipe> recipes) {
        this.overrideRecipesForTest = recipes;
    }

    @Override
    public Collection<ClayMachineRecipe> getAvailableRecipes() {
        if (this.overrideRecipesForTest != null) {
            return this.overrideRecipesForTest;
        }
        return ClayMachineRecipeLookup.recipesForCategory(this.level, "centrifuge");
    }

    @Nullable
    protected ClayMachineRecipe findRecipe(ItemStack material) {
        Collection<ClayMachineRecipe> recipes = this.overrideRecipesForTest != null
                ? this.overrideRecipesForTest
                : ClayMachineRecipeLookup.recipesForCategory(this.level, "centrifuge");
        return ClayMachineRecipeLookup.findBestSingle(recipes, material, getRecipeTier());
    }

    protected boolean canCraft(ItemStack material) {
        if (material.isEmpty()) {
            return false;
        }
        ClayMachineRecipe recipe = findRecipe(material);
        if (recipe == null || recipe.results().isEmpty()) {
            return false;
        }
        List<ItemStack> results = recipe.results();
        int count = Math.min(this.resultSlotNum, results.size());
        for (int i = 0; i < count; i++) {
            if (!canFitOutputStack(i + 1, results.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canProceedCraft() {
        ItemStack buffer = getStack(5);
        return !buffer.isEmpty() ? canCraft(buffer) : canCraft(getStack(0));
    }

    @Override
    public void proceedCraft() {
        if (getStack(5).isEmpty()) {
            ClayMachineRecipe recipe = findRecipe(getStack(0));
            if (recipe == null) {
                return;
            }
            this.machineConsumingEnergy = (long) ((float) recipe.energyPerTick() * this.multConsumingEnergy);
        }

        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(5).isEmpty()) {
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
                setStack(5, taken);
            }

            this.machineCraftTime++;
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ClayMachineRecipe recipe = findRecipe(getStack(5));
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (recipe != null) {
                    List<ItemStack> results = recipe.results();
                    int count = Math.min(this.resultSlotNum, results.size());
                    for (int i = 0; i < count; i++) {
                        addOutputStack(i + 1, results.get(i));
                    }
                    ItemStack buf = getStack(5);
                    buf.shrink(recipe.inputs().get(0).count());
                    if (buf.isEmpty()) {
                        setStack(5, ItemStack.EMPTY);
                    }
                } else {
                    setStack(5, ItemStack.EMPTY);
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
