package net.claustra01.clayium;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.FilterConfigData;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.StorageContentData;
import net.claustra01.clayium.datagen.recipe.ClayiumRecipeHelper;
import net.claustra01.clayium.recipe.AutoEatGadgetSwitchRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe;
import net.claustra01.clayium.recipe.ChemicalMetalSeparatorRecipe.WeightedOutput;
import net.claustra01.clayium.recipe.ClayMachineRecipe;
import net.claustra01.clayium.recipe.ClayMachineRecipe.MachineInput;
import net.claustra01.clayium.recipe.ClayWorkTableRecipe;
import net.claustra01.clayium.recipe.CountedIngredient;
import net.claustra01.clayium.recipe.FluidCapsuleContainerRecipe;
import net.claustra01.clayium.recipe.FluidCapsuleTransferRecipe;
import net.claustra01.clayium.recipe.StorageContainerUpgradeRecipe;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.registry.ClayiumItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecipeParityTest {
    private static RegistryAccess.Frozen registries;

    @BeforeAll
    static void setup() {
        Bootstrap.bootStrap();
        registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    @Test
    void recipeEnergyFormulaMatchesOriginal() {
        assertEquals(10L, ClayiumRecipeHelper.e(3));
        assertEquals(100L, ClayiumRecipeHelper.e(4));
        assertEquals(2500000L, ClayiumRecipeHelper.e(2.5, 8));
        assertEquals(900000000000L, ClayiumRecipeHelper.e(90.0, 12));
        assertEquals(1000000000000L, ClayiumRecipeHelper.e(10.0, 13));
    }

    @Test
    void clayMachineRecipeCodecAndStreamRoundTripPreservesLargeLongValues() {
        ClayMachineRecipe original = new ClayMachineRecipe(
                "reactor",
                List.of(
                        new CountedIngredient(Ingredient.of(Items.DIAMOND), 4),
                        new CountedIngredient(Ingredient.of(Items.EMERALD), 16)
                ),
                List.of(new ItemStack(Items.NETHER_STAR, 2)),
                13,
                100000000000L,
                1000000000000000000L,
                -5
        );

        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        DataResult<JsonElement> encoded = ClayMachineRecipe.Serializer.CODEC.codec().encodeStart(ops, original);
        assertTrue(encoded.isSuccess(), () -> "Encode failed: " + encoded.error());

        DataResult<ClayMachineRecipe> decoded = ClayMachineRecipe.Serializer.CODEC.codec().parse(ops, encoded.getOrThrow());
        assertTrue(decoded.isSuccess(), () -> "Decode failed: " + decoded.error());
        ClayMachineRecipe jsonCopy = decoded.getOrThrow();
        assertEquals("reactor", jsonCopy.categoryId());
        assertEquals(13, jsonCopy.tier());
        assertEquals(100000000000L, jsonCopy.energyPerTick());
        assertEquals(1000000000000000000L, jsonCopy.durationTicks());
        assertEquals(-5, jsonCopy.priority());
        assertEquals(2, jsonCopy.inputs().size());
        assertEquals(16, jsonCopy.inputs().get(1).count());
        assertEquals(2, jsonCopy.results().getFirst().getCount());

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        ClayMachineRecipe.Serializer.STREAM_CODEC.encode(buf, original);
        ClayMachineRecipe netCopy = ClayMachineRecipe.Serializer.STREAM_CODEC.decode(buf);
        assertEquals(1000000000000000000L, netCopy.durationTicks());
        assertEquals(100000000000L, netCopy.energyPerTick());
        assertEquals(13, netCopy.tier());
    }

    @Test
    void clayMachineRecipeMatchingHandlesTierCountSwappedOrderAndExtraSlotRejection() {
        ClayMachineRecipe oneInput = new ClayMachineRecipe(
                "grinder",
                List.of(new CountedIngredient(Ingredient.of(Items.COBBLESTONE), 4)),
                List.of(new ItemStack(Items.GRAVEL, 4)),
                4,
                100L,
                10L,
                0
        );
        assertFalse(oneInput.matches(new MachineInput(List.of(new ItemStack(Items.COBBLESTONE, 4)), 3), null));
        assertFalse(oneInput.matches(new MachineInput(List.of(new ItemStack(Items.COBBLESTONE, 3)), 4), null));
        assertTrue(oneInput.matches(new MachineInput(List.of(new ItemStack(Items.COBBLESTONE, 4)), 4), null));
        assertFalse(oneInput.matches(new MachineInput(List.of(new ItemStack(Items.COBBLESTONE, 4), new ItemStack(Items.DIRT)), 4), null));

        ClayMachineRecipe twoInputs = new ClayMachineRecipe(
                "alloy_smelter",
                List.of(
                        new CountedIngredient(Ingredient.of(Items.COPPER_INGOT), 3),
                        new CountedIngredient(Ingredient.of(Items.GOLD_INGOT), 1)
                ),
                List.of(new ItemStack(Items.NETHERITE_INGOT, 1)),
                6,
                10000L,
                40L,
                0
        );
        assertTrue(twoInputs.matches(new MachineInput(List.of(new ItemStack(Items.COPPER_INGOT, 3), new ItemStack(Items.GOLD_INGOT, 1)), 6), null));
        assertTrue(twoInputs.matches(new MachineInput(List.of(new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.COPPER_INGOT, 3)), 6), null));
        assertFalse(twoInputs.matches(new MachineInput(List.of(new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.COPPER_INGOT, 2)), 6), null));
    }

    @Test
    void clayWorkTableButtonToolRulesAndRoundTripMatchOriginal() {
        ClayWorkTableRecipe recipe = new ClayWorkTableRecipe(
                new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1),
                3,
                new ItemStack(Items.CLAY, 1),
                new ItemStack(Items.CLAY_BALL, 2),
                4
        );
        RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
        JsonElement json = ClayWorkTableRecipe.Serializer.CODEC.codec().encodeStart(ops, recipe).getOrThrow();
        ClayWorkTableRecipe parsed = ClayWorkTableRecipe.Serializer.CODEC.codec().parse(ops, json).getOrThrow();
        assertEquals(3, parsed.buttonId());
        assertEquals(4, parsed.clicks());
        assertEquals(2, parsed.secondaryResult().getCount());

        ItemStack pin = new ItemStack(ClayiumItems.byPath("clay_rolling_pin").get());
        ItemStack slicer = new ItemStack(ClayiumItems.byPath("clay_slicer").get());
        ItemStack spatula = new ItemStack(ClayiumItems.byPath("clay_spatula").get());
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(1, ItemStack.EMPTY));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(2, ItemStack.EMPTY));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(3, pin));
        assertFalse(ClayWorkTableRecipe.toolMatchesButton(3, slicer));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(4, slicer));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(4, spatula));
        assertFalse(ClayWorkTableRecipe.toolMatchesButton(5, slicer));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(5, spatula));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(6, slicer));
        assertTrue(ClayWorkTableRecipe.toolMatchesButton(6, spatula));
    }

    @Test
    void chemicalMetalSeparatorRollBoundaryMatchesWeightedSum() {
        ChemicalMetalSeparatorRecipe recipe = new ChemicalMetalSeparatorRecipe(
                new CountedIngredient(Ingredient.of(Items.CLAY_BALL), 1),
                6,
                5000L,
                40L,
                List.of(
                        new WeightedOutput(new ItemStack(Items.IRON_NUGGET), 200),
                        new WeightedOutput(new ItemStack(Items.GOLD_NUGGET), 288)
                )
        );
        assertEquals(488, recipe.totalWeight());
        assertTrue(recipe.selectRandom(new FixedRandom(0)).is(Items.IRON_NUGGET));
        assertTrue(recipe.selectRandom(new FixedRandom(199)).is(Items.IRON_NUGGET));
        assertTrue(recipe.selectRandom(new FixedRandom(200)).is(Items.GOLD_NUGGET));
        assertTrue(recipe.selectRandom(new FixedRandom(487)).is(Items.GOLD_NUGGET));
    }

    @Test
    void specialCraftingRecipesPreserveComponentsAndTransformCorrectly() {
        // AutoEat toggle preserves FILTER_CONFIG
        AutoEatGadgetSwitchRecipe autoEatRecipe = new AutoEatGadgetSwitchRecipe(CraftingBookCategory.MISC);
        ItemStack eat0 = new ItemStack(ClayiumItems.byPath("gadget_auto_eat0").get());
        FilterConfigData filter = new FilterConfigData("apple", true, ItemContainerContents.EMPTY);
        eat0.set(ClayiumDataComponents.FILTER_CONFIG.get(), filter);
        CraftingInput eatIn = CraftingInput.of(1, 1, List.of(eat0));
        assertTrue(autoEatRecipe.matches(eatIn, null));
        ItemStack eat1 = autoEatRecipe.assemble(eatIn, registries);
        assertTrue(eat1.is(ClayiumItems.byPath("gadget_auto_eat1").get()));
        assertEquals(filter, eat1.get(ClayiumDataComponents.FILTER_CONFIG.get()));

        // FluidCapsule 1000 mB -> 8x 125 mB -> 1000 mB round-trip
        FluidCapsuleTransferRecipe transferRecipe = new FluidCapsuleTransferRecipe(CraftingBookCategory.MISC);
        ItemStack cap1000 = new ItemStack(ClayiumItems.byPath("fluid_capsule_1000").get());
        cap1000.set(ClayiumDataComponents.FLUID_CONTENT.get(), SimpleFluidContent.copyOf(new FluidStack(Fluids.WATER, 1000)));
        ItemStack eight125 = transferRecipe.assemble(CraftingInput.of(1, 1, List.of(cap1000)), registries);
        assertTrue(eight125.is(ClayiumItems.byPath("fluid_capsule_125").get()));
        assertEquals(8, eight125.getCount());
        assertEquals(125, eight125.get(ClayiumDataComponents.FLUID_CONTENT.get()).getAmount());

        List<ItemStack> eightSingle125 = List.of(
                eight125.copyWithCount(1), eight125.copyWithCount(1), eight125.copyWithCount(1), eight125.copyWithCount(1),
                eight125.copyWithCount(1), eight125.copyWithCount(1), eight125.copyWithCount(1), eight125.copyWithCount(1)
        );
        ItemStack compressed1000 = transferRecipe.assemble(CraftingInput.of(3, 3, List.of(
                eightSingle125.get(0), eightSingle125.get(1), eightSingle125.get(2),
                eightSingle125.get(3), eightSingle125.get(4), eightSingle125.get(5),
                eightSingle125.get(6), eightSingle125.get(7), ItemStack.EMPTY
        )), registries);
        assertTrue(compressed1000.is(ClayiumItems.byPath("fluid_capsule_1000").get()));
        assertEquals(1, compressed1000.getCount());
        assertEquals(1000, compressed1000.get(ClayiumDataComponents.FLUID_CONTENT.get()).getAmount());

        // StorageContainer upgrade preserves stored item & count, upgrades maxStorage to Integer.MAX_VALUE
        StorageContainerUpgradeRecipe storageRecipe = new StorageContainerUpgradeRecipe(CraftingBookCategory.MISC);
        ItemStack container = new ItemStack(ClayiumItems.byPath("storage_container").get());
        container.set(ClayiumDataComponents.STORAGE_CONTENT.get(), new StorageContentData(Items.DIAMOND, 12345L, 65536));
        ItemStack core = new ItemStack(ClayiumItems.byPath("clay_core").get());
        CraftingInput storageIn = CraftingInput.of(2, 1, List.of(container, core));
        assertTrue(storageRecipe.matches(storageIn, null));
        ItemStack upgraded = storageRecipe.assemble(storageIn, registries);
        StorageContentData upgradedData = upgraded.get(ClayiumDataComponents.STORAGE_CONTENT.get());
        assertEquals(Items.DIAMOND, upgradedData.item());
        assertEquals(12345L, upgradedData.count());
        assertEquals(Integer.MAX_VALUE, upgradedData.maxStorage());
        // Already-upgraded storage_container cannot be upgraded again
        assertFalse(storageRecipe.matches(CraftingInput.of(2, 1, List.of(upgraded, core)), null));
    }

    @Test
    void fluidCapsuleContainerRecipeExchangesWithBucketWhenCapabilitiesPresent() {
        // Attach vanilla FluidBucketWrapper handler if NeoForge bootstrap capabilities are active
        if (FluidUtil.getFluidHandler(new ItemStack(Items.WATER_BUCKET)).isEmpty()) {
            return;
        }
        FluidCapsuleContainerRecipe recipe = new FluidCapsuleContainerRecipe(CraftingBookCategory.MISC);
        ItemStack emptyCap = new ItemStack(ClayiumItems.byPath("fluid_capsule_1000").get());
        ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
        CraftingInput packIn = CraftingInput.of(2, 1, List.of(emptyCap, waterBucket));
        assertTrue(recipe.matches(packIn, null));
        ItemStack filledCap = recipe.assemble(packIn, registries);
        assertTrue(filledCap.is(ClayiumItems.byPath("fluid_capsule_1000").get()));
        assertEquals(Fluids.WATER, filledCap.get(ClayiumDataComponents.FLUID_CONTENT.get()).getFluid());
        assertEquals(1000, filledCap.get(ClayiumDataComponents.FLUID_CONTENT.get()).getAmount());
        var remaining = recipe.getRemainingItems(packIn);
        assertTrue(remaining.get(0).isEmpty());
        assertTrue(remaining.get(1).is(Items.BUCKET));

        CraftingInput unpackIn = CraftingInput.of(2, 1, List.of(filledCap, new ItemStack(Items.BUCKET)));
        assertTrue(recipe.matches(unpackIn, null));
        ItemStack refilledBucket = recipe.assemble(unpackIn, registries);
        assertTrue(refilledBucket.is(Items.WATER_BUCKET));
        assertTrue(recipe.getRemainingItems(unpackIn).get(0).isEmpty());
        assertTrue(recipe.getRemainingItems(unpackIn).get(1).isEmpty());
    }

    private static final class FixedRandom implements RandomSource {
        private final int nextIntVal;

        FixedRandom(int nextIntVal) {
            this.nextIntVal = nextIntVal;
        }

        @Override
        public int nextInt(int bound) {
            return this.nextIntVal;
        }

        @Override
        public RandomSource fork() {
            return this;
        }

        @Override
        public net.minecraft.world.level.levelgen.PositionalRandomFactory forkPositional() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSeed(long seed) {}

        @Override
        public int nextInt() {
            return this.nextIntVal;
        }

        @Override
        public long nextLong() {
            return this.nextIntVal;
        }

        @Override
        public boolean nextBoolean() {
            return false;
        }

        @Override
        public float nextFloat() {
            return 0.0F;
        }

        @Override
        public double nextDouble() {
            return 0.0;
        }

        @Override
        public double nextGaussian() {
            return 0.0;
        }
    }
}
