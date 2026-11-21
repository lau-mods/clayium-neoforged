package net.claustra01.clayium.registry;

import net.claustra01.clayium.ClayiumMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class FoundationCreativeTabRegistration {
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            ClayiumRegistries.CREATIVE_MODE_TABS.register(
                    "main",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup." + ClayiumMod.MOD_ID))
                            .icon(() -> FoundationItemRegistration.INDUSTRIAL_CLAY_DUST.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                FoundationItemRegistration.ALL.forEach(output::accept);
                                WorldgenRegistration.ITEMS.forEach(output::accept);
                                MaterialRegistration.CREATIVE_TAB_ITEMS.forEach(output::accept);
                                WorkbenchRegistration.ALL_ITEMS.forEach(output::accept);
                                MachineRegistration.ITEMS.forEach(output::accept);
                                CaRegistration.ITEMS.forEach(output::accept);
                                PanRegistration.ITEMS.forEach(output::accept);
                                ClayProductionRegistration.ITEMS.forEach(output::accept);
                                FormingMachineRegistration.ITEMS.forEach(output::accept);
                                AssemblyMachineRegistration.ITEMS.forEach(output::accept);
                                AutoCrafterRegistration.ITEMS.forEach(output::accept);
                                MinerRegistration.ITEMS.forEach(output::accept);
                                AreaMachineRegistration.ITEMS.forEach(output::accept);
                                output.accept(ChunkLoaderRegistration.CHUNK_LOADER_ITEM);
                                EnergyConverterRegistration.ITEMS.forEach(output::accept);
                                InterfaceRegistration.ITEMS.forEach(output::accept);
                                output.accept(InterfaceRegistration.SYNCHRONIZER);
                                LaserRegistration.items().forEach(output::accept);
                                MultiblockRegistration.ITEMS.forEach(output::accept);
                                ClayTreeRegistration.ITEMS.forEach(output::accept);
                                FluidRegistration.ITEMS.forEach(output::accept);
                                output.accept(FluidRegistration.FLUID_FILTER);
                                LogisticsRegistration.BLOCK_ITEMS.forEach(output::accept);
                                output.accept(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_ITEM);
                                MultitrackRegistration.BLOCK_ITEMS.forEach(output::accept);
                                LogisticsRegistration.ITEMS.forEach(output::accept);
                                StorageRegistration.ITEMS.forEach(output::accept);
                                WaterWheelRegistration.ITEMS.forEach(output::accept);
                                EnvironmentMachineRegistration.allItems().forEach(output::accept);
                                output.accept(TradeRegistration.AUTO_TRADER_ITEM);
                                GadgetRegistration.ALL.forEach(output::accept);
                                WeaponRegistration.ALL.forEach(output::accept);
                                AdvancedToolRegistration.ALL.forEach(output::accept);
                            })
                            .build());

    private FoundationCreativeTabRegistration() {
    }
}
