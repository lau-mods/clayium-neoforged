package net.claustra01.clayium.datagen.compat;

import java.util.function.BiConsumer;
import java.util.Locale;
import net.claustra01.clayium.datagen.machine.MachineLanguageEntries;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public final class CompatLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        java.util.Map<String, String> machineNames = new java.util.HashMap<>();
        MachineLanguageEntries.add(locale, machineNames::put);
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            String title = machineNames.getOrDefault("block.clayium." + method.id() + "_tier_4",
                    localizedName(method, japanese));
            add.accept("gui.clayium.recipe." + method.id(), title);
            add.accept("emi.category.clayium." + method.id(), title);
        }
        add.accept("gui.clayium.recipe.tier", japanese ? "必要Tier: %s" : "Required Tier: %s");
        add.accept("gui.clayium.recipe.time", japanese ? "Tier %1$s 時間: %2$s tick" : "Tier %1$s time: %2$s ticks");
        add.accept("gui.clayium.recipe.energy", japanese ? "Tier %1$s 消費: %2$s CE" : "Tier %1$s energy: %2$s CE");
        add.accept("gui.clayium.recipe.fluid_input", japanese ? "液体入力: %s mB" : "Fluid input: %s mB");
        add.accept("gui.clayium.recipe.fluid_output", japanese ? "液体出力: %s mB" : "Fluid output: %s mB");
        add.accept("gui.clayium.recipe.structure", japanese ? "構造倍率は稼働時に適用" : "Structure multipliers apply at runtime");
        add.accept("gui.clayium.recipe.pan_duplication", japanese ? "PAN複製" : "PAN Duplication");
        add.accept("emi.category.clayium.pan_duplication", japanese ? "PAN複製" : "PAN Duplication");
        add.accept("gui.clayium.recipe.pan_energy", japanese ? "合計CEはPANコアの複製費用" : "Total CE is the PAN core duplication cost");
        add.accept("gui.clayium.recipe.pan_sample", japanese ? "任意の見本1個を保持、反物質1個を消費" : "Any sample is retained; one antimatter is consumed");
        add.accept("gui.clayium.recipe.pan_rate", japanese ? "Tier %1$s: %2$s CE/t" : "Tier %1$s: %2$s CE/t");
        add.accept("gui.clayium.recipe.pan_ticks", japanese ? "処理時間 = ceil(合計CE ÷ CE/t)" : "Time = ceil(total CE / CE/t)");
        add.accept("gui.clayium.recipe.work_operation", japanese ? "操作番号: %s" : "Operation: %s");
        add.accept("gui.clayium.recipe.work_clicks", japanese ? "必要操作回数: %s" : "Clicks: %s");
        add.accept("gui.clayium.recipe.work_tool.none", japanese ? "工具不要" : "No tool");
        add.accept("gui.clayium.recipe.work_tool.rolling_pin", japanese ? "粘土めん棒" : "Clay rolling pin");
        add.accept("gui.clayium.recipe.work_tool.slicer_or_spatula", japanese ? "粘土スライサー／へら" : "Clay slicer or spatula");
        add.accept("gui.clayium.recipe.work_tool.spatula", japanese ? "粘土へら" : "Clay spatula");
    }


    private static String localizedName(ClayiumRecipeMethod method, boolean japanese) {
        if (japanese) {
            return switch (method) {
                case CLAY_WORK_TABLE -> "粘土作業台";
                case BENDING_MACHINE -> "プレート作成機";
                case WIRE_DRAWING_MACHINE -> "ワイヤー圧延機";
                case PIPE_DRAWING_MACHINE -> "パイプ圧延機";
                case CUTTING_MACHINE -> "切断機";
                case LATHE -> "旋盤";
                case MILLING_MACHINE -> "フライス盤";
                case CONDENSER -> "凝縮機";
                case GRINDER -> "粉砕機";
                case DECOMPOSER -> "分解機";
                case EC_CONDENSER -> "エネルギー粘土凝縮機";
                case ASSEMBLER -> "組立機";
                case INSCRIBER -> "印刷機";
                case CENTRIFUGE -> "遠心分離機";
                case SMELTER -> "精錬機";
                case CHEMICAL_REACTOR -> "化学反応機";
                case SALT_EXTRACTOR -> "塩析出装置";
                case CHEMICAL_METAL_SEPARATOR -> "化学金属分離装置";
                case ALLOY_SMELTER -> "合金精錬機";
                case BLAST_FURNACE -> "粘土高炉";
                case ELECTROLYSIS_REACTOR -> "電気精錬機";
                case REACTOR -> "粘土反応炉";
                case MATTER_TRANSFORMER -> "物質変換装置";
                case CA_CONDENSER -> "CA凝縮機";
                case CA_INJECTOR -> "CA注入機";
                case CA_REACTOR -> "CA反応炉";
                case EC_DECOMPOSER -> "エネルギー粘土分解機";
                case FLUID_TRANSFER_MACHINE -> "流体転移装置";
            };
        }
        StringBuilder name = new StringBuilder();
        for (String word : method.id().split("_")) {
            if (!name.isEmpty()) {
                name.append(' ');
            }
            if (word.equals("ca") || word.equals("ec")) {
                name.append(word.toUpperCase(Locale.ROOT));
            } else {
                name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return name.toString();
    }

    private CompatLanguageEntries() {
    }
}
