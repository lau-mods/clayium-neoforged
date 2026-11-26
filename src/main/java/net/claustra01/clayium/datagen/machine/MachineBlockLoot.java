package net.claustra01.clayium.datagen.machine;

import java.util.Set;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class MachineBlockLoot extends BlockLootSubProvider {
    public MachineBlockLoot(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return java.util.stream.Stream.of(
                        MachineRegistration.BLOCKS.stream().map(block -> (Block)block.get()),
                        FormingMachineRegistration.BLOCKS.stream().map(block -> (Block)block.get()),
                        AssemblyMachineRegistration.BLOCKS.stream().map(block -> (Block)block.get()))
                .flatMap(stream -> stream)
                .toList();
    }
}
