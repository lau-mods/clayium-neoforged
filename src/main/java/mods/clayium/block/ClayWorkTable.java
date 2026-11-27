package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import mods.clayium.block.tile.TileClayWorkTable;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ClayWorkTable extends BlockContainer implements ITieredBlock, ISpecialToolTip {
    @SideOnly(Side.CLIENT)
    private IIcon TopIcon;
    @SideOnly(Side.CLIENT)
    private IIcon SideIcon;
    private final Random random = new Random();

    public ClayWorkTable() {
        super(Material.field_151576_e);
    }

    public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        world.func_147471_g(x, y, z);
        player.openGui(ClayiumCore.INSTANCE, 0, world, x, y, z);
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
        this.TopIcon = par1IconRegister.func_94245_a("clayium:clayworktable");
        this.SideIcon = par1IconRegister.func_94245_a("clayium:clayworktable_side");
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int par1, int par2) {
        return par1 != 0 && par1 != 1 ? this.SideIcon : this.TopIcon;
    }

    public static void updateBlockState(World world, int x, int y, int z) {
        TileEntity tileentity = UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (tileentity != null) {
            tileentity.func_145829_t();
            world.func_147455_a(x, y, z, tileentity);
        }
    }

    public void func_149749_a(World world, int x, int y, int z, Block block, int meta) {
        TileClayWorkTable tileentityclayworktable = (TileClayWorkTable)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (tileentityclayworktable != null) {
            for (int i = 0; i < tileentityclayworktable.func_70302_i_(); i++) {
                ItemStack itemstack = tileentityclayworktable.func_70301_a(i);
                if (itemstack != null) {
                    float f = this.random.nextFloat() * 0.6F + 0.1F;
                    float f1 = this.random.nextFloat() * 0.6F + 0.1F;
                    float f2 = this.random.nextFloat() * 0.6F + 0.1F;

                    while (itemstack.field_77994_a > 0) {
                        int j = this.random.nextInt(21) + 10;
                        if (j > itemstack.field_77994_a) {
                            j = itemstack.field_77994_a;
                        }

                        itemstack.field_77994_a -= j;
                        EntityItem entityitem = new EntityItem(
                            world,
                            (double)((float)x + f),
                            (double)((float)y + f1),
                            (double)((float)z + f2),
                            new ItemStack(itemstack.func_77973_b(), j, itemstack.func_77960_j())
                        );
                        if (itemstack.func_77942_o()) {
                            entityitem.func_92059_d().func_77982_d((NBTTagCompound)itemstack.func_77978_p().func_74737_b());
                        }

                        float f3 = 0.025F;
                        entityitem.field_70159_w = (double)((float)this.random.nextGaussian() * f3);
                        entityitem.field_70181_x = (double)((float)this.random.nextGaussian() * f3 + 0.1F);
                        entityitem.field_70179_y = (double)((float)this.random.nextGaussian() * f3);
                        world.func_72838_d(entityitem);
                    }
                }
            }

            world.func_147453_f(x, y, z, block);
            tileentityclayworktable.releaseTicket();
        }

        super.func_149749_a(world, x, y, z, block, meta);
    }

    public TileEntity func_149915_a(World world, int par2) {
        return new TileClayWorkTable();
    }

    public void func_149670_a(World world, int x, int y, int z, Entity entity) {
        this.func_149724_b(world, x, y, z, entity);
    }

    public void func_149724_b(World world, int x, int y, int z, Entity entity) {
        TileClayWorkTable te = (TileClayWorkTable)UtilBuilder.safeGetTileEntity(world, x, y, z);
        if (entity instanceof IMerchant) {
            ClayiumCore.logger.info("entity instanceof IMerchant");
            te.merchant = (IMerchant)entity;
            te.setCurrentRecipeIndex(te.merchant.func_70934_b(null).size() - 1);
        }
    }

    public int getTier(ItemStack itemstack) {
        return 0;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return 0;
    }

    public List getTooltip(ItemStack itemStack) {
        return UtilLocale.localizeTooltip(this.func_149739_a() + ".tooltip");
    }
}
