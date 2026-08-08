/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.storage;

import java.util.List;
import java.util.stream.Stream;

/** Original Clayium metal chest dimensions and decorative metal block materials. */
public final class MetalStorageCatalog {
    public record Chest(String material, String displayName, int columns, int rows, int pages, int color) {
        public String blockId() { return material + "_chest"; }
        public String ingotId() { return material + "_ingot"; }
        public int slots() { return columns * rows * pages; }
    }

    public record Metal(String material, String displayName, int color) {
        public String blockId() { return material + "_block"; }
        public String ingotId() { return material + "_ingot"; }
    }

    public static final List<Chest> CHESTS = List.of(
            chest("silicon", "Silicon", 9, 5, 1, 0x281c28),
            chest("aluminium", "Aluminium", 9, 6, 1, 0xbec8ca),
            chest("clay_steel", "Clay Steel", 9, 8, 1, 0x8890ad),
            chest("clayium", "Clayium", 13, 8, 1, 0x5af0d2),
            chest("ultimate_alloy", "Ultimate Alloy", 13, 8, 3, 0x55cd55),
            chest("magnesium", "Magnesium", 9, 6, 1, 0x96d296),
            chest("sodium", "Sodium", 9, 6, 1, 0xaaaade),
            chest("lithium", "Lithium", 9, 7, 1, 0xd2d296),
            chest("zirconium", "Zirconium", 9, 7, 1, 0xbfaa7a),
            chest("zinc", "Zinc", 9, 6, 1, 0xe6aaaa),
            chest("manganese", "Manganese", 9, 6, 1, 0xbef0f0),
            chest("calcium", "Calcium", 9, 6, 1, 0xf0f0f0),
            chest("potassium", "Potassium", 9, 6, 1, 0xf0f0be),
            chest("nickel", "Nickel", 9, 6, 1, 0xd2d2f0),
            chest("beryllium", "Beryllium", 10, 8, 1, 0xd2f0d2),
            chest("lead", "Lead", 9, 6, 1, 0xbef0d2),
            chest("hafnium", "Hafnium", 9, 8, 1, 0xf0d2aa),
            chest("chrome", "Chrome", 13, 8, 1, 0xf0d2d2),
            chest("titanium", "Titanium", 13, 8, 1, 0xd2f0f0),
            chest("strontium", "Strontium", 10, 8, 1, 0xd2aaf2),
            chest("barium", "Barium", 10, 8, 1, 0x965078),
            chest("az91d", "AZ91D Alloy", 13, 8, 1, 0x828c87),
            chest("zk60a", "ZK60A Alloy", 13, 8, 1, 0x4b5550),
            chest("rubidium", "Rubidium", 13, 3, 1, 0xf5f5f5),
            chest("caesium", "Caesium", 13, 3, 1, 0xf5f5f5),
            chest("francium", "Francium", 13, 4, 1, 0xf5f5f5),
            chest("radium", "Radium", 13, 4, 1, 0xf5f5f5),
            chest("actinium", "Actinium", 13, 5, 1, 0xf5f5f5),
            chest("thorium", "Thorium", 13, 5, 1, 0x323232),
            chest("protactinium", "Protactinium", 13, 6, 1, 0x323232),
            chest("uranium", "Uranium", 13, 6, 2, 0x32ff32),
            chest("neptunium", "Neptunium", 13, 6, 3, 0x3232ff),
            chest("plutonium", "Plutonium", 13, 6, 4, 0x464646),
            chest("americium", "Americium", 13, 6, 5, 0x464646),
            chest("curium", "Curium", 13, 6, 6, 0x3232ff),
            chest("lanthanum", "Lanthanum", 13, 2, 2, 0x919191),
            chest("cerium", "Cerium", 13, 2, 4, 0x919191),
            chest("praseodymium", "Praseodymium", 13, 2, 6, 0x919191),
            chest("neodymium", "Neodymium", 13, 2, 8, 0x919191),
            chest("promethium", "Promethium", 13, 4, 8, 0x919191),
            chest("samarium", "Samarium", 13, 6, 8, 0x919191),
            chest("europium", "Europium", 13, 8, 8, 0x919191),
            chest("vanadium", "Vanadium", 4, 8, 1, 0x3c7878),
            chest("cobalt", "Cobalt", 11, 6, 1, 0x1e1ee6),
            chest("palladium", "Palladium", 11, 8, 1, 0x974646),
            chest("platinum", "Platinum", 13, 8, 2, 0xe1b450),
            chest("iridium", "Iridium", 13, 8, 3, 0xdcdceb),
            chest("osmium", "Osmium", 13, 8, 4, 0x6e96dc),
            chest("rhenium", "Rhenium", 13, 8, 5, 0xb4bed2),
            chest("tantalum", "Tantalum", 10, 8, 1, 0xf0d2aa),
            chest("tungsten", "Tungsten", 13, 8, 1, 0x1e1e1e),
            chest("molybdenum", "Molybdenum", 13, 8, 2, 0xb4bed2),
            chest("antimony", "Antimony", 9, 6, 1, 0x464646),
            chest("bismuth", "Bismuth", 9, 6, 1, 0x467846));

    public static final List<Metal> METALS = Stream.concat(
            CHESTS.stream()
                    .filter(chest -> !List.of("silicon", "aluminium", "clay_steel", "clayium", "ultimate_alloy")
                            .contains(chest.material()))
                    .map(chest -> new Metal(chest.material(), chest.displayName(), chest.color())),
            Stream.of(
                    new Metal("steel", "Steel", 0xffd8d8d8),
                    new Metal("silver", "Silver", 0xffdcdcf0),
                    new Metal("tin", "Tin", 0xffe6e6f0)))
            .toList();

    private MetalStorageCatalog() {}

    private static Chest chest(String id, String name, int columns, int rows, int pages, int color) {
        return new Chest(id, name, columns, rows, pages, 0xff000000 | color);
    }
}
