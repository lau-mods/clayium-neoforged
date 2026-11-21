package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.miner.BasicOperationBlock;
import net.claustra01.clayium.machine.miner.BasicOperationBlockEntity;
import net.claustra01.clayium.machine.miner.BasicOperationType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class MinerRegistration {
    public static final int TIER = 6;

    public static final DeferredBlock<BasicOperationBlock> BLOCK_BREAKER = register(BasicOperationType.BLOCK_BREAKER);
    public static final DeferredBlock<BasicOperationBlock> ACTIVATOR = register(BasicOperationType.ACTIVATOR);
    public static final DeferredItem<FoundationBlockItem> BLOCK_BREAKER_ITEM = registerItem(
            BasicOperationType.BLOCK_BREAKER, BLOCK_BREAKER);
    public static final DeferredItem<FoundationBlockItem> ACTIVATOR_ITEM = registerItem(
            BasicOperationType.ACTIVATOR, ACTIVATOR);
    public static final List<DeferredBlock<BasicOperationBlock>> BLOCKS = List.of(BLOCK_BREAKER, ACTIVATOR);
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS = List.of(BLOCK_BREAKER_ITEM, ACTIVATOR_ITEM);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BasicOperationBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "basic_block_operation",
                    () -> BlockEntityType.Builder.of(
                            BasicOperationBlockEntity::new, BLOCK_BREAKER.get(), ACTIVATOR.get()).build(null));

    private static DeferredBlock<BasicOperationBlock> register(BasicOperationType type) {
        return ClayiumRegistries.BLOCKS.register(
                type.id(),
                () -> new BasicOperationBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.STONE)
                                .strength(4.5F, 11.0F)
                                .sound(SoundType.STONE),
                        type));
    }

    private static DeferredItem<FoundationBlockItem> registerItem(
            BasicOperationType type, DeferredBlock<BasicOperationBlock> block) {
        return ClayiumRegistries.ITEMS.register(
                type.id(),
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), TIER)));
    }

    private MinerRegistration() {
    }
}
