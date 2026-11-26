package net.claustra01.clayium.datagen.careactor;

import java.util.function.BiConsumer;

public final class CaReactorLanguageEntries {
    private static final String[] MARKS = {"i", "ii", "iii", "iv"};

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        for (int index = 0; index < MARKS.length; index++) {
            String mark = MARKS[index];
            String numeral = Integer.toString(index + 1);
            add.accept("block.clayium_neoforged.ca_reactor_core_mk_" + mark,
                    japanese ? "巨大CA反応炉コア Mk " + numeral : "Large CA Reactor Core Mk " + numeral);
            add.accept("block.clayium_neoforged.ca_reactor_coil_mk_" + mark,
                    japanese ? "CA反応炉コイル Mk " + numeral : "CA Reactor Coil Mk " + numeral);
        }
        for (int rank = 1; rank <= 10; rank++) {
            add.accept("block.clayium_neoforged.ca_reactor_hull_rank_" + rank,
                    japanese ? "CA反応炉筐体 ランク" + rank : "CA Reactor Hull Rank " + rank);
        }
        // Original gui.CAReactor.* entries.
        add.accept("gui.clayium_neoforged.ca_reactor.invalid", japanese ? "無効" : "Invalid");
        add.accept("gui.clayium_neoforged.ca_reactor.constructed", japanese ? "構築済" : "Constructed");
        add.accept("gui.clayium_neoforged.ca_reactor.rank_size",
                japanese ? "ランク %1$s  サイズ %2$s" : "Rank %1$s  Size %2$s");
        add.accept("gui.clayium_neoforged.ca_reactor.efficiency", japanese ? "効率 %1$s" : "Efficiency %1$s");
    }

    private CaReactorLanguageEntries() {
    }
}
