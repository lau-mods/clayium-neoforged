package net.claustra01.clayium.gametest.forming;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.series.FormingSeries;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FormingMachineGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allTwentySixFormingMachinesMatchTheOriginalTierLedger(GameTestHelper helper) {
        Map<String, int[]> expected = new LinkedHashMap<>();
        expected.put("BendingMachine", new int[] {1, 2, 3, 4, 5, 6, 7, 9});
        expected.put("WireDrawingMachine", new int[] {1, 2, 3, 4});
        expected.put("PipeDrawingMachine", new int[] {1, 2, 3, 4});
        expected.put("CuttingMachine", new int[] {1, 2, 3, 4});
        expected.put("Lathe", new int[] {1, 2, 3, 4});
        expected.put("MillingMachine", new int[] {3, 4});

        require(FormingMachineRegistration.MACHINES.size() == 26,
                "The forming registry must contain exactly 26 machines");
        for (FormingSeries series : FormingSeries.values()) {
            OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                    .filter(candidate -> candidate.legacyName().equals(series.legacyName()))
                    .findFirst().orElseThrow();
            require(java.util.Arrays.equals(series.tiers(), expected.get(family.legacyName())),
                    family.legacyName() + " has an invented or missing tier");
            require(java.util.Arrays.equals(series.tiers(), family.tiers()),
                    family.legacyName() + " differs from the original registry catalog");
            for (int tier : series.tiers()) {
                String id = OriginalRegistryCatalog.machineId(family, tier);
                FormingMachineRegistration.MachineDefinition definition =
                        FormingMachineRegistration.MACHINES.get(id);
                require(definition != null, "Missing forming machine: " + id);
                require(definition.tier() == tier && definition.series().recipeMethod() == series.recipeMethod(),
                        id + " has the wrong tier or recipe type");
                require(BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(id))
                                && BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(id)),
                        id + " is not registered as a matching block and item");
                ClayMachineBlock block = definition.block().get();
                require(block.series() == series && block.tier() == tier,
                        id + " does not use the shared machine definition");
                ResourceLocation craftingId = ClayiumMod.id("forming_machine/" + id);
                RecipeHolder<?> crafting = helper.getLevel().getRecipeManager().byKey(craftingId).orElseThrow();
                require(crafting.value() instanceof ShapedRecipe,
                        id + " has no shaped construction recipe");
                ShapedRecipe shaped = (ShapedRecipe)crafting.value();
                List<ItemStack> grid = shaped.getIngredients().stream()
                        .map(ingredient -> ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy())
                        .toList();
                RecipeHolder<?> matched = helper.getLevel().getRecipeManager().getRecipeFor(
                        RecipeType.CRAFTING, CraftingInput.of(shaped.getWidth(), shaped.getHeight(), grid),
                        helper.getLevel()).orElseThrow();
                require(matched.id().equals(craftingId), id + " crafting grid resolves to " + matched.id());
            }
        }
        require(!FormingMachineRegistration.MACHINES.containsKey("bending_machine_tier_8"),
                "The missing original bending-machine tier 8 was invented");
        require(!FormingMachineRegistration.MACHINES.containsKey("milling_machine_tier_1")
                        && !FormingMachineRegistration.MACHINES.containsKey("milling_machine_tier_2"),
                "The original milling-machine tier gap was filled");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void representativeFormingRecipesPreserveOriginalInputsOutputsAndConditions(GameTestHelper helper) {
        for (String plate : new String[] {"antimatter_plate", "pure_antimatter_plate",
                "octuple_energetic_clay_plate", "octuple_pure_antimatter_plate"}) {
            RecipeHolder<?> holder = helper.getLevel().getRecipeManager()
                    .byKey(ClayiumMod.id("bending_machine/" + plate)).orElseThrow();
            require(holder.value() instanceof ClayiumMachineRecipe
                            && ((ClayiumMachineRecipe)holder.value()).requiredTier() == 9,
                    plate + " must be available to the original tier 9 bending machine");
        }
        List<RecipeExample> examples = List.of(
                example("bending_machine/industrial_clay_plate", FormingSeries.BENDING_MACHINE, 2, 2,
                        material("industrial_clay"), 1, material("industrial_clay_plate"), 1, 2L, 4),
                example("wire_drawing_machine/clay_cylinder_to_sticks", FormingSeries.WIRE_DRAWING_MACHINE, 0, 1,
                        material("clay_cylinder"), 1, material("clay_stick"), 8, 1L, 3),
                example("pipe_drawing_machine/dense_clay_pipe", FormingSeries.PIPE_DRAWING_MACHINE, 0, 1,
                        material("dense_clay_cylinder"), 1, material("dense_clay_pipe"), 2, 1L, 6),
                example("cutting_machine/small_clay_disc", FormingSeries.CUTTING_MACHINE, 0, 1,
                        material("clay_plate"), 1, material("small_clay_disc"), 4, 1L, 3),
                example("lathe/dense_clay_ring", FormingSeries.LATHE, 0, 1,
                        material("dense_clay_disc"), 1, material("dense_clay_ring"), 1, 1L, 4),
                example("milling_machine/clay_circuit_board", FormingSeries.MILLING_MACHINE, 0, 3,
                        material("industrial_clay_plate"), 1,
                        MaterialRegistration.COMPONENTS.get("clay_circuit_board").get(), 1, 1L, 1),
                example("bending_machine/aluminium_plate", FormingSeries.BENDING_MACHINE, 4, 4,
                        material("aluminium_ingot"), 1, material("aluminium_plate"), 1, 100L, 20),
                example("bending_machine/ultimate_alloy_plate", FormingSeries.BENDING_MACHINE, 4, 4,
                        material("ultimate_alloy_ingot"), 1, material("ultimate_alloy_plate"), 1, 100L, 180),
                example("bending_machine/clay_steel_plate", FormingSeries.BENDING_MACHINE, 4, 4,
                        material("clay_steel_ingot"), 1, material("clay_steel_plate"), 1, 100L, 60),
                example("bending_machine/large_aluminium_plate", FormingSeries.BENDING_MACHINE, 4, 4,
                        material("aluminium_plate"), 4, material("large_aluminium_plate"), 1, 100L, 40),
                example("lathe/dense_clay_cylinder", FormingSeries.LATHE, 0, 1,
                        material("dense_clay"), 1, material("dense_clay_cylinder"), 1, 1L, 4),
                example("cutting_machine/large_dense_clay_plate_to_discs", FormingSeries.CUTTING_MACHINE, 0, 1,
                        material("large_dense_clay_plate"), 1, material("dense_clay_disc"), 2, 1L, 6));

        for (RecipeExample example : examples) {
            RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(example.id()).orElseThrow();
            require(holder.value() instanceof ClayiumMachineRecipe,
                    example.id() + " is not a Clayium machine recipe");
            ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)holder.value();
            require(recipe.processingMethod() == example.series().recipeMethod()
                            && recipe.requiredTier() == example.requiredTier()
                            && recipe.itemInputs().size() == 1
                            && recipe.itemInputs().getFirst().count() == example.inputCount()
                            && recipe.itemInputs().getFirst().ingredient().test(new ItemStack(example.input()))
                            && recipe.itemOutputs().size() == 1
                            && recipe.itemOutputs().getFirst().is(example.output().asItem())
                            && recipe.itemOutputs().getFirst().getCount() == example.outputCount()
                            && recipe.clayEnergy() == example.energy()
                            && recipe.processingTicks() == example.ticks(),
                    example.id() + " differs from the original recipe values");

            ClayMachineBlockEntity machine = machine(helper, example.series(), example.machineTier());
            machine.inventory().setStackInSlot(
                    0, new ItemStack(example.input(), example.inputCount()));
            machine.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000L));
            tick(helper, machine);
            TierManager.ProcessingParameters parameters = TierManager.calculate(
                    TierManager.general(example.machineTier()), example.ticks(), example.energy(), 1.0D);
            require(machine.activeRecipe() != null
                            && machine.requiredProgress() == parameters.requiredProgress(),
                    example.id() + " did not start in the shared processing engine");
            runUntilIdle(helper, machine, (int)parameters.requiredProgress() + 2);
            ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
            require(result.is(example.output().asItem()) && result.getCount() == example.outputCount(),
                    example.id() + " produced the wrong item balance");
            require(machine.inventory().getStackInSlot(0).isEmpty()
                            && machine.inventory().getStackInSlot(machine.reservedSlot(0)).isEmpty(),
                    example.id() + " did not consume exactly its reserved input");
            require(machine.energyStorage().stored().internalValue()
                            == 1_000_000L - parameters.totalEnergy().internalValue(),
                    example.id() + " consumed the wrong amount of CE");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void consecutiveFormingRecipesStayRunningBetweenOutputs(GameTestHelper helper) {
        for (FormingSeries series : List.of(FormingSeries.LATHE, FormingSeries.MILLING_MACHINE)) {
            int tier = series == FormingSeries.LATHE ? 1 : 3;
            ItemLike input = material(series == FormingSeries.LATHE ? "dense_clay_disc" : "dense_clay_plate");
            ItemLike output = series == FormingSeries.LATHE
                    ? material("dense_clay_ring")
                    : MaterialRegistration.COMPONENTS.get("clay_circuit_board").get();
            ClayMachineBlockEntity machine = machine(helper, series, tier);
            machine.inventory().setStackInSlot(0, new ItemStack(input, 3));
            machine.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000L));
            tick(helper, machine);
            for (int tick = 0; tick < 32
                    && machine.inventory().getStackInSlot(machine.outputSlot()).isEmpty(); tick++) {
                tick(helper, machine);
            }
            require(machine.inventory().getStackInSlot(machine.outputSlot()).is(output.asItem()),
                    series + " did not complete its first output");
            require(machine.state() == ClayMachineBlockEntity.MachineState.RUNNING
                            && machine.activeRecipe() != null
                            && machine.inventory().getStackInSlot(0).getCount() == 1
                            && machine.inventory().getStackInSlot(machine.reservedSlot(0)).getCount() == 1,
                    series + " showed idle between consecutive recipes");
        }
        helper.succeed();
    }

    private static RecipeExample example(
            String id,
            FormingSeries series,
            int requiredTier,
            int machineTier,
            ItemLike input,
            int inputCount,
            ItemLike output,
            int outputCount,
            long energy,
            int ticks) {
        return new RecipeExample(
                ClayiumMod.id(id), series, requiredTier, machineTier,
                input, inputCount, output, outputCount, energy, ticks);
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static ClayMachineBlockEntity machine(
            GameTestHelper helper, FormingSeries series, int tier) {
        FormingMachineRegistration.MachineDefinition definition =
                FormingMachineRegistration.MACHINES.get(OriginalRegistryCatalog.machineId(
                        OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                                .filter(candidate -> candidate.legacyName().equals(series.legacyName()))
                                .findFirst().orElseThrow(), tier));
        require(definition != null, "Missing machine fixture for " + series + " tier " + tier);
        BlockState state = definition.block().get().defaultBlockState();
        BlockPos pos = new BlockPos(1, 2, 1);
        helper.setBlock(pos, Blocks.AIR);
        helper.setBlock(pos, state);
        return helper.getBlockEntity(pos);
    }

    private static void tick(GameTestHelper helper, ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void runUntilIdle(
            GameTestHelper helper, ClayMachineBlockEntity machine, int maximumTicks) {
        for (int tick = 0; tick < maximumTicks && machine.activeRecipe() != null; tick++) {
            tick(helper, machine);
        }
        require(machine.activeRecipe() == null, "A forming recipe did not complete in time");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private record RecipeExample(
            ResourceLocation id,
            FormingSeries series,
            int requiredTier,
            int machineTier,
            ItemLike input,
            int inputCount,
            ItemLike output,
            int outputCount,
            long energy,
            int ticks) {
    }

    private FormingMachineGameTests() {
    }
}
