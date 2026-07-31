/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.machine;

import java.util.List;
import net.claustra01.clayium.registry.ClayiumRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

/** Single source of truth for the original separator's weighted products. */
public final class ChemicalMetalSeparatorProcess {
    public record Product(String material, int weight) {
        public ItemStack stack() {
            return new ItemStack(ClayiumRegistries.PHASE6_ITEMS.get("impure_" + material + "_dust").get());
        }
    }
    public static final List<Product> PRODUCTS = List.of(
            new Product("aluminium",200),new Product("manganese",80),new Product("magnesium",60),
            new Product("sodium",40),new Product("calcium",20),new Product("potassium",15),
            new Product("nickel",13),new Product("zinc",10),new Product("iron",9),
            new Product("beryllium",8),new Product("lithium",7),new Product("lead",6),
            new Product("zirconium",5),new Product("hafnium",4),new Product("chrome",3),
            new Product("titanium",3),new Product("strontium",2),new Product("barium",2),
            new Product("copper",1));
    public static final int TOTAL_WEIGHT = PRODUCTS.stream().mapToInt(Product::weight).sum();
    private ChemicalMetalSeparatorProcess() {}
    public static ItemStack select(RandomSource random) {
        int value=random.nextInt(TOTAL_WEIGHT);
        for(Product product:PRODUCTS){if(value<product.weight())return product.stack();value-=product.weight();}
        return PRODUCTS.getLast().stack();
    }
}
