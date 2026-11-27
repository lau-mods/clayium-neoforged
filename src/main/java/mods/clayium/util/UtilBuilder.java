package mods.clayium.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import mods.clayium.block.ClayContainer;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.ItemCapsule;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

public class UtilBuilder {
    private static Method createStackedBlock = null;

    public UtilBuilder() {
        super();
    }

    public static ItemStack[] harvestBlock(World theWorld, int xx, int yy, int zz, boolean dropXP, boolean canSilkHarvest, int fortune, boolean collectFluid) {
        Block block = theWorld.func_147439_a(xx, yy, zz);
        int l = theWorld.func_72805_g(xx, yy, zz);
        ItemStack[] itemToDrop = getDropsFromBlock(theWorld, xx, yy, zz, canSilkHarvest, fortune, collectFluid);
        destroyBlock(theWorld, xx, yy, zz, dropXP, canSilkHarvest, fortune);
        return itemToDrop;
    }

    public static ItemStack[] harvestBlock(World theWorld, int xx, int yy, int zz, boolean dropXP, boolean canSilkHarvest, int fortune) {
        return harvestBlock(theWorld, xx, yy, zz, dropXP, canSilkHarvest, fortune, true);
    }

    public static void destroyBlock(World theWorld, int xx, int yy, int zz, boolean dropXP, boolean canSilkHarvest, int fortune) {
        Block block = theWorld.func_147439_a(xx, yy, zz);
        int l = theWorld.func_72805_g(xx, yy, zz);
        if (!theWorld.field_72995_K) {
            theWorld.func_72889_a(null, 2001, xx, yy, zz, Block.func_149682_b(block) + (theWorld.func_72805_g(xx, yy, zz) << 12));
        }

        theWorld.func_147468_f(xx, yy, zz);
        if (dropXP) {
            block.func_149657_c(theWorld, xx, yy, zz, block.getExpDrop(theWorld, l, fortune));
        }
    }

    public static ItemStack getFluidCapsule(World theWorld, int xx, int yy, int zz) {
        return ItemCapsule.getItemCapsuleFromFluidStack(getFluidStack(theWorld, xx, yy, zz));
    }

    public static FluidStack getFluidStack(World theWorld, int xx, int yy, int zz) {
        Block block = theWorld.func_147439_a(xx, yy, zz);
        if (block != Blocks.field_150355_j && block != Blocks.field_150358_i) {
            if (block != Blocks.field_150353_l && block != Blocks.field_150356_k) {
                Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
                if (fluid == null) {
                    return null;
                } else if (block instanceof IFluidBlock) {
                    return !((IFluidBlock)block).canDrain(theWorld, xx, yy, zz) ? null : ((IFluidBlock)block).drain(theWorld, xx, yy, zz, false);
                } else {
                    return theWorld.func_72805_g(xx, yy, zz) == 0 ? new FluidStack(fluid, 1000) : null;
                }
            } else {
                return theWorld.func_72805_g(xx, yy, zz) == 0 ? new FluidStack(FluidRegistry.LAVA, 1000) : null;
            }
        } else {
            return theWorld.func_72805_g(xx, yy, zz) == 0 ? new FluidStack(FluidRegistry.WATER, 1000) : null;
        }
    }

