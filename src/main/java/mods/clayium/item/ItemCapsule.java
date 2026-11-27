package mods.clayium.item;

import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.List;
import mods.clayium.gui.IMultipleRenderIcons;
import mods.clayium.gui.MultipleIcon;
import mods.clayium.util.UtilFluid;
import mods.clayium.util.crafting.ISpecialResultRecipe;
import mods.clayium.util.crafting.RecipeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidContainerRegistry.FluidContainerData;
import org.lwjgl.opengl.GL11;

public class ItemCapsule extends ItemDamaged {
    private int capacity = 1000;
    private IMultipleRenderIcons icon;
    private static volatile List<ItemCapsule> listCapsules;

    public int getCapacity() {
        return this.capacity;
    }

    public ItemCapsule(int capacity, IMultipleRenderIcons icon) {
        super();
        this.capacity = capacity;
        this.icon = icon;
    }

    public ItemCapsule(int capacity, String iconName) {
        this(capacity, new MultipleIcon(3).addIcons(iconName + "_mask", iconName + "_mask", iconName));
    }

    public ItemStack getCapsule(String fluidname, int stackSize) {
        return fluidname == null ? new ItemStack(this, stackSize) : new ItemStack(this, stackSize, UtilFluid.getFluidID(fluidname));
    }

    public ItemStack getCapsule(String fluidname) {
        return this.getCapsule(fluidname, 1);
    }

    public String getFluidNameFromItemStack(ItemStack itemstack) {
        return itemstack == null ? null : UtilFluid.getFluidName(itemstack.func_77960_j());
    }

    public Fluid getFluidFromItemStack(ItemStack itemstack) {
        return itemstack == null ? null : UtilFluid.getFluid(itemstack.func_77960_j());
    }

    public FluidStack getFluidStackFromItemStack(ItemStack itemstack) {
        Fluid fluid = this.getFluidFromItemStack(itemstack);
        return fluid == null ? null : new FluidStack(fluid, this.getCapacity());
    }

    public void registerFluidContainer(boolean display) {
        this.addItemList(String.valueOf(0), 0, this.icon);
        ItemStack empty = this.getCapsule(null);

        for (String fluidname : FluidRegistry.getRegisteredFluids().keySet()) {
            this.addItemList(String.valueOf(UtilFluid.getFluidID(fluidname)), UtilFluid.getFluidID(fluidname), this.icon, display);
            ItemStack itemstack = this.getCapsule(fluidname);
            FluidContainerRegistry.registerFluidContainer(this.getFluidStackFromItemStack(itemstack), itemstack.func_77946_l(), empty.func_77946_l());
        }
    }

    public static void registerCompressionRecipe(ItemCapsule[] items, int[] compressionChain) {
        for (int i = 0; i < compressionChain.length; i++) {
            registerCompressionRecipe(items[i], items[i + 1], compressionChain[i]);
        }
    }

    public static void registerCompressionRecipe(ItemCapsule ingred, ItemCapsule result, int num) {
        GameRegistry.addRecipe(new ItemCapsule.RecipeCapsuleCompression(ingred, num, result, 1));
        GameRegistry.addRecipe(new ItemCapsule.RecipeCapsuleCompression(result, 1, ingred, num));
    }

    public static synchronized List<ItemCapsule> getAllCapsules() {
        if (listCapsules == null) {
            listCapsules = new ArrayList<>();

            for (Item item : GameData.getItemRegistry().typeSafeIterable()) {
                if (item instanceof ItemCapsule) {
                    listCapsules.add((ItemCapsule)item);
                }
            }
        }

        return listCapsules;
    }

    public static ItemStack getItemCapsuleFromFluidStack(FluidStack fluid) {
        if (fluid == null) {
            return null;
        } else {
            for (ItemCapsule capsule : getAllCapsules()) {
                if (capsule.getCapacity() == fluid.amount) {
                    return new ItemStack(capsule, 1, UtilFluid.getFluidID(fluid));
                }
            }

            return null;
        }
    }

