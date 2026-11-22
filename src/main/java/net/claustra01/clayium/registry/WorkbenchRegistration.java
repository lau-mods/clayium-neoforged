package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.item.FoundationItem;
import net.claustra01.clayium.tool.BufferConfigurationToolItem;
import net.claustra01.clayium.tool.ClayCraftingToolItem;
import net.claustra01.clayium.workbench.ClayCraftingBoardBlock;
import net.claustra01.clayium.workbench.ClayCraftingBoardBlockEntity;
import net.claustra01.clayium.workbench.ClayCraftingBoardMenu;
import net.claustra01.clayium.workbench.ClayWorkTableBlock;
import net.claustra01.clayium.workbench.ClayWorkTableBlockEntity;
import net.claustra01.clayium.workbench.ClayWorkTableMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class WorkbenchRegistration {
    public static final DeferredBlock<ClayWorkTableBlock> CLAY_WORK_TABLE = ClayiumRegistries.BLOCKS.register(
            "clay_work_table", () -> new ClayWorkTableBlock(properties(2.5F)));
    public static final DeferredBlock<ClayCraftingBoardBlock> CLAY_CRAFTING_BOARD = ClayiumRegistries.BLOCKS.register(
            "clay_crafting_board", () -> new ClayCraftingBoardBlock(properties(1.5F).noOcclusion()));
    public static final DeferredBlock<Block> RAW_CLAY_MACHINE_HULL = ClayiumRegistries.BLOCKS.registerSimpleBlock(
            "raw_clay_machine_hull", properties(2.0F));
    public static final DeferredBlock<Block> CLAY_MACHINE_HULL = ClayiumRegistries.BLOCKS.registerSimpleBlock(
            "clay_machine_hull", properties(3.0F));

    public static final DeferredItem<BlockItem> CLAY_WORK_TABLE_ITEM = blockItem("clay_work_table", CLAY_WORK_TABLE, 0);
    public static final DeferredItem<BlockItem> CLAY_CRAFTING_BOARD_ITEM = blockItem(
            "clay_crafting_board", CLAY_CRAFTING_BOARD, 0);
    public static final DeferredItem<BlockItem> RAW_CLAY_MACHINE_HULL_ITEM = blockItem(
            "raw_clay_machine_hull", RAW_CLAY_MACHINE_HULL, 0);
    public static final DeferredItem<BlockItem> CLAY_MACHINE_HULL_ITEM = blockItem(
            "clay_machine_hull", CLAY_MACHINE_HULL, 1);

    public static final DeferredItem<FoundationItem> RAW_CLAY_ROLLING_PIN = tool("raw_clay_rolling_pin", 0, 0);
    public static final DeferredItem<FoundationItem> RAW_CLAY_SLICER = tool("raw_clay_slicer", 0, 0);
    public static final DeferredItem<FoundationItem> RAW_CLAY_SPATULA = tool("raw_clay_spatula", 0, 0);
    public static final DeferredItem<ClayCraftingToolItem> CLAY_ROLLING_PIN = craftingTool(
            "clay_rolling_pin", 60, 4);
    public static final DeferredItem<ClayCraftingToolItem> CLAY_SLICER = craftingTool(
            "clay_slicer", 60, 3);
    public static final DeferredItem<ClayCraftingToolItem> CLAY_SPATULA = craftingTool(
            "clay_spatula", 36, 2);
    public static final DeferredItem<FoundationItem> CLAY_IO_CONFIGURATOR = tool("clay_io_configurator", 1, 128);
    public static final DeferredItem<FoundationItem> CLAY_PIPING_CONFIGURATOR = tool(
            "clay_piping_configurator", 1, 128);
    public static final DeferredItem<FoundationItem> MEMORY_CARD = tool("memory_card", 1, 0);
    public static final DeferredItem<BufferConfigurationToolItem> DIRECTION_MEMORY_CARD =
            ClayiumRegistries.ITEMS.register("direction_memory_card", () -> new BufferConfigurationToolItem(
                    new Item.Properties().stacksTo(1).component(FoundationDataComponentRegistration.TIER.get(), 1)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayWorkTableBlockEntity>>
            CLAY_WORK_TABLE_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "clay_work_table",
                    () -> BlockEntityType.Builder.of(ClayWorkTableBlockEntity::new, CLAY_WORK_TABLE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayCraftingBoardBlockEntity>>
            CLAY_CRAFTING_BOARD_BLOCK_ENTITY = ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "clay_crafting_board",
                    () -> BlockEntityType.Builder.of(
                            ClayCraftingBoardBlockEntity::new, CLAY_CRAFTING_BOARD.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<ClayWorkTableMenu>> CLAY_WORK_TABLE_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "clay_work_table", () -> IMenuTypeExtension.create(ClayWorkTableMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayCraftingBoardMenu>> CLAY_CRAFTING_BOARD_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "clay_crafting_board", () -> IMenuTypeExtension.create(ClayCraftingBoardMenu::new));

    public static final List<DeferredBlock<? extends Block>> ALL_BLOCKS = List.of(
            CLAY_WORK_TABLE, CLAY_CRAFTING_BOARD, RAW_CLAY_MACHINE_HULL, CLAY_MACHINE_HULL);
    public static final List<DeferredItem<? extends Item>> ALL_ITEMS = List.of(
            CLAY_WORK_TABLE_ITEM,
            CLAY_CRAFTING_BOARD_ITEM,
            RAW_CLAY_MACHINE_HULL_ITEM,
            CLAY_MACHINE_HULL_ITEM,
            RAW_CLAY_ROLLING_PIN,
            RAW_CLAY_SLICER,
            RAW_CLAY_SPATULA,
            CLAY_ROLLING_PIN,
            CLAY_SLICER,
            CLAY_SPATULA,
            CLAY_IO_CONFIGURATOR,
            CLAY_PIPING_CONFIGURATOR,
            MEMORY_CARD,
            DIRECTION_MEMORY_CARD);

    private static BlockBehaviour.Properties properties(float strength) {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                .strength(strength, strength + 2.0F)
                .sound(SoundType.STONE);
    }

    private static DeferredItem<BlockItem> blockItem(
            String id, DeferredBlock<? extends Block> block, int tier) {
        return ClayiumRegistries.ITEMS.register(
                id,
                () -> new FoundationBlockItem(
                        block.get(),
                        new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier)));
    }

    private static DeferredItem<FoundationItem> tool(String id, int tier, int durability) {
        return ClayiumRegistries.ITEMS.register(id, () -> {
            Item.Properties properties = new Item.Properties()
                    .component(FoundationDataComponentRegistration.TIER.get(), tier);
            if (durability > 0) {
                properties = properties.durability(durability);
            }
            return new FoundationItem(properties);
        });
    }

    private static DeferredItem<ClayCraftingToolItem> craftingTool(
            String id, int durability, int brokenClayBalls) {
        return ClayiumRegistries.ITEMS.register(
                id,
                () -> new ClayCraftingToolItem(
                        new Item.Properties()
                                .durability(durability)
                                .component(FoundationDataComponentRegistration.TIER.get(), 1),
                        brokenClayBalls));
    }

    private WorkbenchRegistration() {
    }
}
