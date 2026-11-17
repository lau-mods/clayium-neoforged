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
                case "smelter" -> smelterName(locale, definition.tier());
                case "alloy_smelter" -> locale.equals("ja_jp") ? "合金精錬機" : "Alloy Smelter";
                case "electrolysis_reactor" -> electrolysisName(locale, definition.tier());
                default -> throw new IllegalStateException(method);
            };
            if (method.equals("smelter") || method.equals("alloy_smelter")
                    || method.equals("electrolysis_reactor")) {
                add.accept("block.clayium." + definition.id(), name);
                return;
            }
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

    private static String smelterName(String locale, int tier) {
        if (locale.equals("ja_jp")) {
            return switch (tier) {
                case 4 -> "精錬機";
                case 5 -> "発展精錬機";
                case 6 -> "精密精錬機";
                case 7 -> "粘土鋼精錬機";
                case 8 -> "クレイウム精錬機";
                case 9 -> "究極精錬機";
                default -> throw new IllegalArgumentException("Unknown smelter tier: " + tier);
            };
        }
        return switch (tier) {
            case 4 -> "Basic Smelter";
            case 5 -> "Advanced Smelter";
            case 6 -> "Precision Smelter";
            case 7 -> "Clay Steel Smelter";
            case 8 -> "Clayium Smelter";
            case 9 -> "Ultimate Smelter";
            default -> throw new IllegalArgumentException("Unknown smelter tier: " + tier);
        };
    }

    private static String electrolysisName(String locale, int tier) {
        if (locale.equals("ja_jp")) {
            return switch (tier) {
                case 6 -> "電気精錬機";
                case 7 -> "粘土鋼電気精錬機";
                case 8 -> "クレイウム電気精錬機";
                case 9 -> "究極電気精錬機";
                default -> throw new IllegalArgumentException("Unknown electrolysis tier: " + tier);
            };
        }
        return switch (tier) {
            case 6 -> "Electrolysis Reactor";
            case 7 -> "Clay Steel Electrolysis Reactor";
            case 8 -> "Clayium Electrolysis Reactor";
            case 9 -> "Ultimate Electrolysis Reactor";
            default -> throw new IllegalArgumentException("Unknown electrolysis tier: " + tier);
        };
    }

    private MachineLanguageEntries() {
    }
}
