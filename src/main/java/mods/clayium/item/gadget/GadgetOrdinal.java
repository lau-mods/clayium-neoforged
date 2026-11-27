package mods.clayium.item.gadget;

import java.util.Arrays;
import java.util.List;
import mods.clayium.item.CItems;
import mods.clayium.item.IItemGadget;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class GadgetOrdinal implements IItemGadget {
    public List<String> itemNames;

    public GadgetOrdinal(String... itemNames) {
        super();
        this.itemNames = Arrays.asList(itemNames);
    }

    public boolean match(ItemStack itemStack, World world, Entity entity, int slot, boolean isCurrentItem) {
        return itemStack != null && itemStack.func_77973_b() == CItems.itemGadget ? this.itemNames.contains(CItems.itemGadget.getItemName(itemStack)) : false;
    }

    public void update(List<ItemStack> list, Entity entity, boolean isRemote) {
        int i = -1;

        for (ItemStack item : list) {
            if (item != null && item.func_77973_b() == CItems.itemGadget) {
                i = Math.max(this.itemNames.indexOf(CItems.itemGadget.getItemName(item)), i);
            }
        }

        this.update(i, entity, isRemote);
    }

    public abstract void update(int var1, Entity var2, boolean var3);
}
