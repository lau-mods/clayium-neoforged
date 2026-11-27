package net.claustra01.clayium.datagen.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.material.CMaterial;
import net.claustra01.clayium.material.CMaterials;
import net.claustra01.clayium.material.CShape;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.ClayiumConfigCondition;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

/**
 * Shared deterministic helpers, energy formulas, and 1.7.10->1.21.1 material/tag resolution for recipe DataGen.
 */
public final class ClayiumRecipeHelper {
    public static final String[] COMPRESSED_CLAY_PATHS = {
            "dense_clay",
            "compressed_clay",
            "industrial_clay",
            "advanced_industrial_clay",
            "energetic_clay",
            "compressed_energetic_clay",
            "double_compressed_energetic_clay",
            "triple_compressed_energetic_clay",
            "quadruple_compressed_energetic_clay",
            "quintuple_compressed_energetic_clay",
            "sextuple_compressed_energetic_clay",
            "septuple_compressed_energetic_clay",
            "octuple_compressed_energetic_clay"
    };

    public static final String[] MACHINE_HULL_PATHS = {
            "raw_clay_machine_hull",
            "clay_machine_hull",
            "dense_clay_machine_hull",
            "simple_machine_hull",
            "basic_machine_hull",
            "advanced_machine_hull",
            "precision_machine_hull",
            "clay_steel_machine_hull",
            "clayium_machine_hull",
            "ultimate_machine_hull",
            "antimatter_machine_hull",
            "pure_antimatter_machine_hull",
            "oec_machine_hull",
            "opa_machine_hull"
    };

    public static final String[] CIRCUIT_PATHS = {
            "clay_circuit",
            "clay_circuit",
            "clay_circuit",
            "simple_circuit",
            "basic_circuit",
            "advanced_circuit",
            "precision_circuit",
            "integrated_circuit",
            "clay_core",
            "clay_brain",
            "clay_spirit",
            "clay_soul",
            "clay_anima",
            "clay_psyche"
    };

    public static final CMaterial[] COMPRESSED_PURE_ANTIMATTERS = {
            CMaterials.PURE_ANTIMATTER,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_1,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_2,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_3,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_4,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_5,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_6,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_7,
            CMaterials.COMPRESSED_PURE_ANTIMATTER_8
    };

    private final RecipeOutput output;
    private final Map<String, Integer> idCounters = new HashMap<>();

    public ClayiumRecipeHelper(RecipeOutput output) {
        this.output = output;
    }

    public RecipeOutput output() {
        return this.output;
    }

    public RecipeOutput withConfig(String key, boolean expected) {
        return this.output.withConditions(new ClayiumConfigCondition(key, expected));
    }

    public static long e(int tier) {
        return e(1.0, tier);
    }

    public static long e(double factor, int tier) {
        return (long) (factor * 100.0 * Math.pow(10.0, tier - 4));
    }

