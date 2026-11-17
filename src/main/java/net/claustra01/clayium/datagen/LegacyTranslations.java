package net.claustra01.clayium.datagen;

import java.util.Map;

public final class LegacyTranslations {
    public static final Map<String, String> ENGLISH = Map.ofEntries(
            Map.entry("itemGroup.clayium", "Clayium"),
            Map.entry("gui.clayium.energy", "%1$sCE"),
            Map.entry("tooltip.clayium.tier", "Tier %1$d"),
            Map.entry("block.clayium.dense_clay", "Dense Clay"),
            Map.entry("block.clayium.compressed_clay", "Compressed Clay"),
            Map.entry("block.clayium.clay_ore", "Clay Ore"),
            Map.entry("block.clayium.deepslate_clay_ore", "Deepslate Clay Ore"),
            Map.entry("block.clayium.dense_clay_ore", "Dense Clay Ore"),
            Map.entry("block.clayium.deepslate_dense_clay_ore", "Deepslate Dense Clay Ore"),
            Map.entry("block.clayium.large_dense_clay_ore", "Large Dense Clay Ore"),
            Map.entry("block.clayium.deepslate_large_dense_clay_ore", "Deepslate Large Dense Clay Ore"),
            Map.entry("block.clayium.clayium_block", "Clayium Block"),
            Map.entry("block.clayium.clay_bending_machine", "Clay Bending Machine"),
            Map.entry("tooltip.clayium.clay_bending_machine.line1", "The first machine."),
            Map.entry("item.clayium.industrial_clay_dust", "Industrial Clay Dust"),
            Map.entry("item.clayium.clay_wrench", "Clay Wrench"),
            Map.entry("tooltip.clayium.clay_wrench.line1", "Rotates machines."),
            Map.entry("item.clayium.clay_pickaxe", "Clay Pickaxe"),
            Map.entry("tooltip.clayium.clay_pickaxe.line1", "Harvests Clay Ores quickly"),
            Map.entry("tooltip.clayium.clay_pickaxe.line2", "and increases their drops."),
            Map.entry("item.clayium.gadget_holder", "Clay Gadget Holder"),
            Map.entry("tooltip.clayium.gadget_holder.line1", "Clay Gadgets give effects"),
            Map.entry("tooltip.clayium.gadget_holder.line2", "when they are put in this.")
    );

    public static final Map<String, String> JAPANESE = Map.ofEntries(
            Map.entry("itemGroup.clayium", "Clayium"),
            Map.entry("gui.clayium.energy", "%1$sCE"),
            Map.entry("tooltip.clayium.tier", "Tier %1$d"),
            Map.entry("block.clayium.dense_clay", "高密度粘土"),
            Map.entry("block.clayium.compressed_clay", "圧縮粘土"),
            Map.entry("block.clayium.clay_ore", "粘土鉱石"),
            Map.entry("block.clayium.deepslate_clay_ore", "深層岩の粘土鉱石"),
            Map.entry("block.clayium.dense_clay_ore", "高密度粘土鉱石"),
            Map.entry("block.clayium.deepslate_dense_clay_ore", "深層岩の高密度粘土鉱石"),
            Map.entry("block.clayium.large_dense_clay_ore", "大高密度粘土鉱石"),
            Map.entry("block.clayium.deepslate_large_dense_clay_ore", "深層岩の大高密度粘土鉱石"),
            Map.entry("block.clayium.clayium_block", "クレイウムブロック"),
            Map.entry("block.clayium.clay_bending_machine", "粘土プレート作成機"),
            Map.entry("tooltip.clayium.clay_bending_machine.line1", "初めての機械"),
            Map.entry("item.clayium.industrial_clay_dust", "工業用粘土粉"),
            Map.entry("item.clayium.clay_wrench", "粘土レンチ"),
            Map.entry("tooltip.clayium.clay_wrench.line1", "装置を回転させる"),
            Map.entry("item.clayium.clay_pickaxe", "粘土ツルハシ"),
            Map.entry("tooltip.clayium.clay_pickaxe.line1", "粘土鉱石を素早く採掘し、"),
            Map.entry("tooltip.clayium.clay_pickaxe.line2", "そのドロップ数を上昇させる"),
            Map.entry("item.clayium.gadget_holder", "粘土ガジェットホルダー"),
            Map.entry("tooltip.clayium.gadget_holder.line1", "粘土ガジェットはこのホルダーに"),
            Map.entry("tooltip.clayium.gadget_holder.line2", "入れて使用すること")
    );

    private LegacyTranslations() {
    }
}
