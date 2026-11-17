package net.claustra01.clayium.gametest.recipe;

import io.netty.buffer.Unpooled;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeCache;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeInput;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.recipe.ClayiumRecipesReloadedEvent;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.RecipeRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.connection.ConnectionType;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MachineRecipeGameTests {
    private static final ResourceLocation TEST_RECIPE = ClayiumMod.id("gametest/machine_recipe");
    private static final ResourceLocation CONDITIONAL_RECIPE =
            ClayiumMod.id("gametest/conditional_missing_external_item");

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void machineRecipeParsingAndExecutionConditionsAreLossless(GameTestHelper helper) {
        RecipeManager manager = helper.getLevel().getRecipeManager();
        require(RecipeRegistration.types().size() == ClayiumRecipeMethod.values().length,
                "Not every original processing method has a recipe type");
        for (ClayiumRecipeMethod method : ClayiumRecipeMethod.values()) {
            require(BuiltInRegistries.RECIPE_TYPE.containsKey(ClayiumMod.id(method.id())),
                    "Processing method recipe type is missing: " + method.id());
        }
        RecipeHolder<?> holder = manager.byKey(TEST_RECIPE)
                .orElseThrow(() -> new GameTestAssertException("Generated machine recipe was not loaded"));
        require(holder.value() instanceof ClayiumMachineRecipe, "Generated recipe has the wrong serializer");
        ClayiumMachineRecipe recipe = (ClayiumMachineRecipe) holder.value();

        require(recipe.processingMethod() == ClayiumRecipeMethod.BENDING_MACHINE, "Processing method was not parsed");
        require(recipe.getType() == RecipeRegistration.type(ClayiumRecipeMethod.BENDING_MACHINE), "Recipe type differs from method");
        require(recipe.requiredTier() == 4, "Required tier was not parsed");
        require(recipe.itemInputs().size() == 1 && recipe.itemInputs().getFirst().count() == 2, "Item input was not parsed");
        require(recipe.fluidInputs().size() == 1 && recipe.fluidInputs().getFirst().getAmount() == 250,
                "Fluid input was not parsed");
        require(recipe.itemOutputs().size() == 1
                        && recipe.itemOutputs().getFirst().is(MaterialRegistration.COMPRESSION_BLOCKS.get("dense_clay").get().asItem()),
                "Item output was not parsed");
        require(recipe.fluidOutputs().size() == 1
                        && recipe.fluidOutputs().getFirst().is(Fluids.LAVA)
                        && recipe.fluidOutputs().getFirst().getAmount() == 125,
                "Fluid output was not parsed");
        require(recipe.clayEnergy() == 750_000L, "Clay Energy was not parsed as a long");
        require(recipe.processingTicks() == 40, "Processing ticks were not parsed");
        require(recipe.randomOutputs().size() == 1
                        && recipe.randomOutputs().getFirst().weight() == 7
                        && recipe.randomOutputs().getFirst().chance() == 0.25F,
                "Weighted probability output was not parsed");
        require(ClayiumRecipeEnergy.e(1.0D, 4) == 100L, "CRecipes.e equivalent differs at Tier 4");

        BlockPos machinePosition = helper.absolutePos(BlockPos.ZERO);
        ClayiumRecipeInput valid = input(4, 750_000L, 2, 250, machinePosition);
        require(recipe.matches(valid, helper.getLevel()), "Valid server input did not match");
        require(!recipe.matches(input(3, 750_000L, 2, 250, machinePosition), helper.getLevel()),
                "Tier-deficient input matched");
        require(!recipe.matches(input(4, 749_999L, 2, 250, machinePosition), helper.getLevel()),
                "Energy-deficient input matched");
        require(!recipe.matches(input(4, 750_000L, 1, 250, machinePosition), helper.getLevel()),
                "Item-deficient input matched");
        require(!recipe.matches(input(4, 750_000L, 2, 249, machinePosition), helper.getLevel()),
                "Fluid-deficient input matched");
        require(!recipe.matches(input(4, 750_000L, 2, 250, new BlockPos(0, 400, 0)), helper.getLevel()),
                "Environment-deficient input matched");
        JsonObject validJson = (JsonObject) ClayiumMachineRecipe.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, recipe).getOrThrow();
        assertInvalid(validJson, "clay_energy", -1);
        JsonObject noOutput = validJson.deepCopy();
        noOutput.remove("item_outputs");
        noOutput.remove("fluid_outputs");
        noOutput.remove("random_outputs");
        require(ClayiumMachineRecipe.CODEC.codec().parse(JsonOps.INSTANCE, noOutput).error().isPresent(),
                "Recipe without output was accepted");
        assertInvalid(validJson, "processing_method", "clayium:missing_method");
        JsonObject badChance = validJson.deepCopy();
        badChance.getAsJsonArray("random_outputs").get(0).getAsJsonObject().addProperty("chance", 1.5F);
        require(ClayiumMachineRecipe.CODEC.codec().parse(JsonOps.INSTANCE, badChance).error().isPresent(),
                "Invalid chance was accepted");
        JsonObject badEnvironment = validJson.deepCopy();
        badEnvironment.getAsJsonObject("environment").addProperty("minimum_y", 400);
        require(ClayiumMachineRecipe.CODEC.codec().parse(JsonOps.INSTANCE, badEnvironment).error().isPresent(),
                "Inverted Y range was accepted");

        require(ClayiumRecipeCache.find(
                        manager, RecipeRegistration.type(ClayiumRecipeMethod.BENDING_MACHINE), valid, helper.getLevel())
                .map(RecipeHolder::id).filter(TEST_RECIPE::equals).isPresent(), "Indexed recipe search missed a valid recipe");
        require(ClayiumRecipeCache.find(
                        manager, RecipeRegistration.type(ClayiumRecipeMethod.WIRE_DRAWING_MACHINE), valid, helper.getLevel())
                .isEmpty(), "A recipe matched the wrong processing method");

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(
                Unpooled.buffer(), helper.getLevel().registryAccess(), ConnectionType.OTHER);
        try {
            ClayiumMachineRecipe.STREAM_CODEC.encode(buffer, recipe);
            ClayiumMachineRecipe decoded = ClayiumMachineRecipe.STREAM_CODEC.decode(buffer);
            require(decoded.processingMethod() == recipe.processingMethod(), "StreamCodec lost processing method");
            require(decoded.requiredTier() == recipe.requiredTier(), "StreamCodec lost tier");
            require(decoded.clayEnergy() == recipe.clayEnergy(), "StreamCodec lost Clay Energy");
            require(decoded.processingTicks() == recipe.processingTicks(), "StreamCodec lost processing ticks");
            require(decoded.itemInputs().getFirst().count() == 2, "StreamCodec lost input count");
            require(decoded.fluidInputs().getFirst().getAmount() == 250, "StreamCodec lost fluid input");
            require(decoded.randomOutputs().getFirst().weight() == 7
                            && decoded.randomOutputs().getFirst().chance() == 0.25F,
                    "StreamCodec lost random output data");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 600)
    public static void datapackReloadReplacesCacheAndSkipsMissingExternalItem(GameTestHelper helper) {
        MinecraftServer server = helper.getLevel().getServer();
        Path pack = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("clayium-reload-gametest");
        removeTestPack(server, pack);
        RecipeManager manager = server.getRecipeManager();
        require(manager.byKey(CONDITIONAL_RECIPE).isEmpty(), "Missing external item recipe was registered");
        ResourceLocation changedId = ClayiumMod.id("gametest/reload_changed");
        ResourceLocation removedId = ClayiumMod.id("gametest/reload_removed");
        ResourceLocation addedId = ClayiumMod.id("gametest/reload_added");
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        ClayiumRecipeInput clay = new ClayiumRecipeInput(
                List.of(new ItemStack(Items.CLAY)), List.of(), 4, 1_000_000L, position);
        ClayiumRecipeInput brick = new ClayiumRecipeInput(
                List.of(new ItemStack(Items.BRICK)), List.of(), 4, 1_000_000L, position);
        ClayiumRecipeInput iron = new ClayiumRecipeInput(
                List.of(new ItemStack(Items.IRON_INGOT)), List.of(), 4, 1_000_000L, position);
        var type = RecipeRegistration.type(ClayiumRecipeMethod.ASSEMBLER);
        require(ClayiumRecipeCache.find(manager, type, clay, helper.getLevel())
                .map(RecipeHolder::id).filter(changedId::equals).isPresent(), "Initial cache was not built");
        require(ClayiumRecipeCache.find(manager, type, brick, helper.getLevel())
                .map(RecipeHolder::id).filter(removedId::equals).isPresent(), "Initial removed recipe is absent");
        require(ClayiumRecipeCache.find(manager, type, iron, helper.getLevel()).isEmpty(),
                "Added recipe exists before reload");

        AtomicInteger notifications = new AtomicInteger();
        Consumer<ClayiumRecipesReloadedEvent> listener = event -> {
            if (event.recipeManager() == server.getRecipeManager()
                    && helper.getLevel().getRecipeManager() == event.recipeManager()
                    && ClayiumRecipeCache.find(event.recipeManager(), type, clay, helper.getLevel())
                            .map(RecipeHolder::value).map(ClayiumMachineRecipe::processingTicks)
                            .filter(ticks -> ticks == 80).isPresent()) {
                notifications.incrementAndGet();
            }
        };
        NeoForge.EVENT_BUS.addListener(ClayiumRecipesReloadedEvent.class, listener);
        try {
            Path recipes = pack.resolve("data/clayium/recipe/gametest");
            Files.createDirectories(recipes);
            Files.writeString(pack.resolve("pack.mcmeta"),
                    "{\"pack\":{\"pack_format\":48,\"description\":\"Clayium GameTest reload\"}}");
            writeRecipe(recipes.resolve("reload_changed.json"), simpleRecipe(80, 900_000L, Items.CLAY));
            writeRecipe(recipes.resolve("reload_added.json"), simpleRecipe(10, 50_000L, Items.IRON_INGOT));
            Files.writeString(recipes.resolve("reload_removed.json"),
                    "{\"neoforge:conditions\":[{\"type\":\"neoforge:mod_loaded\",\"modid\":\"absent_mod\"}],"
                    + "\"type\":\"clayium:machine\",\"processing_method\":\"clayium:assembler\","
                    + "\"required_tier\":4,\"item_outputs\":[{\"id\":\"minecraft:diamond\",\"count\":1}],"
                    + "\"clay_energy\":200000,\"processing_ticks\":30}");
            server.getPackRepository().reload();
            List<String> selected = new ArrayList<>(server.getPackRepository().getSelectedIds());
            selected.add("file/clayium-reload-gametest");
            server.reloadResources(selected).whenComplete((ignored, error) -> {
                if (error != null) {
                    server.execute(() -> { throw new GameTestAssertException("Datapack reload failed: " + error); });
                }
            });
        } catch (IOException error) {
            NeoForge.EVENT_BUS.unregister(listener);
            throw new GameTestAssertException("Could not create test datapack: " + error);
        }
        helper.succeedWhen(() -> {
            require(notifications.get() == 1, "Reload notification did not see the applied manager");
            RecipeManager current = server.getRecipeManager();
            require(current.byKey(CONDITIONAL_RECIPE).isEmpty(), "Missing external item recipe was registered");
            require(ClayiumRecipeCache.find(current, type, brick, helper.getLevel()).isEmpty(),
                    "Removed recipe survived in cache");
            require(ClayiumRecipeCache.find(current, type, iron, helper.getLevel())
                    .map(RecipeHolder::id).filter(addedId::equals).isPresent(), "Added recipe absent from cache");
            require(ClayiumRecipeCache.find(current, type, clay, helper.getLevel())
                    .map(RecipeHolder::value)
                    .filter(recipe -> recipe.processingTicks() == 80 && recipe.clayEnergy() == 900_000L)
                    .isPresent(), "Changed values are stale in cache");
            NeoForge.EVENT_BUS.unregister(listener);
            removeTestPack(server, pack);
        });
    }

    private static void removeTestPack(MinecraftServer server, Path pack) {
        if (!Files.exists(pack)) {
            return;
        }
        try (var files = Files.walk(pack)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(file);
            }
        } catch (IOException error) {
            throw new GameTestAssertException("Could not remove test datapack: " + error);
        }
        server.getPackRepository().reload();
        List<String> selected = new ArrayList<>(server.getPackRepository().getSelectedIds());
        selected.remove("file/clayium-reload-gametest");
        server.reloadResources(selected).join();
    }

    private static void writeRecipe(Path file, ClayiumMachineRecipe recipe) throws IOException {
        JsonObject json = (JsonObject) ClayiumMachineRecipe.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, recipe).getOrThrow();
        json.addProperty("type", "clayium:machine");
        Files.writeString(file, json.toString());
    }

    private static void assertInvalid(JsonObject source, String key, Number value) {
        JsonObject invalid = source.deepCopy();
        invalid.addProperty(key, value);
        require(ClayiumMachineRecipe.CODEC.codec().parse(JsonOps.INSTANCE, invalid).error().isPresent(),
                "Invalid " + key + " was accepted");
    }

    private static void assertInvalid(JsonObject source, String key, String value) {
        JsonObject invalid = source.deepCopy();
        invalid.addProperty(key, value);
        require(ClayiumMachineRecipe.CODEC.codec().parse(JsonOps.INSTANCE, invalid).error().isPresent(),
                "Invalid " + key + " was accepted");
    }

    private static ClayiumRecipeInput input(int tier, long energy, int itemCount, int fluidAmount, BlockPos position) {
        return new ClayiumRecipeInput(
                List.of(new ItemStack(Items.TERRACOTTA, itemCount)),
                List.of(new FluidStack(Fluids.WATER, fluidAmount)),
                tier,
                energy,
                position);
    }

    private static ClayiumMachineRecipe simpleRecipe(int ticks, long energy, net.minecraft.world.level.ItemLike input) {
        return new ClayiumMachineRecipe(
                ClayiumRecipeMethod.ASSEMBLER,
                4,
                List.of(new SizedIngredient(
                        net.minecraft.world.item.crafting.Ingredient.of(input), 1)),
                List.of(),
                List.of(new ItemStack(Items.DIAMOND)),
                List.of(),
                energy,
                ticks,
                ClayiumMachineRecipe.EnvironmentCondition.ANY,
                List.of());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private MachineRecipeGameTests() {
    }
}
