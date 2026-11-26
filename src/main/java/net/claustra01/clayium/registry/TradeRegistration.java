package net.claustra01.clayium.registry;

import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.machine.trade.AutoTraderBlock;
import net.claustra01.clayium.machine.trade.AutoTraderBlockEntity;
import net.claustra01.clayium.machine.trade.AutoTraderMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class TradeRegistration {
    public static final DeferredBlock<AutoTraderBlock> AUTO_TRADER = ClayiumRegistries.BLOCKS.register(
            "auto_trader",
            () -> new AutoTraderBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(5.0F, 13.0F)
                    .sound(SoundType.STONE)));
    public static final DeferredItem<FoundationBlockItem> AUTO_TRADER_ITEM = ClayiumRegistries.ITEMS.register(
            "auto_trader",
            () -> new FoundationBlockItem(AUTO_TRADER.get(), new Item.Properties()
                    .component(FoundationDataComponentRegistration.TIER.get(), AutoTraderBlockEntity.TIER)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AutoTraderBlockEntity>> BLOCK_ENTITY =
            ClayiumRegistries.BLOCK_ENTITY_TYPES.register(
                    "auto_trader",
                    () -> BlockEntityType.Builder.of(AutoTraderBlockEntity::new, AUTO_TRADER.get()).build(null));

    public static final DeferredHolder<MenuType<?>, MenuType<AutoTraderMenu>> MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "auto_trader",
                    () -> IMenuTypeExtension.create((containerId, inventory, data) ->
                            new AutoTraderMenu(containerId, inventory, data.readBlockPos())));

    private TradeRegistration() {
    }
}
