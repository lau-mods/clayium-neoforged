package mods.clayium.block.tile;

import java.util.Random;
import mods.clayium.util.UtilDirection;
import mods.clayium.util.UtilTier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileWaterWheel extends TileClayContainerTiered {
    public int progress = 0;
    public int progressEfficiency = 1000;
    protected static int progressMax = 20000;
    private static Random random = new Random();

    public TileWaterWheel() {
        super();
    }

    public void initParams() {
        super.initParams();
        this.progressEfficiency = (int)((double)this.progressEfficiency * Math.pow(Math.max((double)this.baseTier, 1.0), 3.0));
        this.containerItemStacks = new ItemStack[1];
        this.listSlotsInsert.add(new int[]{0});
        this.listSlotsExtract.add(new int[]{0});
        this.insertRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.extractRoutes = new int[]{-1, -1, -1, -1, -1, -1};
        this.slotsDrop = new int[]{0};
        this.autoInsert = false;
        this.autoExtract = false;
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.progress = tagCompound.func_74762_e("Progress");
        this.progressEfficiency = tagCompound.func_74762_e("ProgressEfficiency");
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74768_a("Progress", this.progress);
        tagCompound.func_74768_a("ProgressEfficiency", this.progressEfficiency);
    }

    public void func_145845_h() {
        super.func_145845_h();
        if (!this.field_145850_b.field_72995_K && random.nextInt(40) < this.countSurroundingWater()) {
            this.progress = (int)((double)this.progress + (double)this.progressEfficiency * Math.pow(Math.max((double)this.baseTier, 1.0), 3.0));
            this.setSyncFlag();
            if (this.progress >= progressMax) {
                this.progress = this.progress - progressMax;
                this.progressEfficiency = this.progressEfficiency - (random.nextInt(5) == 0 ? 1 : 0);
                this.emitEnergy();
            }
        }
    }

    public int getProgressIcon() {
        return this.progress * 10 / progressMax / 2 == 0 ? 0 : 1;
    }

    public double getProgress() {
        return (double)this.progress / (double)progressMax;
    }

    public void func_70295_k_() {
    }

    public void func_70305_f() {
    }

    public void emitEnergy() {
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity te = UtilDirection.getTileEntity(this.field_145850_b, this.field_145851_c, this.field_145848_d, this.field_145849_e, direction);
            if (te != null
                && te instanceof TileClayMachines
                && UtilTier.acceptWaterWheel(((TileClayMachines)te).getTier())
                && (double)((TileClayMachines)te).clayEnergy < 5.0 * Math.pow(Math.max((double)this.baseTier, 1.0), 8.0)) {
                ((TileClayMachines)te).clayEnergy = (long)((double)((TileClayMachines)te).clayEnergy + Math.pow(Math.max((double)this.baseTier, 1.0), 8.0));
                ((TileClayMachines)te).setSyncFlag();
            }
        }
    }

    public int countSurroundingWater() {
        int meta = 0;
        int count = 0;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = this.field_145850_b.func_147439_a(this.field_145851_c + x, this.field_145848_d + y, this.field_145849_e + z);
                    meta = this.field_145850_b.func_72805_g(this.field_145851_c + x, this.field_145848_d + y, this.field_145849_e + z);
                    if (block.func_149688_o() == Material.field_151586_h && block instanceof BlockLiquid && meta != 0) {
                        count++;
                    }
                }
            }
        }

        return count;
    }
}
