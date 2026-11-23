package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.waterwheel.WaterWheelBlock;
import net.claustra01.clayium.machine.waterwheel.WaterWheelBlockEntity;
import net.claustra01.clayium.machine.waterwheel.WaterWheelMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class WaterWheelRegistration {
    public static final WaterWheelDefinition CLAY = register("clay_water_wheel", 1);
    public static final WaterWheelDefinition DENSE_CLAY = register("dense_clay_water_wheel", 2);
    public static final List<WaterWheelDefinition> DEFINITIONS = List.of(CLAY, DENSE_CLAY);
    public static final List<DeferredBlock<WaterWheelBlock>> BLOCKS = DEFINITIONS.stream()
            .map(WaterWheelDefinition::block).toList();
    public static final List<DeferredItem<FoundationBlockItem>> ITEMS = DEFINITIONS.stream()
            .map(WaterWheelDefinition::item).toList();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterWheelBlockEntity>> WATER_WHEEL_BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "water_wheel",
                    () -> BlockEntityType.Builder.of(
                                    WaterWheelBlockEntity::new,
                                    BLOCKS.stream().map(DeferredBlock::get)
                                            .toArray(net.minecraft.world.level.block.Block[]::new))
                            .build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<WaterWheelMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register("water_wheel",
                    () -> IMenuTypeExtension.create((id, inventory, data) ->
                            new WaterWheelMenu(id, inventory, data.readBlockPos())));

    private static WaterWheelDefinition register(String id, int tier) {
        DeferredBlock<WaterWheelBlock> block = ClayiumRegistries.BLOCKS.register(
                id,
                () -> new WaterWheelBlock(
                        BlockBehaviour.Properties.of()
                                .mapColor(MapColor.CLAY)
                                .strength(3.0F + tier * 0.25F, 5.0F + tier)
                                .sound(SoundType.STONE),
                        tier));
        DeferredItem<FoundationBlockItem> item = ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
        return new WaterWheelDefinition(id, tier, block, item);
    }

    public record WaterWheelDefinition(
            String id,
            int tier,
            DeferredBlock<WaterWheelBlock> block,
            DeferredItem<FoundationBlockItem> item) {
    }

    private WaterWheelRegistration() {
    }
}
