package net.claustra01.clayium.blockentity;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipeLookup;
import net.claustra01.clayium.registry.ClayiumBlockEntities;
import net.claustra01.clayium.util.UtilTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Standard 1-input / 1-output Clayium processing machine matching 1.7.10 {@code TileClayMachines}.
 */
public class SimpleClayMachineBlockEntity extends AbstractClayMachineBlockEntity {
    public String recipeCategoryId;
    @Nullable
    private Collection<ClayMachineRecipe> overrideRecipesForTest;

    public SimpleClayMachineBlockEntity(BlockPos pos, BlockState state) {
        this(ClayiumBlockEntities.SIMPLE_MACHINE.get(), pos, state);
        ensureTierInitialized();
    }

    protected SimpleClayMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 4);
    }

    @Override
    protected void initParams() {
        super.initParams();
        this.recipeCategoryId = "";
        this.clayEnergySlot = 3;
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsInsert.add(new int[]{3});
        this.listSlotsExtract.add(new int[]{1});
        this.insertRoutes = new int[]{-1, 0, -1, 1, -1, -1};
        this.maxAutoExtract = new int[]{-1, 1};
        this.extractRoutes = new int[]{0, -1, -1, -1, -1, -1};
        this.maxAutoInsert = new int[]{-1};
        this.slotsDrop = new int[]{0, 1, 3};
        this.autoInsert = true;
        this.autoExtract = true;
    }

    @Override
    public String getRecipeCategoryId() {
        return this.recipeCategoryId;
    }

    @Override
    protected void initParamsByTier(int tier) {
        setDefaultTransportationParamsByTier(tier, ParamMode.MACHINE);
        MachineConfig cfg = resolveMachineConfig(getBlockState());
        this.recipeCategoryId = cfg.categoryId();
        if (cfg.tierTable() != null) {
            this.initCraftTime = cfg.tierTable().multCraftTime(tier);
            this.initConsumingEnergy = cfg.tierTable().multConsumingEnergy(tier);
            this.multCraftTime = this.initCraftTime;
            this.multConsumingEnergy = this.initConsumingEnergy;
        }
    }

    public static MachineConfig resolveMachineConfig(BlockState state) {
        String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
        if (path.endsWith("_bending_machine")) {
            return new MachineConfig("bending_machine", null);
        }
        if (path.endsWith("_wire_drawing_machine")) {
            return new MachineConfig("wire_drawing_machine", null);
        }
        if (path.endsWith("_pipe_drawing_machine")) {
            return new MachineConfig("pipe_drawing_machine", null);
        }
        if (path.endsWith("_cutting_machine")) {
            return new MachineConfig("cutting_machine", null);
        }
        if (path.endsWith("_lathe")) {
            return new MachineConfig("lathe", null);
        }
        if (path.endsWith("_milling_machine")) {
            return new MachineConfig("milling_machine", null);
        }
        if (path.endsWith("_condenser") && !path.endsWith("_ca_condenser")
                && !path.startsWith("auto_clay_condenser") && !path.startsWith("energetic_clay_condenser")) {
            return new MachineConfig("condenser", UtilTier.GENERIC);
        }
        if (path.endsWith("_grinder")) {
            return new MachineConfig("grinder", UtilTier.GENERIC);
        }
        if (path.endsWith("_decomposer") && !path.equals("energetic_clay_decomposer")) {
            return new MachineConfig("decomposer", null);
        }
        if (path.startsWith("energetic_clay_condenser")) {
            return new MachineConfig("energetic_clay_condenser", null);
        }
        if (path.equals("energetic_clay_decomposer")) {
            return new MachineConfig("energetic_clay_decomposer", null);
        }
        if (path.endsWith("_smelter") && !path.endsWith("_alloy_smelter")) {
            return new MachineConfig("smelter", UtilTier.SMELTER);
        }
        if (path.endsWith("_electrolysis_reactor")) {
            return new MachineConfig("electrolysis_reactor", null);
        }
        if (path.endsWith("_matter_transformer")) {
            return new MachineConfig("matter_transformer", null);
        }
        return new MachineConfig("", null);
    }

    public record MachineConfig(String categoryId, @Nullable UtilTier.CraftingTierTable tierTable) {}

    public void setOverrideRecipesForTest(@Nullable Collection<ClayMachineRecipe> recipes) {
        this.overrideRecipesForTest = recipes;
    }

    protected Collection<ClayMachineRecipe> getAvailableRecipes() {
        if (this.overrideRecipesForTest != null) {
            return this.overrideRecipesForTest;
        }
        return ClayMachineRecipeLookup.recipesForCategory(this.level, this.recipeCategoryId);
    }

    @Nullable
    protected ClayMachineRecipe findRecipe(ItemStack material) {
        return ClayMachineRecipeLookup.findBestSingle(getAvailableRecipes(), material, getRecipeTier());
    }

    protected boolean canCraft(ItemStack material) {
        if (material.isEmpty()) {
            return false;
        }
        ClayMachineRecipe recipe = findRecipe(material);
        if (recipe == null || recipe.results().isEmpty()) {
            return false;
        }
        return canFitOutputStack(1, recipe.results().getFirst());
    }

    @Override
    public boolean canProceedCraft() {
        ItemStack buffer = getStack(2);
        return !buffer.isEmpty() ? canCraft(buffer) : canCraft(getStack(0));
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
                int consumed = recipe.inputs().get(0).count();
                ItemStack input = getStack(0);
                ItemStack taken = input.split(consumed);
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
                    addOutputStack(1, recipe.results().getFirst());
                    int consumed = recipe.inputs().get(0).count();
                    ItemStack buffer = getStack(2);
                    buffer.shrink(consumed);
                    if (buffer.isEmpty()) {
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
