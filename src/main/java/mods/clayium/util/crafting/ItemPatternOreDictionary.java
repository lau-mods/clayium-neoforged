package mods.clayium.util.crafting;

import java.util.List;
import mods.clayium.util.UtilItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemPatternOreDictionary implements IItemPattern {
    private String oreDictionary;
    private int id;
    private int stackSize;

    public ItemPatternOreDictionary(String odName, int stackSize) {
        super();
        this.oreDictionary = odName;
        this.id = OreDictionary.getOreID(odName);
        this.stackSize = stackSize;
    }

    public ItemPatternOreDictionary(int odId, int stackSize) {
        super();
        this.id = odId;
        this.stackSize = stackSize;
    }

    public String toString() {
        String ret = this.oreDictionary == null ? "null" : this.oreDictionary;
        return "ItemPatternItemStack<" + ret + ":" + this.stackSize + ">";
    }

    public boolean match(ItemStack itemstack, boolean checkStackSize) {
        return itemstack != null && (!checkStackSize || itemstack.field_77994_a >= this.stackSize) && UtilItemStack.hasOreName(itemstack, this.id);
    }

    public boolean hasIntersection(IItemPattern pattern, boolean checkStackSize) {
        if (pattern == null) {
            return false;
        } else {
            for (ItemStack item : this.toItemStacks()) {
                if (pattern.match(item, checkStackSize)) {
                    return true;
                }
            }

            return false;
        }
    }

    public String getPatternString() {
        if (this.oreDictionary == null) {
            this.oreDictionary = OreDictionary.getOreName(this.id);
        }

        return this.oreDictionary;
    }

    public int getStackSize(ItemStack itemstack) {
        return this.stackSize;
    }

    public ItemStack[] toItemStacks() {
        ItemStack[] stacks = OreDictionary.getOres(this.id).toArray(new ItemStack[0]);
        ItemStack[] stacks1 = new ItemStack[stacks.length];

        for (int i = 0; i < stacks.length; i++) {
            stacks1[i] = stacks[i].func_77946_l();
            stacks1[i].field_77994_a = this.stackSize;
        }

        return stacks1;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this.id;
        return 31 * result + this.stackSize;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            ItemPatternOreDictionary other = (ItemPatternOreDictionary)obj;
            return this.id != other.id ? false : this.stackSize == other.stackSize;
        }
    }

    public ItemStack isSimple() {
        return null;
    }

    public boolean isAvailable() {
        List<ItemStack> list = OreDictionary.getOres(this.id);
        return list != null && list.size() != 0;
    }
}
