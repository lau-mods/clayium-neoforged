package net.claustra01.clayium.datagen.logistics;

import java.util.function.BiConsumer;
import net.claustra01.clayium.registry.LogisticsRegistration;

public final class LogisticsLanguageEntries {
    private static final String[] ENGLISH_PREFIXES = {
            "", "", "", "", "Basic", "Advanced", "Precision", "Clay Steel", "Clayium", "Ultimate",
            "Antimatter", "Pure Antimatter", "OEC", "OPA"
    };
    private static final String[] JAPANESE_PREFIXES = {
            "", "", "", "", "基本", "発展", "精密", "粘土鋼", "クレイウム", "究極", "反物質", "純反物質", "OEC", "OPA"
    };

    public static String tierPrefix(String locale, int tier) {
        return (locale.equals("ja_jp") ? JAPANESE_PREFIXES : ENGLISH_PREFIXES)[tier];
    }

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        LogisticsRegistration.BUFFERS.values().forEach(definition -> add.accept(
                "block.clayium." + definition.id(),
                tierPrefix(locale, definition.tier())
                        + (japanese ? "アイテムバッファ" : " Item Buffer")));
        add.accept("item.clayium.white_list_item_filter", japanese ? "統合アイテムフィルター" : "Unified Item Filter");
        add.accept("item.clayium.item_filter_duplicator", japanese ? "フィルター複写器" : "Item Filter Duplicator");
        add.accept("gui.clayium.item_filter.whitelist", japanese ? "許可" : "White");
        add.accept("gui.clayium.item_filter.blacklist", japanese ? "拒否" : "Black");
        add.accept("tooltip.clayium.item_filter.mode.whitelist", japanese ? "モード: 許可" : "Mode: Allow list");
        add.accept("tooltip.clayium.item_filter.mode.blacklist", japanese ? "モード: 禁止" : "Mode: Deny list");
        add.accept("message.clayium.filter_copy.cleared", japanese ? "複写状態を解除しました" : "Copy state cleared");
        add.accept("message.clayium.filter_copy.rejected", japanese ? "上限を超えるため複写できません" : "Copy rejected: too many entries");
        add.accept("message.clayium.filter_copy.applied", japanese ? "フィルター設定を複写しました" : "Filter settings copied");
        add.accept("message.clayium.filter_copy.stored", japanese ? "フィルター設定を記憶しました" : "Filter settings stored");
        // Original GuiAreaMiner / GuiAreaActivator picture-button narration.
        add.accept("gui.clayium.area.start", japanese ? "開始" : "Start");
        add.accept("gui.clayium.area.stop", japanese ? "停止" : "Stop");
        add.accept("gui.clayium.area.loop", japanese ? "繰り返し" : "Loop");
        add.accept("gui.clayium.area.box", japanese ? "範囲表示" : "Area display");
        add.accept("gui.clayium.area.target", japanese ? "対象" : "Target");
        add.accept("gui.clayium.area.ray_trace", japanese ? "視線判定" : "Ray trace");
        add.accept("gui.clayium.area.sneak", japanese ? "スニーク" : "Sneak");
        add.accept("gui.clayium.area.filter.harvest", japanese ? "採掘" : "Harvest");
        add.accept("gui.clayium.area.filter.fortune", japanese ? "幸運" : "Fortune");
        add.accept("gui.clayium.area.filter.silk_touch", japanese ? "シルクタッチ" : "Silktouch");
        add.accept("tooltip.clayium.filter_copy.stored", japanese ? "複写設定を記憶済み" : "Copy stored");
        add.accept("tooltip.clayium.memory_card.copy", japanese ? "Shift+使用: 設定をコピー" : "Shift-use: copy settings");
        add.accept("tooltip.clayium.memory_card.paste", japanese ? "使用: 設定を貼り付け" : "Use: paste settings");
        add.accept("tooltip.clayium.memory_card.stored", japanese ? "バッファ設定を保存済み" : "Buffer settings stored");
        add.accept("message.clayium.buffer_configuration.copied", japanese ? "バッファ設定をコピーしました" : "Buffer settings copied");
        add.accept("message.clayium.buffer_configuration.pasted", japanese ? "バッファ設定を貼り付けました" : "Buffer settings pasted");
    }

    private LogisticsLanguageEntries() {
    }
}
