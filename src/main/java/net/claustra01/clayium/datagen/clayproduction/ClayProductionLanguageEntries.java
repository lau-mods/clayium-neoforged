package net.claustra01.clayium.datagen.clayproduction;

import java.util.Map;
import java.util.function.BiConsumer;

public final class ClayProductionLanguageEntries {
    private static final Map<String, String> ENGLISH = Map.ofEntries(
            Map.entry("energetic_clay_condenser_mk_i", "Energetic Clay Condenser Mk I"),
            Map.entry("energetic_clay_condenser_mk_ii", "Energetic Clay Condenser Mk II"),
            Map.entry("automatic_clay_condenser_mk_i", "Automatic Clay Condenser Mk I"),
            Map.entry("automatic_clay_condenser_mk_ii", "Automatic Clay Condenser Mk II"),
            Map.entry("solar_clay_fabricator_mk_i", "Solar Clay Fabricator Mk I"),
            Map.entry("solar_clay_fabricator_mk_ii", "Solar Clay Fabricator Mk II"),
            Map.entry("lithium_solar_clay_fabricator", "Lithium Solar Clay Fabricator"),
            Map.entry("clay_fabricator_mk_i", "Clay Fabricator Mk I"),
            Map.entry("clay_fabricator_mk_ii", "Clay Fabricator Mk II"),
            Map.entry("clay_fabricator_mk_iii", "Clay Fabricator Mk III"));
    private static final Map<String, String> JAPANESE = Map.ofEntries(
            Map.entry("energetic_clay_condenser_mk_i", "エネルギー粘土凝縮機 Mk I"),
            Map.entry("energetic_clay_condenser_mk_ii", "エネルギー粘土凝縮機 Mk II"),
            Map.entry("automatic_clay_condenser_mk_i", "全自動粘土圧縮機 Mk I"),
            Map.entry("automatic_clay_condenser_mk_ii", "全自動粘土圧縮機 Mk II"),
            Map.entry("solar_clay_fabricator_mk_i", "ソーラー粘土製造機 Mk I"),
            Map.entry("solar_clay_fabricator_mk_ii", "ソーラー粘土製造機 Mk II"),
            Map.entry("lithium_solar_clay_fabricator", "リチウムソーラー粘土製造機"),
            Map.entry("clay_fabricator_mk_i", "粘土製造機 Mk I"),
            Map.entry("clay_fabricator_mk_ii", "粘土製造機 Mk II"),
            Map.entry("clay_fabricator_mk_iii", "粘土製造機 Mk III"));

    public static void add(String locale, BiConsumer<String, String> add) {
        (locale.equals("ja_jp") ? JAPANESE : ENGLISH)
                .forEach((id, name) -> add.accept("block.clayium_neoforged." + id, name));
        boolean japanese = locale.equals("ja_jp");
        add.accept("gui.clayium_neoforged.clay_production.stage", japanese ? "段階: %1$s" : "Stage: %1$s");
        add.accept("gui.clayium_neoforged.clay_production.remaining", japanese ? "残り: %1$s t" : "Left: %1$s t");
        add.accept("gui.clayium_neoforged.clay_production.state.idle", japanese ? "待機中" : "Idle");
        add.accept("gui.clayium_neoforged.clay_production.state.running", japanese ? "処理中" : "Processing");
        add.accept("gui.clayium_neoforged.clay_production.state.output_full", japanese ? "出力満杯" : "Output full");
        add.accept("gui.clayium_neoforged.clay_production.state.waiting_for_energy", japanese ? "CE不足" : "Waiting for CE");
        add.accept("gui.clayium_neoforged.clay_production.state.sky_blocked", japanese ? "上面が遮蔽" : "Sky blocked");
        add.accept("gui.clayium_neoforged.clay_production.state.invalid_input", japanese ? "対象外の入力" : "Invalid input");
    }

    private ClayProductionLanguageEntries() {
    }
}
