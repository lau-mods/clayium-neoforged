package net.claustra01.clayium.client.screen.gadget;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.gadget.GadgetEffects;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID, value = Dist.CLIENT)
public final class GadgetClientEvents {
    private static int previousUseDelay;
    private static int previousDestroyDelay;

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.gameMode == null || minecraft.screen != null) {
            previousUseDelay = 0;
            previousDestroyDelay = 0;
            return;
        }
        int interval = GadgetEffects.operationInterval(minecraft.player);
        if (interval > 0 && minecraft.options.keyUse.isDown()) {
            if (minecraft.rightClickDelay == 4 && previousUseDelay <= 1) {
                minecraft.rightClickDelay = interval;
            } else if (minecraft.rightClickDelay > interval) {
                minecraft.rightClickDelay = interval;
            }
        }
        if (interval > 0 && minecraft.options.keyAttack.isDown()) {
            if (minecraft.gameMode.destroyDelay == 5 && previousDestroyDelay <= 1) {
                minecraft.gameMode.destroyDelay = interval;
            } else if (minecraft.gameMode.destroyDelay > interval) {
                minecraft.gameMode.destroyDelay = interval;
            }
        }
        previousUseDelay = minecraft.rightClickDelay;
        previousDestroyDelay = minecraft.gameMode.destroyDelay;
    }

    private GadgetClientEvents() {
    }
}
