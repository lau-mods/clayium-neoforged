/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Original Clay Steel tool material: Diamond properties, 10000 durability, sixfold speed. */
public enum ClaySteelToolTier implements Tier {
    INSTANCE;

    @Override public int getUses() { return 10_000; }
    @Override public float getSpeed() { return Tiers.DIAMOND.getSpeed() * 6.0F; }
    @Override public float getAttackDamageBonus() { return Tiers.DIAMOND.getAttackDamageBonus(); }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return Tiers.DIAMOND.getIncorrectBlocksForDrops(); }
    @Override public int getEnchantmentValue() { return Tiers.DIAMOND.getEnchantmentValue(); }
    @Override public Ingredient getRepairIngredient() { return Ingredient.EMPTY; }
}
