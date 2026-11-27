package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.block.MetalChestBlock;
import net.claustra01.clayium.block.RedstoneInterfaceBlock;
import net.claustra01.clayium.block.StorageContainerBlock;
import net.claustra01.clayium.blockentity.BufferBlockEntity;
import net.claustra01.clayium.blockentity.ClayInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.DistributorBlockEntity;
import net.claustra01.clayium.blockentity.FluidTranslatorBlockEntity;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.blockentity.VacuumContainerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers BlockEntityTypes for Clayium transport, storage, interface, and container blocks.
 */
public final class ClayiumBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BufferBlockEntity>> BUFFER =
            REGISTRY.register("buffer", () -> BlockEntityType.Builder.of(
                    BufferBlockEntity::new,
                    collectBlocks(e -> e instanceof ClayContainerBlock cc
                            && "buffer".equals(cc.getCategoryKey())
                            && !"tooltip.MultitrackBuffer".equals(cc.getTooltipKey()))
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultitrackBufferBlockEntity>> MULTITRACK_BUFFER =
            REGISTRY.register("multitrack_buffer", () -> BlockEntityType.Builder.of(
                    MultitrackBufferBlockEntity::new,
                    collectBlocks(e -> e instanceof ClayContainerBlock cc
                            && "buffer".equals(cc.getCategoryKey())
                            && "tooltip.MultitrackBuffer".equals(cc.getTooltipKey()))
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTranslatorBlockEntity>> FLUID_TRANSLATOR =
            REGISTRY.register("fluid_translator", () -> BlockEntityType.Builder.of(
                    FluidTranslatorBlockEntity::new,
                    collectBlocks(e -> e instanceof ClayContainerBlock cc
                            && "fluid".equals(cc.getCategoryKey())
                            && "tooltip.FluidTranslator".equals(cc.getTooltipKey()))
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageContainerBlockEntity>> STORAGE_CONTAINER =
            REGISTRY.register("storage_container", () -> BlockEntityType.Builder.of(
                    StorageContainerBlockEntity::new,
                    collectBlocks(e -> e instanceof StorageContainerBlock)
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VacuumContainerBlockEntity>> VACUUM_CONTAINER =
            REGISTRY.register("vacuum_container", () -> BlockEntityType.Builder.of(
                    VacuumContainerBlockEntity::new,
                    collectBlocks(e -> e instanceof ClayContainerBlock cc
                            && !(e instanceof StorageContainerBlock)
                            && "storage".equals(cc.getCategoryKey()))
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalChestBlockEntity>> METAL_CHEST =
            REGISTRY.register("metal_chest", () -> BlockEntityType.Builder.of(
                    MetalChestBlockEntity::new,
                    collectBlocks(e -> e instanceof MetalChestBlock)
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR =
            REGISTRY.register("distributor", () -> BlockEntityType.Builder.of(
                    DistributorBlockEntity::new,
                    collectBlocks(e -> e instanceof ClayContainerBlock cc
                            && "tooltip.Distributor".equals(cc.getTooltipKey()))
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayInterfaceBlockEntity>> CLAY_INTERFACE =
            REGISTRY.register("clay_interface", () -> BlockEntityType.Builder.of(
                    ClayInterfaceBlockEntity::new,
                    collectBlocks(e -> e instanceof ClayContainerBlock cc
                            && "tooltip.Interface".equals(cc.getTooltipKey()))
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneInterfaceBlockEntity>> REDSTONE_INTERFACE =
            REGISTRY.register("redstone_interface", () -> BlockEntityType.Builder.of(
                    RedstoneInterfaceBlockEntity::new,
                    collectBlocks(e -> e instanceof RedstoneInterfaceBlock)
            ).build(null));

    private static Block[] collectBlocks(java.util.function.Predicate<Block> predicate) {
        List<Block> matched = new ArrayList<>();
        for (var holder : ClayiumBlocks.all().values()) {
            Block b = holder.get();
            if (predicate.test(b)) {
                matched.add(b);
            }
        }
        return matched.toArray(Block[]::new);
    }

    private ClayiumBlockEntities() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
