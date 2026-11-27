package net.claustra01.clayium.datagen;

import java.util.Set;
import net.claustra01.clayium.block.ClayOreBlock;
import net.claustra01.clayium.block.ClayTreeLeavesBlock;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ClayiumLootTableProvider extends BlockLootSubProvider {
    public ClayiumLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        var fortune = this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        Block sapling = ClayiumBlocks.byPath("clay_tree_sapling").get();

        for (var entry : ClayiumBlocks.all().entrySet()) {
            Block block = entry.getValue().get();
            if (block instanceof ClayOreBlock ore && ore.getVariantIndex() == 0) {
                add(ore, b -> createSilkTouchDispatchTable(
                        b,
                        applyExplosionDecay(
                                b,
                                LootItem.lootTableItem(Items.CLAY_BALL)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 16.0F)))
                                        .apply(ApplyBonusCount.addOreBonusCount(fortune))
                        )
                ));
            } else if (block instanceof ClayTreeLeavesBlock leaves) {
                add(leaves, b -> createLeavesDrops(b, sapling, NORMAL_LEAVES_SAPLING_CHANCES)
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .when(LootItemRandomChanceCondition.randomChance(0.05F))
                                .add(LootItem.lootTableItem(ClayiumItems.byPath("dense_clay_dust").get()))));
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
