package net.claustra01.clayium.datagen.environment;

import java.util.Map;

public final class EnvironmentLanguageEntries {
    private static final Map<String, String> ENGLISH = Map.ofEntries(
            Map.entry("clay_cobblestone_generator", "Clay Cobblestone Generator"),
            Map.entry("dense_clay_cobblestone_generator", "Dense Clay Cobblestone Generator"),
            Map.entry("simple_cobblestone_generator", "Simple Cobblestone Generator"),
            Map.entry("basic_cobblestone_generator", "Basic Cobblestone Generator"),
            Map.entry("advanced_cobblestone_generator", "Advanced Cobblestone Generator"),
            Map.entry("precision_cobblestone_generator", "Precision Cobblestone Generator"),
            Map.entry("clay_steel_cobblestone_generator", "Clay Steel Cobblestone Generator"),
            Map.entry("salt_extractor_tier_4", "Basic Salt Extractor"),
            Map.entry("salt_extractor_tier_5", "Advanced Salt Extractor"),
            Map.entry("salt_extractor_tier_6", "Precision Salt Extractor"),
            Map.entry("salt_extractor_tier_7", "Clay Steel Salt Extractor"),
            Map.entry("quartz_crucible", "Quartz Crucible"));
    private static final Map<String, String> JAPANESE = Map.ofEntries(
            Map.entry("clay_cobblestone_generator", "粘土丸石製造機"),
            Map.entry("dense_clay_cobblestone_generator", "高密度粘土丸石製造機"),
            Map.entry("simple_cobblestone_generator", "簡易丸石製造機"),
            Map.entry("basic_cobblestone_generator", "基本丸石製造機"),
            Map.entry("advanced_cobblestone_generator", "発展丸石製造機"),
            Map.entry("precision_cobblestone_generator", "精密丸石製造機"),
            Map.entry("clay_steel_cobblestone_generator", "粘土鋼丸石製造機"),
            Map.entry("salt_extractor_tier_4", "基本塩析出装置"),
            Map.entry("salt_extractor_tier_5", "発展塩析出装置"),
            Map.entry("salt_extractor_tier_6", "精密塩析出装置"),
            Map.entry("salt_extractor_tier_7", "粘土鋼塩析出装置"),
            Map.entry("quartz_crucible", "クォーツるつぼ"));

    public static Map<String, String> forLocale(String locale) {
        return locale.equals("ja_jp") ? JAPANESE : ENGLISH;
    }

    private EnvironmentLanguageEntries() {
    }
}
