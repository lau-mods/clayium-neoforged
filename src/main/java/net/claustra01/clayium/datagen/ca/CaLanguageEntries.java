package net.claustra01.clayium.datagen.ca;

import java.util.Map;
import java.util.function.BiConsumer;

public final class CaLanguageEntries {
    private static final Map<Integer, String> ENGLISH_TIERS = Map.of(
            9, "", 10, "Antimatter ", 11, "Pure Antimatter ", 12, "OEC ", 13, "OPA ");
    private static final Map<Integer, String> JAPANESE_TIERS = Map.of(
            9, "", 10, "反物質 ", 11, "純反物質 ", 12, "OEC ", 13, "OPA ");

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        Map<Integer, String> prefixes = japanese ? JAPANESE_TIERS : ENGLISH_TIERS;
        int[] condenserTiers = {9, 10, 11};
        for (int tier : condenserTiers) {
            add.accept("block.clayium.ca_condenser_tier_" + tier,
                    prefixes.get(tier) + (japanese ? "CA 凝縮機" : "CA Condenser"));
        }
        int[] injectorTiers = {9, 10, 11, 12, 13};
        for (int tier : injectorTiers) {
            add.accept("block.clayium.ca_injector_tier_" + tier,
                    prefixes.get(tier) + (japanese ? "CA 注入機" : "CA Injector"));
        }
        add.accept("block.clayium.ca_resonating_collector",
                japanese ? "共鳴型反物質収集機" : "CA Resonating Collector");
        String[] ids = {"antimatter", "pure_antimatter", "oec", "opa"};
        String[] english = {"Antimatter", "Pure Antimatter", "OEC", "OPA"};
        String[] japaneseNames = {"反物質", "純反物質", "OEC", "OPA"};
        for (int index = 0; index < ids.length; index++) {
            String name = japanese ? japaneseNames[index] : english[index];
            add.accept("block.clayium." + ids[index] + "_resonator",
                    name + (japanese ? "共鳴装置" : " Resonator"));
            add.accept("block.clayium." + ids[index] + "_overclocker",
                    name + (japanese ? "オーバークロッカー" : " Overclocker"));
            add.accept("block.clayium." + ids[index] + "_energy_storage_upgrade",
                    name + (japanese ? "エネルギーストレージアップグレード" : " Energy Storage Upgrade"));
        }
    }

    private CaLanguageEntries() {
    }
}
