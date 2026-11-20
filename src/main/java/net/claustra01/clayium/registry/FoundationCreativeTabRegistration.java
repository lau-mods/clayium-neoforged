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
                                ClayProductionRegistration.ITEMS.forEach(output::accept);
                                FormingMachineRegistration.ITEMS.forEach(output::accept);
                                AssemblyMachineRegistration.ITEMS.forEach(output::accept);
                                EnergyConverterRegistration.ITEMS.forEach(output::accept);
                                FluidRegistration.ITEMS.forEach(output::accept);
                                output.accept(FluidRegistration.FLUID_FILTER);
                                LogisticsRegistration.BLOCK_ITEMS.forEach(output::accept);
                                output.accept(CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE_ITEM);
                                MultitrackRegistration.BLOCK_ITEMS.forEach(output::accept);
                                LogisticsRegistration.ITEMS.forEach(output::accept);
                                WaterWheelRegistration.ITEMS.forEach(output::accept);
                                EnvironmentMachineRegistration.allItems().forEach(output::accept);
                                GadgetRegistration.ALL.forEach(output::accept);
                                WeaponRegistration.ALL.forEach(output::accept);
                            })
                            .build());

    private FoundationCreativeTabRegistration() {
    }
}