    public static ItemStack createStackedBlock(Block block, int metadata) {
        if (createStackedBlock == null) {
            Class classBlock = null;

            try {
                classBlock = Class.forName("net.minecraft.block.Block");
            } catch (ClassNotFoundException var14) {
                try {
                    classBlock = Class.forName("net.minecraft.aji");
                } catch (ClassNotFoundException var13) {
                    ClayiumCore.logger.catching(var13);
                }
            }

            if (classBlock != null) {
                try {
                    createStackedBlock = classBlock.getDeclaredMethod("createStackedBlock", int.class);
                } catch (NoSuchMethodException var11) {
                    try {
                        createStackedBlock = classBlock.getDeclaredMethod("func_149644_j", int.class);
                    } catch (NoSuchMethodException var9) {
                        try {
                            createStackedBlock = classBlock.getDeclaredMethod("j", int.class);
                        } catch (NoSuchMethodException var7) {
                            ClayiumCore.logger.catching(var7);
                        } catch (SecurityException var8) {
                            ClayiumCore.logger.catching(var8);
                        }
                    } catch (SecurityException var10) {
                        ClayiumCore.logger.catching(var10);
                    }
                } catch (SecurityException var12) {
                    ClayiumCore.logger.catching(var12);
                }
            }

            if (createStackedBlock != null) {
                createStackedBlock.setAccessible(true);
            }
        }

        ItemStack itemstack = null;
        if (createStackedBlock != null) {
            Object obj = null;

            try {
                obj = createStackedBlock.invoke(block, metadata);
            } catch (Exception var6) {
                ClayiumCore.logger.catching(var6);
            }

            if (obj instanceof ItemStack) {
                itemstack = (ItemStack)obj;
            }
        }

        return itemstack;
    }

    public static ItemStack[] getDropsFromBlock(World theWorld, int xx, int yy, int zz, boolean canSilkHarvest, int fortune, boolean collectFluid) {
        Block block = theWorld.func_147439_a(xx, yy, zz);
        if (block == Blocks.field_150350_a) {
            return new ItemStack[0];
        } else {
            int l = theWorld.func_72805_g(xx, yy, zz);
            List<ItemStack> itemToDrop = new ArrayList<>();
            if (collectFluid) {
                ItemStack fluidCapsule = getFluidCapsule(theWorld, xx, yy, zz);
                if (fluidCapsule != null) {
                    itemToDrop.add(fluidCapsule);
                    return itemToDrop.toArray(new ItemStack[0]);
                }
            }

            if (canSilkHarvest && block.canSilkHarvest(theWorld, UtilPlayer.getFakePlayer(null), xx, yy, zz, l)) {
                ItemStack itemstack = createStackedBlock(block, l);
                if (itemstack != null) {
                    itemToDrop.add(itemstack);
                }
            } else {
                float g = 1.0F;
                if (!theWorld.field_72995_K && !theWorld.restoringBlockSnapshots) {
                    ArrayList<ItemStack> items = block.getDrops(theWorld, xx, yy, zz, l, fortune);
                    g = ForgeEventFactory.fireBlockHarvesting(items, theWorld, block, xx, yy, zz, l, fortune, g, false, null);

                    for (ItemStack item : items) {
                        if (theWorld.field_73012_v.nextFloat() <= g) {
                            itemToDrop.add(item);
                        }
                    }
                }
            }

            return itemToDrop.toArray(new ItemStack[0]);
        }
    }

    public static ItemStack[] getDropsFromBlock(World theWorld, int xx, int yy, int zz, boolean canSilkHarvest, int fortune) {
        return getDropsFromBlock(theWorld, xx, yy, zz, canSilkHarvest, fortune, true);
    }

    public static ItemStack getItemBlock(World theWorld, int xx, int yy, int zz) {
        ItemStack capsule = getFluidCapsule(theWorld, xx, yy, zz);
        if (capsule != null) {
            return capsule;
        } else {
            ItemStack silkitem = getRawItemBlock(theWorld, xx, yy, zz);
            ArrayList<ItemStack> dropitems = theWorld.func_147439_a(xx, yy, zz).getDrops(theWorld, xx, yy, zz, theWorld.func_72805_g(xx, yy, zz), 0);

            for (ItemStack dropitem : dropitems) {
                if (UtilItemStack.areItemDamageEqual(silkitem, dropitem)) {
                    ItemStack res = dropitem.func_77946_l();
                    res.field_77994_a = 1;
                    return res;
                }
            }

            for (ItemStack dropitemx : dropitems) {
                if (UtilItemStack.areItemEqual(silkitem, dropitemx)) {
                    ItemStack res = dropitemx.func_77946_l();
                    res.field_77994_a = 1;
                    return res;
                }
            }

            return silkitem;
        }
    }

