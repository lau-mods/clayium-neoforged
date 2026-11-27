package net.claustra01.clayium.blockentity;

import java.util.Collection;
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
 * 2-input / 1-output Clayium machine (Assembler, Inscriber, Alloy Smelter) matching 1.7.10 {@code TileClayAssembler}.
 */
public class AssemblerBlockEntity extends AbstractClayMachineBlockEntity {
    public String recipeCategoryId = "";
    @Nullable
    private Collection<ClayMachineRecipe> overrideRecipesForTest;

    public AssemblerBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.ASSEMBLER.get(), pos, state);
        ensureTierInitialized();
    }

    protected AssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 6);
    }

    @Override
    protected void initParams() {
        this.items = NonNullList.withSize(6, ItemStack.EMPTY);
        this.clayEnergySlot = 5;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{1});
        this.listSlotsInsert.add(new int[]{0, 1});
        this.listSlotsInsert.add(new int[]{5});
        this.listSlotsExtract.add(new int[]{2});
        this.insertRoutes = new int[]{-1, 2, -1, 3, -1, -1};
        this.maxAutoExtract = new int[]{-1, -1, -1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 2, 5};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        String path = BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).getPath();
        if (path.endsWith("_assembler")) {
            this.recipeCategoryId = "assembler";
        } else if (path.endsWith("_inscriber")) {
            this.recipeCategoryId = "inscriber";
        } else if (path.endsWith("_alloy_smelter")) {
            this.recipeCategoryId = "alloy_smelter";
        } else if (path.endsWith("_ca_injector")) {
            this.recipeCategoryId = "ca_injector";
        }
    }

    public void setOverrideRecipesForTest(@Nullable Collection<ClayMachineRecipe> recipes) {
        this.overrideRecipesForTest = recipes;
    }

    protected Collection<ClayMachineRecipe> getAvailableRecipes() {
        if (this.overrideRecipesForTest != null) {
            return this.overrideRecipesForTest;
        }
        return ClayMachineRecipeLookup.recipesForCategory(this.level, this.recipeCategoryId);
    }

    protected boolean canCraftOutputs(ClayMachineRecipe recipe) {
        return !recipe.results().isEmpty() && canFitOutputStack(2, recipe.results().getFirst());
    }

    @Nullable
    protected PermutationMatch findCurrentMatch(boolean fromBuffers) {
        int a = fromBuffers ? 3 : 0;
        int b = fromBuffers ? 4 : 1;
        return ClayMachineRecipeLookup.findBestTwoInput(
                getAvailableRecipes(),
                getStack(a),
                getStack(b),
                getRecipeTier(),
                this::canCraftOutputs
        );
    }

    protected double getCraftTimeMultiplier() {
        return 1.0;
    }

    @Override
    public boolean canProceedCraft() {
        boolean buffersEmpty = getStack(3).isEmpty() && getStack(4).isEmpty();
        return findCurrentMatch(!buffersEmpty) != null;
    }

    @Override
    public void proceedCraft() {
        if (getStack(3).isEmpty() && getStack(4).isEmpty()) {
            PermutationMatch match = findCurrentMatch(false);
            if (match == null) {
                return;
            }
            this.machineConsumingEnergy = (long) ((float) match.recipe().energyPerTick() * this.multConsumingEnergy);
        }

        if (consumeClayEnergy(this.machineConsumingEnergy)) {
            if (getStack(3).isEmpty() && getStack(4).isEmpty()) {
                PermutationMatch match = findCurrentMatch(false);
                if (match == null) {
                    return;
                }
                this.machineTimeToCraft = (long) ((double) match.recipe().durationTicks() * getCraftTimeMultiplier() * (double) this.multCraftTime);
                int[] perm = match.permutation();
                for (int i = 0; i < perm.length; i++) {
                    ItemStack src = getStack(perm[i]);
                    int needed = match.consumedCount(i);
                    ItemStack moved = src.split(needed);
                    if (src.isEmpty()) {
                        setStack(perm[i], ItemStack.EMPTY);
                    }
                    setStack(i + 3, moved);
                }
            }

            this.machineCraftTime++;
            this.doingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                PermutationMatch match = findCurrentMatch(true);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (match != null) {
                    addOutputStack(2, match.recipe().results().getFirst());
                    int[] perm = match.permutation();
                    for (int i = 0; i < perm.length; i++) {
                        int bufSlot = perm[i] + 3;
                        ItemStack buf = getStack(bufSlot);
                        buf.shrink(match.consumedCount(i));
                        if (buf.isEmpty()) {
                            setStack(bufSlot, ItemStack.EMPTY);
                        }
                    }
                } else {
                    setStack(3, ItemStack.EMPTY);
                    setStack(4, ItemStack.EMPTY);
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
