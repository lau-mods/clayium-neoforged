package net.claustra01.clayium.datagen;

import java.util.Set;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.block.ClayTreeLeavesBlock;
import net.claustra01.clayium.loot.ClayOreBonusLootFunction;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ClayiumLootTableProvider extends BlockLootSubProvider {
    private static final float[] NORMAL_APPLE_OR_DENSE_CLAY_CHANCES = {
            0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F
    };

    public ClayiumLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        var fortune = this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        Item clayDust = ClayiumItems.byPath("clay_dust").get();
        Item denseClayDust = ClayiumItems.byPath("dense_clay_dust").get();

        for (var entry : ClayiumBlocks.all().entrySet()) {
            Block block = entry.getValue().get();
            if (block instanceof ClayOreBlock ore && ore.getVariantIndex() == 0) {
                add(ore, b -> createSilkTouchDispatchTable(
                        b,
                        applyExplosionDecay(
                                b,
                                LootItem.lootTableItem(Items.CLAY_BALL)
                                        .apply(ClayOreBonusLootFunction.builder())
                        )
                ));
            } else if (block instanceof ClayTreeLeavesBlock leaves) {
                add(leaves, b -> createSilkTouchOrShearsDispatchTable(
                        b,
                        applyExplosionDecay(
                                b,
                                LootItem.lootTableItem(clayDust)
                                        .when(BonusLevelTableCondition.bonusLevelFlatChance(fortune, NORMAL_LEAVES_SAPLING_CHANCES))
                        )
                ).withPool(
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(HAS_SHEARS.or(this.hasSilkTouch()).invert())
                                .add(applyExplosionDecay(
                                        b,
                                        LootItem.lootTableItem(denseClayDust)
                                                .when(BonusLevelTableCondition.bonusLevelFlatChance(fortune, NORMAL_APPLE_OR_DENSE_CLAY_CHANCES))
                                ))
                ));
            } else {
                dropSelf(block);
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ClayiumBlocks.REGISTRY.getEntries().stream()
                .<Block>map(DeferredHolder::value)
                .toList();
    }
}
