package mods.clayium.block.tile;

import mods.clayium.item.CMaterials;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;

public class TileQuartzCrucible extends TileGeneric {
    public int craftTime;
    public int ingotQuantity;
    public static int timeToCraft = 600;

    public TileQuartzCrucible() {
        super();
    }

    public void func_145839_a(NBTTagCompound tagCompound) {
        super.func_145839_a(tagCompound);
        this.craftTime = tagCompound.func_74765_d("CraftTime");
        this.ingotQuantity = tagCompound.func_74765_d("IngotQuantity");
    }

    public void func_145841_b(NBTTagCompound tagCompound) {
        super.func_145841_b(tagCompound);
        tagCompound.func_74777_a("CraftTime", (short)this.craftTime);
        tagCompound.func_74777_a("IngotQuantity", (short)this.ingotQuantity);
    }

    public void func_145845_h() {
        super.func_145845_h();
        if (this.ingotQuantity > 0) {
            this.craftTime++;
        }
    }

    public boolean putIngot() {
        if (!this.field_145850_b.field_72995_K && this.ingotQuantity < 9) {
            this.ingotQuantity++;
            this.setMetadata(this.ingotQuantity);
            return true;
        } else {
            return false;
        }
    }

    public boolean consumeString() {
        if (this.field_145850_b != null && !this.field_145850_b.field_72995_K && this.ingotQuantity > 0 && this.craftTime >= this.ingotQuantity * timeToCraft) {
            EntityItem entityitem = new EntityItem(
                this.field_145850_b,
                (double)((float)this.field_145851_c + 0.4F),
                (double)((float)this.field_145848_d + 0.4F),
                (double)((float)this.field_145849_e + 0.4F),
                CMaterials.get(CMaterials.SILICON, CMaterials.INGOT, this.ingotQuantity)
            );
            this.field_145850_b.func_72838_d(entityitem);
            this.ingotQuantity = 0;
            this.craftTime = 0;
            this.setMetadata(0);
            return true;
        } else {
            return false;
        }
    }

    public void setMetadata(int meta) {
        if (this.field_145850_b != null) {
            this.field_145850_b.func_72921_c(this.field_145851_c, this.field_145848_d, this.field_145849_e, meta, 3);
        }
    }
}
