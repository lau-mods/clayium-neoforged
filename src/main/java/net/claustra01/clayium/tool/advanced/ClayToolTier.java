package net.claustra01.clayium.tool.advanced;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum ClayToolTier implements Tier {
    CLAY_PICKAXE(500, 4.0F, 1.0F, BlockTags.INCORRECT_FOR_STONE_TOOL),
    CLAY_SHOVEL(500, 2.0F, 0.0F, BlockTags.INCORRECT_FOR_WOODEN_TOOL),
    CLAY_STEEL(10_000, 8.0F, 3.0F, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);

    private final int uses;
    private final float speed;
    private final float attackDamage;
    private final TagKey<Block> incorrectBlocks;

    ClayToolTier(int uses, float speed, float attackDamage, TagKey<Block> incorrectBlocks) {
        this.uses = uses;
        this.speed = speed;
        this.attackDamage = attackDamage;
        this.incorrectBlocks = incorrectBlocks;
    }

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamage;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocks;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.EMPTY;
    }
}