    public String func_77667_c(ItemStack itemStack) {
        return super.func_77658_a();
    }

    @SideOnly(Side.CLIENT)
    public void func_77624_a(ItemStack itemstack, EntityPlayer player, List list, boolean flag) {
        super.func_77624_a(itemstack, player, list, flag);
        Fluid fluid = this.getFluidFromItemStack(itemstack);
        if (fluid != null) {
            list.add(fluid.getLocalizedName(this.getFluidStackFromItemStack(itemstack)) + "(" + this.getFluidNameFromItemStack(itemstack) + ")");
        }

        list.add(this.getCapacity() + "mB");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        if (pass == 1) {
            Fluid fluid = this.getFluidFromItemStack(stack);
            return fluid != null ? fluid.getIcon() : Blocks.field_150435_aG.func_149691_a(0, 0);
        } else {
            return this.func_77618_c(stack.func_77960_j(), pass);
        }
    }

    @SideOnly(Side.CLIENT)
    public int func_82790_a(ItemStack itemstack, int pass) {
        if (pass == 1) {
            Fluid fluid = this.getFluidFromItemStack(itemstack);
            return fluid != null ? fluid.getColor(this.getFluidStackFromItemStack(itemstack)) : 16777215;
        } else {
            return super.func_82790_a(itemstack, pass);
        }
    }

    @SideOnly(Side.CLIENT)
    public int func_94901_k() {
        return this.renderPass == 1 ? 0 : 1;
    }

    @SideOnly(Side.CLIENT)
    public void preRenderItemPass(ItemRenderType type, ItemStack itemstack, int pass, Object... data) {
        super.preRenderItemPass(type, itemstack, pass, data);
        if (pass == 1) {
            GL11.glDepthMask(false);
            GL11.glDepthFunc(514);
        }
    }

    @SideOnly(Side.CLIENT)
    public void postRenderItemPass(ItemRenderType type, ItemStack itemstack, int pass, Object... data) {
        super.postRenderItemPass(type, itemstack, pass, data);
        if (pass == 1) {
            GL11.glDepthMask(true);
            GL11.glDepthFunc(515);
        }
    }

    public static class RecipeCapsuleCompression implements IRecipe, ISpecialResultRecipe {
        ItemCapsule ingred;
        int ingredNum = 9;
        ItemCapsule result;
        int resultNum = 1;

        public RecipeCapsuleCompression(ItemCapsule ingred, int ingredNum, ItemCapsule result, int resultNum) {
            super();
            this.ingred = ingred;
            this.ingredNum = ingredNum;
            this.result = result;
            this.resultNum = resultNum;
        }

        public boolean func_77569_a(InventoryCrafting inv, World world) {
            return this.func_77572_b(inv) != null;
        }

        public ItemStack func_77572_b(InventoryCrafting inv) {
            int damage = -1;
            int c = 0;

            for (int i = 0; i < inv.func_70302_i_(); i++) {
                ItemStack ing = inv.func_70301_a(i);
                if (ing != null) {
                    if (ing.func_77973_b() != this.ingred || damage != -1 && ing.func_77960_j() != damage) {
                        return null;
                    }

                    damage = ing.func_77960_j();
                    c++;
                }
            }

            return c == this.ingredNum ? this.getRecipeOutput(damage) : null;
        }

        public int func_77570_a() {
            return this.ingredNum;
        }

        public ItemStack func_77571_b() {
            return new ItemStack(this.result, this.resultNum);
        }

        public ItemStack getRecipeOutput(int damage) {
            return new ItemStack(this.result, this.resultNum, damage);
        }

        public List<ItemStack> getRecipeInput(int damage) {
            List<ItemStack> list = new ArrayList<>();

            for (int i = 0; i < this.ingredNum; i++) {
                list.add(new ItemStack(this.ingred, 1, damage));
            }

            return list;
        }

