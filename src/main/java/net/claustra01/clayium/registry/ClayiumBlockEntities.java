package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.blockentity.AreaActivatorBlockEntity;
import net.claustra01.clayium.blockentity.AreaCollectorBlockEntity;
import net.claustra01.clayium.blockentity.AreaMinerBlockEntity;
import net.claustra01.clayium.blockentity.AssemblerBlockEntity;
import net.claustra01.clayium.blockentity.AutoClayCondenserBlockEntity;
import net.claustra01.clayium.blockentity.AutoCrafterBlockEntity;
import net.claustra01.clayium.blockentity.AutoTraderBlockEntity;
import net.claustra01.clayium.blockentity.BlastFurnaceBlockEntity;
import net.claustra01.clayium.blockentity.BufferBlockEntity;
import net.claustra01.clayium.blockentity.CACollectorBlockEntity;
import net.claustra01.clayium.blockentity.CACondenserBlockEntity;
import net.claustra01.clayium.blockentity.CAInjectorBlockEntity;
import net.claustra01.clayium.blockentity.CAReactorBlockEntity;
import net.claustra01.clayium.blockentity.CentrifugeBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalMetalSeparatorBlockEntity;
import net.claustra01.clayium.blockentity.ChemicalReactorBlockEntity;
import net.claustra01.clayium.blockentity.ClayChunkLoaderBlockEntity;
import net.claustra01.clayium.blockentity.ClayCraftingTableBlockEntity;
import net.claustra01.clayium.blockentity.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.blockentity.ClayFabricatorBlockEntity;
import net.claustra01.clayium.blockentity.ClayInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.ClayMarkerBlockEntity;
import net.claustra01.clayium.blockentity.ClayWorkTableBlockEntity;
import net.claustra01.clayium.blockentity.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.blockentity.CreativeEnergySourceBlockEntity;
import net.claustra01.clayium.blockentity.DistributorBlockEntity;
import net.claustra01.clayium.blockentity.FluidTranslatorBlockEntity;
import net.claustra01.clayium.blockentity.LaserInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.LaserReflectorBlockEntity;
import net.claustra01.clayium.blockentity.MetalChestBlockEntity;
import net.claustra01.clayium.blockentity.MultitrackBufferBlockEntity;
import net.claustra01.clayium.blockentity.PANDuplicatorBlockEntity;
import net.claustra01.clayium.blockentity.QuartzCrucibleBlockEntity;
import net.claustra01.clayium.blockentity.ReactorBlockEntity;
import net.claustra01.clayium.blockentity.RedstoneInterfaceBlockEntity;
import net.claustra01.clayium.blockentity.SaltExtractorBlockEntity;
import net.claustra01.clayium.blockentity.SimpleClayMachineBlockEntity;
import net.claustra01.clayium.blockentity.SolarClayFabricatorBlockEntity;
import net.claustra01.clayium.blockentity.StorageContainerBlockEntity;
import net.claustra01.clayium.blockentity.VacuumContainerBlockEntity;
import net.claustra01.clayium.blockentity.WaterWheelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers BlockEntityTypes for Clayium transport, storage, interface, crafting, and machine blocks.
 */
