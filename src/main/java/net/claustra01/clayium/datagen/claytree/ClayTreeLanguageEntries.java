package net.claustra01.clayium.datagen.claytree;

import java.util.Map;
import java.util.function.BiConsumer;

public final class ClayTreeLanguageEntries {
    private static final Map<String, String> ENGLISH = Map.of(
            "clay_tree_sapling", "Clay Tree Sapling",
            "clay_tree_leaf", "Clay Tree Leaves",
            "clay_tree_log", "Clay Tree Log");
    private static final Map<String, String> JAPANESE = Map.of(
            "clay_tree_sapling", "粘土樹の苗木",
            "clay_tree_leaf", "粘土樹の葉",
            "clay_tree_log", "粘土樹の原木");

    public static void add(String locale, BiConsumer<String, String> consumer) {
        (locale.equals("ja_jp") ? JAPANESE : ENGLISH)
                .forEach((id, name) -> consumer.accept("block.clayium." + id, name));
    }

    private ClayTreeLanguageEntries() {
    }
}
