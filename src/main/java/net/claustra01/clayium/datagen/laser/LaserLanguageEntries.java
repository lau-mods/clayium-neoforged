package net.claustra01.clayium.datagen.laser;

import java.util.List;
import java.util.function.BiConsumer;
import net.claustra01.clayium.laser.LaserSpecification;

public final class LaserLanguageEntries {
    private static final List<String> ENGLISH = List.of(
            "Clay Steel Clay Energy Laser", "Clayium Clay Energy Laser",
            "Ultimate Clay Energy Laser", "Antimatter Clay Energy Laser");
    private static final List<String> JAPANESE = List.of(
            "粘土鋼粘土エネルギーレーザー", "クレイウム粘土エネルギーレーザー",
            "究極粘土エネルギーレーザー", "反物質粘土エネルギーレーザー");

    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        List<String> names = japanese ? JAPANESE : ENGLISH;
        for (int index = 0; index < LaserSpecification.VALUES.size(); index++) {
            add.accept("block.clayium." + LaserSpecification.VALUES.get(index).id(), names.get(index));
        }
        add.accept("block.clayium.laser_reflector", japanese ? "レーザー反射器" : "Laser Reflector");
    }

    private LaserLanguageEntries() {
    }
}