        public RecipeMap[] getUsageRecipeMap(ItemStack ingredient) {
            return ingredient != null && ingredient.func_77973_b() == this.ingred
                ? new RecipeMap[]{
                    new RecipeMap(
                        this.getRecipeInput(ingredient.func_77960_j()).toArray(new ItemStack[0]), this.getRecipeOutput(ingredient.func_77960_j()), "shapeless"
                    )
                }
                : null;
        }

        public RecipeMap[] getCraftingRecipeMap(ItemStack result) {
            return result != null && result.func_77973_b() == this.result
                ? new RecipeMap[]{
                    new RecipeMap(
                        this.getRecipeInput(result.func_77960_j()).toArray(new ItemStack[0]), this.getRecipeOutput(result.func_77960_j()), "shapeless"
                    )
                }
                : null;
        }
    }

    public static class RecipeCapsulePackaging implements IRecipe, ISpecialResultRecipe {
        public RecipeCapsulePackaging() {
            super();
        }

        public RecipeMap[] getUsageRecipeMap(ItemStack ingredient) {
            if (ingredient == null) {
                return null;
            } else {
                ItemStack ingredtemp = ingredient.func_77946_l();
                ingredtemp.field_77994_a = 1;
                if (ingredtemp.func_77973_b() instanceof ItemCapsule) {
                    if (ingredtemp.func_77960_j() == 0) {
                        List<RecipeMap> list = new ArrayList<>();

                        for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                            if (((ItemCapsule)ingredient.func_77973_b()).getCapacity() == data.fluid.amount
                                && data.filledContainer.func_77973_b() instanceof ItemCapsule) {
                                list.add(
                                    new RecipeMap(
                                        new ItemStack[]{data.filledContainer.func_77946_l(), ingredtemp.func_77946_l()},
                                        new ItemStack(ingredtemp.func_77973_b(), 1, UtilFluid.getFluidID(data.fluid)),
                                        "shapeless"
                                    )
                                );
                            }
                        }

                        return list.toArray(new RecipeMap[0]);
                    } else {
                        return null;
                    }
                } else if (FluidContainerRegistry.isFilledContainer(ingredtemp)) {
                    List<RecipeMap> list = new ArrayList<>();
                    FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(ingredtemp);

                    for (ItemCapsule capsule : ItemCapsule.getAllCapsules()) {
                        if (capsule.getCapacity() == fluid.amount) {
                            list.add(
                                new RecipeMap(
                                    new ItemStack[]{ingredtemp.func_77946_l(), new ItemStack(capsule)},
                                    new ItemStack(capsule, 1, UtilFluid.getFluidID(fluid)),
                                    "shapeless"
                                )
                            );
                        }
                    }

                    return list.toArray(new RecipeMap[0]);
                } else {
                    return null;
                }
            }
        }

