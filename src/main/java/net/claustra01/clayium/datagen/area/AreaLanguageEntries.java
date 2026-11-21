package net.claustra01.clayium.datagen.area;

import java.util.Map;
import java.util.function.BiConsumer;

public final class AreaLanguageEntries {
    private static final Map<String, String> ENGLISH = Map.ofEntries(
            Map.entry("clay_marker", "Clay Marker"),
            Map.entry("clay_marker_open_pit", "Open Pit Marker"),
            Map.entry("clay_marker_ground_leveling", "Ground Leveling Marker"),
            Map.entry("clay_marker_prism", "Prism Marker"),
            Map.entry("area_item_collector", "Area Item Collector"),
            Map.entry("area_miner", "Area Miner"),
            Map.entry("advanced_area_miner", "Advanced Area Miner"),
            Map.entry("area_replacer", "Area Replacer"),
            Map.entry("area_activator", "Area Activator"));
    private static final Map<String, String> JAPANESE = Map.ofEntries(
            Map.entry("clay_marker", "粘土マーカー"),
            Map.entry("clay_marker_open_pit", "露天掘マーカー"),
            Map.entry("clay_marker_ground_leveling", "整地マーカー"),
            Map.entry("clay_marker_prism", "角柱マーカー"),
            Map.entry("area_item_collector", "範囲収集機"),
            Map.entry("area_miner", "範囲採掘機"),
            Map.entry("advanced_area_miner", "発展型範囲採掘機"),
            Map.entry("area_replacer", "範囲置換機"),
            Map.entry("area_activator", "範囲代行機"));

    public static void add(String locale, BiConsumer<String, String> add) {
        (locale.equals("ja_jp") ? JAPANESE : ENGLISH)
                .forEach((id, name) -> add.accept("block.clayium." + id, name));
    }

    private AreaLanguageEntries() {
    }
}
