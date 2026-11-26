package net.claustra01.clayium.datagen.fluid;

import java.util.function.BiConsumer;
import net.claustra01.clayium.datagen.logistics.LogisticsLanguageEntries;
import net.claustra01.clayium.fluid.FluidTransportSpecification;

public final class FluidLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        FluidTransportSpecification.TRANSLATORS.forEach(specification -> add.accept(
                "block.clayium." + specification.id(),
                LogisticsLanguageEntries.tierPrefix(locale, specification.tier())
                        + (japanese ? "流体バッファ" : " Fluid Buffer")));
        add.accept("gui.clayium.fluid.empty", japanese ? "空" : "Empty");
    }

    private FluidLanguageEntries() {
    }
}
