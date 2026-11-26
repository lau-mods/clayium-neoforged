package net.claustra01.clayium.registry;

import java.util.List;
import net.claustra01.clayium.tool.advanced.AdvancedToolPattern;
import net.claustra01.clayium.tool.advanced.ClayPickaxeItem;
import net.claustra01.clayium.tool.advanced.ClayShovelItem;
import net.claustra01.clayium.tool.advanced.ClaySteelPickaxeItem;
import net.claustra01.clayium.tool.advanced.ClaySteelShovelItem;
import net.claustra01.clayium.tool.advanced.ClayToolTier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class AdvancedToolRegistration {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MODE =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "advanced_tool_mode",
                    builder -> builder.persistent(com.mojang.serialization.Codec.intRange(0, 64))
                            .networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AdvancedToolPattern>> PATTERN =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "advanced_tool_pattern",
                    builder -> builder.persistent(AdvancedToolPattern.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(AdvancedToolPattern.CODEC))
                            .cacheEncoding());

    public static final DeferredItem<ClayPickaxeItem> CLAY_PICKAXE = ClayiumRegistries.ITEMS.register(
            "clay_pickaxe",
            () -> new ClayPickaxeItem(properties(1, ClayToolTier.CLAY_PICKAXE, 1.0F, -2.8F)));
    public static final DeferredItem<ClayShovelItem> CLAY_SHOVEL = ClayiumRegistries.ITEMS.register(
            "clay_shovel",
            () -> new ClayShovelItem(properties(1, ClayToolTier.CLAY_SHOVEL, 1.5F, -3.0F)));
    public static final DeferredItem<ClaySteelPickaxeItem> CLAY_STEEL_PICKAXE = ClayiumRegistries.ITEMS.register(
            "clay_steel_pickaxe",
            () -> new ClaySteelPickaxeItem(advancedProperties(1.0F, -2.8F)));
    public static final DeferredItem<ClaySteelShovelItem> CLAY_STEEL_SHOVEL = ClayiumRegistries.ITEMS.register(
            "clay_steel_shovel",
            () -> new ClaySteelShovelItem(advancedProperties(1.5F, -3.0F)));

    public static final List<DeferredItem<? extends Item>> ALL = List.of(
            CLAY_PICKAXE, CLAY_SHOVEL, CLAY_STEEL_PICKAXE, CLAY_STEEL_SHOVEL);

    private static Item.Properties properties(int tier, ClayToolTier toolTier, float damage, float speed) {
        return new Item.Properties()
                .component(FoundationDataComponentRegistration.TIER.get(), tier)
                .attributes(DiggerItem.createAttributes(toolTier, damage, speed));
    }

    private static Item.Properties advancedProperties(float damage, float speed) {
        return properties(7, ClayToolTier.CLAY_STEEL, damage, speed)
                .component(MODE.get(), 0)
                .component(PATTERN.get(), AdvancedToolPattern.EMPTY);
    }

    private AdvancedToolRegistration() {
    }
}
