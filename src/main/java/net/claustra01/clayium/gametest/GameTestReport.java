package net.claustra01.clayium.gametest;

import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import net.claustra01.clayium.ClayiumMod;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.gametest.framework.JUnitLikeTestReporter;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;

@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class GameTestReport {
    @SubscribeEvent
    public static void configure(RegisterGameTestsEvent event) {
        if (!GameTestHooks.isGametestServer()) {
            return;
        }
        String reportPath = System.getProperty("clayium.gametestReport");
        if (reportPath == null) {
            return;
        }
        File report = new File(reportPath);
        File parent = report.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try {
            GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(report));
        } catch (ParserConfigurationException exception) {
            throw new IllegalStateException("Could not create the GameTest report", exception);
        }
    }

    private GameTestReport() {
    }
}
