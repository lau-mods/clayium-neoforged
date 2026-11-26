package net.claustra01.clayium.datagen.gadget;

import java.util.LinkedHashMap;
import java.util.Map;
import net.claustra01.clayium.gadget.GadgetType;

public final class GadgetTranslations {
    private static final Map<String, String> ENGLISH = translations(false,
            "Clay Gadget Template",
            "Clay Gadget: Overclock I",
            "Clay Gadget: Overclock II",
            "Clay Gadget: Overclock III",
            "Clay Gadget: Overclock IV",
            "Clay Gadget: Flight",
            "Clay Gadget: Free Flight",
            "Clay Gadget: Supersonic Flight",
            "Clay Gadget: Extra Health I",
            "Clay Gadget: Extra Health II",
            "Clay Gadget: Extra Health III",
            "Clay Gadget: Economical Auto Eat",
            "Clay Gadget: Auto Eat",
            "Clay Gadget: Repeated Attack",
            "Clay Gadget: Long Arm",
            "Clay Gadget: Longer Arm",
            "Clay Gadget: Longest Arm");
    private static final Map<String, String> JAPANESE = translations(true,
            "粘土ガジェット素材",
            "粘土ガジェット: オーバークロック I",
            "粘土ガジェット: オーバークロック II",
            "粘土ガジェット: オーバークロック III",
            "粘土ガジェット: オーバークロック IV",
            "粘土ガジェット: 飛行",
            "粘土ガジェット: 自由飛行",
            "粘土ガジェット: 超音速飛行",
            "粘土ガジェット: 体力 I",
            "粘土ガジェット: 体力 II",
            "粘土ガジェット: 体力 III",
            "粘土ガジェット: 自動摂食（倹約）",
            "粘土ガジェット: 自動摂食",
            "粘土ガジェット: 連続攻撃",
            "粘土ガジェット: ロングアーム",
            "粘土ガジェット: ロングアーム II",
            "粘土ガジェット: ロングアーム III");

    public static Map<String, String> forLocale(String locale) {
        return locale.equals("ja_jp") ? JAPANESE : ENGLISH;
    }

    private static Map<String, String> translations(boolean japanese, String... names) {
        Map<String, String> translations = new LinkedHashMap<>();
        GadgetType[] types = GadgetType.values();
        for (int index = 0; index < types.length; index++) {
            translations.put("item.clayium." + types[index].id(), names[index]);
        }
        translations.put("screen.clayium.gadget_holder", japanese ? "粘土ガジェット設定" : "Clay Gadget Setup");
        translations.put("screen.clayium.gadget_holder.installed", japanese ? "装着中のガジェット" : "Installed Gadgets");
        translations.put("tooltip.clayium.gadget_holder.empty", japanese ? "ガジェット未装着" : "No gadgets installed");
        translations.put("tooltip.clayium.gadget_holder.configured", japanese ? "%1$d 個装着中" : "%1$d installed");
        return Map.copyOf(translations);
    }

    private GadgetTranslations() {
    }
}
