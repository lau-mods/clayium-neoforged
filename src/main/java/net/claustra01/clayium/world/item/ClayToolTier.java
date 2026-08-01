/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Tool materials used by the original Clayium shovel and pickaxe.
 *
 * <p>The legacy tools used the vanilla wood/stone material values, but
 * explicitly changed their durability to 500.  Delegating every value except
 * durability keeps their mining, enchantment, and repair behavior aligned with
 * the corresponding vanilla material.</p>
 */
public final class ClayToolTier implements Tier {
    public static final Tier SHOVEL = new ClayToolTier(Tiers.WOOD);
    public static final Tier PICKAXE = new ClayToolTier(Tiers.STONE);

    private static final int USES = 500;
    private final Tier delegate;

    private ClayToolTier(Tier delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getUses() {
        return USES;
    }

    @Override
    public float getSpeed() {
        return delegate.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return delegate.getAttackDamageBonus();
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return delegate.getIncorrectBlocksForDrops();
    }

    @Override
    public int getEnchantmentValue() {
        return delegate.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return delegate.getRepairIngredient();
    }
}
