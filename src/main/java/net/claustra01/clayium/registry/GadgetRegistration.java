package net.claustra01.clayium.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.gadget.GadgetHolderItem;
import net.claustra01.clayium.gadget.GadgetHolderMenu;
import net.claustra01.clayium.gadget.GadgetItem;
import net.claustra01.clayium.gadget.GadgetPlayerState;
import net.claustra01.clayium.gadget.GadgetType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

public final class GadgetRegistration {
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> GADGETS =
            ClayiumRegistries.DATA_COMPONENT_TYPES.registerComponentType(
                    "gadgets",
                    builder -> builder.persistent(ItemContainerContents.CODEC)
                            .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                            .cacheEncoding());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<GadgetPlayerState>> PLAYER_STATE =
            ClayiumRegistries.ATTACHMENT_TYPES.register(
                    "gadget_player_state",
                    () -> AttachmentType.builder(GadgetPlayerState::new)
                            .serialize(GadgetPlayerState.CODEC)
                            .copyOnDeath()
                            .build());
    public static final DeferredHolder<MenuType<?>, MenuType<GadgetHolderMenu>> GADGET_HOLDER_MENU =
            ClayiumRegistries.MENU_TYPES.register(
                    "gadget_holder",
                    () -> new MenuType<>((IContainerFactory<GadgetHolderMenu>) GadgetHolderMenu::client,
                            net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredItem<GadgetHolderItem> GADGET_HOLDER = ClayiumRegistries.ITEMS.register(
            "gadget_holder",
            () -> new GadgetHolderItem(new Item.Properties()
                    .stacksTo(1)
                    .component(GADGETS.get(), ItemContainerContents.EMPTY)));

    public static final Map<GadgetType, DeferredItem<GadgetItem>> GADGETS_BY_TYPE = registerGadgets();
    public static final List<DeferredItem<? extends Item>> ALL = allItems();

    private static Map<GadgetType, DeferredItem<GadgetItem>> registerGadgets() {
        Map<GadgetType, DeferredItem<GadgetItem>> gadgets = new EnumMap<>(GadgetType.class);
        for (GadgetType type : GadgetType.values()) {
            gadgets.put(type, ClayiumRegistries.ITEMS.register(
                    type.id(), () -> new GadgetItem(type, new Item.Properties().stacksTo(1))));
        }
        return Collections.unmodifiableMap(gadgets);
    }

    private static List<DeferredItem<? extends Item>> allItems() {
        List<DeferredItem<? extends Item>> items = new ArrayList<>();
        items.add(GADGET_HOLDER);
        items.addAll(GADGETS_BY_TYPE.values());
        return List.copyOf(items);
    }

    private GadgetRegistration() {
    }
}
