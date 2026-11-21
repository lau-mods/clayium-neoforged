package net.claustra01.clayium.storage;

import java.util.List;
import net.claustra01.clayium.material.ClayMaterial;
import net.claustra01.clayium.material.ClayMaterials;

public record MetalChestType(String materialId, String id, int columns, int rows, int pages) {
    public static final List<MetalChestType> ALL = List.of(
            type("silicon", 9, 5, 1),
            type("aluminium", 9, 6, 1),
            type("clay_steel", 9, 8, 1),
            type("clayium", 13, 8, 1),
            type("ultimate_alloy", 13, 8, 3),
            type("magnesium", 9, 6, 1),
            type("sodium", 9, 6, 1),
            type("lithium", 9, 7, 1),
            type("zirconium", 9, 7, 1),
            type("zinc", 9, 6, 1),
            type("manganese", 9, 6, 1),
            type("calcium", 9, 6, 1),
            type("potassium", 9, 6, 1),
            type("nickel", 9, 6, 1),
            type("beryllium", 10, 8, 1),
            type("lead", 9, 6, 1),
            type("hafnium", 9, 8, 1),
            type("chrome", 13, 8, 1),
            type("titanium", 13, 8, 1),
            type("strontium", 10, 8, 1),
            type("barium", 10, 8, 1),
            type("az91d_alloy", "az91_d_alloy_chest", 13, 8, 1),
            type("zk60a_alloy", "zk60_a_alloy_chest", 13, 8, 1),
            type("rubidium", 13, 3, 1),
            type("caesium", 13, 3, 1),
            type("francium", 13, 4, 1),
            type("radium", 13, 4, 1),
            type("actinium", 13, 5, 1),
            type("thorium", 13, 5, 1),
            type("protactinium", 13, 6, 1),
            type("uranium", 13, 6, 2),
            type("neptunium", 13, 6, 3),
            type("plutonium", 13, 6, 4),
            type("americium", 13, 6, 5),
            type("curium", 13, 6, 6),
            type("lanthanum", 13, 2, 2),
            type("cerium", 13, 2, 4),
            type("praseodymium", 13, 2, 6),
            type("neodymium", 13, 2, 8),
            type("promethium", 13, 4, 8),
            type("samarium", 13, 6, 8),
            type("europium", 13, 8, 8),
            type("vanadium", 4, 8, 1),
            type("cobalt", 11, 6, 1),
            type("palladium", 11, 8, 1),
            type("platinum", 13, 8, 2),
            type("iridium", 13, 8, 3),
            type("osmium", 13, 8, 4),
            type("rhenium", 13, 8, 5),
            type("tantalum", 10, 8, 1),
            type("tungsten", 13, 8, 1),
            type("molybdenum", 13, 8, 2),
            type("antimony", 9, 6, 1),
            type("bismuth", 9, 6, 1),
            type("brass", 9, 6, 1),
            type("electrum", 13, 8, 1),
            type("invar", 9, 8, 1));

    public int pageSize() {
        return columns * rows;
    }

    public int slots() {
        return pageSize() * pages;
    }

    public ClayMaterial material() {
        return ClayMaterials.MATERIALS.get(materialId);
    }

    private static MetalChestType type(String materialId, int columns, int rows, int pages) {
        return type(materialId, materialId + "_chest", columns, rows, pages);
    }

    private static MetalChestType type(
            String materialId, String id, int columns, int rows, int pages) {
        return new MetalChestType(materialId, id, columns, rows, pages);
    }
}
