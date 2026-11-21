package net.claustra01.clayium.pan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.claustra01.clayium.material.CompressionTier;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

public final class PanRecipeClosure {
    public static Calculation begin(
            RecipeManager manager,
            HolderLookup.Provider registries,
            Set<RecipeType<?>> vanillaTypes,
            Map<ClayiumRecipeMethod, Integer> machineTiers) {
        List<RecipeHolder<?>> holders = new ArrayList<>();
        for (RecipeType<?> type : vanillaTypes) {
            for (RecipeHolder<?> holder : manager.getRecipes()) {
                if (holder.value().getType() == type) {
                    holders.add(holder);
                }
            }
        }
        for (ClayiumRecipeMethod method : machineTiers.keySet()) {
            holders.addAll(manager.getAllRecipesFor(RecipeRegistration.type(method)));
        }
        return new Calculation(baseCosts(), holders, registries, machineTiers);
    }

    public static Result calculate(Map<ResourceLocation, Long> bases, Collection<Edge> edges) {
        Calculation calculation = new Calculation(bases, edges);
        while (!calculation.advance(Integer.MAX_VALUE)) {
        }
        return calculation.result();
    }

    public static boolean isPlain(ItemStack stack) {
        return !stack.isEmpty() && stack.getComponentsPatch().isEmpty();
    }

