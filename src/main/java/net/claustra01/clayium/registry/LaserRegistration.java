package net.claustra01.clayium.registry;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.laser.ClayEnergyLaserBlock;
import net.claustra01.clayium.laser.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.laser.LaserReflectorBlock;
import net.claustra01.clayium.laser.LaserReflectorBlockEntity;
import net.claustra01.clayium.laser.LaserSpecification;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class LaserRegistration {
    public static final Map<Integer, LaserDefinition> LASERS;
    public static final List<DeferredBlock<ClayEnergyLaserBlock>> LASER_BLOCKS;
    public static final List<DeferredItem<FoundationBlockItem>> LASER_ITEMS;

    static {
        Map<Integer, LaserDefinition> lasers = new LinkedHashMap<>();
        for (LaserSpecification specification : LaserSpecification.VALUES) {
            DeferredBlock<ClayEnergyLaserBlock> block = ClayiumRegistries.BLOCKS.register(
                    specification.id(),
                    () -> new ClayEnergyLaserBlock(
                            BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                                    .strength(3.0F + specification.tier() * 0.25F, 5.0F + specification.tier())
                                    .sound(SoundType.STONE),
                            specification));
            DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                    specification.id(),
                    () -> new FoundationBlockItem(block.get(), new Item.Properties().component(
                            FoundationDataComponentRegistration.TIER.get(), specification.tier())));
            lasers.put(specification.tier(), new LaserDefinition(specification, block, item));
        }
        LASERS = Collections.unmodifiableMap(lasers);
        LASER_BLOCKS = LASERS.values().stream().map(LaserDefinition::block).toList();
        LASER_ITEMS = LASERS.values().stream().map(LaserDefinition::item).toList();
    }

    public static final DeferredBlock<LaserReflectorBlock> LASER_REFLECTOR = ClayiumRegistries.BLOCKS.register(
            "laser_reflector",
            () -> new LaserReflectorBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                    .strength(3.0F, 5.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredItem<FoundationBlockItem> LASER_REFLECTOR_ITEM = ClayiumRegistries.ITEMS.register(
            "laser_reflector",
            () -> new FoundationBlockItem(LASER_REFLECTOR.get(),
                    new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), 7)));

    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>,
            net.minecraft.world.inventory.MenuType<net.claustra01.clayium.laser.ClayEnergyLaserMenu>>
            LASER_MENU = ClayiumRegistries.MENU_TYPES.register(
                    "clay_energy_laser",
                    () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create((id, inventory, data) ->
                            new net.claustra01.clayium.laser.ClayEnergyLaserMenu(id, inventory, data.readBlockPos())));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayEnergyLaserBlockEntity>>
            LASER_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "clay_energy_laser",
                    () -> BlockEntityType.Builder.of(
                            ClayEnergyLaserBlockEntity::new,
                            LASER_BLOCKS.stream().map(DeferredBlock::get)
                                    .toArray(net.minecraft.world.level.block.Block[]::new)).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaserReflectorBlockEntity>>
            REFLECTOR_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "laser_reflector",
                    () -> BlockEntityType.Builder.of(
                            LaserReflectorBlockEntity::new, LASER_REFLECTOR.get()).build(null));

    public static List<DeferredItem<FoundationBlockItem>> items() {
        return java.util.stream.Stream.concat(LASER_ITEMS.stream(), java.util.stream.Stream.of(LASER_REFLECTOR_ITEM))
                .toList();
    }

    public record LaserDefinition(
            LaserSpecification specification,
            DeferredBlock<ClayEnergyLaserBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private LaserRegistration() {
    }
}
