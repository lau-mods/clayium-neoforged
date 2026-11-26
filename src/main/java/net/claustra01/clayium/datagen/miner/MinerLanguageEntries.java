package net.claustra01.clayium.datagen.miner;

import java.util.function.BiConsumer;

public final class MinerLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        add.accept("block.clayium_neoforged.block_breaker", japanese ? "採掘機" : "Block Breaker");
        add.accept("block.clayium_neoforged.activator", japanese ? "右クリック代行機" : "Activator");
    }

    private MinerLanguageEntries() {
    }
}
