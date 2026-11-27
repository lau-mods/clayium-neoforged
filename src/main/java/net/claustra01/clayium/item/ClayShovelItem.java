package net.claustra01.clayium.item;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.util.ClayiumTooltips;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clay Shovel (Wood tier, 500 durability, 32.0F speed on clay blocks, 12.0F speed and harvestable on Clay Ore).
 */
public class ClayShovelItem extends ShovelItem implements ITieredItem {
    public static final float EFFICIENCY_ON_CLAY_BLOCKS = 32.0F;
    public static final float EFFICIENCY_ON_CLAY_ORE = 12.0F;
    public static final TagKey<Block> CLAY_BLOCKS_TAG = BlockTags.create(ClayiumMod.id("clay_blocks"));
    private static final String ICON_NAME = "clayshovel";

    public ClayShovelItem() {
        super(ClayToolItem.tierWithDurability(Tiers.WOOD, 500), new Properties()
                .attributes(ShovelItem.createAttributes(Tiers.WOOD, 1.5F, -3.0F)));
    }

    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public int getTier(ItemStack stack) {
        return -1;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(Blocks.CLAY) || state.is(CLAY_BLOCKS_TAG)) {
            return EFFICIENCY_ON_CLAY_BLOCKS;
        }
        if (state.getBlock() instanceof ClayOreBlock) {
            return EFFICIENCY_ON_CLAY_ORE;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.getBlock() instanceof ClayOreBlock) {
            return true;
        }
        return super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ClayiumTooltips.appendLocalizedTooltip(tooltip, stack.getDescriptionId() + ".tooltip", flag);
    }
}
