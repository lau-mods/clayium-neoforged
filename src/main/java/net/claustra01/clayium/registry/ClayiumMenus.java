package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.menu.AreaActivatorMenu;
import net.claustra01.clayium.menu.AreaMinerMenu;
import net.claustra01.clayium.menu.AutoClayCondenserMenu;
import net.claustra01.clayium.menu.AutoCrafterMenu;
import net.claustra01.clayium.menu.AutoTraderMenu;
import net.claustra01.clayium.menu.ClayCraftingTableMenu;
import net.claustra01.clayium.menu.ClayWorkTableMenu;
import net.claustra01.clayium.menu.DistributorMenu;
import net.claustra01.clayium.menu.EnergyOnlyMenu;
import net.claustra01.clayium.menu.GadgetHolderMenu;
import net.claustra01.clayium.menu.ItemFilterStringMenu;
import net.claustra01.clayium.menu.ItemFilterWhitelistMenu;
import net.claustra01.clayium.menu.MultitrackBufferMenu;
import net.claustra01.clayium.menu.NormalInventoryMenu;
import net.claustra01.clayium.menu.PANAdapterMenu;
import net.claustra01.clayium.menu.PANCoreMenu;
import net.claustra01.clayium.menu.ProcessingMachineMenu;
import net.claustra01.clayium.menu.StorageContainerMenu;
import net.claustra01.clayium.menu.WaterWheelMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all Clayium {@link MenuType} definitions.
 */
public final class ClayiumMenus {
    public static final DeferredRegister<MenuType<?>> REGISTRY =
            DeferredRegister.create(Registries.MENU, ClayiumMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ClayWorkTableMenu>> CLAY_WORK_TABLE =
            REGISTRY.register("clay_work_table", () -> IMenuTypeExtension.create(ClayWorkTableMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ProcessingMachineMenu>> PROCESSING_MACHINE =
            REGISTRY.register("processing_machine", () -> IMenuTypeExtension.create(ProcessingMachineMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AutoClayCondenserMenu>> AUTO_CLAY_CONDENSER =
            REGISTRY.register("auto_clay_condenser", () -> IMenuTypeExtension.create(AutoClayCondenserMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AutoCrafterMenu>> AUTO_CRAFTER =
            REGISTRY.register("auto_crafter", () -> IMenuTypeExtension.create(AutoCrafterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<NormalInventoryMenu>> NORMAL_INVENTORY =
            REGISTRY.register("normal_inventory", () -> IMenuTypeExtension.create(NormalInventoryMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MultitrackBufferMenu>> MULTITRACK_BUFFER =
            REGISTRY.register("multitrack_buffer", () -> IMenuTypeExtension.create(MultitrackBufferMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<DistributorMenu>> DISTRIBUTOR =
            REGISTRY.register("distributor", () -> IMenuTypeExtension.create(DistributorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<StorageContainerMenu>> STORAGE_CONTAINER =
            REGISTRY.register("storage_container", () -> IMenuTypeExtension.create(StorageContainerMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<WaterWheelMenu>> WATER_WHEEL =
            REGISTRY.register("water_wheel", () -> IMenuTypeExtension.create(WaterWheelMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<EnergyOnlyMenu>> ENERGY_ONLY =
            REGISTRY.register("energy_only", () -> IMenuTypeExtension.create(EnergyOnlyMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AutoTraderMenu>> AUTO_TRADER =
            REGISTRY.register("auto_trader", () -> IMenuTypeExtension.create(AutoTraderMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ClayCraftingTableMenu>> CLAY_CRAFTING_TABLE =
            REGISTRY.register("clay_crafting_table", () -> IMenuTypeExtension.create(ClayCraftingTableMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemFilterWhitelistMenu>> ITEM_FILTER_WHITELIST =
            REGISTRY.register("item_filter_whitelist", () -> IMenuTypeExtension.create(ItemFilterWhitelistMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ItemFilterStringMenu>> ITEM_FILTER_STRING =
            REGISTRY.register("item_filter_string", () -> IMenuTypeExtension.create(ItemFilterStringMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<GadgetHolderMenu>> GADGET_HOLDER =
            REGISTRY.register("gadget_holder", () -> IMenuTypeExtension.create(GadgetHolderMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AreaMinerMenu>> AREA_MINER =
            REGISTRY.register("area_miner", () -> IMenuTypeExtension.create(AreaMinerMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<AreaActivatorMenu>> AREA_ACTIVATOR =
            REGISTRY.register("area_activator", () -> IMenuTypeExtension.create(AreaActivatorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PANAdapterMenu>> PAN_ADAPTER =
            REGISTRY.register("pan_adapter", () -> IMenuTypeExtension.create(PANAdapterMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PANCoreMenu>> PAN_CORE =
            REGISTRY.register("pan_core", () -> IMenuTypeExtension.create(PANCoreMenu::new));

    private ClayiumMenus() {}

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
