package net.claustra01.clayium.client.screen;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MachineRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class MachineScreenRegistration {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(MachineRegistration.MACHINE_MENU.get(), ClayMachineScreen::new);
        event.register(net.claustra01.clayium.registry.EnvironmentMachineRegistration.COBBLESTONE_GENERATOR_MENU.get(),
                net.claustra01.clayium.client.screen.logistics.NormalInventoryScreen::new);
        event.register(net.claustra01.clayium.registry.CaRegistration.COLLECTOR_MENU.get(),
                net.claustra01.clayium.client.screen.logistics.NormalInventoryScreen::new);
        event.register(net.claustra01.clayium.registry.LaserRegistration.LASER_MENU.get(),
                net.claustra01.clayium.client.screen.legacy.ClayEnergyLaserScreen::new);
    }

    private MachineScreenRegistration() {
    }
}
