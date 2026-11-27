package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.block.CompressedClayBlock;
import net.claustra01.clayium.block.ITieredBlock;
import net.claustra01.clayium.energy.IClayEnergy;
import net.claustra01.clayium.util.ClayiumFormatting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * BlockItem for Clayium blocks displaying Tier and optional Compressed Clay CE value.
 */
public class TieredBlockItem extends BlockItem implements ITieredItem, IClayEnergy {
    public TieredBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getTier(ItemStack stack) {
        return getBlock() instanceof ITieredBlock tiered ? tiered.getTier(stack) : -1;
    }

    @Override
    public long getClayEnergy(ItemStack stack) {
        return getBlock() instanceof CompressedClayBlock compressed ? compressed.getClayEnergyValue() : 0L;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        int tier = getTier(stack);
        if (tier >= 0) {
            tooltip.add(Component.translatable("gui.Common.tier", tier).withStyle(ChatFormatting.GRAY));
        }
        long ce = getClayEnergy(stack);
        if (ce > 0L) {
            tooltip.add(Component.literal(ClayiumFormatting.formatClayEnergy(ce) + "CE").withStyle(ChatFormatting.YELLOW));
        }
    }
}
