package net.claustra01.clayium.datagen.trade;

import java.util.Set;
import net.claustra01.clayium.registry.TradeRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class TradeBlockLoot extends BlockLootSubProvider {
    public TradeBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(TradeRegistration.AUTO_TRADER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(TradeRegistration.AUTO_TRADER.get());
    }
}
