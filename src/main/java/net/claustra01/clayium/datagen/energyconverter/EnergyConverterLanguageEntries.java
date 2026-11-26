package net.claustra01.clayium.datagen.energyconverter;

import java.util.List;
import java.util.function.BiConsumer;
import net.claustra01.clayium.energy.fe.EnergyConverterSpecification;

public final class EnergyConverterLanguageEntries {
    private static final List<String> ENGLISH = List.of(
            "CE-FE Converter", "Advanced CE-FE Converter", "Precision CE-FE Converter",
            "Clay Steel CE-FE Converter", "Clayium CE-FE Converter", "Ultimate CE-FE Converter",
            "Antimatter CE-FE Converter", "Pure Antimatter CE-FE Converter",
            "OEC CE-FE Converter", "OPA CE-FE Converter");
    private static final List<String> JAPANESE = List.of(
            "CE-FE変換器", "発展CE-FE変換器", "精密CE-FE変換器", "粘土鋼CE-FE変換器",
            "クレイウムCE-FE変換器", "究極CE-FE変換器", "反物質CE-FE変換器",
            "純反物質CE-FE変換器", "OEC CE-FE変換器", "OPA CE-FE変換器");

    public static void add(String locale, BiConsumer<String, String> add) {
        List<String> names = locale.equals("ja_jp") ? JAPANESE : ENGLISH;
        for (int index = 0; index < EnergyConverterSpecification.VALUES.size(); index++) {
            add.accept("block.clayium_neoforged." + EnergyConverterSpecification.VALUES.get(index).id(), names.get(index));
        }
        add.accept("gui.clayium_neoforged.converter.stored_ce", locale.equals("ja_jp") ? "蓄積 %s" : "Stored %s");
        add.accept("gui.clayium_neoforged.converter.stored_fe", locale.equals("ja_jp") ? "FE %s / %s" : "FE %s / %s");
        add.accept("gui.clayium_neoforged.converter.generation", locale.equals("ja_jp") ? "生成 %s FE/t" : "+%s FE/t");
        add.accept("gui.clayium_neoforged.converter.output", locale.equals("ja_jp") ? "出力 %s FE/t" : "Out %s FE/t");
    }

    private EnergyConverterLanguageEntries() {
    }
}
