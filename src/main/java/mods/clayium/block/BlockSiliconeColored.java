package mods.clayium.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemDye;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockSiliconeColored extends BlockDamaged {
    public BlockSiliconeColored() {
        super(Material.field_151573_f);
        this.func_149658_d("clayium:siliconecolored");
        this.func_149663_c("blockSilicone");
        this.func_149711_c(2.0F);
        this.func_149752_b(2.0F);
        this.func_149672_a(Block.field_149777_j);

        for (int i = 0; i < 16; i++) {
            this.addBlockList(ItemDye.field_150921_b[BlockColored.func_150031_c(i)], i);
            this.setTier(5);
            this.setSubBlockSurfix(ItemDye.field_150921_b[BlockColored.func_150031_c(i)]);
            this.setIconName("clayium:siliconecolored");
        }
    }

    @SideOnly(Side.CLIENT)
    public int func_149741_i(int p_149741_1_) {
        int r = this.func_149728_f(p_149741_1_).field_76291_p >> 16 & 0xFF;
        int g = this.func_149728_f(p_149741_1_).field_76291_p >> 8 & 0xFF;
        int b = this.func_149728_f(p_149741_1_).field_76291_p & 0xFF;
        r = (r * 3 + 256) / 4;
        g = (g * 3 + 256) / 4;
        b = (b * 3 + 256) / 4;
        return (r << 16) + (g << 8) + b;
    }

    @SideOnly(Side.CLIENT)
    public int func_149720_d(IBlockAccess world, int x, int y, int z) {
        return this.func_149741_i(world.func_72805_g(x, y, z));
    }

    public MapColor func_149728_f(int p_149728_1_) {
        return MapColor.func_151644_a(p_149728_1_);
    }

    public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
        int meta = world.func_72805_g(x, y, z);
        if (meta != colour) {
            world.func_72921_c(x, y, z, colour, 3);
            return true;
        } else {
            return false;
        }
    }
}