    public static ItemStack getRawItemBlock(World theWorld, int xx, int yy, int zz) {
        Block block = theWorld.func_147439_a(xx, yy, zz);
        int j = 0;
        Item item = Item.func_150898_a(block);
        if (item != null && item.func_77614_k()) {
            j = block.func_149643_k(theWorld, xx, yy, zz);
        }

        return new ItemStack(item, 1, j);
    }

    public static void dropItems(World theWorld, int xx, int yy, int zz, ItemStack[] itemToDrop) {
        for (ItemStack item : itemToDrop) {
            if (!theWorld.field_72995_K && theWorld.func_82736_K().func_82766_b("doTileDrops") && !theWorld.restoringBlockSnapshots) {
                float f = 0.7F;
                double d0 = (double)(theWorld.field_73012_v.nextFloat() * f) + (double)(1.0F - f) * 0.5;
                double d1 = (double)(theWorld.field_73012_v.nextFloat() * f) + (double)(1.0F - f) * 0.5;
                double d2 = (double)(theWorld.field_73012_v.nextFloat() * f) + (double)(1.0F - f) * 0.5;
                EntityItem entityitem = new EntityItem(theWorld, (double)xx + d0, (double)yy + d1, (double)zz + d2, item);
                entityitem.field_145804_b = 10;
                theWorld.func_72838_d(entityitem);
            }
        }
    }

    public static void harvestBlockAndDropItems(
        World theWorld, int xx, int yy, int zz, boolean dropXP, boolean canSilkHarvest, int fortune, boolean collectFluid
    ) {
        dropItems(theWorld, xx, yy, zz, harvestBlock(theWorld, xx, yy, zz, dropXP, canSilkHarvest, fortune, collectFluid));
    }

    public static void harvestBlockAndDropItems(World theWorld, int xx, int yy, int zz, boolean dropXP, boolean canSilkHarvest, int fortune) {
        harvestBlockAndDropItems(theWorld, xx, yy, zz, dropXP, canSilkHarvest, fortune, true);
    }

    public static boolean placeBlockByRightClick(
        Block block,
        int meta,
        ItemStack itemstack,
        EntityPlayer player,
        World world,
        int x,
        int y,
        int z,
        int side,
        float hitX,
        float hitY,
        float hitZ,
        int num
    ) {
        int[] coord = coordTransformOnPlaceBlock(world, x, y, z, side);
        x = coord[0];
        y = coord[1];
        z = coord[2];
        side = coord[3];
        if (itemstack.field_77994_a == 0) {
            return false;
        } else if (!player.func_82247_a(x, y, z, side, itemstack)) {
            return false;
        } else if (y == 255 && block.func_149688_o().func_76220_a()) {
            return false;
        } else if (world.func_147472_a(block, x, y, z, false, side, player, itemstack)) {
            int j1 = block.func_149660_a(world, x, y, z, side, hitX, hitY, hitZ, meta);
            if (placeBlockByPlayer(block, itemstack, player, world, x, y, z, side, hitX, hitY, hitZ, j1)) {
                postBlockPlace(block, world, itemstack, x, y, z, num);
            }

            return true;
        } else {
            return false;
        }
    }

    public static int[] coordTransformOnPlaceBlock(World world, int x, int y, int z, int side) {
        Block b = world.func_147439_a(x, y, z);
        if (b == Blocks.field_150431_aC && (world.func_72805_g(x, y, z) & 7) < 1) {
            side = 1;
        } else if (b != Blocks.field_150395_bd && b != Blocks.field_150329_H && b != Blocks.field_150330_I && !b.isReplaceable(world, x, y, z)) {
            if (side == 0) {
                y--;
            }

            if (side == 1) {
                y++;
            }

            if (side == 2) {
                z--;
            }

            if (side == 3) {
                z++;
            }

            if (side == 4) {
                x--;
            }

            if (side == 5) {
                x++;
            }
        }

        return new int[]{x, y, z, side};
    }

