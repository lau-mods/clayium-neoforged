package net.claustra01.clayium.machine.chemical;

import java.util.List;

public final class ChemicalMetalSeparation {
    public static final List<Product> PRODUCTS = List.of(
            product("aluminium", 200),
            product("magnesium", 60),
            product("sodium", 40),
            product("lithium", 7),
            product("zirconium", 5),
            product("zinc", 10),
            product("manganese", 80),
            product("calcium", 20),
            product("potassium", 15),
            product("nickel", 13),
            product("iron", 9),
            product("beryllium", 8),
            product("lead", 6),
            product("hafnium", 4),
            product("chrome", 3),
            product("titanium", 3),
            product("strontium", 2),
            product("barium", 2),
            product("copper", 1));
    public static final int TOTAL_WEIGHT = PRODUCTS.stream().mapToInt(Product::weight).sum();

    private static Product product(String material, int weight) {
        return new Product("impure_" + material + "_dust", weight);
    }

    public record Product(String itemId, int weight) {
    }

    private ChemicalMetalSeparation() {
    }
}
