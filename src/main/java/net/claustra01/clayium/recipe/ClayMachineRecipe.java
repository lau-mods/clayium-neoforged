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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Unified processing recipe for all 25 Clayium machine recipe categories, preserving multi-input/output,
 * minimum tier, long CE/t cost, duration ticks, and priority ordering.
 */
public record ClayMachineRecipe(
        String categoryId,
        List<CountedIngredient> inputs,
        List<ItemStack> results,
        int tier,
        long energyPerTick,
        long durationTicks,
        int priority) implements Recipe<ClayMachineRecipe.MachineInput> {

    @Override
    public boolean matches(MachineInput input, Level level) {
        if (input.machineTier() < this.tier) {
            return false;
        }
        int required = this.inputs.size();
        if (input.size() < required) {
            return false;
        }
        // Check exact slot alignment first, and for 2-slot machines also allow swapped pair when not strictly slot-pinned
        boolean exact = true;
        for (int i = 0; i < required; i++) {
            if (!this.inputs.get(i).test(input.getItem(i))) {
                exact = false;
                break;
            }
        }
        if (exact) {
            // Ensure no extra non-empty input is required when 1-input recipe matches a 2-slot machine
            for (int i = required; i < input.size(); i++) {
                if (!input.getItem(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        if (required == 2 && input.size() >= 2) {
            return this.inputs.get(0).test(input.getItem(1)) && this.inputs.get(1).test(input.getItem(0));
        }
        return false;
    }

    @Override
    public ItemStack assemble(MachineInput input, HolderLookup.Provider registries) {
        return this.results.isEmpty() ? ItemStack.EMPTY : this.results.getFirst().copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.results.isEmpty() ? ItemStack.EMPTY : this.results.getFirst();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.CLAY_MACHINE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ClayiumRecipeTypes.byCategory(this.categoryId).get();
    }

    public record MachineInput(List<ItemStack> slots, int machineTier) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index >= 0 && index < this.slots.size() ? this.slots.get(index) : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return this.slots.size();
        }
    }

    public static final class Serializer implements RecipeSerializer<ClayMachineRecipe> {
        public static final MapCodec<ClayMachineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(ClayMachineRecipe::categoryId),
                CountedIngredient.CODEC.listOf(1, 2).fieldOf("inputs").forGetter(ClayMachineRecipe::inputs),
                ItemStack.STRICT_CODEC.listOf(1, 4).fieldOf("results").forGetter(ClayMachineRecipe::results),
                Codec.INT.optionalFieldOf("tier", 0).forGetter(ClayMachineRecipe::tier),
                Codec.LONG.optionalFieldOf("energy", 0L).forGetter(ClayMachineRecipe::energyPerTick),
                Codec.LONG.optionalFieldOf("time", 1L).forGetter(ClayMachineRecipe::durationTicks),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(ClayMachineRecipe::priority)
        ).apply(instance, ClayMachineRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClayMachineRecipe> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public ClayMachineRecipe decode(RegistryFriendlyByteBuf buf) {
                String categoryId = ByteBufCodecs.STRING_UTF8.decode(buf);
                List<CountedIngredient> inputs = CountedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                List<ItemStack> results = ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                int tier = ByteBufCodecs.VAR_INT.decode(buf);
                long energy = ByteBufCodecs.VAR_LONG.decode(buf);
                long duration = ByteBufCodecs.VAR_LONG.decode(buf);
                int priority = ByteBufCodecs.VAR_INT.decode(buf);
                return new ClayMachineRecipe(categoryId, inputs, results, tier, energy, duration, priority);
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, ClayMachineRecipe value) {
                ByteBufCodecs.STRING_UTF8.encode(buf, value.categoryId());
                CountedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, value.inputs());
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, value.results());
                ByteBufCodecs.VAR_INT.encode(buf, value.tier());
                ByteBufCodecs.VAR_LONG.encode(buf, value.energyPerTick());
                ByteBufCodecs.VAR_LONG.encode(buf, value.durationTicks());
                ByteBufCodecs.VAR_INT.encode(buf, value.priority());
            }
        };

        @Override
        public MapCodec<ClayMachineRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClayMachineRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
