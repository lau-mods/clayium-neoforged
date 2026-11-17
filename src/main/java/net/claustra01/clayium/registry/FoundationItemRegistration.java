package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.item.FoundationBlockItem;
import net.claustra01.clayium.item.FoundationItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

public final class FoundationItemRegistration {
    public static final DeferredItem<BlockItem> DENSE_CLAY = registerBlockItem("dense_clay", FoundationBlockRegistration.DENSE_CLAY);
    public static final DeferredItem<BlockItem> COMPRESSED_CLAY = registerBlockItem(
            "compressed_clay", FoundationBlockRegistration.COMPRESSED_CLAY);
    public static final DeferredItem<BlockItem> CLAYIUM_BLOCK = registerBlockItem(
            "clayium_block", FoundationBlockRegistration.CLAYIUM_BLOCK);
    public static final DeferredItem<FoundationBlockItem> CLAY_BENDING_MACHINE = ClayiumRegistries.ITEMS.register(
            "clay_bending_machine",
            () -> new FoundationBlockItem(
                    FoundationBlockRegistration.CLAY_BENDING_MACHINE.get(),
                    new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), 1)));

    public static final DeferredItem<Item> INDUSTRIAL_CLAY_DUST = ClayiumRegistries.ITEMS.register(
            "industrial_clay_dust", () -> new FoundationItem(tier(3)));
    public static final DeferredItem<Item> CLAY_WRENCH = ClayiumRegistries.ITEMS.register(
            "clay_wrench", () -> new FoundationItem(tier(1).stacksTo(1)));
    public static final DeferredItem<Item> CLAY_PICKAXE = ClayiumRegistries.ITEMS.register(
            "clay_pickaxe", () -> new FoundationItem(tier(1).stacksTo(1)));
    public static final DeferredItem<Item> GADGET_HOLDER = ClayiumRegistries.ITEMS.register(
            "gadget_holder", () -> new FoundationItem(new Item.Properties().stacksTo(1)));

    public static final List<DeferredItem<? extends Item>> ALL = List.of(
            DENSE_CLAY,
            COMPRESSED_CLAY,
            CLAYIUM_BLOCK,
            CLAY_BENDING_MACHINE,
            INDUSTRIAL_CLAY_DUST,
            CLAY_WRENCH,
            CLAY_PICKAXE,
            GADGET_HOLDER);

    private static DeferredItem<BlockItem> registerBlockItem(
            String id, net.neoforged.neoforge.registries.DeferredBlock<? extends net.minecraft.world.level.block.Block> block) {
        return ClayiumRegistries.ITEMS.register(id, () -> new FoundationBlockItem(
                block.get(), tier(id.equals("dense_clay") ? 0 : id.equals("compressed_clay") ? 1 : 8)));
    }

    private static Item.Properties tier(int tier) {
        return new Item.Properties().component(FoundationDataComponentRegistration.TIER.get(), tier);
    }

    private FoundationItemRegistration() {
    }
}