    public ResourceLocation nextId(String category, String slug) {
        String key = category + "/" + slug;
        int n = this.idCounters.merge(key, 1, Integer::sum);
        return n == 1 ? ClayiumMod.id(key) : ClayiumMod.id(key + "_" + n);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> hasClay() {
        return InventoryChangeTrigger.TriggerInstance.hasItems(Items.CLAY_BALL);
    }

    public static Item item(String path) {
        return ClayiumItems.byPath(path).get();
    }

    public static Item blockItem(String path) {
        return ClayiumBlocks.byPath(path).get().asItem();
    }

    public static ItemStack stack(String path) {
        return new ItemStack(item(path), 1);
    }

    public static ItemStack stack(String path, int count) {
        return new ItemStack(item(path), count);
    }

    public static ItemStack stack(ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    public static ItemStack compressedClay(int tierIndex, int count) {
        return stack(COMPRESSED_CLAY_PATHS[tierIndex], count);
    }

    public static ItemStack machineHull(int tier) {
        return stack(MACHINE_HULL_PATHS[tier], 1);
    }

    public static ItemStack circuit(int tier, int count) {
        return stack(CIRCUIT_PATHS[tier], count);
    }

    public static Item matItem(CMaterial mat, CShape shape) {
        if (shape == CShape.BLOCK) {
            if (mat == CMaterials.OCTUPLE_CLAY) {
                return item("octuple_compressed_energetic_clay");
            }
            if (mat == CMaterials.IRON) {
                return Items.IRON_BLOCK;
            }
            if (mat == CMaterials.GOLD) {
                return Items.GOLD_BLOCK;
            }
            if (mat == CMaterials.COPPER) {
                return Items.COPPER_BLOCK;
            }
            if (mat == CMaterials.DIAMOND) {
                return Items.DIAMOND_BLOCK;
            }
            if (mat == CMaterials.EMERALD) {
                return Items.EMERALD_BLOCK;
            }
            if (mat == CMaterials.LAPIS) {
                return Items.LAPIS_BLOCK;
            }
            if (mat == CMaterials.COAL) {
                return Items.COAL_BLOCK;
            }
            if (mat == CMaterials.QUARTZ) {
                return Items.QUARTZ_BLOCK;
            }
        }
        if (mat == CMaterials.DIAMOND && shape == CShape.GEM) {
            return Items.DIAMOND;
        }
        if (mat == CMaterials.EMERALD && shape == CShape.GEM) {
            return Items.EMERALD;
        }
        if (mat == CMaterials.LAPIS && shape == CShape.GEM) {
            return Items.LAPIS_LAZULI;
        }
        if (mat == CMaterials.QUARTZ && shape == CShape.GEM) {
            return Items.QUARTZ;
        }
        if (mat == CMaterials.REDSTONE && shape == CShape.DUST) {
            return Items.REDSTONE;
        }
        if (mat == CMaterials.IRON && shape == CShape.INGOT) {
            return Items.IRON_INGOT;
        }
        if (mat == CMaterials.GOLD && shape == CShape.INGOT) {
            return Items.GOLD_INGOT;
        }
        if (mat == CMaterials.COPPER && shape == CShape.INGOT) {
            return Items.COPPER_INGOT;
        }
        return CMaterials.getItem(mat, shape);
    }

    public static ItemStack matStack(CMaterial mat, CShape shape) {
        return matStack(mat, shape, 1);
    }

    public static ItemStack matStack(CMaterial mat, CShape shape, int count) {
        Item found = matItem(mat, shape);
        if (found == Items.AIR) {
            throw new IllegalStateException("Missing item for material=" + mat.name() + ", shape=" + shape.shapeName());
        }
        return new ItemStack(found, count);
    }

    public static boolean hasMatItem(CMaterial mat, CShape shape) {
        return matItem(mat, shape) != Items.AIR;
    }

    public static TagKey<Item> commonTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static TagKey<Item> matTag(CMaterial mat, CShape shape) {
        return commonTag(shape.tagFolder() + "/" + mat.idPrefix());
    }

    public static TagKey<Item> oreTag(CMaterial mat) {
        return commonTag("ores/" + mat.idPrefix());
    }

    public static CountedIngredient counted(ItemLike item) {
        return counted(item, 1);
    }

    public static CountedIngredient counted(ItemLike item, int count) {
        return new CountedIngredient(Ingredient.of(item), count);
    }

    public static CountedIngredient counted(ItemStack stack) {
        return new CountedIngredient(Ingredient.of(stack.getItem()), stack.getCount());
    }

    public static CountedIngredient counted(TagKey<Item> tag) {
        return counted(tag, 1);
    }

    public static CountedIngredient counted(TagKey<Item> tag, int count) {
        return new CountedIngredient(Ingredient.of(tag), count);
    }

    public static CountedIngredient counted(CMaterial mat, CShape shape) {
        return counted(mat, shape, 1);
    }

    public static CountedIngredient counted(CMaterial mat, CShape shape, int count) {
        return counted(matStack(mat, shape), count);
    }

    public static CountedIngredient counted(ItemStack base, int count) {
        return new CountedIngredient(Ingredient.of(base.getItem()), count);
    }

    public static CountedIngredient countedTag(CMaterial mat, CShape shape) {
        return countedTag(mat, shape, 1);
    }

    public static CountedIngredient countedTag(CMaterial mat, CShape shape, int count) {
        if (mat == CMaterials.CLAY && shape == CShape.BALL) {
            return counted(Items.CLAY_BALL, count);
        }
        if (mat == CMaterials.CLAY && shape == CShape.BLOCK) {
            return counted(Items.CLAY, count);
        }
        if (mat == CMaterials.IRON && shape == CShape.INGOT) {
            return counted(Tags.Items.INGOTS_IRON, count);
        }
        if (mat == CMaterials.GOLD && shape == CShape.INGOT) {
            return counted(Tags.Items.INGOTS_GOLD, count);
        }
        if (mat == CMaterials.COPPER && shape == CShape.INGOT) {
            return counted(Tags.Items.INGOTS_COPPER, count);
        }
        if (mat == CMaterials.DIAMOND && shape == CShape.GEM) {
            return counted(Tags.Items.GEMS_DIAMOND, count);
        }
        if (mat == CMaterials.EMERALD && shape == CShape.GEM) {
            return counted(Tags.Items.GEMS_EMERALD, count);
        }
        if (mat == CMaterials.LAPIS && shape == CShape.GEM) {
            return counted(Tags.Items.GEMS_LAPIS, count);
        }
        if (mat == CMaterials.QUARTZ && shape == CShape.GEM) {
            return counted(Tags.Items.GEMS_QUARTZ, count);
        }
        if (mat == CMaterials.REDSTONE && shape == CShape.DUST) {
            return counted(Tags.Items.DUSTS_REDSTONE, count);
        }
        return counted(matTag(mat, shape), count);
    }

    public static String itemSlug(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    public void workTable(String slug, CountedIngredient input, int buttonId, ItemStack primary, ItemStack secondary, int clicks) {
        ResourceLocation id = nextId("clay_work_table", slug);
        this.output.accept(id, new ClayWorkTableRecipe(input, buttonId, primary, secondary, clicks), null);
    }

    public void machine(
            String categoryId,
            List<CountedIngredient> inputs,
            int tier,
            List<ItemStack> results,
            long energyPerTick,
            long durationTicks) {
        machine(this.output, categoryId, inputs, tier, results, energyPerTick, durationTicks, 0);
    }

    public void machine(
            RecipeOutput targetOutput,
            String categoryId,
            List<CountedIngredient> inputs,
            int tier,
            List<ItemStack> results,
            long energyPerTick,
            long durationTicks,
            int priority) {
        String outSlug = itemSlug(results.getFirst().getItem());
        ResourceLocation id = nextId(categoryId, outSlug);
        targetOutput.accept(
                id,
                new ClayMachineRecipe(categoryId, inputs, results, tier, energyPerTick, durationTicks, priority),
                null
        );
    }

    public void simple(String categoryId, ItemLike input, int count, int tier, ItemStack result, long energy, long time) {
        machine(categoryId, List.of(counted(input, count)), tier, List.of(result), energy, time);
    }

    public void simple(String categoryId, ItemStack input, ItemStack result, long energy, long time) {
        machine(categoryId, List.of(counted(input)), 0, List.of(result), energy, time);
    }

    public void simple(String categoryId, ItemStack input, int tier, ItemStack result, long energy, long time) {
        machine(categoryId, List.of(counted(input)), tier, List.of(result), energy, time);
    }

    public void simpleTag(String categoryId, TagKey<Item> tag, int count, int tier, ItemStack result, long energy, long time) {
        machine(categoryId, List.of(counted(tag, count)), tier, List.of(result), energy, time);
    }

    public void simpleMatTag(String categoryId, CMaterial mat, CShape shape, int count, int tier, ItemStack result, long energy, long time) {
        machine(categoryId, List.of(countedTag(mat, shape, count)), tier, List.of(result), energy, time);
    }

    public void registerODChainRecipes(
            String categoryId,
            CMaterial[] materials,
            CShape shape,
            int[] stackSizes,
            int[] tiers,
            int time) {
        int matPos = 0;
        long energy = 0L;
        for (int i = 1; i < materials.length; i++) {
            energy += e(tiers[i]);
            if (hasMatItem(materials[i], shape)) {
                int g = gcd(stackSizes[matPos], stackSizes[i]);
                machine(
                        categoryId,
                        List.of(countedTag(materials[matPos], shape, stackSizes[matPos] / g)),
                        tiers[i],
                        List.of(matStack(materials[i], shape, stackSizes[i] / g)),
                        energy,
                        time
                );
                matPos = i;
                energy = 0L;
            }
        }
    }

    public void registerODChainRecipes(
            String categoryId,
            CMaterial[] materials,
            CShape shape,
            int[] tiers,
            int time) {
        int[] stackSizes = new int[materials.length];
        for (int i = 0; i < stackSizes.length; i++) {
            stackSizes[i] = 1;
        }
        registerODChainRecipes(categoryId, materials, shape, stackSizes, tiers, time);
    }

    public void registerStackChainRecipes(
            String categoryId,
            ItemStack[] itemstacks,
            int[] stackSizes,
            int[] tiers,
            int time) {
        int matPos = 0;
        long energy = 0L;
        for (int i = 1; i < itemstacks.length; i++) {
            energy += e(tiers[i]);
            if (itemstacks[i] != null && !itemstacks[i].isEmpty()) {
                int g = gcd(stackSizes[matPos], stackSizes[i]);
                machine(
                        categoryId,
                        List.of(counted(itemstacks[matPos], stackSizes[matPos] / g)),
                        tiers[i],
                        List.of(new ItemStack(itemstacks[i].getItem(), stackSizes[i] / g)),
                        energy,
                        time
                );
                matPos = i;
                energy = 0L;
            }
        }
    }

    public void registerStackChainRecipes(
            String categoryId,
            ItemStack[] itemstacks,
            int[] tiers,
            int time) {
        int[] stackSizes = new int[itemstacks.length];
        for (int i = 0; i < stackSizes.length; i++) {
            stackSizes[i] = 1;
        }
        registerStackChainRecipes(categoryId, itemstacks, stackSizes, tiers, time);
    }

    private static int gcd(int a, int b) {
        int x = a;
        int y = b;
        if (x < y) {
            int tmp = y;
            y = x;
            x = tmp;
        }
        while (x % y != 0) {
            int tmp = x % y;
            x = y;
            y = tmp;
        }
        return y;
    }
}
