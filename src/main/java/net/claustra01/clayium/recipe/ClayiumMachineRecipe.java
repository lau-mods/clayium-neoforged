package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.minecraft.world.level.Level;
import net.claustra01.clayium.util.WaterDetection;
import net.neoforged.neoforge.fluids.FluidStack;

public record ClayiumMachineRecipe(
        ClayiumRecipeMethod processingMethod,
        int requiredTier,
        List<SizedIngredient> itemInputs,
        List<FluidStack> fluidInputs,
        List<ItemStack> itemOutputs,
        List<FluidStack> fluidOutputs,
        long clayEnergy,
        long processingTicks,
        EnvironmentCondition environment,
        List<RandomOutput> randomOutputs) implements Recipe<ClayiumRecipeInput> {
    public static final MapCodec<ClayiumMachineRecipe> CODEC = recipeCodec().validate(ClayiumMachineRecipe::validate);

    private static MapCodec<ClayiumMachineRecipe> recipeCodec() {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ClayiumRecipeMethod.CODEC.fieldOf("processing_method")
                                .forGetter(ClayiumMachineRecipe::processingMethod),
                        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("required_tier")
                                .forGetter(ClayiumMachineRecipe::requiredTier),
                        SizedIngredient.NESTED_CODEC.listOf().optionalFieldOf("item_inputs", List.of())
                                .forGetter(ClayiumMachineRecipe::itemInputs),
                        FluidStack.CODEC.listOf().optionalFieldOf("fluid_inputs", List.of())
                                .forGetter(ClayiumMachineRecipe::fluidInputs),
                        ItemStack.CODEC.listOf().optionalFieldOf("item_outputs", List.of())
                                .forGetter(ClayiumMachineRecipe::itemOutputs),
                        FluidStack.CODEC.listOf().optionalFieldOf("fluid_outputs", List.of())
                                .forGetter(ClayiumMachineRecipe::fluidOutputs),
                        Codec.LONG.validate(value -> value >= 0
                                        ? DataResult.success(value)
                                        : DataResult.error(() -> "clay_energy must be non-negative"))
                                .fieldOf("clay_energy")
                                .forGetter(ClayiumMachineRecipe::clayEnergy),
                        Codec.LONG.validate(value -> value > 0L
                                        ? DataResult.success(value)
                                        : DataResult.error(() -> "processing_ticks must be positive"))
                                .fieldOf("processing_ticks")
                                .forGetter(ClayiumMachineRecipe::processingTicks),
                        EnvironmentCondition.CODEC.codec()
                                .optionalFieldOf("environment", EnvironmentCondition.ANY)
                                .forGetter(ClayiumMachineRecipe::environment),
                        RandomOutput.CODEC.codec().listOf().optionalFieldOf("random_outputs", List.of())
                                .forGetter(ClayiumMachineRecipe::randomOutputs))
                .apply(instance, ClayiumMachineRecipe::new));
    }

    private record RecipeData(ClayiumRecipeMethod method, int tier, List<SizedIngredient> items,
                              List<FluidStack> fluids, List<ItemStack> outputs, List<FluidStack> fluidOutputs) {
    }

    private record RecipeSettings(long energy, long ticks, EnvironmentCondition environment,
                                  List<RandomOutput> randomOutputs) {
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, RecipeData> DATA_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC.map(ClayiumRecipeMethod::byId, ClayiumRecipeMethod::location), RecipeData::method,
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeData::items,
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeData::fluids,
            ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeData::outputs,
            FluidStack.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeData::fluidOutputs,
            ByteBufCodecs.VAR_INT, RecipeData::tier,
            (method, items, fluids, outputs, fluidOutputs, tier) ->
                    new RecipeData(method, tier, items, fluids, outputs, fluidOutputs));
    private static final StreamCodec<RegistryFriendlyByteBuf, RecipeSettings> SETTINGS_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, RecipeSettings::energy,
            ByteBufCodecs.VAR_LONG, RecipeSettings::ticks,
            EnvironmentCondition.STREAM_CODEC, RecipeSettings::environment,
            RandomOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), RecipeSettings::randomOutputs,
            RecipeSettings::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClayiumMachineRecipe> STREAM_CODEC = StreamCodec.composite(
            DATA_CODEC, recipe -> new RecipeData(recipe.processingMethod, recipe.requiredTier, recipe.itemInputs,
                    recipe.fluidInputs, recipe.itemOutputs, recipe.fluidOutputs),
            SETTINGS_CODEC, recipe -> new RecipeSettings(recipe.clayEnergy, recipe.processingTicks,
                    recipe.environment, recipe.randomOutputs),
            (data, settings) -> new ClayiumMachineRecipe(data.method, data.tier, data.items, data.fluids,
                    data.outputs, data.fluidOutputs, settings.energy, settings.ticks,
                    settings.environment, settings.randomOutputs));

    public ClayiumMachineRecipe {
        itemInputs = List.copyOf(itemInputs);
        fluidInputs = copyFluids(fluidInputs);
        itemOutputs = copyItems(itemOutputs);
        fluidOutputs = copyFluids(fluidOutputs);
        randomOutputs = List.copyOf(randomOutputs);
    }

    private static DataResult<ClayiumMachineRecipe> validate(ClayiumMachineRecipe recipe) {
        if (recipe.itemOutputs.isEmpty() && recipe.fluidOutputs.isEmpty() && recipe.randomOutputs.isEmpty()) {
            return DataResult.error(() -> "Clayium machine recipe must define an output");
        }
        return DataResult.success(recipe);
    }

    @Override
    public boolean matches(ClayiumRecipeInput input, Level level) {
        return input.machineTier() >= requiredTier
                && input.availableClayEnergy() >= clayEnergy
                && environment.matches(level, input.machinePosition())
                && matchesItems(input.items())
                && matchesFluids(input.fluids());
    }

    private boolean matchesItems(List<ItemStack> available) {
        int[] remaining = available.stream().mapToInt(ItemStack::getCount).toArray();
        List<SizedIngredient> ordered = new ArrayList<>(itemInputs);
        ordered.sort(Comparator.comparingInt(input -> input.ingredient().getItems().length));
        for (SizedIngredient required : ordered) {
            int missing = required.count();
            for (int index = 0; index < available.size() && missing > 0; index++) {
                if (remaining[index] > 0 && required.ingredient().test(available.get(index))) {
                    int used = Math.min(missing, remaining[index]);
                    missing -= used;
                    remaining[index] -= used;
                }
            }
            if (missing > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesFluids(List<FluidStack> available) {
        int[] remaining = available.stream().mapToInt(FluidStack::getAmount).toArray();
        for (FluidStack required : fluidInputs) {
            int missing = required.getAmount();
            for (int index = 0; index < available.size() && missing > 0; index++) {
                FluidStack candidate = available.get(index);
                if (remaining[index] > 0 && FluidStack.isSameFluidSameComponents(required, candidate)) {
                    int used = Math.min(missing, remaining[index]);
                    missing -= used;
                    remaining[index] -= used;
                }
            }
            if (missing > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(ClayiumRecipeInput input, HolderLookup.Provider registries) {
        return getResultItem(registries).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        if (!itemOutputs.isEmpty()) {
            return itemOutputs.getFirst();
        }
        return randomOutputs.isEmpty() ? ItemStack.EMPTY : randomOutputs.getFirst().stack();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        itemInputs.forEach(input -> ingredients.add(input.ingredient()));
        return ingredients;
    }

    @Override
    public boolean isIncomplete() {
        return itemInputs.stream().anyMatch(input -> input.ingredient().hasNoItems());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistration.MACHINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistration.type(processingMethod);
    }

    private static List<ItemStack> copyItems(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    private static List<FluidStack> copyFluids(List<FluidStack> stacks) {
        return stacks.stream().map(FluidStack::copy).toList();
    }

    public record EnvironmentCondition(
            Optional<ResourceLocation> dimension,
            Optional<Integer> minimumY,
            Optional<Integer> maximumY,
            boolean requiresSky,
            boolean requiresAdjacentWaterSource) {
        public static final EnvironmentCondition ANY = new EnvironmentCondition(
                Optional.empty(), Optional.empty(), Optional.empty(), false, false);

        public EnvironmentCondition(
                Optional<ResourceLocation> dimension,
                Optional<Integer> minimumY,
                Optional<Integer> maximumY,
                boolean requiresSky) {
            this(dimension, minimumY, maximumY, requiresSky, false);
        }

        public static final MapCodec<EnvironmentCondition> CODEC = environmentCodec().validate(EnvironmentCondition::validate);

        private static MapCodec<EnvironmentCondition> environmentCodec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                        ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(EnvironmentCondition::dimension),
                        Codec.INT.optionalFieldOf("minimum_y").forGetter(EnvironmentCondition::minimumY),
                        Codec.INT.optionalFieldOf("maximum_y").forGetter(EnvironmentCondition::maximumY),
                        Codec.BOOL.optionalFieldOf("requires_sky", false).forGetter(EnvironmentCondition::requiresSky),
                        Codec.BOOL.optionalFieldOf("requires_adjacent_water_source", false)
                                .forGetter(EnvironmentCondition::requiresAdjacentWaterSource))
                .apply(instance, EnvironmentCondition::new));
        }

        private static DataResult<EnvironmentCondition> validate(EnvironmentCondition condition) {
            return condition.minimumY().isPresent()
                            && condition.maximumY().isPresent()
                            && condition.minimumY().get() > condition.maximumY().get()
                        ? DataResult.error(() -> "minimum_y must not exceed maximum_y")
                        : DataResult.success(condition);
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, EnvironmentCondition> STREAM_CODEC =
                StreamCodec.composite(
                        ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional), EnvironmentCondition::dimension,
                        ByteBufCodecs.INT.apply(ByteBufCodecs::optional), EnvironmentCondition::minimumY,
                        ByteBufCodecs.INT.apply(ByteBufCodecs::optional), EnvironmentCondition::maximumY,
                        ByteBufCodecs.BOOL, EnvironmentCondition::requiresSky,
                        ByteBufCodecs.BOOL, EnvironmentCondition::requiresAdjacentWaterSource,
                        EnvironmentCondition::new);

        public boolean matches(Level level, net.minecraft.core.BlockPos position) {
            if (!dimension.map(id -> id.equals(level.dimension().location())).orElse(true)
                    || !minimumY.map(y -> position.getY() >= y).orElse(true)
                    || !maximumY.map(y -> position.getY() <= y).orElse(true)
                    || requiresSky && !level.canSeeSky(position)) {
                return false;
            }
            if (!requiresAdjacentWaterSource) {
                return true;
            }
            for (Direction direction : Direction.values()) {
                if (WaterDetection.isWaterSource(level, position.relative(direction))) {
                    return true;
                }
            }
            return false;
        }
    }

    public record RandomOutput(ItemStack stack, int weight, float chance) {
        public static final MapCodec<RandomOutput> CODEC = randomOutputCodec().validate(RandomOutput::validate);

        private static MapCodec<RandomOutput> randomOutputCodec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                            ItemStack.CODEC.fieldOf("stack").forGetter(RandomOutput::stack),
                            ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(RandomOutput::weight),
                            Codec.FLOAT.fieldOf("chance").forGetter(RandomOutput::chance))
                    .apply(instance, RandomOutput::new));
        }

        private static DataResult<RandomOutput> validate(RandomOutput output) {
            return output.chance() >= 0.0F && output.chance() <= 1.0F
                    ? DataResult.success(output)
                    : DataResult.error(() -> "Random output chance must be between 0 and 1");
        }

        public static final StreamCodec<RegistryFriendlyByteBuf, RandomOutput> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, RandomOutput::stack,
                ByteBufCodecs.VAR_INT, RandomOutput::weight,
                ByteBufCodecs.FLOAT, RandomOutput::chance,
                RandomOutput::new);

        public RandomOutput {
            stack = stack.copy();
        }
    }

    public static final class Serializer implements RecipeSerializer<ClayiumMachineRecipe> {
        @Override
        public MapCodec<ClayiumMachineRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClayiumMachineRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
