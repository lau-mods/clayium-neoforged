package mods.clayium.block.tile;

import mods.clayium.block.ITieredBlock;
import mods.clayium.util.UtilBuilder;
import mods.clayium.util.UtilDirection;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

public abstract class TileMultiblockMachines extends TileClayMachines {
    public boolean constructed = false;
    private int constructionCounter = 0;
    protected int constructionTime = 20;

    public TileMultiblockMachines() {
        super();
    }

    public abstract boolean isConstructed();

    protected abstract void onConstruction();

    protected abstract void onDestruction();

    public boolean canProceedCraftWhenConstructed() {
        return super.canProceedCraft();
    }

    public void func_145845_h() {
        this.constructionCounter++;
        if (this.constructionCounter >= this.constructionTime) {
            this.verifyConstruction();
            this.constructionCounter = 0;
        }

        super.func_145845_h();
    }

    public int[] getRelativeCoord(int xx, int yy, int zz) {
        UtilDirection direction = UtilDirection.getOrientation(this.func_145832_p());
        UtilDirection xxVector = direction.getSide(UtilDirection.RIGHTSIDE);
        UtilDirection yyVector = UtilDirection.UP;
        UtilDirection zzVector = direction.getOpposite();
        return new int[]{
            this.field_145851_c + xxVector.offsetX * xx + yyVector.offsetX * yy + zzVector.offsetX * zz,
            this.field_145848_d + xxVector.offsetY * xx + yyVector.offsetY * yy + zzVector.offsetY * zz,
            this.field_145849_e + xxVector.offsetZ * xx + yyVector.offsetZ * yy + zzVector.offsetZ * zz
        };
    }

    protected TileEntity getTileEntity(int xx, int yy, int zz) {
        int[] coord = this.getRelativeCoord(xx, yy, zz);
        return this.field_145850_b == null ? null : UtilBuilder.safeGetTileEntity(this.field_145850_b, coord[0], coord[1], coord[2]);
    }

    protected Block getBlock(int xx, int yy, int zz) {
        int[] coord = this.getRelativeCoord(xx, yy, zz);
        return this.field_145850_b == null ? null : this.field_145850_b.func_147439_a(coord[0], coord[1], coord[2]);
    }

    protected int getBlockMetadata(int xx, int yy, int zz) {
        int[] coord = this.getRelativeCoord(xx, yy, zz);
        return this.field_145850_b == null ? 0 : this.field_145850_b.func_72805_g(coord[0], coord[1], coord[2]);
    }

    protected int getBlockTier(int xx, int yy, int zz) {
        int[] coord = this.getRelativeCoord(xx, yy, zz);
        Block block = this.getBlock(xx, yy, zz);
        return block != null && block instanceof ITieredBlock ? ((ITieredBlock)block).getTier(this.field_145850_b, coord[0], coord[1], coord[2]) : -1;
    }

    public void forceVerification() {
        this.verifyConstruction();
        this.constructionCounter = 0;
    }

    public boolean canProceedCraft() {
        return this.constructed && this.canProceedCraftWhenConstructed();
    }

    protected void verifyConstruction() {
        boolean newConstructed = this.isConstructed();
        if (newConstructed && !this.constructed) {
            this.onConstruction();
        }

        if (!newConstructed && this.constructed) {
            this.onDestruction();
        }

        this.constructed = newConstructed;
    }
}
