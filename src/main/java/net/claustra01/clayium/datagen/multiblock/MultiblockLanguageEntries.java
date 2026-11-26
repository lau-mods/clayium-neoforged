package net.claustra01.clayium.datagen.multiblock;

import java.util.Map;

public final class MultiblockLanguageEntries {
    public static Map<String, String> forLocale(String locale) {
        if (locale.equals("ja_jp")) {
            return Map.of(
                    "block.clayium.clay_blast_furnace", "粘土高炉",
                    "block.clayium.clay_reactor", "粘土反応炉");
        }
        return Map.of(
                "block.clayium.clay_blast_furnace", "Clay Blast Furnace",
                "block.clayium.clay_reactor", "Clay Reactor");
    }

    private MultiblockLanguageEntries() {
    }
}
