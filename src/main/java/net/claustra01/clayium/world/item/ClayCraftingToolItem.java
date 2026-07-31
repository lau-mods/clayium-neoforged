/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.world.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Clay crafting tool with the original clay-ball remainder when it breaks. */
public final class ClayCraftingToolItem extends ClayConfiguratorItem {
    private final int brokenClayBalls;

    public ClayCraftingToolItem(Properties properties, Mode mode, int brokenClayBalls) {
        super(properties, mode);
        this.brokenClayBalls = brokenClayBalls;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return new ItemStack(Items.CLAY_BALL, brokenClayBalls);
        }
        ItemStack damaged = stack.copyWithCount(1);
        damaged.setDamageValue(stack.getDamageValue() + 1);
        return damaged;
    }
}
