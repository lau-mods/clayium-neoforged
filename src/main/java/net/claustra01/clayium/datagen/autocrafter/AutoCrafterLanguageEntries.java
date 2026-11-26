package net.claustra01.clayium.datagen.autocrafter;

import java.util.Map;
import java.util.function.BiConsumer;

public final class AutoCrafterLanguageEntries {
    private static final Map<Integer, String> ENGLISH = Map.of(
            5, "Advanced Auto Crafter",
            6, "Precision Auto Crafter",
            7, "Clay Steel Auto Crafter",
            8, "Clayium Auto Crafter",
            9, "Ultimate Auto Crafter");
    private static final Map<Integer, String> JAPANESE = Map.of(
            5, "発展自動クラフト装置",
            6, "精密自動クラフト装置",
            7, "粘土鋼自動クラフト装置",
            8, "クレイウム自動クラフト装置",
            9, "究極自動クラフト装置");

    public static void add(String locale, BiConsumer<String, String> add) {
        Map<Integer, String> names = locale.equals("ja_jp") ? JAPANESE : ENGLISH;
        net.claustra01.clayium.registry.AutoCrafterRegistration.AUTO_CRAFTERS.forEach((tier, definition) ->
                add.accept("block.clayium_neoforged." + definition.id(), names.get(tier)));
        add.accept("gui.clayium_neoforged.auto_crafter.pattern", locale.equals("ja_jp") ? "レシピ" : "Pattern");
        add.accept("gui.clayium_neoforged.auto_crafter.materials", locale.equals("ja_jp") ? "材料" : "Materials");
        add.accept("gui.clayium_neoforged.auto_crafter.outputs", locale.equals("ja_jp") ? "出力" : "Outputs");
        add.accept("gui.clayium_neoforged.auto_crafter.cycle",
                locale.equals("ja_jp") ? "%1$s tickごとに最大%2$s回" : "Up to %2$s craft(s) / %1$s tick(s)");
    }

    private AutoCrafterLanguageEntries() {
    }
}
