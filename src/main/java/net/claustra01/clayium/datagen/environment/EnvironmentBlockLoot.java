package net.claustra01.clayium.datagen.environment;

import java.util.Set;
import java.util.stream.Stream;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class EnvironmentBlockLoot extends BlockLootSubProvider {
    public EnvironmentBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Stream.concat(
                        Stream.concat(
                                EnvironmentMachineRegistration.COBBLESTONE_GENERATORS.stream()
                                        .map(definition -> (Block)definition.block().get()),
                                EnvironmentMachineRegistration.SALT_EXTRACTOR_BLOCKS.stream()
                                        .map(block -> (Block)block.get())),
                        Stream.of(EnvironmentMachineRegistration.QUARTZ_CRUCIBLE.get()))
                .toList();
    }
}