    public static boolean placeBlockByPlayer(
        Block block, ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata
    ) {
        if (!world.func_147465_d(x, y, z, block, metadata, 3)) {
            return false;
        } else {
            if (world.func_147439_a(x, y, z) == block) {
                block.func_149689_a(world, x, y, z, player, stack);
                block.func_149714_e(world, x, y, z, metadata);
            }

            return true;
        }
    }

    public static boolean placeBlockByItemBlock(ItemStack itemstack, World world, int x, int y, int z) {
        return placeBlockByItemBlock(itemstack, world, x, y, z, 1, 0.5F, 0.5F, 0.5F);
    }

    public static boolean placeBlockByItemBlock(ItemStack itemstack, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (itemstack != null && itemstack.func_77973_b() instanceof ItemBlock) {
            ItemBlock itemBlock = (ItemBlock)itemstack.func_77973_b();
            int[] coord = coordTransformOnPlaceBlock(world, x, y, z, side);
            x = coord[0];
            y = coord[1];
            z = coord[2];
            side = coord[3];
            if (itemstack.field_77994_a == 0) {
                return false;
            } else if (y == 255 && itemBlock.field_150939_a.func_149688_o().func_76220_a()) {
                return false;
            } else {
                int i1 = itemBlock.func_77647_b(itemstack.func_77960_j());
                int j1 = itemBlock.field_150939_a.func_149660_a(world, x, y, z, side, hitX, hitY, hitZ, i1);
                if (placeBlockAt(itemBlock.field_150939_a, itemstack, world, x, y, z, j1, true)) {
                    postBlockPlace(itemBlock.field_150939_a, world, itemstack, x, y, z, 1);
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean placeBlockAt(Block block, ItemStack stack, World world, int x, int y, int z, int metadata, boolean useFakePlayer) {
        if (!world.func_147465_d(x, y, z, block, metadata, 3)) {
            return false;
        } else {
            if (world.func_147439_a(x, y, z) == block) {
                if (useFakePlayer) {
                    block.func_149689_a(world, x, y, z, UtilPlayer.getFakePlayer(null, world, x, y, z), stack);
                }

                block.func_149714_e(world, x, y, z, metadata);
            }

            return true;
        }
    }

    public static boolean placeBlockAt(Block block, World world, int x, int y, int z, int metadata) {
        return placeBlockAt(block, null, world, x, y, z, metadata, false);
    }

    public static boolean rotateBlockByWrench(World world, int x, int y, int z, int side) {
        Block block = world.func_147439_a(x, y, z);
        if (block instanceof ClayContainer) {
            return false;
        } else {
            ForgeDirection direction = ForgeDirection.getOrientation(side);
            ForgeDirection[] axes = block.getValidRotations(world, x, y, z);
            if (axes != null && axes.length != 0) {
                ForgeDirection axis = axes[0];

                for (ForgeDirection axis1 : block.getValidRotations(world, x, y, z)) {
                    if (axis1 == direction) {
                        axis = axis1;
                    }
                }

                return block.rotateBlock(world, x, y, z, axis);
            } else {
                return false;
            }
        }
    }

    public static void postBlockPlace(Block block, World world, ItemStack item, int x, int y, int z, int num) {
        world.func_72908_a(
            (double)((float)x + 0.5F),
            (double)((float)y + 0.5F),
            (double)((float)z + 0.5F),
            block.field_149762_H.func_150496_b(),
            (block.field_149762_H.func_150497_c() + 1.0F) / 2.0F,
            block.field_149762_H.func_150494_d() * 0.8F
        );
        item.field_77994_a -= num;
    }

    public static TileEntity safeGetTileEntity(IBlockAccess world, int x, int y, int z) {
        return y >= 0 && y < 256 && x >= -30000000 && z >= -30000000 && x < 30000000 && z < 30000000 ? world.func_147438_o(x, y, z) : null;
    }
}
