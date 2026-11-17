package net.claustra01.clayium.datagen.waterwheel;

import java.util.function.BiConsumer;

public final class WaterWheelLanguageEntries {
    public static void add(String locale, BiConsumer<String, String> add) {
        boolean japanese = locale.equals("ja_jp");
        add.accept("block.clayium.clay_water_wheel", japanese ? "粘土水車" : "Clay Water Wheel");
        add.accept("block.clayium.dense_clay_water_wheel", japanese ? "高密度粘土水車" : "Dense Clay Water Wheel");
    }

    private WaterWheelLanguageEntries() {
    }
}
