package mods.clayium.util.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CItems;
import mods.clayium.item.CMaterials;
import mods.clayium.util.UtilItemStack;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ClayWorkTableRecipes {
    private static final ClayWorkTableRecipes SMELTING_BASE = new ClayWorkTableRecipes();
    private Map smeltingList = new HashMap();
    private Map experienceList = new HashMap();
    public Map kneadingList = new HashMap();

    public static ClayWorkTableRecipes smelting() {
        return SMELTING_BASE;
    }

    private ClayWorkTableRecipes() {
        super();
        this.addRecipe(new ItemStack(Items.field_151119_aD), 1, CMaterials.itemClayParts.getItemStack("Stick"), null, 4);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("LargeBall"), 2, CMaterials.itemClayParts.getItemStack("Disc"), null, 30);
        this.addRecipe(
            CMaterials.itemClayParts.getItemStack("LargeBall"), 3, CMaterials.itemClayParts.getItemStack("Disc"), new ItemStack(Items.field_151119_aD, 2), 4
        );
        this.addRecipe(CMaterials.itemClayParts.getItemStack("LargeBall"), 1, CMaterials.itemClayParts.getItemStack("Cylinder"), null, 4);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Plate"), 2, CMaterials.itemClayParts.getItemStack("Blade"), null, 10);
        this.addRecipe(
            CMaterials.itemClayParts.getItemStack("Plate"), 3, CMaterials.itemClayParts.getItemStack("Blade"), new ItemStack(Items.field_151119_aD, 2), 1
        );
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Plate"), 6, CMaterials.itemClayParts.getItemStack("Stick", 4), null, 3);
        this.addRecipe(
            CMaterials.itemClayParts.getItemStack("Disc"), 4, CMaterials.itemClayParts.getItemStack("Plate"), new ItemStack(Items.field_151119_aD, 2), 4
        );
        this.addRecipe(
            CMaterials.itemClayParts.getItemStack("Disc"),
            5,
            CMaterials.itemClayParts.getItemStack("Ring"),
            CMaterials.itemClayParts.getItemStack("SmallDisc"),
            2
        );
        this.addRecipe(
            CMaterials.itemClayParts.getItemStack("SmallDisc"),
            5,
            CMaterials.itemClayParts.getItemStack("SmallRing"),
            CMaterials.itemClayParts.getItemStack("ShortStick"),
            1
        );
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Cylinder"), 1, CMaterials.itemClayParts.getItemStack("Needle"), null, 3);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Cylinder"), 6, CMaterials.itemClayParts.getItemStack("SmallDisc", 8), null, 7);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Disc"), 2, CItems.itemRawClayCraftingTools.getItemStack("Slicer"), null, 15);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Disc"), 3, CItems.itemRawClayCraftingTools.getItemStack("Slicer"), null, 2);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Plate", 6), 3, CMaterials.itemClayParts.getItemStack("LargePlate"), null, 10);
        this.addRecipe(CMaterials.itemClayParts.getItemStack("Plate", 3), 1, CMaterials.itemClayParts.getItemStack("LargeBall"), null, 40);
    }

    public void addRecipe(Item item, ItemStack itemstack, float experience) {
        this.addLists(item, itemstack, experience);
    }

    public void addLists(Item item, ItemStack itemstack, float experience) {
        this.putLists(new ItemStack(item), itemstack, experience);
    }

    public void putLists(ItemStack itemstack, ItemStack itemstack2, float experience) {
        this.smeltingList.put(itemstack, itemstack2);
        this.experienceList.put(itemstack2, experience);
    }

    public ItemStack getSmeltingResult(ItemStack itemstack) {
        for (Entry entry : this.smeltingList.entrySet()) {
            if (this.canBeSmelted(itemstack, (ItemStack)entry.getKey())) {
                return (ItemStack)entry.getValue();
            }
        }

        return null;
    }

    private boolean canBeSmelted(ItemStack itemstack, ItemStack itemstack2) {
        return UtilItemStack.areItemEqual(itemstack2, itemstack)
            && (itemstack2.func_77960_j() == 32767 || UtilItemStack.areDamageEqual(itemstack2, itemstack))
            && itemstack2.field_77994_a <= itemstack.field_77994_a;
    }

    public float giveExperience(ItemStack itemstack) {
        for (Entry entry : this.experienceList.entrySet()) {
            if (this.canBeSmelted(itemstack, (ItemStack)entry.getKey())) {
                if (itemstack.func_77973_b().getSmeltingExperience(itemstack) != -1.0F) {
                    return itemstack.func_77973_b().getSmeltingExperience(itemstack);
                }

                return (Float)entry.getValue();
            }
        }

        return 0.0F;
    }

    public void addRecipe(Item item, int buttonId, ItemStack itemstack, ItemStack itemstack2, int cookTime) {
        this.addRecipe(new ItemStack(item), buttonId, itemstack, itemstack2, cookTime);
    }

    public void addRecipe(ItemStack itemstack, int buttonId, ItemStack itemstack2, ItemStack itemstack3, int cookTime) {
        Map keyMap = new HashMap();
        Map valueMap = new HashMap();
        keyMap.put("Material", itemstack);
        keyMap.put("ButtonId", buttonId);
        valueMap.put("Result", itemstack2);
        valueMap.put("Result2", itemstack3);
        valueMap.put("CookTime", ClayiumCore.divideByProgressionRateI(cookTime));
        this.kneadingList.put(keyMap, valueMap);
    }

    public Map getKneadingResultMap(ItemStack itemstack, int buttonId) {
        Entry entry_ = null;
        int maxStackSize = 0;

        for (Entry entry : this.kneadingList.entrySet()) {
            if (this.canBeSmelted(itemstack, (ItemStack)((Map)entry.getKey()).get("Material"))
                && (Integer)((Map)entry.getKey()).get("ButtonId") == buttonId
                && ((ItemStack)((Map)entry.getKey()).get("Material")).field_77994_a > maxStackSize) {
                entry_ = entry;
                maxStackSize = ((ItemStack)((Map)entry.getKey()).get("Material")).field_77994_a;
            }
        }

        return entry_ == null ? null : (Map)entry_.getValue();
    }

    public int getConsumedStackSize(ItemStack itemstack, int buttonId) {
        Entry entry_ = null;
        int maxStackSize = 0;

        for (Entry entry : this.kneadingList.entrySet()) {
            if (this.canBeSmelted(itemstack, (ItemStack)((Map)entry.getKey()).get("Material"))
                && (Integer)((Map)entry.getKey()).get("ButtonId") == buttonId
                && ((ItemStack)((Map)entry.getKey()).get("Material")).field_77994_a > maxStackSize) {
                entry_ = entry;
                maxStackSize = ((ItemStack)((Map)entry.getKey()).get("Material")).field_77994_a;
            }
        }

        return entry_ == null ? 0 : maxStackSize;
    }

    public ItemStack getKneadingResult(ItemStack itemstack, int buttonId) {
        return this.getKneadingResultMap(itemstack, buttonId) == null ? null : (ItemStack)this.getKneadingResultMap(itemstack, buttonId).get("Result");
    }

    public ItemStack getKneadingResult2(ItemStack itemstack, int buttonId) {
        return this.getKneadingResultMap(itemstack, buttonId) == null ? null : (ItemStack)this.getKneadingResultMap(itemstack, buttonId).get("Result2");
    }

    public int getKneadingTime(ItemStack itemstack, int buttonId) {
        return this.getKneadingResultMap(itemstack, buttonId) == null ? 0 : (Integer)this.getKneadingResultMap(itemstack, buttonId).get("CookTime");
    }

    public boolean hasKneadingResult(ItemStack itemstack) {
        boolean flag = false;

        for (Entry entry : this.kneadingList.entrySet()) {
            if (this.canBeSmelted(itemstack, (ItemStack)((Map)entry.getKey()).get("Material"))) {
                flag = true;
            }
        }

        return flag;
    }
}
