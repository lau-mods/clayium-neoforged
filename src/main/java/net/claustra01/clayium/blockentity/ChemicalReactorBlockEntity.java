package net.claustra01.clayium.blockentity;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup.PermutationMatch;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 2-input / 2-output Chemical Reactor (T4/5/8) and Advanced Fluid Transfer Machine (T5) matching 1.7.10 {@code TileClayChemicalReactor}.
 */
public class ChemicalReactorBlockEntity extends AbstractClayMachineBlockEntity {
    public String recipeCategoryId;
    public int resultSlotNum;
    @Nullable
    private Collection<ClayMachineRecipe> overrideRecipesForTest;

    public ChemicalReactorBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.CHEMICAL_REACTOR.get(), pos, state);
        ensureTierInitialized();
    }

    protected ChemicalReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 7);
    }

    @Override
    protected void initParams() {
        this.recipeCategoryId = "";
        this.resultSlotNum = 2;
        this.items = NonNullList.withSize(7, ItemStack.EMPTY);
        this.clayEnergySlot = 6;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{6});
        this.listSlotsExtract.add(new int[]{2});
        this.listSlotsExtract.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{2, 3});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{2, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1, -1, -1};
        this.slotsDrop = new int[]{0, 1, 2, 3, 6};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        String path = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();
        if (path.endsWith("_fluid_transfer_machine")) {
            this.recipeCategoryId = "fluid_transfer_machine";
        } else if (path.equals("clay_blast_furnace")) {
            this.recipeCategoryId = "blast_furnace";
        } else if (path.equals("clay_reactor")) {
            this.recipeCategoryId = "reactor";
        } else {
            this.recipeCategoryId = "chemical_reactor";
        }
    }

    @Override
    public String getRecipeCategoryId() {
        return this.recipeCategoryId;
    }

    public void setOverrideRecipesForTest(@Nullable Collection<ClayMachineRecipe> recipes) {
        this.overrideRecipesForTest = recipes;
    }

    @Override
    public Collection<ClayMachineRecipe> getAvailableRecipes() {
        if (this.overrideRecipesForTest != null) {
            return this.overrideRecipesForTest;
        }
        return ClayMachineRecipeLookup.recipesForCategory(this.level, this.recipeCategoryId);
    }

    protected boolean canCraftOutputs(ClayMachineRecipe recipe) {
        List<ItemStack> results = recipe.results();
        if (results.isEmpty()) {
            return false;
        }
        int count = Math.min(this.resultSlotNum, results.size());
        for (int i = 0; i < count; i++) {
            if (!canFitOutputStack(i + 2, results.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected PermutationMatch findCurrentMatch(boolean fromBuffers) {
        int a = fromBuffers ? 4 : 0;
        int b = fromBuffers ? 5 : 1;
        return ClayMachineRecipeLookup.findBestTwoInput(
                getAvailableRecipes(),
                getStack(a),
                getStack(b),
                getRecipeTier(),
                this::canCraftOutputs
        );
    }

    @Override
    public boolean canProceedCraft() {
        boolean buffersEmpty = getStack(4).isEmpty() && getStack(5).isEmpty();
        return findCurrentMatch(!buffersEmpty) != null;
    }

    @Override
    public void proceedCraft() {
        if (getStack(4).isEmpty() && getStack(5).isEmpty()) {
            PermutationMatch match = findCurrentMatch(false);
            if (match == null) {
                return;
            }
            this.machineConsumingEnergy = (long) ((float) match.recipe().energyPerTick() * this.multConsumingEnergy);
        }

        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(4).isEmpty() && getStack(5).isEmpty()) {
                PermutationMatch match = findCurrentMatch(false);
                if (match == null) {
                    return;
                }
                this.machineTimeToCraft = (long) ((float) match.recipe().durationTicks() * this.multCraftTime);
                int[] perm = match.permutation();
                for (int i = 0; i < perm.length; i++) {
                    ItemStack src = getStack(perm[i]);
                    ItemStack moved = src.split(match.consumedCount(i));
                    if (src.isEmpty()) {
                        setStack(perm[i], ItemStack.EMPTY);
                    }
                    setStack(i + 4, moved);
                }
            }

            onCraftingStep();
            this.machineCraftTime++;
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                PermutationMatch match = findCurrentMatch(true);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (match != null) {
                    List<ItemStack> results = match.recipe().results();
                    int count = Math.min(this.resultSlotNum, results.size());
                    for (int i = 0; i < count; i++) {
                        addOutputStack(i + 2, results.get(i));
                    }
                    int[] perm = match.permutation();
                    for (int i = 0; i < perm.length; i++) {
                        int bufSlot = perm[i] + 4;
                        ItemStack buf = getStack(bufSlot);
                        buf.shrink(match.consumedCount(i));
                        if (buf.isEmpty()) {
                            setStack(bufSlot, ItemStack.EMPTY);
                        }
                    }
                } else {
                    setStack(4, ItemStack.EMPTY);
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

    protected void onCraftingStep() {}

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("RecipeId", this.recipeCategoryId);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("RecipeId") && !tag.getString("RecipeId").isEmpty()) {
            this.recipeCategoryId = tag.getString("RecipeId");
        }
    }
}
