package mods.clayium.item;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public interface IHarvestCoord {
    List<Vec3> getHarvestedCoordList(ItemStack var1, int var2, int var3, int var4, Vec3 var5, Vec3 var6, Vec3 var7);

    boolean onItemUse(ItemStack var1, EntityPlayer var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10);
}
