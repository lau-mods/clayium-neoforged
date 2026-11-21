package net.claustra01.clayium.datagen.careactor;

import java.util.function.BiConsumer;

public final class CaReactorLanguageEntries {
    private static final String[] MARKS = {"i", "ii", "iii", "iv"};

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        for (int index = 0; index < MARKS.length; index++) {
            String mark = MARKS[index];
            String numeral = Integer.toString(index + 1);
            add.accept("block.clayium.ca_reactor_core_mk_" + mark,
                    japanese ? "巨大CA反応炉コア Mk " + numeral : "Large CA Reactor Core Mk " + numeral);
            add.accept("block.clayium.ca_reactor_coil_mk_" + mark,
                    japanese ? "CA反応炉コイル Mk " + numeral : "CA Reactor Coil Mk " + numeral);
        }
        for (int rank = 1; rank <= 10; rank++) {
            add.accept("block.clayium.ca_reactor_hull_rank_" + rank,
                    japanese ? "CA反応炉筐体 ランク" + rank : "CA Reactor Hull Rank " + rank);
        }
        add.accept("gui.clayium.ca_reactor.structure",
                japanese ? "構造: %1$s  コイル %2$s  筐体 %3$s" : "Structure: %1$s  Coils %2$s  Hulls %3$s");
        add.accept("gui.clayium.ca_reactor.rank",
                japanese ? "平均ランク: %1$s" : "Average rank: %1$s");
        add.accept("gui.clayium.ca_reactor.formed", japanese ? "有効" : "Formed");
        add.accept("gui.clayium.ca_reactor.incomplete", japanese ? "未完成" : "Incomplete");
    }

    private CaReactorLanguageEntries() {
    }
}
