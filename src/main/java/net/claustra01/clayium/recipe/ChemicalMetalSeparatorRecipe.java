package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Weighted probabilistic output recipe for Chemical Metal Separator.
 */
public record ChemicalMetalSeparatorRecipe(
        CountedIngredient input,
        int tier,
        long energyPerTick,
        long durationTicks,
        List<WeightedOutput> outputs) implements Recipe<SingleRecipeInput> {

    public int totalWeight() {
        int sum = 0;
        for (WeightedOutput out : this.outputs) {
            sum += out.weight();
        }
        return sum;
    }

    public ItemStack selectByRoll(int roll) {
        int total = totalWeight();
        if (total <= 0 || this.outputs.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int cursor = Math.floorMod(roll, total);
        for (WeightedOutput out : this.outputs) {
            if (cursor < out.weight()) {
                return out.stack().copy();
            }
            cursor -= out.weight();
        }
        return this.outputs.getLast().stack().copy();
    }

    public ItemStack selectRandom(RandomSource random) {
        int total = totalWeight();
        if (total <= 0) {
            return ItemStack.EMPTY;
        }
        return selectByRoll(random.nextInt(total));
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return this.outputs.isEmpty() ? ItemStack.EMPTY : this.outputs.getFirst().stack().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.outputs.isEmpty() ? ItemStack.EMPTY : this.outputs.getFirst().stack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.CHEMICAL_METAL_SEPARATOR.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ClayiumRecipeTypes.CHEMICAL_METAL_SEPARATOR.get();
    }

    public record WeightedOutput(ItemStack stack, int weight) {
        public static final Codec<WeightedOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("stack").forGetter(WeightedOutput::stack),
                ExtraCodecs.POSITIVE_INT.fieldOf("weight").forGetter(WeightedOutput::weight)
        ).apply(instance, WeightedOutput::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, WeightedOutput> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, WeightedOutput::stack,
                ByteBufCodecs.VAR_INT, WeightedOutput::weight,
                WeightedOutput::new
        );
    }

    public static final class Serializer implements RecipeSerializer<ChemicalMetalSeparatorRecipe> {
        public static final MapCodec<ChemicalMetalSeparatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CountedIngredient.CODEC.fieldOf("input").forGetter(ChemicalMetalSeparatorRecipe::input),
                Codec.INT.optionalFieldOf("tier", 6).forGetter(ChemicalMetalSeparatorRecipe::tier),
                Codec.LONG.optionalFieldOf("energy", 5000L).forGetter(ChemicalMetalSeparatorRecipe::energyPerTick),
                Codec.LONG.optionalFieldOf("time", 40L).forGetter(ChemicalMetalSeparatorRecipe::durationTicks),
                WeightedOutput.CODEC.listOf(1, 64).fieldOf("outputs").forGetter(ChemicalMetalSeparatorRecipe::outputs)
        ).apply(instance, ChemicalMetalSeparatorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ChemicalMetalSeparatorRecipe> STREAM_CODEC = StreamCodec.composite(
                CountedIngredient.STREAM_CODEC, ChemicalMetalSeparatorRecipe::input,
                ByteBufCodecs.VAR_INT, ChemicalMetalSeparatorRecipe::tier,
                ByteBufCodecs.VAR_LONG, ChemicalMetalSeparatorRecipe::energyPerTick,
                ByteBufCodecs.VAR_LONG, ChemicalMetalSeparatorRecipe::durationTicks,
                WeightedOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), ChemicalMetalSeparatorRecipe::outputs,
                ChemicalMetalSeparatorRecipe::new
        );

        @Override
        public MapCodec<ChemicalMetalSeparatorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChemicalMetalSeparatorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
