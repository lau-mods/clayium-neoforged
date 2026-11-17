package net.claustra01.clayium.datagen.weapon;

import java.util.Map;

public final class WeaponTranslations {
    private static final Map<String, String> ENGLISH = Map.ofEntries(
            Map.entry("item.clayium.basic_clay_shooter", "Basic Clay Shooter"),
            Map.entry("item.clayium.advanced_clay_shooter", "Advanced Clay Shooter"),
            Map.entry("item.clayium.sniper_clay_shooter", "Sniper Clay Shooter"),
            Map.entry("item.clayium.sniper_clay_shooter_pro", "Sniper Clay Shooter Pro"),
            Map.entry("item.clayium.instant_teleporter", "Instant Teleporter"),
            Map.entry("entity.clayium.clay_ball", "Clay Ball"),
            Map.entry("entity.clayium.teleport_ball", "Teleport Ball"));
    private static final Map<String, String> JAPANESE = Map.ofEntries(
            Map.entry("item.clayium.basic_clay_shooter", "粘土銃"),
            Map.entry("item.clayium.advanced_clay_shooter", "発展粘土銃"),
            Map.entry("item.clayium.sniper_clay_shooter", "スナイパー粘土銃"),
            Map.entry("item.clayium.sniper_clay_shooter_pro", "スナイパー粘土銃プロ"),
            Map.entry("item.clayium.instant_teleporter", "テレポートシューター"),
            Map.entry("entity.clayium.clay_ball", "粘土弾"),
            Map.entry("entity.clayium.teleport_ball", "テレポート弾"));

    public static Map<String, String> forLocale(String locale) {
        return locale.equals("ja_jp") ? JAPANESE : ENGLISH;
    }

    private WeaponTranslations() {
    }
}
