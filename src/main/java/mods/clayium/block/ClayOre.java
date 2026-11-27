package mods.clayium.block;

import java.util.ArrayList;
import java.util.Random;
import mods.clayium.core.ClayiumCore;
import mods.clayium.item.CItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class ClayOre extends BlockDamaged {
    private Random random = new Random();

    public ClayOre() {
        super(Material.field_151576_e, 3);
        this.func_149647_a(ClayiumCore.creativeTabClayium);
        this.func_149711_c(3.0F);
        this.func_149752_b(5.0F);
        this.func_149672_a(Block.field_149769_e);
        this.setHarvestLevel("pickaxe", 1);
    }

    public Item func_149650_a(int meta, Random random, int fortune) {
        return meta == 0 ? Items.field_151119_aD : Item.func_150898_a(this);
    }

    public int func_149692_a(int damage) {
        return damage == 0 ? 0 : damage;
    }

    public int func_149745_a(Random random) {
        return ClayiumCore.multiplyProgressionRateI(4 + random.nextInt(5) * random.nextInt(4));
    }

    public int func_149679_a(int fortune, Random random) {
        if (fortune > 0 && Item.func_150898_a(this) != this.func_149650_a(0, random, fortune)) {
            int i = random.nextInt(fortune + 2) - 1;
            if (i < 0) {
                i = 0;
            }

            return this.func_149745_a(random) * (i + 1);
        } else {
            return this.func_149745_a(random);
        }
    }

    public int quantityDropped(int meta, int fortune, Random random) {
        return meta == 0 ? this.func_149679_a(fortune, random) : 1;
    }

    public int getExpDrop(IBlockAccess iBlockAccess, int meta, int fortune) {
        return meta == 0 ? MathHelper.func_76136_a(this.random, 0, 1) : 0;
    }

    public void func_149636_a(World p_149636_1_, EntityPlayer p_149636_2_, int p_149636_3_, int p_149636_4_, int p_149636_5_, int p_149636_6_) {
        p_149636_2_.func_71064_a(StatList.field_75934_C[func_149682_b(this)], 1);
        p_149636_2_.func_71020_j(0.025F);
        if (this.canSilkHarvest(p_149636_1_, p_149636_2_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_) && EnchantmentHelper.func_77502_d(p_149636_2_)) {
            ArrayList<ItemStack> items = new ArrayList<>();
            ItemStack itemstack = this.func_149644_j(p_149636_6_);
            if (itemstack != null) {
                items.add(itemstack);
            }

            ForgeEventFactory.fireBlockHarvesting(items, p_149636_1_, this, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_, 0, 1.0F, true, p_149636_2_);

            for (ItemStack is : items) {
                this.func_149642_a(p_149636_1_, p_149636_3_, p_149636_4_, p_149636_5_, is);
            }
        } else {
            this.harvesters.set(p_149636_2_);
            int i1 = EnchantmentHelper.func_77517_e(p_149636_2_);
            if (p_149636_2_.func_70694_bm() != null) {
                if (p_149636_2_.func_70694_bm().func_77973_b() == CItems.itemClayShovel) {
                    i1 = (i1 + 1) * 3;
                }

                if (p_149636_2_.func_70694_bm().func_77973_b() == CItems.itemClayPickaxe) {
                    i1 = (i1 + 1) * 4;
                }
            }

            this.func_149697_b(p_149636_1_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_, i1);
            this.harvesters.set(null);
        }
    }

    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return super.canHarvestBlock(player, meta)
            || player.func_70694_bm() != null
                && (player.func_70694_bm().func_77973_b() == CItems.itemClayShovel || player.func_70694_bm().func_77973_b() == CItems.itemClayPickaxe);
    }
}
