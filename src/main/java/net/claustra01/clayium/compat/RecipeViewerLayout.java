package net.claustra01.clayium.compat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record RecipeViewerLayout(
        int width,
        int height,
        Background background,
        List<Slot> slots,
        List<TextLine> text,
        Progress progress, int operation) {
    public static final ResourceLocation NEI_TEXTURE = ClayiumMod.id("textures/gui/nei.png");
    public static final int WIDTH = 166;
    public static final int HEIGHT = 65;
    public static final int TIER_X = 6;
    public static final int TIER_Y = 43;
    public static final int ENERGY_X = 6;
    public static final int ENERGY_Y = 52;
    private static final Background NEI_BACKGROUND = new Background(NEI_TEXTURE, 5, 11, WIDTH, HEIGHT, 256, 256);
    private static final Progress NEI_PROGRESS = new Progress(71, 21, 24, 17);
    private static final Background WORK_TABLE = new Background(ClayiumMod.id("textures/gui/clayworktable.png"),
            5, 11, WIDTH, HEIGHT, 256, 256);

    public static RecipeViewerLayout machine(ClayiumMachineRecipe recipe) {
        RecipeViewerData data = RecipeViewerData.from(recipe);
        List<Slot> slots = new ArrayList<>();
        if (recipe.processingMethod() == ClayiumRecipeMethod.CLAY_WORK_TABLE) {
            slots.add(new Slot(SlotKind.ITEM_INPUT, 12, 19, 0));
            if (!data.workToolItems().isEmpty()) {
                slots.add(new Slot(SlotKind.TOOL, 75, 6, 0));
            }
            for (int i = 0; i < recipe.itemOutputs().size(); i++) {
                slots.add(new Slot(SlotKind.ITEM_OUTPUT, 138, 19 + 25 * i, i));
            }
            var count = new TextLine(Component.literal(Long.toString(data.processingTicks())),
                    43 + 16 * (data.requiredTier() - 1), 31, 0xFF000000, true);
            return new RecipeViewerLayout(WIDTH, HEIGHT, WORK_TABLE, List.copyOf(slots), List.of(count),
                    new Progress(43, 18, 80, 16), data.requiredTier());
        }
        int inputIndex = 0;
        for (int index = 0; index < recipe.itemInputs().size(); index++) {
            slots.add(new Slot(SlotKind.ITEM_INPUT, inputX(inputIndex++), 21, index));
        }
        for (int index = 0; index < recipe.fluidInputs().size(); index++) {
            slots.add(new Slot(SlotKind.FLUID_INPUT, inputX(inputIndex++), 21, index));
        }
        int outputIndex = 0;
        for (int index = 0; index < recipe.itemOutputs().size(); index++) {
            slots.add(new Slot(SlotKind.ITEM_OUTPUT, outputX(outputIndex++), 21, index));
        }
        for (int index = 0; index < recipe.fluidOutputs().size(); index++) {
            slots.add(new Slot(SlotKind.FLUID_OUTPUT, outputX(outputIndex++), 21, index));
        }
        if (recipe.processingMethod() == ClayiumRecipeMethod.CHEMICAL_METAL_SEPARATOR && !recipe.randomOutputs().isEmpty()) {
            slots.add(new Slot(SlotKind.WEIGHTED_OUTPUTS, outputX(outputIndex), 21, -1));
        } else {
            for (int index = 0; index < recipe.randomOutputs().size(); index++) {
                slots.add(new Slot(SlotKind.RANDOM_OUTPUT, outputX(outputIndex++), 21, index));
            }
        }

        List<TextLine> machineText = new ArrayList<>(List.of(
                new TextLine(Component.translatable(
                        "gui.clayium.recipe.tier", data.requiredTier()), TIER_X, TIER_Y, 0xFF000000),
                new TextLine(Component.translatable(
                        "gui.clayium.recipe.energy_equation",
                        data.formattedClayEnergyPerTick(), data.processingTicks(), data.formattedClayEnergy()),
                        ENERGY_X, ENERGY_Y, 0xFF000000)));
        if (data.structureDependent()) {
            machineText.add(new TextLine(Component.translatable("gui.clayium.recipe.structure"),
                    85, 43, 0xFF000000));
        }
        return new RecipeViewerLayout(WIDTH, HEIGHT, NEI_BACKGROUND, List.copyOf(slots),
                List.copyOf(machineText), NEI_PROGRESS, 0);
    }

    public static RecipeViewerLayout pan(int tier) {
        return new RecipeViewerLayout(
                WIDTH,
                HEIGHT,
                NEI_BACKGROUND,
                List.of(
                        new Slot(SlotKind.ITEM_INPUT, inputX(0), 21, 0),
                        new Slot(SlotKind.ITEM_INPUT, inputX(1), 21, 1),
                        new Slot(SlotKind.ITEM_OUTPUT, outputX(0), 21, 0)),
                List.of(
                        new TextLine(Component.translatable("gui.clayium.recipe.tier", tier),
                                TIER_X, TIER_Y, 0xFF000000),
                        new TextLine(Component.translatable("gui.clayium.recipe.pan_rate",
                                net.claustra01.clayium.energy.ClayEnergy.ofInternal(
                                        RecipeViewerData.panEnergyPerTick(tier)).formatValue()),
                                85, TIER_Y, 0xFF000000),
                        new TextLine(Component.translatable("gui.clayium.recipe.pan_sample"), 32, 6, 0xFF000000),
                        new TextLine(Component.translatable("gui.clayium.recipe.pan_ticks"), ENERGY_X, ENERGY_Y, 0xFF000000)),
                NEI_PROGRESS, 0);
    }

    public static int totalRandomWeight(ClayiumMachineRecipe recipe) {
        return recipe.randomOutputs().stream().mapToInt(ClayiumMachineRecipe.RandomOutput::weight).sum();
    }

    public static double randomOutputPercent(
            ClayiumMachineRecipe recipe, ClayiumMachineRecipe.RandomOutput output) {
        int totalWeight = totalRandomWeight(recipe);
        return 100.0D * output.chance() * output.weight() / totalWeight;
    }

    public static String formattedRandomOutputPercent(
            ClayiumMachineRecipe recipe, ClayiumMachineRecipe.RandomOutput output) {
        return BigDecimal.valueOf(randomOutputPercent(recipe, output))
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static int inputX(int index) {
        return 52 - index * 17;
    }

    private static int outputX(int index) {
        return 98 + index * 17;
    }

    public record Background(
            ResourceLocation texture, int u, int v, int width, int height, int textureWidth, int textureHeight) {
    }

    public record Slot(SlotKind kind, int x, int y, int sourceIndex) {
    }

    public record TextLine(Component value, int x, int y, int color, boolean centered) {
        TextLine(Component value, int x, int y, int color) {
            this(value, x, y, color, false);
        }
    }

    public record Progress(int x, int y, int width, int height) {
    }

    public enum SlotKind {
        ITEM_INPUT,
        FLUID_INPUT,
        TOOL,
        ITEM_OUTPUT,
        FLUID_OUTPUT,
        RANDOM_OUTPUT,
        WEIGHTED_OUTPUTS
    }
}
