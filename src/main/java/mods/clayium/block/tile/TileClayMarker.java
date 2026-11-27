package mods.clayium.block.tile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.clayium.block.ClayMarker;
import mods.clayium.util.UtilBuilder;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileClayMarker extends TileGeneric implements IAxisAlignedBBProvider, IAxisAlignedBBContainer {
    protected AxisAlignedBB aabb;
    public int state = 0;
    public static int maxRange = 64;
    protected int xx = 0;
    protected int yy = 0;
    protected int zz = 0;

    public TileClayMarker() {
        super();
    }

    public void activate() {
        if (!this.field_145850_b.field_72995_K) {
            this.setInstantSyncFlag();
            int i = 1;
            this.xx = this.yy = this.zz = 0;

            for (int var2 = 1; var2 < maxRange; var2++) {
                if (this.isMarker(var2, 0, 0)) {
                    this.xx = var2;
                    break;
                }

                if (this.isMarker(-var2, 0, 0)) {
                    this.xx = -var2;
                    break;
                }
            }

            for (int var3 = 1; var3 < maxRange; var3++) {
                if (this.isMarker(0, var3, 0)) {
                    this.yy = var3;
                    break;
                }

                if (this.isMarker(0, -var3, 0)) {
                    this.yy = -var3;
                    break;
                }
            }

            for (int var4 = 1; var4 < maxRange; var4++) {
                if (this.isMarker(0, 0, var4)) {
                    this.zz = var4;
                    break;
                }

                if (this.isMarker(0, 0, -var4)) {
                    this.zz = -var4;
                    break;
                }
            }

            if (this.xx == 0 && this.yy == 0 && this.zz == 0 && this.state != 1) {
                this.aabb = null;
                this.state = this.state == 0 ? 1 : 0;
            } else {
                this.aabb = AxisAlignedBB.func_72330_a(
                    (double)Math.min(this.field_145851_c, this.field_145851_c + this.xx),
                    (double)Math.min(this.field_145848_d, this.field_145848_d + this.yy),
                    (double)Math.min(this.field_145849_e, this.field_145849_e + this.zz),
                    (double)(Math.max(this.field_145851_c, this.field_145851_c + this.xx) + 1),
                    (double)(Math.max(this.field_145848_d, this.field_145848_d + this.yy) + 1),
                    (double)(Math.max(this.field_145849_e, this.field_145849_e + this.zz) + 1)
                );
                this.state = this.state <= 1 ? 3 : (++this.state >= 5 ? 2 : this.state);
            }
        }
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.state = tagCompound.func_74771_c("State");
        readAxisAlignedBBFromNBT(tagCompound, this);
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74774_a("State", (byte)this.state);
        writeAxisAlignedBBToNBT(tagCompound, this);
    }

    protected boolean isMarker(int xx, int yy, int zz) {
        return this.field_145850_b.func_147439_a(this.field_145851_c + xx, this.field_145848_d + yy, this.field_145849_e + zz) instanceof ClayMarker;
    }

    public AxisAlignedBB getAxisAlignedBB() {
        return this.aabb;
    }

    public void setAxisAlignedBB(AxisAlignedBB aabb) {
        this.aabb = aabb;
    }

    public boolean hasAxisAlignedBB() {
        return this.aabb != null && this.state >= 2;
    }

    public int getBoxAppearance() {
        return this.state < 2 ? 0 : this.state - 2;
    }

    public void setAxisAlignedBBToMachine() {
        this.func_70296_d();
        this.breakMarker(this.xx, 0, 0);
        this.breakMarker(0, this.yy, 0);
        this.breakMarker(0, 0, this.zz);
        this.breakMarker(0, 0, 0);
    }

    protected void breakMarker(int xx, int yy, int zz) {
        Block block = this.field_145850_b.func_147439_a(this.field_145851_c + xx, this.field_145848_d + yy, this.field_145849_e + zz);
        if (block instanceof ClayMarker) {
            UtilBuilder.dropItems(
                this.field_145850_b,
                this.field_145851_c,
                this.field_145848_d,
                this.field_145849_e,
                UtilBuilder.harvestBlock(this.field_145850_b, this.field_145851_c + xx, this.field_145848_d + yy, this.field_145849_e + zz, false, false, 0)
            );
        }
    }

    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    public double func_145833_n() {
        return Double.POSITIVE_INFINITY;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
    }

    public static void writeAxisAlignedBBToNBT(NBTTagCompound tagCompound, IAxisAlignedBBContainer tile) {
        if (tile.hasAxisAlignedBB()) {
            tagCompound.func_74757_a("hasAABB", true);
            tagCompound.func_74780_a("AABBMinX", tile.getAxisAlignedBB().field_72340_a);
            tagCompound.func_74780_a("AABBMinY", tile.getAxisAlignedBB().field_72338_b);
            tagCompound.func_74780_a("AABBMinZ", tile.getAxisAlignedBB().field_72339_c);
            tagCompound.func_74780_a("AABBMaxX", tile.getAxisAlignedBB().field_72336_d);
            tagCompound.func_74780_a("AABBMaxY", tile.getAxisAlignedBB().field_72337_e);
            tagCompound.func_74780_a("AABBMaxZ", tile.getAxisAlignedBB().field_72334_f);
        } else {
            tagCompound.func_74757_a("hasAABB", false);
        }
    }

    public static void readAxisAlignedBBFromNBT(NBTTagCompound tagCompound, IAxisAlignedBBContainer tile) {
        boolean hasAABB = tagCompound.func_74767_n("hasAABB");
        if (hasAABB) {
            double minX = tagCompound.func_74769_h("AABBMinX");
            double minY = tagCompound.func_74769_h("AABBMinY");
            double minZ = tagCompound.func_74769_h("AABBMinZ");
            double maxX = tagCompound.func_74769_h("AABBMaxX");
            double maxY = tagCompound.func_74769_h("AABBMaxY");
            double maxZ = tagCompound.func_74769_h("AABBMaxZ");
            if (!tile.hasAxisAlignedBB()
                || tile.getAxisAlignedBB().field_72340_a != minX
                || tile.getAxisAlignedBB().field_72338_b != minY
                || tile.getAxisAlignedBB().field_72339_c != minZ
                || tile.getAxisAlignedBB().field_72336_d != maxX
                || tile.getAxisAlignedBB().field_72337_e != maxY
                || tile.getAxisAlignedBB().field_72334_f != maxZ) {
                tile.setAxisAlignedBB(AxisAlignedBB.func_72330_a(minX, minY, minZ, maxX, maxY, maxZ));
            }
        }
    }
}
