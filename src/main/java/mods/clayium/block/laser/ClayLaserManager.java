package mods.clayium.block.laser;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.block.CBlocks;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class ClayLaserManager {
    public ClayLaser clayLaser;
    public static int historyLength = 10;
    public static int laserLengthMax = 32;
    public long totalIrradiatedEnergy = 0L;
    protected long[] laserEnergyHistory = new long[historyLength];
    protected int targetBlockId;
    protected int targetBlockMeta;
    protected int laserLength;
    protected World world;
    protected int x;
    protected int y;
    protected int z;
    protected UtilDirection direction;
    protected int xx;
    protected int yy;
    protected int zz;
    protected boolean hasTarget;
    protected boolean isIrradiating;
    protected boolean initialized = false;

    public ClayLaserManager() {
        super();
        this.initialized = false;
    }

    public ClayLaserManager(World world, int x, int y, int z, UtilDirection direction) {
        super();
        this.reset(world, x, y, z, direction);
    }

    public void reset(World world, int x, int y, int z, UtilDirection direction) {
        this.laserEnergyHistory = new long[historyLength];
        this.totalIrradiatedEnergy = 0L;
        this.targetBlockId = 0;
        this.targetBlockMeta = 0;
        this.laserLength = 0;
        this.hasTarget = false;
        this.isIrradiating = false;
        this.set(world, x, y, z, direction);
    }

    public void set(World world, int x, int y, int z, UtilDirection direction) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.direction = direction;
        this.initialized = true;
    }

    public void update(boolean irradiation) {
        if (irradiation) {
            if (this.initialized) {
                this.isIrradiating = false;
                if (this.clayLaser != null) {
                    int[] numbers = this.clayLaser.numbers;
                    if (numbers != null) {
                        boolean flag = false;

                        for (int n : numbers) {
                            if (n >= 1) {
                                flag = true;
                            }
                        }

                        if (flag) {
                            this.irradiateLaser();
                            this.isIrradiating = true;
                        }
                    }
                }
            }
        } else {
            this.isIrradiating = false;
        }
    }

    public void irradiateLaser() {
        if (this.world != null) {
            int x = this.x;
            int dx = this.direction.offsetX;
            int y = this.y;
            int dy = this.direction.offsetY;
            int z = this.z;
            int dz = this.direction.offsetZ;
            int newTargetBlockId = 0;
            int newTargetBlockMeta = 0;
            int newLaserLength = laserLengthMax;
            this.hasTarget = false;

            for (int i = 1; i < laserLengthMax; i++) {
                int xx = x + i * dx;
                int yy = y + i * dy;
                int zz = z + i * dz;
                if (!this.canGoThrough(xx, yy, zz)) {
                    Block block = this.world.func_147439_a(xx, yy, zz);
                    newTargetBlockId = Block.func_149682_b(block);
                    newTargetBlockMeta = this.world.func_72805_g(xx, yy, zz);
                    newLaserLength = i;
                    this.hasTarget = true;
                    this.xx = xx;
                    this.yy = yy;
                    this.zz = zz;
                    break;
                }
            }

            if (!this.hasTarget || newTargetBlockId != this.targetBlockId || newTargetBlockMeta != this.targetBlockMeta || newLaserLength != this.laserLength) {
                this.laserEnergyHistory = new long[historyLength];
                this.totalIrradiatedEnergy = 0L;
            }

            this.targetBlockId = newTargetBlockId;
            this.targetBlockMeta = newTargetBlockMeta;
            this.laserLength = newLaserLength;
            if (this.hasTarget) {
                long n = (long)this.clayLaser.getEnergy();
                this.totalIrradiatedEnergy += n;
                long m = n;

                for (long h : this.laserEnergyHistory) {
                    m = Math.min(m, h);
                }

                this.totalIrradiatedEnergy = (long)((double)m * (double)this.totalIrradiatedEnergy / (double)n);

                for (int ix = 0; ix < this.laserEnergyHistory.length; ix++) {
                    if (ix < this.laserEnergyHistory.length - 1) {
                        this.laserEnergyHistory[ix] = this.laserEnergyHistory[ix + 1];
                    } else {
                        this.laserEnergyHistory[ix] = n;
                    }
                }

                Block block = this.world.func_147439_a(this.xx, this.yy, this.zz);
                int meta = this.world.func_72805_g(this.xx, this.yy, this.zz);
                TileEntity tile = UtilBuilder.safeGetTileEntity(this.world, this.xx, this.yy, this.zz);
                if (block.func_149688_o() == Material.field_151576_e
                    && block != Blocks.field_150357_h
                    && this.totalIrradiatedEnergy >= (long)(block.func_149712_f(this.world, this.xx, this.yy, this.zz) + 1.0F) * 100L) {
                    ItemStack[] items = this.harvestBlock(this.world, this.xx, this.yy, this.zz);
                    if (block == Blocks.field_150365_q) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150402_ci, 0, 3);
                    } else if (block == Blocks.field_150366_p) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150339_S, 0, 3);
                    } else if (block == Blocks.field_150352_o) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150340_R, 0, 3);
                    } else if (block == Blocks.field_150482_ag) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150484_ah, 0, 3);
                    } else if (block == Blocks.field_150450_ax) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150451_bX, 0, 3);
                    } else if (block == Blocks.field_150369_x) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150368_y, 0, 3);
                    } else if (block == Blocks.field_150412_bA) {
                        this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150475_bE, 0, 3);
                    } else {
                        this.dropItems(this.world, this.xx, this.yy, this.zz, items);
                    }
                }

                if (block == Blocks.field_150345_g && this.totalIrradiatedEnergy >= 300L && this.totalIrradiatedEnergy < 1000L) {
                    this.harvestBlock(this.world, this.xx, this.yy, this.zz);
                    this.world.func_147465_d(this.xx, this.yy, this.zz, Blocks.field_150345_g, meta >= 5 ? 0 : meta + 1, 3);
                }

                if (block == Blocks.field_150345_g && this.totalIrradiatedEnergy >= 300000L) {
                    this.harvestBlock(this.world, this.xx, this.yy, this.zz);
                    this.world.func_147465_d(this.xx, this.yy, this.zz, CBlocks.blockClayTreeSapling, 0, 3);
                }

                if (tile != null && tile instanceof IClayLaserMachine) {
                    ((IClayLaserMachine)tile).irradiateClayLaser(this.clayLaser, this.direction.getOpposite());
                }
            }
        }
    }

    protected boolean canGoThrough(int x, int y, int z) {
        if (this.world == null) {
            return false;
        } else {
            Block block = this.world.func_147439_a(x, y, z);
            return block == null ? false : block.func_149688_o() == Material.field_151579_a || block.func_149688_o() == Material.field_151592_s;
        }
    }

    public UtilDirection getDirection() {
        return this.direction;
    }

    public int getLaserLength() {
        return this.laserLength;
    }

    public int[] getTargetCoord() {
        return this.hasTarget ? new int[]{this.xx, this.yy, this.zz} : null;
    }

    public boolean hasTarget() {
        return this.hasTarget;
    }

    public boolean isIrradiating() {
        return this.isIrradiating;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        this.totalIrradiatedEnergy = tagCompound.func_74763_f("TotalIrradiatedEnergy");
        NBTTagList tagList = tagCompound.func_150295_c("LaserEnergyHistory", 10);
        this.laserEnergyHistory = new long[historyLength];

        for (int i = 0; i < tagList.func_74745_c() && i < this.laserEnergyHistory.length; i++) {
            NBTTagCompound tagCompound1 = tagList.func_150305_b(i);
            this.laserEnergyHistory[i] = tagCompound1.func_74763_f("Energy");
        }

        this.targetBlockId = tagCompound.func_74762_e("TargetBlockId");
        this.targetBlockMeta = tagCompound.func_74762_e("TargetBlockMeta");
        this.laserLength = tagCompound.func_74762_e("LaserLength");
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.func_74772_a("TotalIrradiatedEnergy", this.totalIrradiatedEnergy);
        NBTTagList tagList = new NBTTagList();

        for (long h : this.laserEnergyHistory) {
            NBTTagCompound tagCompound1 = new NBTTagCompound();
            tagCompound1.func_74772_a("Energy", h);
            tagList.func_74742_a(tagCompound1);
        }

        tagCompound.func_74782_a("LaserEnergyHistory", tagList);
        tagCompound.func_74768_a("TargetBlockId", this.targetBlockId);
        tagCompound.func_74768_a("TargetBlockMeta", this.targetBlockMeta);
        tagCompound.func_74768_a("LaserLength", this.laserLength);
    }

    public ItemStack[] harvestBlock(World theWorld, int xx, int yy, int zz) {
        List<ItemStack> itemToDrop = new ArrayList<>();
        Block block = theWorld.func_147439_a(xx, yy, zz);
        int l = theWorld.func_72805_g(xx, yy, zz);
        if (!theWorld.field_72995_K) {
            theWorld.func_72889_a(null, 2001, xx, yy, zz, Block.func_149682_b(block) + (theWorld.func_72805_g(xx, yy, zz) << 12));
        }

        theWorld.func_147468_f(xx, yy, zz);
        boolean dropXP = false;
        boolean canSilkHarvest = false;
        int fortune = 0;
        if (canSilkHarvest) {
            int j = 0;
            Item item = Item.func_150898_a(block);
            if (item != null && item.func_77614_k()) {
                j = l;
            }

            ItemStack itemstack = new ItemStack(item, 1, j);
            itemToDrop.add(itemstack);
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

        if (dropXP) {
            block.func_149657_c(theWorld, xx, yy, zz, block.getExpDrop(theWorld, l, fortune));
        }

        return itemToDrop.toArray(new ItemStack[0]);
    }

    public void dropItems(World theWorld, int xx, int yy, int zz, ItemStack[] itemToDrop) {
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
}
