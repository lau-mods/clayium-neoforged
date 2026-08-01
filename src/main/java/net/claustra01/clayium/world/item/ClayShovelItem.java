/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Clayium's shovel from 1.7.10.
 *
 * <p>It is a wooden shovel for ordinary blocks, is much faster on ground, and
 * has a dedicated speed for Clay Ore.  The ore block itself accepts this item
 * as a correct harvesting tool.</p>
 */
public final class ClayShovelItem extends ShovelItem {
    private static final float CLAY_BLOCK_SPEED = 32.0F;
    private static final float CLAY_ORE_SPEED = 12.0F;

    public ClayShovelItem(Item.Properties properties) {
        super(ClayToolTier.SHOVEL, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (ClayOreToolHelper.isClayOre(state)) {
            return CLAY_ORE_SPEED;
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return CLAY_BLOCK_SPEED;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return ClayOreToolHelper.isClayOre(state) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.clayium_neoforged.clay_shovel.tooltip"));
    }
}
