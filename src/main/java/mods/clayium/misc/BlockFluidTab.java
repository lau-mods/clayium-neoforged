package mods.clayium.misc;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockFluidTab extends BlockContainer {
    public BlockFluidTab() {
        super(Material.field_151575_d);
        this.func_149672_a(Block.field_149766_f);
        this.func_149711_c(1.0F);
    }

    public boolean func_149727_a(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
        ItemStack itemstack = par5EntityPlayer.field_71071_by.func_70448_g();
        TileFluidTab tile = (TileFluidTab)UtilBuilder.safeGetTileEntity(par1World, par2, par3, par4);
        if (tile != null) {
            FluidStack fluid = tile.productTank.getFluid();
            if (itemstack == null) {
                String s = "";
                if (fluid != null && fluid.getFluid() != null) {
                    s = "Fluid current in the tab : " + fluid.getFluid().getLocalizedName(fluid);
                } else {
                    s = "No fluid in the tab";
                }

                if (!par1World.field_72995_K) {
                    par5EntityPlayer.func_145747_a(new ChatComponentText(s));
                }

                return true;
            }

            FluidStack fluid2 = FluidContainerRegistry.getFluidForFilledItem(itemstack);
            if (fluid2 == null || fluid2.getFluid() == null) {
                if (fluid != null && fluid.getFluid() != null) {
                    if (fluid.amount < 1000) {
                        return true;
                    }

                    ItemStack get = FluidContainerRegistry.fillFluidContainer(new FluidStack(fluid.getFluid(), 1000), itemstack);
                    if (get != null) {
                        tile.drain(ForgeDirection.UNKNOWN, 1000, true);
                        if (!par5EntityPlayer.field_71071_by.func_70441_a(get.func_77946_l())) {
                            par5EntityPlayer.func_70099_a(get.func_77946_l(), 1.0F);
                        }

                        if (!par5EntityPlayer.field_71075_bZ.field_75098_d && itemstack.field_77994_a-- <= 0) {
                            par5EntityPlayer.field_71071_by.func_70299_a(par5EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                        }

                        tile.func_70296_d();
                        par5EntityPlayer.field_71071_by.func_70296_d();
                        par1World.func_147471_g(par2, par3, par4);
                        par1World.func_72956_a(par5EntityPlayer, "random.pop", 0.4F, 1.8F);
                    }

                    return true;
                }

                return true;
            }

            int put = tile.fill(ForgeDirection.UNKNOWN, fluid2, false);
            if (put == fluid2.amount) {
                tile.fill(ForgeDirection.UNKNOWN, fluid2, true);
                ItemStack emptyContainer = FluidContainerRegistry.drainFluidContainer(itemstack);
                if (emptyContainer != null && !par5EntityPlayer.field_71071_by.func_70441_a(emptyContainer.func_77946_l())) {
                    par5EntityPlayer.func_70099_a(emptyContainer.func_77946_l(), 1.0F);
                }

                if (!par5EntityPlayer.field_71075_bZ.field_75098_d && itemstack.field_77994_a-- <= 0) {
                    par5EntityPlayer.field_71071_by.func_70299_a(par5EntityPlayer.field_71071_by.field_70461_c, (ItemStack)null);
                }

                tile.func_70296_d();
                par5EntityPlayer.field_71071_by.func_70296_d();
                par1World.func_147471_g(par2, par3, par4);
                par1World.func_72956_a(par5EntityPlayer, "random.pop", 0.4F, 1.8F);
                return true;
            }
        }

        return true;
    }

    public TileEntity func_149915_a(World world, int a) {
        return new TileFluidTab();
    }

    public void func_149743_a(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity) {
        this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
        super.func_149743_a(world, x, y, z, aabb, list, entity);
        float f = 0.0675F;
        this.func_149676_a(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
        super.func_149743_a(world, x, y, z, aabb, list, entity);
        this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
        super.func_149743_a(world, x, y, z, aabb, list, entity);
        this.func_149676_a(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        super.func_149743_a(world, x, y, z, aabb, list, entity);
        this.func_149676_a(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
        super.func_149743_a(world, x, y, z, aabb, list, entity);
        this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        this.func_149683_g();
    }

    public boolean func_149662_c() {
        return false;
    }

    public int func_149645_b() {
        return ClayiumCore.fluidTabRenderId;
    }

    public boolean func_149686_d() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister p_149651_1_) {
        this.field_149761_L = Blocks.field_150344_f.func_149691_a(0, 0);
    }
}
