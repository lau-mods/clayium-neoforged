package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.multiblock.MultiblockControllerBlock;
import net.claustra01.clayium.multiblock.MultiblockControllerBlockEntity;
import net.claustra01.clayium.multiblock.MultiblockMachineType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class MultiblockRegistration {
    public static final Map<MultiblockMachineType, DeferredBlock<MultiblockControllerBlock>> CONTROLLERS;
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS;

    static {
        Map<MultiblockMachineType, DeferredBlock<MultiblockControllerBlock>> controllers = new LinkedHashMap<>();
        List<DeferredItem<FoundationBlockItem>> items = new ArrayList<>();
        for (MultiblockMachineType machineType : MultiblockMachineType.values()) {
            DeferredBlock<MultiblockControllerBlock> block = ClayiumRegistries.BLOCKS.register(
                    machineType.id(),
                    () -> new MultiblockControllerBlock(properties(machineType.minimumTier()), machineType));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    machineType.id(),
                    () -> new FoundationBlockItem(
                            block.get(),
                            new Item.Properties().component(
                                    FoundationDataComponentRegistration.TIER.get(), machineType.minimumTier())));
            controllers.put(machineType, block);
            items.add(item);
        }
        CONTROLLERS = Collections.unmodifiableMap(controllers);
        ITEMS = List.copyOf(items);
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultiblockControllerBlockEntity>>
            CONTROLLER_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "multiblock_controller",
                    () -> BlockEntityType.Builder.of(
                            MultiblockControllerBlockEntity::new,
                            CONTROLLERS.values().stream().map(DeferredBlock::get).toArray(Block[]::new))
                            .build(null));

    public static int hullTier(Block block) {
        return AssemblyMachineRegistration.MACHINE_HULLS.entrySet().stream()
                .filter(entry -> entry.getValue().get() == block)
                .mapToInt(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }

    private static BlockBehaviour.Properties properties(int tier) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F + tier * 0.25F, 5.0F + tier)
                .sound(SoundType.STONE);
    }

    private MultiblockRegistration() {
    }
}
