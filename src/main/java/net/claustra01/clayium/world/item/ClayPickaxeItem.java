/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.item;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;

/** Clayium's high-speed Clay Ore pickaxe from 1.7.10. */
public final class ClayPickaxeItem extends PickaxeItem {
    private static final float CLAY_ORE_SPEED = 32.0F;

    public ClayPickaxeItem(Item.Properties properties) {
        super(ClayToolTier.PICKAXE, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return ClayOreToolHelper.isClayOre(state) ? CLAY_ORE_SPEED : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return ClayOreToolHelper.isClayOre(state) || super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("item.clayium_neoforged.clay_pickaxe.tooltip"));
    }
}
