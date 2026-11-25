package net.claustra01.clayium.client.screen.workbench;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class WorkbenchScreens {
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(WorkbenchRegistration.CLAY_WORK_TABLE_MENU.get(), ClayWorkTableScreen::new);
        event.register(WorkbenchRegistration.CLAY_CRAFTING_BOARD_MENU.get(), ClayCraftingBoardScreen::new);
    }

    private WorkbenchScreens() {
    }
}
