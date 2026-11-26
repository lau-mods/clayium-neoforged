package net.claustra01.clayium.datagen.claytree;

import java.util.Set;
import net.claustra01.clayium.registry.ClayTreeRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class ClayTreeBlockLoot extends BlockLootSubProvider {
    public ClayTreeBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(ClayTreeRegistration.CLAY_TREE_SAPLING.get());
        dropSelf(ClayTreeRegistration.CLAY_TREE_LOG.get());
        add(ClayTreeRegistration.CLAY_TREE_LEAF.get(), clayTreeLeaves());
    }

    private LootTable.Builder clayTreeLeaves() {
        var fortune = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
        Block leaves = ClayTreeRegistration.CLAY_TREE_LEAF.get();
        LootTable.Builder table = createSilkTouchOrShearsDispatchTable(
                leaves,
                applyExplosionCondition(
                        leaves,
                        LootItem.lootTableItem(MaterialRegistration.MATERIAL_ITEMS.get("clay_dust").get())
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                        fortune, 0.05F, 0.0625F, 0.083333336F, 0.1F))));
        return table.withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(doesNotHaveSilkTouch())
                .when(InvertedLootItemCondition.invert(HAS_SHEARS))
                .add(applyExplosionCondition(
                        leaves,
                        LootItem.lootTableItem(MaterialRegistration.MATERIAL_ITEMS.get("dense_clay_dust").get())
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(
                                        fortune, 0.005F, 0.005555556F, 0.00625F, 0.008333334F)))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ClayTreeRegistration.BLOCKS.stream().map(block -> (Block) block.get()).toList();
    }
}
