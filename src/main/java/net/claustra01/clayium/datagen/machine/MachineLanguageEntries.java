package net.claustra01.clayium.datagen.machine;

import java.util.function.BiConsumer;
import net.claustra01.clayium.registry.MachineRegistration;

public final class MachineLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        String[] englishPrefixes = {"", "", "Dense Clay", "Simple", "Basic", "Advanced", "Precision", "", "", "", "Antimatter"};
        String[] japanesePrefixes = {"", "", "高密度粘土", "簡易", "基本", "発展", "精密", "", "", "", "反物質"};
        MachineRegistration.MACHINES.values().forEach(definition -> {
            String method = definition.series().recipeMethod().id();
            String name = switch (method) {
                case "condenser" -> locale.equals("ja_jp") ? "凝縮機" : "Condenser";
                case "grinder" -> locale.equals("ja_jp") ? "粉砕機" : "Grinder";
                case "decomposer" -> locale.equals("ja_jp") ? "分解機" : "Decomposer";
                default -> throw new IllegalStateException(method);
            };
            String prefix = locale.equals("ja_jp")
                    ? japanesePrefixes[definition.tier()] : englishPrefixes[definition.tier()];
            add.accept("block.clayium." + definition.id(), prefix + (locale.equals("ja_jp") ? "" : " ") + name);
        });
        add.accept("gui.clayium.machine.state.idle", locale.equals("ja_jp") ? "停止中" : "Idle");
        add.accept("gui.clayium.machine.state.running", locale.equals("ja_jp") ? "処理中" : "Processing");
        add.accept("gui.clayium.machine.state.output_full", locale.equals("ja_jp") ? "出力満杯" : "Output full");
        add.accept("gui.clayium.machine.state.waiting_for_energy", locale.equals("ja_jp") ? "CE不足" : "Waiting for CE");
        add.accept("gui.clayium.machine.state.recipe_changed", locale.equals("ja_jp") ? "レシピ更新待ち" : "Recipe changed");
    }

    private MachineLanguageEntries() {
    }
}
