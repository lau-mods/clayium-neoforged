package net.claustra01.clayium.datagen.logistics;

import java.util.Locale;
import java.util.function.BiConsumer;
import net.claustra01.clayium.logistics.filter.ItemFilterRule;
import net.claustra01.clayium.registry.LogisticsRegistration;

public final class LogisticsLanguageEntries {
    private static final String[] ENGLISH_PREFIXES = {
            "", "", "", "", "Basic", "Advanced", "Precision", "Clay Steel", "Clayium", "Ultimate",
            "Antimatter", "Pure Antimatter", "OEC", "OPA"
    };
    private static final String[] JAPANESE_PREFIXES = {
            "", "", "", "", "基本", "発展", "精密", "粘土鋼", "クレイウム", "究極", "反物質", "純反物質", "OEC", "OPA"
    };

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        LogisticsRegistration.BUFFERS.values().forEach(definition -> add.accept(
                "block.clayium." + definition.id(),
                (japanese ? JAPANESE_PREFIXES[definition.tier()] : ENGLISH_PREFIXES[definition.tier()])
                        + (japanese ? "アイテムバッファ" : " Item Buffer")));
        add.accept("item.clayium.white_list_item_filter", japanese ? "統合アイテムフィルター" : "Unified Item Filter");
        add.accept("gui.clayium.buffer.mode.whitelist", japanese ? "許可" : "Allow");
        add.accept("gui.clayium.buffer.mode.blacklist", japanese ? "禁止" : "Deny");
        add.accept("gui.clayium.buffer.redstone.ignore", japanese ? "赤石:無視" : "RS:Any");
        add.accept("gui.clayium.buffer.redstone.powered", japanese ? "赤石:ON" : "RS:On");
        add.accept("gui.clayium.buffer.redstone.unpowered", japanese ? "赤石:OFF" : "RS:Off");
        add.accept("gui.clayium.buffer.conditions", japanese ? "条件: %s" : "Rules: %s");
        add.accept("gui.clayium.buffer.filter_slot", japanese ? "フィルター" : "Filter");
        for (ItemFilterRule.MatchType type : ItemFilterRule.MatchType.values()) {
            String english = switch (type) {
                case FUZZY -> "Fuzzy";
                case TAG -> "Tag";
                case DISPLAY_NAME -> "Name";
                case INTERNAL_ID -> "ID";
                case MOD_ID -> "Mod";
                case DURABILITY -> "Damage";
                case COMPONENTS -> "Data";
                case NESTED -> "Nested";
            };
            String japaneseName = switch (type) {
                case FUZZY -> "ファジー";
                case TAG -> "タグ";
                case DISPLAY_NAME -> "名前";
                case INTERNAL_ID -> "内部ID";
                case MOD_ID -> "Mod ID";
                case DURABILITY -> "耐久値";
                case COMPONENTS -> "データ";
                case NESTED -> "入れ子";
            };
            add.accept("gui.clayium.buffer.rule." + type.name().toLowerCase(Locale.ROOT), japanese ? japaneseName : english);
        }
        add.accept("tooltip.clayium.item_filter.mode.whitelist", japanese ? "モード: 許可" : "Mode: Allow list");
        add.accept("tooltip.clayium.item_filter.mode.blacklist", japanese ? "モード: 禁止" : "Mode: Deny list");
        add.accept("tooltip.clayium.item_filter.rules", japanese ? "条件数: %s" : "Rules: %s");
        add.accept("tooltip.clayium.memory_card.copy", japanese ? "Shift+使用: 設定をコピー" : "Shift-use: copy settings");
        add.accept("tooltip.clayium.memory_card.paste", japanese ? "使用: 設定を貼り付け" : "Use: paste settings");
        add.accept("tooltip.clayium.memory_card.stored", japanese ? "バッファ設定を保存済み" : "Buffer settings stored");
        add.accept("message.clayium.buffer_configuration.copied", japanese ? "バッファ設定をコピーしました" : "Buffer settings copied");
        add.accept("message.clayium.buffer_configuration.pasted", japanese ? "バッファ設定を貼り付けました" : "Buffer settings pasted");
    }

    private LogisticsLanguageEntries() {
    }
}
