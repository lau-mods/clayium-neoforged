package mods.clayium.block.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.IClayContainerModifier;
import mods.clayium.block.IOverclocker;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.UtilItemStack;
import mods.clayium.util.crafting.Recipes;
import mods.clayium.util.crafting.SimpleMachinesRecipes;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TileClayMachines extends TileClayContainerTiered implements IExternalControl {
    public long machineCraftTime;
    public long machineTimeToCraft;
    public long machineConsumingEnergy;
    protected String recipeId;
    protected SimpleMachinesRecipes recipe;
    public float multCraftTime = 1.0F;
    public float multConsumingEnergy = 1.0F;
    public float initCraftTime = 1.0F;
    public float initConsumingEnergy = 1.0F;
    public int externalControlState = 0;
    public boolean isDoingWork = false;
    public double overclockTick = 0.0;
    public double overclockFactor = 1.0;
    public double overclockTotalFactor = 1.0;
    private ItemStack oldMaterial;
    private int oldBaseTier = -1;
    private Recipes.RecipeCondition oldRecipeCondition;
    private Recipes.RecipeResult oldRecipeResult;
    private Recipes.RecipeCondition[] oldRecipeConditions = new Recipes.RecipeCondition[65];
    private Recipes.RecipeResult[] oldRecipeResults = new Recipes.RecipeResult[65];
    private boolean[] refresh = new boolean[65];

    public TileClayMachines() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.containerItemStacks = new ItemStack[4];
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

    public void initParamsByTier(int tier) {
        this.setDefaultTransportationParamsByTier(tier, TileClayContainerTiered.ParamMode.MACHINE);
    }

    public void setRecipe(String recipeId_) {
        this.recipeId = recipeId_;
        this.refreshRecipe();
    }

    public String getRecipeId() {
        return this.recipeId;
    }

    public void refreshRecipe() {
        SimpleMachinesRecipes recipe = (SimpleMachinesRecipes)Recipes.GetRecipes(this.recipeId);
        if (recipe != null) {
            this.recipe = recipe;
        }
    }

    public SimpleMachinesRecipes getRecipe() {
        return this.recipe;
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.machineCraftTime = tagCompound.func_74763_f("CraftTime");
        this.machineTimeToCraft = tagCompound.func_74763_f("TimeToCraft");
        this.recipeId = tagCompound.func_74779_i("RecipeId");
        this.machineConsumingEnergy = tagCompound.func_74763_f("ConsumingEnergy");
        this.multCraftTime = tagCompound.func_74760_g("CraftTimeMultiplier");
        this.multConsumingEnergy = tagCompound.func_74760_g("ConsumingEnergyMultiplier");
        this.initCraftTime = tagCompound.func_74760_g("CraftTimeInitMultiplier");
        this.initConsumingEnergy = tagCompound.func_74760_g("ConsumingEnergyInitMultiplier");
        this.externalControlState = tagCompound.func_74762_e("ExternalControlState");
        this.overclockTick = tagCompound.func_74769_h("OverclockTick");
        this.overclockFactor = tagCompound.func_74769_h("OverclockFactor");
        this.overclockTotalFactor = tagCompound.func_74769_h("OverclockTotalFactor");
        this.refreshRecipe();
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74772_a("CraftTime", this.machineCraftTime);
        tagCompound.func_74772_a("TimeToCraft", this.machineTimeToCraft);
        tagCompound.func_74778_a("RecipeId", this.recipeId);
        tagCompound.func_74772_a("ConsumingEnergy", this.machineConsumingEnergy);
        tagCompound.func_74776_a("CraftTimeMultiplier", this.multCraftTime);
        tagCompound.func_74776_a("ConsumingEnergyMultiplier", this.multConsumingEnergy);
        tagCompound.func_74776_a("CraftTimeInitMultiplier", this.initCraftTime);
        tagCompound.func_74776_a("ConsumingEnergyInitMultiplier", this.initConsumingEnergy);
        tagCompound.func_74768_a("ExternalControlState", this.externalControlState);
        tagCompound.func_74780_a("OverclockTick", this.overclockTick);
        tagCompound.func_74780_a("OverclockFactor", this.overclockFactor);
        tagCompound.func_74780_a("OverclockTotalFactor", this.overclockTotalFactor);
    }

    @SideOnly(Side.CLIENT)
    public int getCraftProgressScaled(int par1) {
        return this.machineTimeToCraft == 0L ? 0 : (int)(this.machineCraftTime * (long)par1 / this.machineTimeToCraft);
    }

    public boolean isCrafting() {
        return this.machineCraftTime > 0L;
    }

    public void func_145845_h() {
        super.func_145845_h();
        this.isDoingWork = false;
        UtilDirection[] directions = new UtilDirection[]{
            UtilDirection.UP, UtilDirection.DOWN, UtilDirection.NORTH, UtilDirection.SOUTH, UtilDirection.WEST, UtilDirection.EAST
        };
        this.clayEnergyStorageSize = 1;
        this.overclockTotalFactor = 1.0;

        for (UtilDirection direction : directions) {
            Block block = this.field_145850_b
                .func_147439_a(this.field_145851_c + direction.offsetX, this.field_145848_d + direction.offsetY, this.field_145849_e + direction.offsetZ);
            if (this.canOverclock() && block instanceof IOverclocker) {
                this.overclockTotalFactor = this.overclockTotalFactor
                    * ((IOverclocker)block)
                        .getOverclockFactor(
                            this.field_145850_b,
                            this.field_145851_c + direction.offsetX,
                            this.field_145848_d + direction.offsetY,
                            this.field_145849_e + direction.offsetZ
                        );
            }

            if (block instanceof IClayContainerModifier) {
                ((IClayContainerModifier)block)
                    .modifyClayContainer(
                        this.field_145850_b,
                        this.field_145851_c + direction.offsetX,
                        this.field_145848_d + direction.offsetY,
                        this.field_145849_e + direction.offsetZ,
                        this
                    );
            }
        }

        this.overclockFactor = this.overclockTotalFactor > 10.0 ? this.overclockTotalFactor / 10.0 : 1.0;
        this.overclockTick = this.overclockTick + this.overclockTotalFactor / this.overclockFactor;
        this.multCraftTime = (float)((double)this.initCraftTime / this.overclockFactor);

        for (this.multConsumingEnergy = (float)((double)this.initConsumingEnergy * Math.pow(this.overclockFactor, 1.5));
            this.overclockTick >= 1.0;
            this.overclockTick--
        ) {
            if (this.externalControlState >= 0 && this.canProceedCraft()) {
                this.proceedCraft();
            }
        }
    }

    public boolean canOverclock() {
        return true;
    }

    protected Recipes.RecipeCondition getRecipeCondition(ItemStack material, int baseTier) {
        if (material == null) {
            return null;
        } else {
            if (!UtilItemStack.areTypeEqual(this.oldMaterial, material) || this.oldBaseTier != baseTier) {
                this.oldMaterial = material.func_77946_l();
                this.oldBaseTier = baseTier;

                for (int i = 0; i < this.oldRecipeConditions.length; i++) {
                    this.refresh[i] = false;
                }
            }

            int n = Math.min(64, Math.max(material.field_77994_a, 0));
            if (!this.refresh[n]) {
                Recipes.RecipeCondition newRecipeCondition = this.recipe.getRecipeConditionFromInventory(new ItemStack[]{material}, 0, baseTier);
                if (newRecipeCondition == null && this.oldRecipeCondition != null
                    || newRecipeCondition != null && !newRecipeCondition.equals(this.oldRecipeCondition)) {
                    this.oldRecipeResult = this.recipe.getRecipeResult(newRecipeCondition);
                }

                this.oldRecipeCondition = newRecipeCondition;
                this.oldRecipeConditions[n] = this.oldRecipeCondition;
                this.oldRecipeResults[n] = this.oldRecipeResult;
                this.refresh[n] = true;
            }

            return this.oldRecipeConditions[n];
        }
    }

    protected Recipes.RecipeResult getRecipeResult(ItemStack material, int baseTier) {
        if (material == null) {
            return null;
        } else {
            this.getRecipeCondition(material, baseTier);
            return this.oldRecipeResults[Math.min(64, Math.max(material.field_77994_a, 0))];
        }
    }

    protected long getEnergy(ItemStack material, int baseTier) {
        Recipes.RecipeResult result = this.getRecipeResult(material, baseTier);
        return result == null ? 0L : result.getEnergy();
    }

    protected long getTime(ItemStack material, int baseTier) {
        Recipes.RecipeResult result = this.getRecipeResult(material, baseTier);
        return result == null ? 0L : result.getTime();
    }

    protected int getConsumedStackSize(ItemStack material, int baseTier) {
        Recipes.RecipeCondition cond = this.getRecipeCondition(material, baseTier);
        if (cond == null) {
            return 0;
        } else {
            int[] i = cond.getStackSizes(new ItemStack[]{material});
            return i != null && i.length != 0 ? i[0] : 0;
        }
    }

    protected ItemStack getResult(ItemStack material, int baseTier) {
        Recipes.RecipeResult result = this.getRecipeResult(material, baseTier);
        if (result == null) {
            return null;
        } else {
            ItemStack[] results = result.getResults();
            return results != null && results.length != 0 ? (results[0] == null ? null : results[0].func_77946_l()) : null;
        }
    }

    protected boolean canCraft(ItemStack material) {
        if (material != null && this.recipe != null) {
            ItemStack itemstack = this.getResult(material, this.baseTier);
            if (itemstack == null) {
                return false;
            } else if (this.containerItemStacks[1] == null) {
                return true;
            } else if (!this.containerItemStacks[1].func_77969_a(itemstack)) {
                return false;
            } else {
                int result = this.containerItemStacks[1].field_77994_a + itemstack.field_77994_a;
                return result <= this.func_70297_j_() && result <= this.containerItemStacks[1].func_77976_d();
            }
        } else {
            return false;
        }
    }

    public boolean canProceedCraft() {
        if (this.containerItemStacks[2] != null) {
            ItemStack itemstack = this.containerItemStacks[2];
            return this.canCraft(itemstack);
        } else {
            ItemStack itemstack = this.containerItemStacks[0];
            return this.canCraft(itemstack);
        }
    }

    public int canPushButton() {
        return this.canProceedCraft() ? 1 : 0;
    }

    public void pushButton() {
        if (this.canPushButton() == 1) {
            this.clayEnergy += 5L;
        }

        this.setSyncFlag();
    }

    public void pushButton(EntityPlayer player, int action) {
        this.pushButton();
    }

    public void proceedCraft() {
        if (this.containerItemStacks[2] == null) {
            this.machineConsumingEnergy = (long)((float)this.getEnergy(this.containerItemStacks[0], this.baseTier) * this.multConsumingEnergy);
        }

        if (this.consumeClayEnergy(this.machineConsumingEnergy)) {
            if (this.containerItemStacks[2] == null) {
                this.machineTimeToCraft = (long)((float)this.getTime(this.containerItemStacks[0], this.baseTier) * this.multCraftTime);
                this.containerItemStacks[2] = this.containerItemStacks[0].func_77979_a(this.getConsumedStackSize(this.containerItemStacks[0], this.baseTier));
                if (this.containerItemStacks[0].field_77994_a <= 0) {
                    this.containerItemStacks[0] = null;
                }
            }

            this.machineCraftTime++;
            this.isDoingWork = true;
            if (this.machineCraftTime >= this.machineTimeToCraft) {
                ItemStack itemstack = this.getResult(this.containerItemStacks[2], this.baseTier);
                this.machineCraftTime = 0L;
                this.machineConsumingEnergy = 0L;
                if (this.containerItemStacks[1] == null) {
                    this.containerItemStacks[1] = itemstack.func_77946_l();
                } else if (this.containerItemStacks[1].func_77973_b() == itemstack.func_77973_b()) {
                    this.containerItemStacks[1].field_77994_a = this.containerItemStacks[1].field_77994_a + itemstack.field_77994_a;
                }

                if ((
                        this.containerItemStacks[2].field_77994_a = this.containerItemStacks[2].field_77994_a
                            - this.getConsumedStackSize(this.containerItemStacks[2], this.baseTier)
                    )
                    <= 0) {
                    this.containerItemStacks[2] = null;
                }

                if (this.externalControlState > 0) {
                    this.externalControlState--;
                    if (this.externalControlState == 0) {
                        this.externalControlState = -1;
                    }
                }
            }
        }
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public String getNEIOutputId() {
        return this.getRecipeId();
    }

    public void doWorkOnce() {
        if (this.externalControlState > 0) {
            this.externalControlState++;
        } else {
            this.externalControlState = 1;
        }
    }

    public void startWork() {
        this.externalControlState = 0;
    }

    public void stopWork() {
        this.externalControlState = -1;
    }

    public boolean isScheduled() {
        return this.canProceedCraft();
    }

    public boolean isDoingWork() {
        return this.isDoingWork;
    }
}
