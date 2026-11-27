package mods.clayium.item;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IItemGadget {
    boolean match(ItemStack var1, World var2, Entity var3, int var4, boolean var5);

    void update(List<ItemStack> var1, Entity var2, boolean var3);
}
