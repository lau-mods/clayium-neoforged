package net.claustra01.clayium.datagen.machine;

import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;

public final class MachineLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        String[] englishPrefixes = {
                "", "Clay", "Dense Clay", "Simple", "Basic", "Advanced", "Precision",
                "Clay Steel", "Clayium", "Ultimate", "Antimatter", "Pure Antimatter", "OEC", "OPA"
        };
        String[] japanesePrefixes = {
                "", "粘土", "高密度粘土", "簡易", "基本", "発展", "精密",
                "粘土鋼", "クレイウム", "究極", "反物質", "純反物質", "OEC", "OPA"
        };
        Stream.of(
                MachineRegistration.MACHINES.values().stream().map(definition ->
                        new MachineName(definition.id(), definition.tier(), definition.series().recipeMethod().id())),
                FormingMachineRegistration.MACHINES.values().stream().map(definition ->
                        new MachineName(definition.id(), definition.tier(), definition.series().recipeMethod().id())),
                AssemblyMachineRegistration.MACHINES.values().stream().map(definition ->
                        new MachineName(definition.id(), definition.tier(), definition.series().recipeMethod().id())))
                .flatMap(java.util.function.Function.identity())
                .filter(definition -> !definition.id().equals("clay_bending_machine"))
                .forEach(definition -> {
                    String name = switch (definition.method()) {
                        case "condenser" -> locale.equals("ja_jp") ? "凝縮機" : "Condenser";
                        case "grinder" -> locale.equals("ja_jp") ? "粉砕機" : "Grinder";
                        case "ec_decomposer" -> locale.equals("ja_jp") ? "エネルギー粘土分解機" : "Energetic Clay Decomposer";
                        case "decomposer" -> locale.equals("ja_jp") ? "分解機" : "Decomposer";
                        case "bending_machine" -> locale.equals("ja_jp") ? "プレート作成機" : "Bending Machine";
                        case "wire_drawing_machine" -> locale.equals("ja_jp") ? "ワイヤー圧延機" : "Wire Drawing Machine";
                        case "pipe_drawing_machine" -> locale.equals("ja_jp") ? "パイプ圧延機" : "Pipe Drawing Machine";
                        case "cutting_machine" -> locale.equals("ja_jp") ? "切断機" : "Cutting Machine";
                        case "lathe" -> locale.equals("ja_jp") ? "旋盤" : "Lathe";
                        case "milling_machine" -> locale.equals("ja_jp") ? "フライス盤" : "Milling Machine";
                        case "smelter" -> smelterName(locale, definition.tier());
                        case "alloy_smelter" -> locale.equals("ja_jp") ? "合金精錬機" : "Alloy Smelter";
                        case "electrolysis_reactor" -> electrolysisName(locale, definition.tier());
                        case "assembler" -> locale.equals("ja_jp") ? "組立機" : "Assembler";
                        case "inscriber" -> locale.equals("ja_jp") ? "印刷機" : "Inscriber";
                        case "centrifuge" -> locale.equals("ja_jp") ? "遠心分離機" : "Centrifuge";
                        case "chemical_reactor" -> chemicalReactorName(locale, definition.tier());
                        case "chemical_metal_separator" -> locale.equals("ja_jp")
                                ? "精密化学金属分離装置" : "Precision Chemical Metal Separator";
                        case "matter_transformer" -> matterTransformerName(locale, definition.tier());
                        default -> throw new IllegalStateException(definition.method());
                    };
                    if (definition.method().equals("ec_decomposer") || definition.method().equals("smelter") || definition.method().equals("alloy_smelter")
                            || definition.method().equals("electrolysis_reactor")
                            || definition.method().equals("chemical_reactor")
                            || definition.method().equals("chemical_metal_separator")
                            || definition.method().equals("matter_transformer")) {
                        add.accept("block.clayium_neoforged." + definition.id(), name);
                        return;
                    }
                    String prefix = locale.equals("ja_jp")
                            ? japanesePrefixes[definition.tier()] : englishPrefixes[definition.tier()];
                    add.accept("block.clayium_neoforged." + definition.id(),
                            prefix + (locale.equals("ja_jp") ? "" : " ") + name);
                });
        AssemblyMachineRegistration.MACHINE_HULL_DEFINITIONS.values().stream()
                .filter(definition -> definition.primary()
                        && definition.tier() >= 3 && definition.tier() <= 10)
                .forEach(definition -> {
                    String prefix = locale.equals("ja_jp")
                            ? japanesePrefixes[definition.tier()] : englishPrefixes[definition.tier()];
                    add.accept("block.clayium_neoforged." + definition.id(),
                            prefix + (locale.equals("ja_jp") ? "マシン筐体" : " Machine Hull"));
                });
        add.accept("gui.clayium_neoforged.machine.state.idle", locale.equals("ja_jp") ? "停止中" : "Idle");
        add.accept("gui.clayium_neoforged.machine.state.running", locale.equals("ja_jp") ? "処理中" : "Processing");
        add.accept("gui.clayium_neoforged.machine.state.output_full", locale.equals("ja_jp") ? "出力満杯" : "Output full");
        add.accept("gui.clayium_neoforged.machine.state.waiting_for_energy", locale.equals("ja_jp") ? "CE不足" : "Waiting for CE");
        add.accept("gui.clayium_neoforged.machine.state.recipe_changed", locale.equals("ja_jp") ? "レシピ更新待ち" : "Recipe changed");
        // Original gui.Common.* entries.
        add.accept("gui.clayium_neoforged.machine.energy", "%sCE");
        add.accept("gui.clayium_neoforged.machine.tier", "Tier %s");
        add.accept("gui.clayium_neoforged.machine.resonance", locale.equals("ja_jp") ? "共鳴率 %s" : "Resonance %s");
        add.accept("gui.clayium_neoforged.machine.overclock",
                locale.equals("ja_jp") ? "オーバークロック x%s" : "Overclock x%s");
        add.accept("gui.clayium_neoforged.machine.laser", locale.equals("ja_jp") ? "レーザー %s" : "Laser : %s");
        add.accept("gui.clayium_neoforged.machine.inventory", locale.equals("ja_jp") ? "持ち物" : "Inventory");
        add.accept("gui.clayium_neoforged.machine.empty", locale.equals("ja_jp") ? "空" : "Empty");
    }

    private record MachineName(String id, int tier, String method) {
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

    private static String chemicalReactorName(String locale, int tier) {
        if (locale.equals("ja_jp")) {
            return switch (tier) {
                case 4 -> "基本化学反応機";
                case 5 -> "発展化学反応機";
                case 8 -> "クレイウム化学反応機";
                default -> throw new IllegalArgumentException("Unknown chemical reactor tier: " + tier);
            };
        }
        return switch (tier) {
            case 4 -> "Basic Chemical Reactor";
            case 5 -> "Advanced Chemical Reactor";
            case 8 -> "Clayium Chemical Reactor";
            default -> throw new IllegalArgumentException("Unknown chemical reactor tier: " + tier);
        };
    }

    private static String matterTransformerName(String locale, int tier) {
        String[] english = {"Clay Steel", "Clayium", "Ultimate", "Antimatter", "Pure Antimatter", "OEC"};
        String[] japanese = {"粘土鋼", "クレイウム", "究極", "反物質", "純反物質", "OEC"};
        if (tier < 7 || tier > 12) {
            throw new IllegalArgumentException("Unknown matter transformer tier: " + tier);
        }
        return (locale.equals("ja_jp") ? japanese[tier - 7] + "物質変換装置"
                : english[tier - 7] + " Matter Transformer");
    }

    private MachineLanguageEntries() {
    }
}
