package net.claustra01.clayium.material;

import java.util.List;

public record MaterialBlockDefinition(String id, ClayMaterial material, int tier) {
    private static final List<String> MATERIAL_IDS = List.of(
            "magnesium", "sodium", "lithium", "zirconium", "zinc", "manganese", "calcium",
            "potassium", "nickel", "beryllium", "lead", "hafnium", "chrome", "titanium",
            "strontium", "barium", "az91_d_alloy", "zk60_a_alloy", "rubidium", "caesium",
            "francium", "radium", "actinium", "thorium", "protactinium", "uranium", "neptunium",
            "plutonium", "americium", "curium", "lanthanum", "cerium", "praseodymium",
            "neodymium", "promethium", "samarium", "europium", "vanadium", "cobalt", "palladium",
            "platinum", "iridium", "osmium", "rhenium", "tantalum", "tungsten", "molybdenum",
            "antimony", "bismuth", "brass", "electrum", "invar", "steel", "copper", "tin",
            "silver", "bronze");

    public static final List<MaterialBlockDefinition> ORIGINAL_METAL_BLOCKS = MATERIAL_IDS.stream()
            .map(MaterialBlockDefinition::fromMaterial)
            .toList();

    private static MaterialBlockDefinition fromMaterial(String materialId) {
        String catalogId = switch (materialId) {
            case "az91_d_alloy" -> "az91d_alloy";
            case "zk60_a_alloy" -> "zk60a_alloy";
            default -> materialId;
        };
        ClayMaterial material = ClayMaterials.MATERIALS.get(catalogId);
        MaterialVariant ingot = ClayMaterials.VARIANTS_BY_ID.get(materialId + "_ingot");
        if (material == null || ingot == null) {
            throw new IllegalStateException("Missing original metal block material: " + materialId);
        }
        return new MaterialBlockDefinition(materialId + "_block", material, Math.max(0, ingot.tier()));
    }
}