    private static Map<ResourceLocation, Long> baseCosts() {
        Map<ResourceLocation, Long> bases = new LinkedHashMap<>();
        put(bases, new ItemStack(Items.CLAY), 1L);
        put(bases, new ItemStack(Items.COBBLESTONE), 1L);
        put(bases, new ItemStack(Items.OAK_LOG), 1L);
        for (CompressionTier tier : CompressionTier.values()) {
            long cost = 1L;
            for (int index = 0; index <= tier.ordinal(); index++) {
                cost *= 10L;
            }
            put(bases, new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS.get(tier.id()).get()), cost);
        }
        addMaterialBase(bases, "salt", 500L);
        addMaterialBase(bases, "antimatter", 100_000L);
        String[] metals = {"aluminium", "magnesium", "sodium", "lithium", "zirconium", "zinc",
                "manganese", "calcium", "potassium", "nickel", "iron", "beryllium", "lead", "hafnium",
                "chrome", "titanium", "strontium", "barium", "copper"};
        int[] weights = {200, 60, 40, 7, 5, 10, 80, 20, 15, 13, 9, 8, 6, 4, 3, 3, 2, 2, 1};
        for (int index = 0; index < metals.length; index++) {
            addMaterialBase(bases, "impure_" + metals[index] + "_dust",
                    ceilDivide(201_000L * 488L, weights[index]));
        }
        return bases;
    }

    private static void addMaterialBase(Map<ResourceLocation, Long> bases, String id, long cost) {
        var item = MaterialRegistration.MATERIAL_ITEMS.get(id);
        if (item != null) {
            put(bases, new ItemStack(item.get()), cost);
        }
    }

    private static void put(Map<ResourceLocation, Long> bases, ItemStack stack, long cost) {
        bases.put(BuiltInRegistries.ITEM.getKey(stack.getItem()), cost);
    }

    private static void addVanilla(List<Edge> edges, Recipe<?> recipe, HolderLookup.Provider registries) {
        ItemStack output = recipe.getResultItem(registries);
        if (!isPlain(output) || output.getCount() <= 0) {
            return;
        }
        List<Input> inputs = new ArrayList<>();
        boolean supported = true;
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            ItemStack[] choices = ingredient.getItems();
            List<ResourceLocation> alternatives = java.util.Arrays.stream(choices)
                    .filter(PanRecipeClosure::isPlain)
                    .map(choice -> BuiltInRegistries.ITEM.getKey(choice.getItem()))
                    .distinct()
                    .toList();
            if (alternatives.isEmpty()) {
                supported = false;
                break;
            }
            int existing = -1;
            for (int index = 0; index < inputs.size(); index++) {
                if (inputs.get(index).alternatives().equals(alternatives)) {
                    existing = index;
                    break;
                }
            }
            if (existing >= 0) {
                Input previous = inputs.get(existing);
                inputs.set(existing, new Input(alternatives, previous.count() + 1));
            } else {
                inputs.add(new Input(alternatives, 1));
            }
        }
        if (supported && !inputs.isEmpty()) {
            long energy = recipe.getType() == RecipeType.CRAFTING ? 10L : 800L;
            edges.add(new Edge(inputs, BuiltInRegistries.ITEM.getKey(output.getItem()), output.getCount(), energy));
        }
    }

    private static void addMachine(List<Edge> edges, ClayiumMachineRecipe recipe) {
        if (recipe.itemOutputs().isEmpty() || !recipe.fluidInputs().isEmpty() || !recipe.randomOutputs().isEmpty()) {
            return;
        }
        List<Input> inputs = new ArrayList<>();
        for (var sized : recipe.itemInputs()) {
            ItemStack[] choices = sized.ingredient().getItems();
            List<ResourceLocation> alternatives = java.util.Arrays.stream(choices)
                    .filter(PanRecipeClosure::isPlain)
                    .map(choice -> BuiltInRegistries.ITEM.getKey(choice.getItem()))
                    .distinct()
                    .toList();
            if (alternatives.isEmpty()) {
                return;
            }
            inputs.add(new Input(alternatives, sized.count()));
        }
        if (!inputs.isEmpty()) {
            for (ItemStack output : recipe.itemOutputs()) {
                if (isPlain(output)) {
                    edges.add(new Edge(inputs, BuiltInRegistries.ITEM.getKey(output.getItem()),
                            output.getCount(), recipe.clayEnergy()));
                }
            }
        }
    }

    private static long ceilDivide(long value, int divisor) {
        return value / divisor + (value % divisor == 0L ? 0L : 1L);
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    private static long saturatedMultiply(long value, long multiplier) {
        return value != 0L && multiplier > Long.MAX_VALUE / value ? Long.MAX_VALUE : value * multiplier;
    }

    public record Input(List<ResourceLocation> alternatives, int count) {
        public Input(ResourceLocation item, int count) {
            this(List.of(item), count);
        }

        public Input {
            alternatives = List.copyOf(alternatives);
        }
    }

    public record Edge(List<Input> inputs, ResourceLocation output, int outputCount, long energy) {
        public Edge(List<Input> inputs, ResourceLocation output, int outputCount) {
            this(inputs, output, outputCount, 0L);
        }

        public Edge {
            inputs = List.copyOf(inputs);
        }
    }

    public record Result(Map<ResourceLocation, Long> costs, Map<ResourceLocation, Long> consumption) {
        public long consumption(ItemStack stack) {
            if (!isPlain(stack)) {
                return 0L;
            }
            return consumption.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), 0L);
        }
    }

    public static final class Calculation {
        private final Map<ResourceLocation, Long> costs = new LinkedHashMap<>();
        private final Map<ResourceLocation, Long> consumptions = new LinkedHashMap<>();
        private final List<Edge> edges;
        private final List<RecipeHolder<?>> holders;
        private final HolderLookup.Provider registries;
        private final Map<ClayiumRecipeMethod, Integer> machineTiers;
        private int holderIndex;
        private int index;
        private int passes;
        private boolean changed;
        private boolean complete;

        private Calculation(Map<ResourceLocation, Long> bases, Collection<Edge> edges) {
            bases.forEach((item, cost) -> {
                if (cost > 0L) {
                    costs.put(item, cost);
                    consumptions.put(item, cost);
                }
            });
            this.edges = List.copyOf(edges);
            holders = List.of();
            registries = null;
            machineTiers = Map.of();
            complete = this.edges.isEmpty();
        }

        private Calculation(Map<ResourceLocation, Long> bases, List<RecipeHolder<?>> holders,
                HolderLookup.Provider registries, Map<ClayiumRecipeMethod, Integer> machineTiers) {
            bases.forEach((item, cost) -> {
                if (cost > 0L) {
                    costs.put(item, cost);
                    consumptions.put(item, cost);
                }
            });
            edges = new ArrayList<>();
            this.holders = holders;
            this.registries = registries;
            this.machineTiers = machineTiers;
            complete = holders.isEmpty();
        }

        public boolean advance(int budget) {
            int processed = 0;
            while (holderIndex < holders.size() && processed++ < budget) {
                Recipe<?> recipe = holders.get(holderIndex++).value();
                if (recipe instanceof ClayiumMachineRecipe machine) {
                    Integer tier = machineTiers.get(machine.processingMethod());
                    if (tier != null && machine.requiredTier() <= tier) {
                        addMachine(edges, machine);
                    }
                } else {
                    addVanilla(edges, recipe, registries);
                }
            }
            if (holderIndex == holders.size() && index == 0 && passes == 0 && edges.isEmpty()) {
                complete = true;
            }
            while (!complete && holderIndex == holders.size() && processed++ < budget) {
                update(edges.get(index++));
                if (index == edges.size()) {
                    if (changed && ++passes <= edges.size()) {
                        index = 0;
                        changed = false;
                    } else {
                        complete = true;
                    }
                }
            }
            return complete;
        }

        public Result result() {
            if (!complete) {
                throw new IllegalStateException("PAN recipe closure is not complete");
            }
            return new Result(Map.copyOf(costs), Map.copyOf(consumptions));
        }

        private void update(Edge edge) {
            long inputCost = 0L;
            long inputConsumption = 0L;
            boolean reachable = !edge.inputs().isEmpty();
            for (Input input : edge.inputs()) {
                long unitCost = Long.MAX_VALUE;
                for (ResourceLocation alternative : input.alternatives()) {
                    Long alternativeCost = costs.get(alternative);
                    if (alternativeCost != null) {
                        unitCost = Math.min(unitCost, alternativeCost);
                    }
                }
                if (unitCost == Long.MAX_VALUE || input.count() <= 0) {
                    reachable = false;
                    break;
                }
                inputCost = saturatedAdd(inputCost, saturatedMultiply(unitCost, input.count()));
                long unitConsumption = Long.MAX_VALUE;
                for (ResourceLocation alternative : input.alternatives()) {
                    Long value = consumptions.get(alternative);
                    if (value != null) unitConsumption = Math.min(unitConsumption, value);
                }
                inputConsumption = saturatedAdd(inputConsumption, saturatedMultiply(unitConsumption, input.count()));
            }
            if (!reachable || inputCost <= 0L || edge.outputCount() <= 0) {
                return;
            }
            long cost = Math.max(1L, ceilDivide(inputCost, edge.outputCount()));
            long consumption = Math.max(1L, ceilDivide(saturatedAdd(inputConsumption,
                    Math.max(0L, edge.energy())), edge.outputCount()));
            Long current = costs.get(edge.output());
            if (current == null || cost < current || cost == current
                    && consumption < consumptions.get(edge.output())) {
                costs.put(edge.output(), cost);
                consumptions.put(edge.output(), consumption);
                changed = true;
            }
        }
    }

    private PanRecipeClosure() {
    }
}
