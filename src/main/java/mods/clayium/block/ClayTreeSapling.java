package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import mods.clayium.worldgen.WorldGenGenericTrees;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ClayTreeSapling extends BlockBush implements IGrowable, ITieredBlock {
    private IIcon saplingIcon;

    public ClayTreeSapling() {
        super();
        float f = 0.4F;
        this.func_149676_a(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
    }

    public void func_149674_a(World world, int x, int y, int z, Random random) {
        if (!world.field_72995_K) {
            super.func_149674_a(world, x, y, z, random);
            if (world.func_72957_l(x, y + 1, z) >= 9 && random.nextInt(7) == 0) {
                this.growUp(world, x, y, z, random);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
        return this.saplingIcon;
    }

    public void growUp(World world, int x, int y, int z, Random random) {
        int l = world.func_72805_g(x, y, z);
        if ((l & 8) == 0) {
            world.func_72921_c(x, y, z, l | 8, 4);
        } else {
            this.growTree(world, x, y, z, random);
        }
    }

    public void growTree(World world, int x, int y, int z, Random random) {
        if (TerrainGen.saplingGrowTree(world, random, x, y, z)) {
            int l = world.func_72805_g(x, y, z) & 7;
            Object object = new WorldGenGenericTrees(true, 5, CBlocks.blockClayTreeLog, 0, CBlocks.blockClayTreeLeaf, 0, false);
            int i1 = 0;
            int j1 = 0;
            boolean flag = false;
            switch (l) {
                case 1:
                    label50:
                    for (i1 = 0; i1 >= -1; i1--) {
                        for (j1 = 0; j1 >= -1; j1--) {
                            if (this.isSameTypeSapling(world, x + i1, y, z + j1, l)
                                && this.isSameTypeSapling(world, x + i1 + 1, y, z + j1, l)
                                && this.isSameTypeSapling(world, x + i1, y, z + j1 + 1, l)
                                && this.isSameTypeSapling(world, x + i1 + 1, y, z + j1 + 1, l)) {
                                object = new WorldGenMegaPineTree(false, random.nextBoolean());
                                flag = true;
                                break label50;
                            }
                        }
                    }

                    if (!flag) {
                        j1 = 0;
                        i1 = 0;
                        object = new WorldGenTaiga2(true);
                    }
                case 0:
                default:
                    Block block = Blocks.field_150350_a;
                    if (flag) {
                        world.func_147465_d(x + i1, y, z + j1, block, 0, 4);
                        world.func_147465_d(x + i1 + 1, y, z + j1, block, 0, 4);
                        world.func_147465_d(x + i1, y, z + j1 + 1, block, 0, 4);
                        world.func_147465_d(x + i1 + 1, y, z + j1 + 1, block, 0, 4);
                    } else {
                        world.func_147465_d(x, y, z, block, 0, 4);
                    }

                    if (!((WorldGenerator)object).func_76484_a(world, random, x + i1, y, z + j1)) {
                        if (flag) {
                            world.func_147465_d(x + i1, y, z + j1, this, l, 4);
                            world.func_147465_d(x + i1 + 1, y, z + j1, this, l, 4);
                            world.func_147465_d(x + i1, y, z + j1 + 1, this, l, 4);
                            world.func_147465_d(x + i1 + 1, y, z + j1 + 1, this, l, 4);
                        } else {
                            world.func_147465_d(x, y, z, this, l, 4);
                        }
                    }
            }
        }
    }

    public boolean isSameTypeSapling(World world, int x, int y, int z, int meta) {
        return world.func_147439_a(x, y, z) == this && (world.func_72805_g(x, y, z) & 7) == meta;
    }

    public int func_149692_a(int meta) {
        return MathHelper.func_76125_a(meta & 7, 0, 5);
    }

    @SideOnly(Side.CLIENT)
    public void func_149666_a(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
    }

    @SideOnly(Side.CLIENT)
    public void func_149651_a(IIconRegister p_149651_1_) {
        this.saplingIcon = p_149651_1_.func_94245_a(this.func_149641_N());
    }

    public boolean func_149851_a(World p_149851_1_, int p_149851_2_, int p_149851_3_, int p_149851_4_, boolean p_149851_5_) {
        return true;
    }

    public boolean func_149852_a(World p_149852_1_, Random p_149852_2_, int p_149852_3_, int p_149852_4_, int p_149852_5_) {
        return (double)p_149852_1_.field_73012_v.nextFloat() < 0.45;
    }

    public void func_149853_b(World p_149853_1_, Random p_149853_2_, int p_149853_3_, int p_149853_4_, int p_149853_5_) {
        this.growUp(p_149853_1_, p_149853_3_, p_149853_4_, p_149853_5_, p_149853_2_);
    }

    public int getTier(ItemStack itemstack) {
        return 7;
    }

    public int getTier(IBlockAccess world, int x, int y, int z) {
        return 7;
    }
}
