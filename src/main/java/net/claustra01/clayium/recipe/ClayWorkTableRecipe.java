package net.claustra01.clayium.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.claustra01.clayium.registry.ClayiumItems;
import net.claustra01.clayium.registry.ClayiumRecipeSerializers;
import net.claustra01.clayium.registry.ClayiumRecipeTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Clay Work Table kneading/tool recipe preserving button ID, tool constraints, secondary drops, and required clicks.
 */
public record ClayWorkTableRecipe(
        CountedIngredient input,
        int buttonId,
        ItemStack primaryResult,
        ItemStack secondaryResult,
        int clicks) implements Recipe<ClayWorkTableRecipe.Input> {

    public static boolean toolMatchesButton(int buttonId, ItemStack tool) {
        return switch (buttonId) {
            case 3 -> !tool.isEmpty() && tool.is(ClayiumItems.byPath("clay_rolling_pin").get());
            case 4, 6 -> !tool.isEmpty() && (tool.is(ClayiumItems.byPath("clay_slicer").get())
                    || tool.is(ClayiumItems.byPath("clay_spatula").get()));
            case 5 -> !tool.isEmpty() && tool.is(ClayiumItems.byPath("clay_spatula").get());
            default -> true;
        };
    }

    @Override
    public boolean matches(Input input, Level level) {
        return input.buttonId() == this.buttonId && this.input.test(input.material());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return this.primaryResult.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.primaryResult;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ClayiumRecipeSerializers.CLAY_WORK_TABLE.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ClayiumRecipeTypes.CLAY_WORK_TABLE.get();
    }

    public record Input(ItemStack material, int buttonId) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? this.material : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    public static final class Serializer implements RecipeSerializer<ClayWorkTableRecipe> {
        public static final MapCodec<ClayWorkTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CountedIngredient.CODEC.fieldOf("input").forGetter(ClayWorkTableRecipe::input),
                ExtraCodecs.POSITIVE_INT.fieldOf("button").forGetter(ClayWorkTableRecipe::buttonId),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(ClayWorkTableRecipe::primaryResult),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("secondary_result", ItemStack.EMPTY).forGetter(ClayWorkTableRecipe::secondaryResult),
                ExtraCodecs.POSITIVE_INT.fieldOf("clicks").forGetter(ClayWorkTableRecipe::clicks)
        ).apply(instance, ClayWorkTableRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ClayWorkTableRecipe> STREAM_CODEC = StreamCodec.composite(
                CountedIngredient.STREAM_CODEC, ClayWorkTableRecipe::input,
                ByteBufCodecs.VAR_INT, ClayWorkTableRecipe::buttonId,
                ItemStack.STREAM_CODEC, ClayWorkTableRecipe::primaryResult,
                ItemStack.OPTIONAL_STREAM_CODEC, ClayWorkTableRecipe::secondaryResult,
                ByteBufCodecs.VAR_INT, ClayWorkTableRecipe::clicks,
                ClayWorkTableRecipe::new
        );

        @Override
        public MapCodec<ClayWorkTableRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ClayWorkTableRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
