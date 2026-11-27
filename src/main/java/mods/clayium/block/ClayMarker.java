package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import mods.clayium.block.tile.TileClayMarker;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilLocale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ClayMarker extends BlockContainer implements ITieredBlock, ISpecialToolTip {
    public Block iconBlock;
    public Class<? extends TileEntity> tileEntityClass;
    public int tier;
    public int iconMetadata = -1;

    public ClayMarker(int tier, Block iconBlock, int iconMetadata, Class<? extends TileEntity> tileEntityClass) {
        super(Material.field_151571_B);
        this.func_149672_a(field_149767_g);
        this.func_149711_c(0.5F);
        this.func_149752_b(5.0F);
        this.setHarvestLevel("shovel", 0);
        float f = 0.1875F;
        this.func_149676_a(0.5F - f, 0.5F - f, 0.5F - f, 0.5F + f, 0.5F + f, 0.5F + f);
        this.iconBlock = iconBlock;
        this.tileEntityClass = tileEntityClass;
        this.tier = tier;
        this.iconMetadata = iconMetadata;
    }

    public ClayMarker(int tier, Block iconBlock, Class<? extends TileEntity> tileEntityClass) {
        this(tier, iconBlock, -1, tileEntityClass);
    }

    public AxisAlignedBB func_149668_a(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
        this.func_149719_a(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
        return super.func_149668_a(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
    }

    public void func_149719_a(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {
        float f = 0.1875F;
        this.func_149676_a(0.5F - f, 0.5F - f, 0.5F - f, 0.5F + f, 0.5F + f, 0.5F + f);
    }

    public void func_149743_a(
        World p_149743_1_, int p_149743_2_, int p_149743_3_, int p_149743_4_, AxisAlignedBB p_149743_5_, List p_149743_6_, Entity p_149743_7_
    ) {
        this.func_149719_a(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_);
        super.func_149743_a(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_, p_149743_5_, p_149743_6_, p_149743_7_);
    }

    public void func_149749_a(World world, int x, int y, int z, Block block, int meta) {
        world.func_147453_f(x, y, z, block);
        super.func_149749_a(world, x, y, z, block, meta);
    }

    public TileEntity func_149915_a(World p_149915_1_, int p_149915_2_) {
        try {
            return this.tileEntityClass.newInstance();
        } catch (InstantiationException var4) {
            ClayiumCore.logger.catching(var4);
            return null;
        } catch (IllegalAccessException var5) {
            ClayiumCore.logger.catching(var5);
            return null;
        }
    }

    public boolean func_149727_a(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileClayMarker te = (TileClayMarker)UtilBuilder.safeGetTileEntity(world, x, y, z);
        te.activate();
        return true;
    }

    public boolean func_149662_c() {
        return false;
    }

    public boolean func_149686_d() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
        return this.iconBlock.func_149691_a(p_149691_1_, this.iconMetadata == -1 ? p_149691_2_ : this.iconMetadata);
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister par1IconRegister) {
    }

    public int getTier(ItemStack itemstack) {
        return this.tier;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return this.tier;
    }

    public List getTooltip(ItemStack itemStack) {
        return UtilLocale.localizeTooltip(this.func_149739_a() + ".tooltip");
    }
}
