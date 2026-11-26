package net.claustra01.clayium.datagen.advancedtool;

import java.util.Map;
import java.util.function.BiConsumer;

public final class AdvancedToolLanguageEntries {
    private static final Map<String, String> ENGLISH = Map.ofEntries(
            Map.entry("item.clayium_neoforged.clay_shovel", "Clay Shovel"),
            Map.entry("item.clayium_neoforged.clay_steel_pickaxe", "Clay Steel Pickaxe"),
            Map.entry("item.clayium_neoforged.clay_steel_shovel", "Clay Steel Shovel"),
            Map.entry("tooltip.clayium_neoforged.clay_shovel", "Mines Clay Ores and increases their drops."),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode", "Mining mode: %1$s"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode.single", "Single block"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode.area", "%1$sx%2$s area"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode.remembered", "Remembered shape"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.pattern", "Remembered blocks: %1$s"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.durability", "Durability: %1$s / %2$s"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode_change", "Right-click to change mining mode."),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.pattern_help", "Use on a Clay Block to remember its surrounding shape."),
            Map.entry("message.clayium_neoforged.advanced_tool.mode", "Mining mode set to %1$s."),
            Map.entry("message.clayium_neoforged.advanced_tool.pattern_saved", "Remembered a shape of %1$s Clay Blocks."));
    private static final Map<String, String> JAPANESE = Map.ofEntries(
            Map.entry("item.clayium_neoforged.clay_shovel", "粘土シャベル"),
            Map.entry("item.clayium_neoforged.clay_steel_pickaxe", "粘土鋼ツルハシ"),
            Map.entry("item.clayium_neoforged.clay_steel_shovel", "粘土鋼シャベル"),
            Map.entry("tooltip.clayium_neoforged.clay_shovel", "粘土鉱石を採掘でき、ドロップ数を上昇させる。"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode", "採掘モード: %1$s"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode.single", "単体"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode.area", "%1$sx%2$s 範囲"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode.remembered", "記憶形状"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.pattern", "記憶ブロック数: %1$s"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.durability", "耐久値: %1$s / %2$s"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.mode_change", "右クリックで採掘モードを変更する。"),
            Map.entry("tooltip.clayium_neoforged.advanced_tool.pattern_help", "粘土ブロックに使用して形状を記憶する。"),
            Map.entry("message.clayium_neoforged.advanced_tool.mode", "採掘モードを%1$sに設定した。"),
            Map.entry("message.clayium_neoforged.advanced_tool.pattern_saved", "%1$s個の粘土ブロック形状を記憶した。"));

    public static void add(String locale, BiConsumer<String, String> output) {
        (locale.equals("ja_jp") ? JAPANESE : ENGLISH).forEach(output);
    }

    private AdvancedToolLanguageEntries() {
    }
}
