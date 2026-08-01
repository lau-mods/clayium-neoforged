/*
 * SPDX-License-Identifier: CC-BY-4.0
 *
 * This file is part of the Clayium NeoForge port.
 */
package net.claustra01.clayium.world.level.block;

import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.world.item.ClayPickaxeItem;
import net.claustra01.clayium.world.item.ClayShovelItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Common implementation for all Stone and Deepslate Clay Ore blocks.
 *
 * <p>The original mod used one metadata block.  The current port exposes
 * stable block IDs, so the metadata-dependent drop behavior is represented by
 * {@code dropsClayBalls}: normal Clay Ore produces clay balls, while Dense and
 * Large Dense Clay Ore drop themselves.</p>
 */
public final class ClayOreBlock extends DropExperienceBlock {
    private final boolean dropsClayBalls;

    public ClayOreBlock(boolean dropsClayBalls, IntProvider experience, BlockBehaviour.Properties properties) {
        super(experience, properties);
        this.dropsClayBalls = dropsClayBalls;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack tool = params.getOptionalParameter(LootContextParams.TOOL);
        if (tool == null
                || !dropsClayBalls
                || (!(tool.getItem() instanceof ClayShovelItem)
                        && !(tool.getItem() instanceof ClayPickaxeItem))) {
            // Ordinary drops, Silk Touch, Fortune, and explosion decay are
            // intentionally data-pack controlled by the block loot table.
            return super.getDrops(state, params);
        }

        LootContext context = new LootContext.Builder(
                        params.withParameter(LootContextParams.BLOCK_STATE, state)
                                .create(LootContextParamSets.BLOCK))
                .create(Optional.empty());

        // Let the datapack loot table handle explosion decay.  Player and
        // direct block-drop paths use the legacy Clay Ore random sequence.
        if (context.hasParam(LootContextParams.EXPLOSION_RADIUS)) {
            return super.getDrops(state, params);
        }

        if (hasEnchantment(context, tool, Enchantments.SILK_TOUCH)) {
            return List.of(new ItemStack(this));
        }

        int fortune = enchantmentLevel(context, tool, Enchantments.FORTUNE);
        int effectiveFortune = tool.getItem() instanceof ClayShovelItem
                ? (fortune + 1) * 3
                : (fortune + 1) * 4;
        if (!(tool.getItem() instanceof ClayShovelItem) && !(tool.getItem() instanceof ClayPickaxeItem)) {
            effectiveFortune = fortune;
        }

        RandomSource random = context.getRandom();
        int base = 4 + random.nextInt(5) * random.nextInt(4);
        int bonus = 1;
        if (effectiveFortune > 0) {
            int i = random.nextInt(effectiveFortune + 2) - 1;
            bonus = Math.max(i, 0) + 1;
        }
        return List.of(new ItemStack(Items.CLAY_BALL, base * bonus));
    }

    private static boolean hasEnchantment(
            LootContext context, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        return enchantmentLevel(context, stack, enchantment) > 0;
    }

    private static int enchantmentLevel(
            LootContext context, ItemStack stack, ResourceKey<Enchantment> enchantment) {
        Holder<Enchantment> holder = context.getResolver()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(enchantment);
        return stack.getEnchantmentLevel(holder);
    }
}
