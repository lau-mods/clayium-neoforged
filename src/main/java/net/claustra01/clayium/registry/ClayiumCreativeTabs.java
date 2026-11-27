package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ClayiumCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTRY =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ClayiumMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = REGISTRY.register(
            "clayium",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ClayiumMod.MOD_ID + ".clayium"))
                    .icon(() -> new ItemStack(ClayiumItems.byPath("clay_work_table").get()))
                    .displayItems((parameters, output) -> {
                        for (var entry : ClayiumItems.blockItems().entrySet()) {
                            output.accept(entry.getValue().get());
                        }
                        for (var entry : ClayiumItems.standaloneItems().entrySet()) {
                            String path = entry.getKey();
                            if (path.startsWith("fluid_capsule_") && ClayiumConfig.FLUID_CAPSULE_CREATIVE_TAB_MODE.get() != 0) {
                                continue;
                            }
                            output.accept(entry.getValue().get());
                        }
                    })
                    .build()
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CAPSULES = REGISTRY.register(
            "clayium_capsule",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ClayiumMod.MOD_ID + ".clayium_capsule"))
                    .icon(() -> new ItemStack(ClayiumItems.byPath("fluid_capsule_1000").get()))
                    .displayItems((parameters, output) -> {
                        if (ClayiumConfig.FLUID_CAPSULE_CREATIVE_TAB_MODE.get() == 0) {
                            return;
                        }
                        for (MapEntryHelper capsule : MapEntryHelper.CAPSULE_PATHS) {
                            DeferredItem<? extends Item> holder = ClayiumItems.byPath(capsule.path());
                            output.accept(holder.get());
                        }
                    })
                    .build()
    );

    private record MapEntryHelper(String path) {
        private static final MapEntryHelper[] CAPSULE_PATHS = {
                new MapEntryHelper("fluid_capsule_1000"),
                new MapEntryHelper("fluid_capsule_125"),
                new MapEntryHelper("fluid_capsule_25"),
                new MapEntryHelper("fluid_capsule_5"),
                new MapEntryHelper("fluid_capsule_1")
        };
    }

    private ClayiumCreativeTabs() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