public final class ClayiumBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ClayiumMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BufferBlockEntity>> BUFFER =
            regByCategory("buffer", BufferBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultitrackBufferBlockEntity>> MULTITRACK_BUFFER =
            regByCategory("multitrack_buffer", MultitrackBufferBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FluidTranslatorBlockEntity>> FLUID_TRANSLATOR =
            regByCategory("fluid_translator", FluidTranslatorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageContainerBlockEntity>> STORAGE_CONTAINER =
            regByCategory("storage_container", StorageContainerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VacuumContainerBlockEntity>> VACUUM_CONTAINER =
            regByCategory("vacuum_container", VacuumContainerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MetalChestBlockEntity>> METAL_CHEST =
            regByCategory("metal_chest", MetalChestBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DistributorBlockEntity>> DISTRIBUTOR =
            regByCategory("distributor", DistributorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayInterfaceBlockEntity>> CLAY_INTERFACE =
            regByCategory("clay_interface", ClayInterfaceBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RedstoneInterfaceBlockEntity>> REDSTONE_INTERFACE =
            regByCategory("redstone_interface", RedstoneInterfaceBlockEntity::new);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserInterfaceBlockEntity>> LASER_INTERFACE =
            regByCategory("laser_interface", LaserInterfaceBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SimpleClayMachineBlockEntity>> SIMPLE_MACHINE =
            regByCategory("simple_machine", SimpleClayMachineBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AssemblerBlockEntity>> ASSEMBLER =
            regByCategory("assembler", AssemblerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CentrifugeBlockEntity>> CENTRIFUGE =
            regByCategory("centrifuge", CentrifugeBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorBlockEntity>> CHEMICAL_REACTOR =
            regByCategory("chemical_reactor", ChemicalReactorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalMetalSeparatorBlockEntity>> CHEMICAL_METAL_SEPARATOR =
            regByCategory("chemical_metal_separator", ChemicalMetalSeparatorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CAInjectorBlockEntity>> CA_INJECTOR =
            regByCategory("ca_injector", CAInjectorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CACondenserBlockEntity>> CA_CONDENSER =
            regByCategory("ca_condenser", CACondenserBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CACollectorBlockEntity>> CA_COLLECTOR =
            regByCategory("ca_collector", CACollectorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlastFurnaceBlockEntity>> BLAST_FURNACE =
            regByCategory("blast_furnace", BlastFurnaceBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorBlockEntity>> REACTOR =
            regByCategory("reactor", ReactorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CAReactorBlockEntity>> CA_REACTOR =
            regByCategory("ca_reactor", CAReactorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoClayCondenserBlockEntity>> AUTO_CLAY_CONDENSER =
            regByCategory("auto_clay_condenser", AutoClayCondenserBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarClayFabricatorBlockEntity>> SOLAR_CLAY_FABRICATOR =
            regByCategory("solar_clay_fabricator", SolarClayFabricatorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayFabricatorBlockEntity>> CLAY_FABRICATOR =
            regByCategory("clay_fabricator", ClayFabricatorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CobblestoneGeneratorBlockEntity>> COBBLESTONE_GENERATOR =
            regByCategory("cobblestone_generator", CobblestoneGeneratorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaltExtractorBlockEntity>> SALT_EXTRACTOR =
            regByCategory("salt_extractor", SaltExtractorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>> WATER_WHEEL =
            regByCategory("water_wheel", WaterWheelBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeEnergySourceBlockEntity>> CREATIVE_CE_SOURCE =
            regByCategory("creative_ce_source", CreativeEnergySourceBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoCrafterBlockEntity>> AUTO_CRAFTER =
            regByCategory("auto_crafter", AutoCrafterBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoTraderBlockEntity>> AUTO_TRADER =
            regByCategory("auto_trader", AutoTraderBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PANDuplicatorBlockEntity>> PAN_DUPLICATOR =
            regByCategory("pan_duplicator", PANDuplicatorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayWorkTableBlockEntity>> CLAY_WORK_TABLE =
            regByCategory("clay_work_table", ClayWorkTableBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayCraftingTableBlockEntity>> CLAY_CRAFTING_TABLE =
            regByCategory("clay_crafting_table", ClayCraftingTableBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<QuartzCrucibleBlockEntity>> QUARTZ_CRUCIBLE =
            regByCategory("quartz_crucible", QuartzCrucibleBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayChunkLoaderBlockEntity>> CLAY_CHUNK_LOADER =
            regByCategory("clay_chunk_loader", ClayChunkLoaderBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayEnergyLaserBlockEntity>> CLAY_ENERGY_LASER =
            regByCategory("clay_energy_laser", ClayEnergyLaserBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserReflectorBlockEntity>> LASER_REFLECTOR =
            regByCategory("laser_reflector", LaserReflectorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayMarkerBlockEntity>> CLAY_MARKER =
            regByCategory("clay_marker", ClayMarkerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AreaMinerBlockEntity>> AREA_MINER =
            regByCategory("area_miner", AreaMinerBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AreaCollectorBlockEntity>> AREA_COLLECTOR =
            regByCategory("area_collector", AreaCollectorBlockEntity::new);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AreaActivatorBlockEntity>> AREA_ACTIVATOR =
            regByCategory("area_activator", AreaActivatorBlockEntity::new);

    private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> regByCategory(
            String category,
            BlockEntitySupplier<T> factory) {
        return REGISTRY.register(category, () -> BlockEntityType.Builder.of(
                factory,
                collectBlocksForCategory(category)
        ).build(null));
    }

    private static Block[] collectBlocksForCategory(String category) {
        List<Block> matched = new ArrayList<>();
        for (var holder : ClayiumBlocks.all().values()) {
            Block b = holder.get();
            if (category.equals(ClayContainerBlock.resolveBlockEntityCategory(b))) {
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
