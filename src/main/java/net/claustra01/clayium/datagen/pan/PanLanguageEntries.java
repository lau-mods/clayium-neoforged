package net.claustra01.clayium.datagen.pan;

import java.util.function.BiConsumer;

public final class PanLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        add.accept("block.clayium.pan_cable", japanese ? "PANケーブル" : "PAN Cable");
        add.accept("block.clayium.pan_core", japanese ? "PANコア" : "PAN Core");
        String[] prefixes = japanese
                ? new String[] {"基礎", "発展", "精密", "粘土鋼", "クレイウム", "究極", "反物質", "純反物質", "OEC", "OPA"}
                : new String[] {"Basic", "Advanced", "Precision", "Clay Steel", "Clayium", "Ultimate", "Antimatter", "Pure Antimatter", "OEC", "OPA"};
        for (int tier = 10; tier <= 13; tier++) {
            add.accept("block.clayium.pan_adapter_tier_" + tier,
                    prefixes[tier - 4] + (japanese ? " PANアダプター" : " PAN Adapter"));
        }
        for (int tier = 4; tier <= 13; tier++) {
            add.accept("block.clayium.pan_duplicator_tier_" + tier,
                    prefixes[tier - 4] + (japanese ? " PAN複製機" : " PAN Duplicator"));
        }
        add.accept("gui.clayium.pan.tier", "Tier %s");
        add.accept("gui.clayium.pan.reachable", japanese ? "見本: 複製可能" : "Sample: available");
        add.accept("gui.clayium.pan.unreachable", japanese ? "見本: 複製不可" : "Sample: unavailable");
        add.accept("gui.clayium.pan.energy", japanese ? "必要CE: %s" : "Required: %s");
        add.accept("gui.clayium.pan.progress", japanese ? "進捗: %s / %s" : "Progress: %s / %s");
        add.accept("gui.clayium.pan.network", japanese ? "ノード %s / アダプター %s" : "Nodes %s / Adapters %s");
        add.accept("gui.clayium.pan.sort.0", japanese ? "ID順" : "ID");
        add.accept("gui.clayium.pan.sort.1", japanese ? "原価順" : "Cost");
        add.accept("gui.clayium.pan.sort.2", japanese ? "CE順" : "CE");
        add.accept("gui.clayium.pan.ascending", japanese ? "昇順" : "Asc");
        add.accept("gui.clayium.pan.descending", japanese ? "降順" : "Desc");
        add.accept("gui.clayium.pan.prohibit", japanese ? "禁止" : "Ban");
        add.accept("gui.clayium.pan.cost", japanese ? "原価: %s" : "Cost: %s");
        add.accept("gui.clayium.pan.consumption", japanese ? "複製CE: %s" : "Duplication: %s");
        add.accept("gui.clayium.pan.prohibited", japanese ? "複製禁止" : "Prohibited");
        add.accept("gui.clayium.pan.permitted", japanese ? "複製可能" : "Permitted");
    }

    private PanLanguageEntries() {
    }
}
