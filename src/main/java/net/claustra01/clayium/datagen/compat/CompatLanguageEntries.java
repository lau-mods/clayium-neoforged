package net.claustra01.clayium.datagen.compat;

import java.util.function.BiConsumer;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;

public final class CompatLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            String title = localizedName(method, japanese);
            add.accept("gui.clayium.recipe." + method.id(), title);
        }
        add.accept("gui.clayium.recipe.show", japanese ? "レシピを見る" : "Show recipes");
        add.accept("gui.clayium.recipe.tier", japanese ? "Tier: %s" : "Tier: %s");
        add.accept("gui.clayium.recipe.energy_equation",
                japanese ? "%1$s CE/t × %2$s tick = %3$s CE" : "%1$s CE/t × %2$s ticks = %3$s CE");
        add.accept("gui.clayium.recipe.weight", japanese ? "重み %1$s/%2$s" : "Weight %1$s/%2$s");
        add.accept("gui.clayium.recipe.probability_weight",
                japanese ? "確率 %1$s%%（重み %2$s/%3$s）" : "%1$s%% chance (weight %2$s/%3$s)");
        add.accept("gui.clayium.recipe.structure", japanese ? "構造倍率は稼働時に適用" : "Structure multipliers apply at runtime");
        add.accept("gui.clayium.recipe.pan_duplication", japanese ? "PAN複製" : "PAN Duplication");
        add.accept("gui.clayium.recipe.pan_rate", "%s CE/t");
        add.accept("gui.clayium.recipe.pan_sample", japanese ? "見本を保持、反物質1個を消費" : "Keep sample, use 1 antimatter");
        add.accept("gui.clayium.recipe.pan_ticks", japanese ? "時間 = ceil(コアの複製費用 ÷ CE/t)" : "Time = ceil(core cost / CE/t)");
        description(add, japanese, "cobblestone_generator",
                "丸石製造機",
                "水と溶岩を水平に隣接させると丸石を生成します。Tierが上がるほど生成間隔が短くなります。",
                "Cobblestone Generator",
                "Generates cobblestone while water and lava are horizontally adjacent. Higher Tiers shorten the interval.");
        description(add, japanese, "salt_extractor",
                "塩析出装置",
                "上下左右前後いずれかに隣接する水源とCEを必要とし、入力なしで塩を析出します。",
                "Salt Extractor",
                "Needs a water source on any of its six adjacent sides and CE to extract salt. It has no input.");
        description(add, japanese, "quartz_crucible",
                "クォーツるつぼ",
                "不純シリコンインゴットを投入して加熱し、準備完了後に糸を使ってシリコンを引き上げます。",
                "Quartz Crucible",
                "Insert impure silicon ingots, wait for heating, then use string to draw the finished silicon.");
        description(add, japanese, "solar_clay_fabricator",
                "ソーラー粘土製造機",
                "上面から空が見える昼間に粘土を製造します。機種ごとに処理できる圧縮段階が異なります。",
                "Solar Clay Fabricator",
                "Fabricates clay in daylight when the sky is visible above it. Each model supports different compression ranks.");
        description(add, japanese, "clay_tree",
                "粘土樹",
                "対象の苗木へ1000以上の粘土レーザー値を照射すると粘土樹の苗木へ変化します。自然生成はしません。",
                "Clay Tree",
                "Irradiating an eligible sapling with at least 1000 clay-laser energy converts it into a Clay Tree sapling. It does not generate naturally.");
    }

    private static void description(
            BiConsumer<String, String> add,
            boolean japanese,
            String id,
            String japaneseTitle,
            String japaneseText,
            String englishTitle,
            String englishText) {
        add.accept("gui.clayium.recipe.description." + id + ".title", japanese ? japaneseTitle : englishTitle);
        add.accept("gui.clayium.recipe.description." + id + ".text", japanese ? japaneseText : englishText);
    }

    // Category titles use the original recipe.* translations shared by all machine tiers.
    private static String localizedName(ClayiumRecipeMethod method, boolean japanese) {
        return switch (method) {
            case CLAY_WORK_TABLE -> japanese ? "粘土作業台" : "Clay Work Table";
            case BENDING_MACHINE -> japanese ? "粘土プレート作成機" : "Clay Bending Machine";
            case WIRE_DRAWING_MACHINE -> japanese ? "粘土ワイヤー圧延機" : "Clay Wire Drawing Machine";
            case PIPE_DRAWING_MACHINE -> japanese ? "粘土パイプ圧延機" : "Clay Pipe Drawing Machine";
            case CUTTING_MACHINE -> japanese ? "粘土切断機" : "Clay Cutting Machine";
            case LATHE -> japanese ? "粘土旋盤" : "Clay Lathe";
            case MILLING_MACHINE -> japanese ? "粘土フライス盤" : "Clay Milling Machine";
            case CONDENSER -> japanese ? "粘土コンデンサー" : "Clay Condenser";
            case GRINDER -> japanese ? "粘土粉砕機" : "Clay Grinder";
            case DECOMPOSER -> japanese ? "粘土分解機" : "Clay Decomposer";
            case EC_CONDENSER -> japanese ? "エネルギー粘土凝縮機" : "Energetic Clay Condenser";
            case ASSEMBLER -> japanese ? "粘土組立機" : "Clay Assembler";
            case INSCRIBER -> japanese ? "粘土印刷機" : "Clay Inscriber";
            case CENTRIFUGE -> japanese ? "粘土遠心分離機" : "Clay Centrifuge";
            case SMELTER -> japanese ? "粘土精錬機" : "Clay Smelter";
            case CHEMICAL_REACTOR -> japanese ? "粘土化学反応機" : "Clay Chemical Reactor";
            case SALT_EXTRACTOR -> japanese ? "塩析出装置" : "Salt Extractor";
            case CHEMICAL_METAL_SEPARATOR -> japanese ? "粘土金属分離機" : "Chemical Metal Separator";
            case ALLOY_SMELTER -> japanese ? "粘土合金精錬機" : "Alloy Smelter";
            case BLAST_FURNACE -> japanese ? "粘土高炉" : "Clay Blast Furnace";
            case ELECTROLYSIS_REACTOR -> japanese ? "粘土電気精錬機" : "Clay Electrolysis Reactor";
            case REACTOR -> japanese ? "粘土反応炉" : "Clay Reactor";
            case MATTER_TRANSFORMER -> japanese ? "粘土物質変換装置" : "Matter Transformer";
            case CA_CONDENSER -> japanese ? "CA 凝縮機" : "CA Condenser";
            case CA_INJECTOR -> japanese ? "CA 注入機" : "CA Injector";
            case CA_REACTOR -> japanese ? "反物質反応炉" : "CA Reactor";
            case EC_DECOMPOSER -> japanese ? "エネルギー粘土分解機" : "Energetic Clay Decomposer";
            case FLUID_TRANSFER_MACHINE -> japanese ? "液体転移装置" : "Fluid Transfer Machine";
        };
    }

    private CompatLanguageEntries() {
    }
}
