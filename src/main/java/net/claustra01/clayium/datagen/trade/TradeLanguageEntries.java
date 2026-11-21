package net.claustra01.clayium.datagen.trade;

import java.util.function.BiConsumer;
import net.claustra01.clayium.machine.trade.AutoTraderBlockEntity;

public final class TradeLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        add.accept("block.clayium.auto_trader", japanese ? "自動取引装置" : "Auto Trader");
        add.accept("gui.clayium.auto_trader.offers", japanese ? "取引一覧" : "Trades");
        add.accept("gui.clayium.auto_trader.uses", japanese ? "%1$s / %2$s 回" : "%1$s / %2$s uses");
        add.accept("gui.clayium.auto_trader.energy", japanese ? "取引CE: %1$s" : "Trade cost: %1$s");
        add.accept("gui.clayium.auto_trader.stored", japanese ? "蓄積: %1$s" : "Stored: %1$s");
        for (AutoTraderBlockEntity.State state : AutoTraderBlockEntity.State.values()) {
            String english = switch (state) {
                case READY -> "Ready";
                case TRADED -> "Trade completed";
                case NO_MERCHANT -> "No merchant above";
                case MERCHANT_BUSY -> "Merchant is busy";
                case NO_OFFERS -> "No available trades";
                case OUT_OF_STOCK -> "Selected trade is locked";
                case OUTPUT_FULL -> "Output full";
                case MISSING_INPUT -> "Missing required items";
                case WAITING_FOR_ENERGY -> "Waiting for CE";
            };
            String japaneseText = switch (state) {
                case READY -> "取引可能";
                case TRADED -> "取引完了";
                case NO_MERCHANT -> "上方に取引相手がいません";
                case MERCHANT_BUSY -> "取引相手は使用中です";
                case NO_OFFERS -> "取引がありません";
                case OUT_OF_STOCK -> "選択取引は上限です";
                case OUTPUT_FULL -> "出力満杯";
                case MISSING_INPUT -> "必要材料が不足しています";
                case WAITING_FOR_ENERGY -> "CE不足";
            };
            add.accept("gui.clayium.auto_trader.state." + state.name().toLowerCase(java.util.Locale.ROOT),
                    japanese ? japaneseText : english);
        }
    }

    private TradeLanguageEntries() {
    }
}
