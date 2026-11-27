package mods.clayium.item;

import java.util.ArrayList;
import java.util.List;
import mods.clayium.core.ClayiumCore;
import mods.clayium.util.UtilAdvancedTools;
import mods.clayium.util.UtilBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class HarvestCoordClaySteelTools implements IHarvestCoord {
    public int maxRange = 2;

    public HarvestCoordClaySteelTools(int maxRange) {
        super();
        this.maxRange = maxRange;
    }

    public List<Vec3> getHarvestedCoordList(ItemStack itemstack, int x, int y, int z, Vec3 xxVector, Vec3 yyVector, Vec3 zzVector) {
        List<Vec3> res = new ArrayList<>();
        NBTTagCompound tag = itemstack.func_77942_o() ? itemstack.func_77978_p() : new NBTTagCompound();
        int mode = tag.func_74764_b("Mode") ? tag.func_74762_e("Mode") : 0;
        if (mode == this.maxRange && tag.func_74764_b("Coords")) {
            NBTTagList coords = tag.func_150295_c("Coords", 10);

            for (int i = 0; i < coords.func_74745_c(); i++) {
                NBTTagCompound coord = coords.func_150305_b(i);
                int xx = coord.func_74762_e("xx");
                int yy = coord.func_74762_e("yy");
                int zz = coord.func_74762_e("zz");
                res.add(
                    Vec3.func_72443_a(
                        (double)x + xxVector.field_72450_a * (double)xx + yyVector.field_72450_a * (double)yy + zzVector.field_72450_a * (double)zz,
                        (double)y + xxVector.field_72448_b * (double)xx + yyVector.field_72448_b * (double)yy + zzVector.field_72448_b * (double)zz,
                        (double)z + xxVector.field_72449_c * (double)xx + yyVector.field_72449_c * (double)yy + zzVector.field_72449_c * (double)zz
                    )
                );
            }

            return res;
        } else {
            int size = mode < this.maxRange ? mode : this.maxRange;

            for (int yy = -size; yy <= size; yy++) {
                for (int xx = -size; xx <= size; xx++) {
                    for (int zz = 0; zz <= 0; zz++) {
                        res.add(
                            Vec3.func_72443_a(
                                (double)x + xxVector.field_72450_a * (double)xx + yyVector.field_72450_a * (double)yy + zzVector.field_72450_a * (double)zz,
                                (double)y + xxVector.field_72448_b * (double)xx + yyVector.field_72448_b * (double)yy + zzVector.field_72448_b * (double)zz,
                                (double)z + xxVector.field_72449_c * (double)xx + yyVector.field_72449_c * (double)yy + zzVector.field_72449_c * (double)zz
                            )
                        );
                    }
                }
            }

            return res;
        }
    }

    public boolean onItemUse(
        ItemStack p_77648_1_,
        EntityPlayer p_77648_2_,
        World p_77648_3_,
        int p_77648_4_,
        int p_77648_5_,
        int p_77648_6_,
        int p_77648_7_,
        float p_77648_8_,
        float p_77648_9_,
        float p_77648_10_
    ) {
        NBTTagCompound tag = p_77648_1_.func_77942_o() ? p_77648_1_.func_77978_p() : new NBTTagCompound();
        int mode = tag.func_74764_b("Mode") ? tag.func_74762_e("Mode") : 0;
        if (!p_77648_2_.func_70093_af()) {
            if (mode == this.maxRange && p_77648_3_.func_147439_a(p_77648_4_, p_77648_5_, p_77648_6_) == Blocks.field_150435_aG) {
                Vec3[] mat = UtilAdvancedTools.getEigenVectorsInSafe(p_77648_2_);
                if (mat != null) {
                    Vec3[] ev = UtilAdvancedTools.getInverse(mat);
                    NBTTagList coords = new NBTTagList();
                    int range = this.maxRange;

                    for (int x = -range; x <= range; x++) {
                        for (int y = -range; y <= range; y++) {
                            for (int z = -range; z <= range; z++) {
                                if (p_77648_3_.func_147439_a(p_77648_4_ + x, p_77648_5_ + y, p_77648_6_ + z) == Blocks.field_150435_aG) {
                                    NBTTagCompound coord = new NBTTagCompound();
                                    coord.func_74768_a(
                                        "xx", (int)(ev[0].field_72450_a * (double)x + ev[1].field_72450_a * (double)y + ev[2].field_72450_a * (double)z)
                                    );
                                    coord.func_74768_a(
                                        "yy", (int)(ev[0].field_72448_b * (double)x + ev[1].field_72448_b * (double)y + ev[2].field_72448_b * (double)z)
                                    );
                                    coord.func_74768_a(
                                        "zz", (int)(ev[0].field_72449_c * (double)x + ev[1].field_72449_c * (double)y + ev[2].field_72449_c * (double)z)
                                    );
                                    coords.func_74742_a(coord);
                                }
                            }
                        }
                    }

                    tag.func_74782_a("Coords", coords);
                    p_77648_1_.func_77982_d(tag);
                    if (!p_77648_3_.field_72995_K) {
                        p_77648_2_.func_145747_a(new ChatComponentText("Customized."));
                    }

                    return false;
                }
            }

            if (mode++ >= this.maxRange) {
                mode = 0;
            }

            tag.func_74768_a("Mode", mode);
            p_77648_1_.func_77982_d(tag);
            if (!p_77648_3_.field_72995_K) {
                p_77648_2_.func_145747_a(new ChatComponentText("Set mode " + mode + "."));
            }

            return false;
        } else {
            return mode == this.maxRange
                && !ClayiumCore.cfgUtilityMode
                && UtilBuilder.placeBlockByRightClick(
                    Blocks.field_150435_aG,
                    0,
                    p_77648_1_,
                    p_77648_2_,
                    p_77648_3_,
                    p_77648_4_,
                    p_77648_5_,
                    p_77648_6_,
                    p_77648_7_,
                    p_77648_8_,
                    p_77648_9_,
                    p_77648_10_,
                    0
                );
        }
    }
}
