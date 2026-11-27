package mods.clayium.util.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mods.clayium.util.UtilItemStack;
import net.minecraft.item.ItemStack;

public class ItemPatternItemStack implements IItemPattern {
    private List<ItemStack> items;

    public ItemPatternItemStack(List<ItemStack> itemstacks) {
        super();
        this.items = itemstacks;
    }

    public ItemPatternItemStack(ItemStack itemstack) {
        super();
        if (itemstack != null) {
            this.items = new ArrayList<>(Arrays.asList(itemstack.func_77946_l()));
        } else {
            this.items = new ArrayList<>(Arrays.asList(null));
        }
    }

    public String toString() {
        String ret = "";
        if (this.items == null) {
            ret = "null";
        } else {
            for (int i = 0; i < this.items.size(); i++) {
                ret = ret + (this.items.get(i) == null ? null : this.items.get(i).toString());
                if (i < this.items.size() - 1) {
                    ret = ret + ",";
                }
            }
        }

        return "ItemPatternItemStack<" + ret + ">";
    }

    public boolean match(ItemStack itemstack, boolean checkStackSize) {
        for (ItemStack item : this.items) {
            if (this.canBeCrafted(itemstack, item, checkStackSize)) {
                return true;
            }
        }

        return false;
    }

    public boolean canBeCrafted(ItemStack ingred, ItemStack recipe, boolean checkStackSize) {
        return Recipes.canBeCrafted(ingred, recipe, checkStackSize);
    }

    public boolean hasIntersection(IItemPattern pattern, boolean checkStackSize) {
        if (pattern == null) {
            return false;
        } else {
            for (ItemStack item : this.items) {
                if (pattern.match(item, checkStackSize)) {
                    return true;
                }

                if (item != null && item.func_77960_j() == 32767) {
                    ItemStack[] patternitems = pattern.toItemStacks();

                    for (ItemStack patternitem : patternitems) {
                        if (UtilItemStack.areItemEqual(patternitem, item)) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public List<ItemStack> getPatternItems() {
        return this.items;
    }

    public int getStackSize(ItemStack itemstack) {
        for (ItemStack item : this.items) {
            if (item == null && itemstack == null) {
                return 1;
            }

            if (item != null && item.func_77960_j() == 32767) {
                if (UtilItemStack.areItemEqual(itemstack, item)) {
                    return Math.max(item.field_77994_a, 1);
                }
            } else if (UtilItemStack.areItemDamageEqual(itemstack, item)) {
                return Math.max(item.field_77994_a, 1);
            }
        }

        return 1;
    }

    public ItemStack[] toItemStacks() {
        return this.items.toArray(new ItemStack[0]);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        return 31 * result + UtilItemStack.getItemStackHashCode(this.items);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            ItemPatternItemStack other = (ItemPatternItemStack)obj;
            if (this.items == null) {
                if (other.items != null) {
                    return false;
                }
            } else if (!UtilItemStack.areStacksEqual(this.items, other.items)) {
                return false;
            }

            return true;
        }
    }

    public ItemStack isSimple() {
        if (this.items.size() != 1) {
            return null;
        } else {
            ItemStack ret = this.items.get(0);
            return ret != null && ret.func_77960_j() == 32767 ? null : ret;
        }
    }

    public boolean isAvailable() {
        return this.items != null && this.items.size() >= 1;
    }
}
