package net.claustra01.clayium.datagen.storage;

import java.util.Map;
import java.util.function.BiConsumer;
import net.claustra01.clayium.registry.StorageRegistration;

public final class StorageLanguageEntries {
    private static final Map<String, String> JAPANESE_MATERIALS = Map.ofEntries(
            Map.entry("silicon", "シリコン"),
            Map.entry("aluminium", "アルミニウム"),
            Map.entry("clay_steel", "粘土鋼"),
            Map.entry("clayium", "クレイウム"),
            Map.entry("ultimate_alloy", "究極合金"),
            Map.entry("magnesium", "マグネシウム"),
            Map.entry("sodium", "ナトリウム"),
            Map.entry("lithium", "リチウム"),
            Map.entry("zirconium", "ジルコニウム"),
            Map.entry("zinc", "亜鉛"),
            Map.entry("manganese", "マンガン"),
            Map.entry("calcium", "カルシウム"),
            Map.entry("potassium", "カリウム"),
            Map.entry("nickel", "ニッケル"),
            Map.entry("beryllium", "ベリリウム"),
            Map.entry("lead", "鉛"),
            Map.entry("hafnium", "ハフニウム"),
            Map.entry("chrome", "クロム"),
            Map.entry("titanium", "チタン"),
            Map.entry("strontium", "ストロンチウム"),
            Map.entry("barium", "バリウム"),
            Map.entry("az91d_alloy", "AZ91D 合金"),
            Map.entry("zk60a_alloy", "ZK60A 合金"),
            Map.entry("rubidium", "ルビジウム"),
            Map.entry("caesium", "セシウム"),
            Map.entry("francium", "フランシウム"),
            Map.entry("radium", "ラジウム"),
            Map.entry("actinium", "アクチニウム"),
            Map.entry("thorium", "トリウム"),
            Map.entry("protactinium", "プロトアクチニウム"),
            Map.entry("uranium", "ウラン"),
            Map.entry("neptunium", "ネプツニウム"),
            Map.entry("plutonium", "プルトニウム"),
            Map.entry("americium", "アメリシウム"),
            Map.entry("curium", "キュリウム"),
            Map.entry("lanthanum", "ランタン"),
            Map.entry("cerium", "セリウム"),
            Map.entry("praseodymium", "プラセオジム"),
            Map.entry("neodymium", "ネオジム"),
            Map.entry("promethium", "プロメチウム"),
            Map.entry("samarium", "サマリウム"),
            Map.entry("europium", "ユーロピウム"),
            Map.entry("vanadium", "バナジウム"),
            Map.entry("cobalt", "コバルト"),
            Map.entry("palladium", "パラジウム"),
            Map.entry("platinum", "白金"),
            Map.entry("iridium", "イリジウム"),
            Map.entry("osmium", "オスミウム"),
            Map.entry("rhenium", "レニウム"),
            Map.entry("tantalum", "タンタル"),
            Map.entry("tungsten", "タングステン"),
            Map.entry("molybdenum", "モリブデン"),
            Map.entry("antimony", "アンチモン"),
            Map.entry("bismuth", "ビスマス"),
            Map.entry("brass", "ブラス"),
            Map.entry("electrum", "エレクトラム"),
            Map.entry("invar", "インバー"));

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        add.accept("block.clayium.storage_container", japanese ? "ストレージコンテナ" : "Storage Container");
        add.accept("block.clayium.void_container", japanese ? "虚空コンテナ" : "Void Container");
        add.accept("gui.clayium.storage.expanded", japanese ? "Clay Core 拡張済み" : "Clay Core expanded");
        add.accept("gui.clayium.storage.void_hint", japanese ? "投入品を破棄します" : "Inserted items are discarded");
        StorageRegistration.METAL_CHESTS.values().forEach(definition -> {
            String material = japanese
                    ? JAPANESE_MATERIALS.get(definition.type().materialId())
                    : englishMaterial(definition.type().materialId());
            add.accept("block.clayium." + definition.type().id(),
                    japanese ? material + "チェスト" : material + " Chest");
        });
    }

    private static String englishMaterial(String id) {
        if (id.equals("az91d_alloy")) return "AZ91D Alloy";
        if (id.equals("zk60a_alloy")) return "ZK60A Alloy";
        StringBuilder title = new StringBuilder();
        for (String part : id.split("_")) {
            if (!title.isEmpty()) title.append(' ');
            title.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return title.toString();
    }

    private StorageLanguageEntries() {
    }
}