        public RecipeMap[] getCraftingRecipeMap(ItemStack result) {
            if (result == null) {
                return null;
            } else if (result.func_77973_b() instanceof ItemCapsule && result.func_77960_j() != 0) {
                List<RecipeMap> list = new ArrayList<>();
                ItemStack resulttemp = result.func_77946_l();
                resulttemp.field_77994_a = 1;
                ItemCapsule capsule = (ItemCapsule)resulttemp.func_77973_b();

                for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                    if (capsule.getCapacity() == data.fluid.amount
                        && resulttemp.func_77960_j() == UtilFluid.getFluidID(data.fluid)
                        && !(data.filledContainer.func_77973_b() instanceof ItemCapsule)) {
                        list.add(
                            new RecipeMap(new ItemStack[]{data.filledContainer.func_77946_l(), new ItemStack(capsule)}, resulttemp.func_77946_l(), "shapeless")
                        );
                    }
                }

                return list.toArray(new RecipeMap[0]);
            } else {
                return null;
            }
        }

        public boolean func_77569_a(InventoryCrafting inv, World p_77569_2_) {
            return this.checkPlacedItem(inv) && this.getFluidContainer(inv) != null && this.getCapsule(inv) != null;
        }

        public ItemStack func_77572_b(InventoryCrafting inv) {
            if (!this.checkPlacedItem(inv)) {
                return null;
            } else {
                ItemStack fluidContainer = this.getFluidContainer(inv);
                if (fluidContainer == null) {
                    return null;
                } else {
                    FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(fluidContainer);
                    if (fluid == null) {
                        return null;
                    } else {
                        ItemStack capsule = this.getCapsule(inv);
                        if (capsule == null) {
                            return null;
                        } else {
                            ItemCapsule itemCapsule = (ItemCapsule)capsule.func_77973_b();
                            if (itemCapsule.getCapacity() != fluid.amount) {
                                return null;
                            } else {
                                ItemStack ret = new ItemStack(itemCapsule, 1, UtilFluid.getFluidID(fluid));
                                if (fluidContainer.func_77942_o()) {
                                    ret.func_77982_d((NBTTagCompound)fluidContainer.func_77978_p().func_74737_b());
                                }

                                return ret;
                            }
                        }
                    }
                }
            }
        }

        private boolean checkPlacedItem(InventoryCrafting inv) {
            if (inv == null) {
                return false;
            } else {
                ItemStack ret = null;
                int count = 0;

                for (int i = 0; i < inv.func_70302_i_(); i++) {
                    if (inv.func_70301_a(i) != null) {
                        count++;
                    }

                    if (count > 2) {
                        return false;
                    }
                }

                return count == 2;
            }
        }

        private ItemStack getFluidContainer(InventoryCrafting inv) {
            if (inv == null) {
                return null;
            } else {
                ItemStack ret = null;

                for (int i = 0; i < inv.func_70302_i_(); i++) {
                    ItemStack item = inv.func_70301_a(i);
                    if (item != null && !(item.func_77973_b() instanceof ItemCapsule) && FluidContainerRegistry.isFilledContainer(item)) {
                        if (ret != null) {
                            return null;
                        }

                        ret = item.func_77946_l();
                    }
                }

                return ret;
            }
        }

        private ItemStack getCapsule(InventoryCrafting inv) {
            if (inv == null) {
                return null;
            } else {
                ItemStack ret = null;

                for (int i = 0; i < inv.func_70302_i_(); i++) {
                    ItemStack item = inv.func_70301_a(i);
                    if (item != null && item.func_77973_b() instanceof ItemCapsule && item.func_77960_j() == 0) {
                        if (ret != null) {
                            return null;
                        }

                        ret = item.func_77946_l();
                    }
                }

                return ret;
            }
        }

        public int func_77570_a() {
            return 2;
        }

        public ItemStack func_77571_b() {
            return null;
        }
    }

    public static class RecipeCapsuleUnpackaging implements IRecipe, ISpecialResultRecipe {
        public RecipeCapsuleUnpackaging() {
            super();
        }

        public RecipeMap[] getUsageRecipeMap(ItemStack ingredient) {
            if (ingredient == null) {
                return null;
            } else if (ingredient.func_77973_b() instanceof ItemCapsule) {
                if (ingredient.func_77960_j() != 0) {
                    ItemStack ingredtemp = ingredient.func_77946_l();
                    ingredtemp.field_77994_a = 1;
                    List<RecipeMap> list = new ArrayList<>();

                    for (FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                        if (((ItemCapsule)ingredient.func_77973_b()).getCapacity() == data.fluid.amount
                            && ingredient.func_77960_j() == UtilFluid.getFluidID(data.fluid)
                            && data.emptyContainer != null
                            && !(data.filledContainer.func_77973_b() instanceof ItemCapsule)) {
                            list.add(
                                new RecipeMap(
                                    new ItemStack[]{data.emptyContainer.func_77946_l(), ingredtemp.func_77946_l()},
                                    data.filledContainer.func_77946_l(),
                                    "shapeless"
                                )
                            );
                        }
                    }

                    return list.toArray(new RecipeMap[0]);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }

        public RecipeMap[] getCraftingRecipeMap(ItemStack result) {
            if (result == null) {
                return null;
            } else if (!(result.func_77973_b() instanceof ItemCapsule) && FluidContainerRegistry.isFilledContainer(result)) {
                ItemStack empty = FluidContainerRegistry.drainFluidContainer(result);
                if (empty == null) {
                    return null;
                } else {
                    List<RecipeMap> list = new ArrayList<>();
                    ItemStack resulttemp = result.func_77946_l();
                    resulttemp.field_77994_a = 1;
                    FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(resulttemp);

                    for (ItemCapsule capsule : ItemCapsule.getAllCapsules()) {
                        if (capsule.getCapacity() == fluid.amount) {
                            list.add(
                                new RecipeMap(
                                    new ItemStack[]{empty.func_77946_l(), new ItemStack(capsule, 1, UtilFluid.getFluidID(fluid))},
                                    resulttemp.func_77946_l(),
                                    "shapeless"
                                )
                            );
                        }
                    }

                    return list.toArray(new RecipeMap[0]);
                }
            } else {
                return null;
            }
        }

        public boolean func_77569_a(InventoryCrafting inv, World p_77569_2_) {
            return this.func_77572_b(inv) != null;
        }

        public ItemStack func_77572_b(InventoryCrafting inv) {
            if (!this.checkPlacedItem(inv)) {
                return null;
            } else {
                ItemStack fluidContainer = this.getFluidContainer(inv);
                if (fluidContainer == null) {
                    return null;
                } else {
                    ItemStack capsule = this.getCapsule(inv);
                    if (capsule == null) {
                        return null;
                    } else {
                        ItemCapsule itemCapsule = (ItemCapsule)capsule.func_77973_b();
                        ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(itemCapsule.getFluidStackFromItemStack(capsule), fluidContainer);
                        if (filledContainer != null && itemCapsule.getCapacity() == FluidContainerRegistry.getContainerCapacity(filledContainer)) {
                            if (capsule.func_77942_o()) {
                                filledContainer.func_77982_d((NBTTagCompound)capsule.func_77978_p().func_74737_b());
                            }

                            return filledContainer.func_77946_l();
                        } else {
                            return null;
                        }
                    }
                }
            }
        }

        private boolean checkPlacedItem(InventoryCrafting inv) {
            if (inv == null) {
                return false;
            } else {
                int count = 0;

                for (int i = 0; i < inv.func_70302_i_(); i++) {
                    if (inv.func_70301_a(i) != null) {
                        count++;
                    }

                    if (count > 2) {
                        return false;
                    }
                }

                return count == 2;
            }
        }

        private ItemStack getFluidContainer(InventoryCrafting inv) {
            if (inv == null) {
                return null;
            } else {
                ItemStack ret = null;

                for (int i = 0; i < inv.func_70302_i_(); i++) {
                    ItemStack item = inv.func_70301_a(i);
                    if (item != null && !(item.func_77973_b() instanceof ItemCapsule) && FluidContainerRegistry.isEmptyContainer(item)) {
                        if (ret != null) {
                            return null;
                        }

                        ret = item.func_77946_l();
                    }
                }

                return ret;
            }
        }

        private ItemStack getCapsule(InventoryCrafting inv) {
            if (inv == null) {
                return null;
            } else {
                ItemStack ret = null;

                for (int i = 0; i < inv.func_70302_i_(); i++) {
                    ItemStack item = inv.func_70301_a(i);
                    if (item != null && item.func_77973_b() instanceof ItemCapsule && item.func_77960_j() != 0) {
                        if (ret != null) {
                            return null;
                        }

                        ret = item.func_77946_l();
                    }
                }

                return ret;
            }
        }

        public int func_77570_a() {
            return 2;
        }

        public ItemStack func_77571_b() {
            return null;
        }
    }
}
