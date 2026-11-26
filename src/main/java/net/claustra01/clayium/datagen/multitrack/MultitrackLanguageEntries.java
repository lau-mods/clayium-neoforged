package net.claustra01.clayium.datagen.multitrack;

import java.util.function.BiConsumer;
import net.claustra01.clayium.datagen.logistics.LogisticsLanguageEntries;
import net.claustra01.clayium.registry.MultitrackRegistration;

public final class MultitrackLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean ja = locale.equals("ja_jp");
        MultitrackRegistration.MULTITRACK_BUFFERS.values().forEach(definition -> add.accept(
                "block.clayium_neoforged." + definition.id(), LogisticsLanguageEntries.tierPrefix(locale, definition.tier())
                        + (ja ? "複線アイテムバッファ" : " Multitrack Item Buffer")));
        MultitrackRegistration.DISTRIBUTORS.values().forEach(definition -> add.accept(
                "block.clayium_neoforged." + definition.id(), LogisticsLanguageEntries.tierPrefix(locale, definition.tier())
                        + (ja ? "分配機" : " Distributor")));
        add.accept("gui.clayium_neoforged.multitrack.track", ja ? "トラック %1$s: %2$s" : "Track %1$s: %2$s");
        add.accept("gui.clayium_neoforged.multitrack.filter", ja ? "個別フィルター" : "Track filter");
        add.accept("gui.clayium_neoforged.distributor.fair", ja ? "均等分配" : "Fair output");
    }
    private MultitrackLanguageEntries() { }
}
