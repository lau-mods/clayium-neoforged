package net.claustra01.clayium.datagen.worldgen;

import java.util.Set;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public final class WorldgenBlockLoot extends BlockLootSubProvider {
    public WorldgenBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        add(WorldgenRegistration.CLAY_ORE.get(), clayDrops(WorldgenRegistration.CLAY_ORE.get()));
        add(WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(), clayDrops(WorldgenRegistration.DEEPSLATE_CLAY_ORE.get()));
        dropSelf(WorldgenRegistration.DENSE_CLAY_ORE.get());
        dropSelf(WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get());
        dropSelf(WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get());
        dropSelf(WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get());
    }

    private LootTable.Builder clayDrops(Block block) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        LootPool.Builder drops = LootPool.lootPool().setRolls(ConstantValue.exactly(1));
        weightedClayDrop(block, drops, enchantments, 4, 8);
        weightedClayDrop(block, drops, enchantments, 5, 1);
        weightedClayDrop(block, drops, enchantments, 6, 2);
        weightedClayDrop(block, drops, enchantments, 7, 2);
        weightedClayDrop(block, drops, enchantments, 8, 2);
        weightedClayDrop(block, drops, enchantments, 10, 2);
        weightedClayDrop(block, drops, enchantments, 12, 1);
        weightedClayDrop(block, drops, enchantments, 13, 1);
        weightedClayDrop(block, drops, enchantments, 16, 1);
        return LootTable.lootTable()
                .withPool(applyExplosionCondition(
                        block,
                        LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .when(hasSilkTouch())
                                .add(LootItem.lootTableItem(block))))
                .withPool(drops.when(doesNotHaveSilkTouch()));
    }

    private void weightedClayDrop(
            Block block,
            LootPool.Builder pool,
            HolderLookup.RegistryLookup<Enchantment> enchantments,
            int count,
            int weight) {
        pool.add(applyExplosionDecay(
                block,
                LootItem.lootTableItem(Items.CLAY_BALL)
                        .setWeight(weight)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(count)))
                        .apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return WorldgenRegistration.BLOCKS.stream().map(block -> (Block) block.get()).toList();
    }
}
