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
        add.accept("block.clayium.fluid_transfer_machine_tier_5",
                japanese ? "発展流体転送機" : "Advanced Fluid Transfer Machine");
        add.accept("item.clayium.fluid_filter", japanese ? "流体フィルター" : "Fluid Filter");
        add.accept("gui.clayium.fluid.empty", japanese ? "空" : "Empty");
        add.accept("gui.clayium.fluid.pending", japanese ? "保留: %s" : "Pending: %s");
        add.accept("gui.clayium.fluid.redstone.ignore", japanese ? "赤石:無視" : "RS:Any");
        add.accept("gui.clayium.fluid.redstone.powered", japanese ? "赤石:ON" : "RS:On");
        add.accept("gui.clayium.fluid.redstone.unpowered", japanese ? "赤石:OFF" : "RS:Off");
        add.accept("gui.clayium.fluid.filter.whitelist", japanese ? "許可" : "Allow");
        add.accept("gui.clayium.fluid.filter.blacklist", japanese ? "禁止" : "Deny");
        add.accept("gui.clayium.fluid.filter.set", japanese ? "設定" : "Set");
        add.accept("gui.clayium.fluid.filter.clear", japanese ? "消去" : "Clear");
        add.accept("gui.clayium.fluid.tier", japanese ? "Tier %s" : "Tier %s");
    }

    private FluidLanguageEntries() {
    }
}
