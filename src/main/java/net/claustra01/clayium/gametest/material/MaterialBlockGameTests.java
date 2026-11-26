package net.claustra01.clayium.gametest.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.series.ElementalMillingSeries;
import net.claustra01.clayium.material.MaterialBlockDefinition;
import net.claustra01.clayium.material.SiliconeBlockDefinition;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MaterialBlockGameTests {
    private static final List<String> RESTORED_HULLS = List.of(
            "dense_clay_machine_hull",
            "pure_antimatter_machine_hull",
            "oec_machine_hull",
            "opa_machine_hull",
            "az91_d_alloy_hull",
            "zk60_a_alloy_hull");

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allEightyThreeRestoredVarietiesHaveRegistriesDataAndNames(GameTestHelper helper) {
        require(MaterialRegistration.METAL_BLOCKS.size() == 57, "The original 57 metal blocks are incomplete");
        require(MaterialRegistration.SILICONE_BLOCKS.size() == 16, "The 16 silicone colors are incomplete");
        require(MaterialRegistration.SHARDS.size() == 3, "The three compressed-clay shards are incomplete");
        Set<String> originalIds = OriginalRegistryCatalog.ORIGINAL_VARIETIES.stream()
                .map(OriginalRegistryCatalog.OriginalVariety::namedId)
                .collect(java.util.stream.Collectors.toSet());
        JsonObject english = jsonResource("assets/clayium_neoforged/lang/en_us.json");
        JsonObject japanese = jsonResource("assets/clayium_neoforged/lang/ja_jp.json");
        List<String> restoredBlocks = new ArrayList<>();
        restoredBlocks.addAll(MaterialRegistration.METAL_BLOCKS.keySet());
        restoredBlocks.addAll(MaterialRegistration.SILICONE_BLOCKS.keySet());
        restoredBlocks.addAll(RESTORED_HULLS);
        restoredBlocks.add("elemental_milling_machine");
        require(restoredBlocks.size() + MaterialRegistration.SHARDS.size() == 83,
                "The restored ledger must contain exactly 83 varieties");
        int translated = 0;
        try (InputStream stream = resource("data/clayium_neoforged/original_material_translations.tsv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                String[] columns = line.split("\t", -1);
                if (columns.length == 3 && (restoredBlocks.contains(columns[0].replace("block.clayium_neoforged.", ""))
                        || MaterialRegistration.SHARDS.containsKey(columns[0].replace("item.clayium_neoforged.", "")))) {
                    require(english.get(columns[0]).getAsString().equals(columns[1])
                                    && japanese.get(columns[0]).getAsString().equals(columns[2]),
                            "Generated display name differs from the original lang ledger: " + columns[0]);
                    translated++;
                }
            }
        } catch (IOException exception) {
            throw new GameTestAssertException("Unable to read original translation ledger: " + exception.getMessage());
        }
        require(translated == 83, "The original translation ledger lacks a restored variety");

        for (String id : restoredBlocks) {
            require(originalIds.contains(id), "The restored ID is absent from the original ledger: " + id);
            require(BuiltInRegistries.BLOCK.containsKey(ClayiumMod.id(id)), "Missing block registry: " + id);
            require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(id)), "Missing block item registry: " + id);
            require(english.has("block.clayium_neoforged." + id), "Missing English name: " + id);
            require(japanese.has("block.clayium_neoforged." + id), "Missing Japanese name: " + id);
            jsonResource("assets/clayium_neoforged/blockstates/" + id + ".json");
            jsonResource("assets/clayium_neoforged/models/block/" + id + ".json");
            jsonResource("assets/clayium_neoforged/models/item/" + id + ".json");
            jsonResource("data/clayium_neoforged/loot_table/blocks/" + id + ".json");
            Block block = BuiltInRegistries.BLOCK.get(ClayiumMod.id(id));
            require(block.defaultBlockState().is(block), "The restored block has no placeable default state: " + id);
            if (MaterialRegistration.METAL_BLOCKS.containsKey(id)
                    || MaterialRegistration.SILICONE_BLOCKS.containsKey(id)) {
                require(block.defaultBlockState().is(BlockTags.MINEABLE_WITH_PICKAXE)
                                && block.defaultBlockState().requiresCorrectToolForDrops(),
                        "The material block does not preserve its pickaxe harvest requirement: " + id);
            }
        }
        for (String id : MaterialRegistration.SHARDS.keySet()) {
            require(originalIds.contains(id), "The shard is absent from the original ledger: " + id);
            require(MaterialRegistration.SHARDS.get(id).get().getDefaultInstance()
                            .get(FoundationDataComponentRegistration.TIER.get()) == null,
                    "The original untiered shard unexpectedly has a Tier: " + id);
            require(BuiltInRegistries.ITEM.containsKey(ClayiumMod.id(id)), "Missing shard registry: " + id);
            require(english.has("item.clayium_neoforged." + id) && japanese.has("item.clayium_neoforged." + id),
                    "Missing shard translation: " + id);
            jsonResource("assets/clayium_neoforged/models/item/" + id + ".json");
        }
        for (MaterialBlockDefinition definition : MaterialBlockDefinition.ORIGINAL_METAL_BLOCKS) {
            Integer tier = MaterialRegistration.METAL_BLOCKS.get(definition.id()).get().asItem()
                    .getDefaultInstance().get(FoundationDataComponentRegistration.TIER.get());
            require(tier != null && tier == definition.tier(),
                    "Metal block tier differs from its registered material tier: " + definition.id());
        }
        for (String id : RESTORED_HULLS) {
            var definition = AssemblyMachineRegistration.MACHINE_HULL_DEFINITIONS.get(id);
            Integer tier = definition.block().get().asItem().getDefaultInstance()
                    .get(FoundationDataComponentRegistration.TIER.get());
            require(tier != null && tier == definition.tier(), "Wrong hull tier: " + id);
        }
        ClayMachineBlock elemental = FormingMachineRegistration.ELEMENTAL_MILLING_MACHINE.get();
        require(elemental.series() == ElementalMillingSeries.INSTANCE && elemental.tier() == 1,
                "The elemental milling machine is not an independent tier-one machine");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void oreShardsAndCondensationPreserveOriginalBalances(GameTestHelper helper) {
        List<MachineExample> examples = List.of(
                grinder("clay_ore", WorldgenRegistration.CLAY_ORE.get(), "compressed_clay_shard", 2, 3),
                grinder("deepslate_clay_ore", WorldgenRegistration.DEEPSLATE_CLAY_ORE.get(),
                        "compressed_clay_shard", 2, 3),
                grinder("dense_clay_ore", WorldgenRegistration.DENSE_CLAY_ORE.get(),
                        "industrial_clay_shard", 3, 6),
                grinder("deepslate_dense_clay_ore", WorldgenRegistration.DEEPSLATE_DENSE_CLAY_ORE.get(),
                        "industrial_clay_shard", 3, 6),
                grinder("large_dense_clay_ore", WorldgenRegistration.LARGE_DENSE_CLAY_ORE.get(),
                        "advanced_industrial_clay_shard", 5, 9),
                grinder("deepslate_large_dense_clay_ore", WorldgenRegistration.DEEPSLATE_LARGE_DENSE_CLAY_ORE.get(),
                        "advanced_industrial_clay_shard", 5, 9),
                condenser("compressed_clay_shard", "compressed_clay", 3),
                condenser("industrial_clay_shard", "industrial_clay", 6),
                condenser("advanced_industrial_clay_shard", "advanced_industrial_clay", 9));
        for (MachineExample example : examples) {
            ClayiumMachineRecipe recipe = recipe(helper, example.recipeId());
            require(recipe.processingMethod() == example.method()
                            && recipe.requiredTier() == 0
                            && recipe.clayEnergy() == 1L
                            && recipe.processingTicks() == example.ticks()
                            && recipe.itemInputs().size() == 1
                            && recipe.itemInputs().getFirst().count() == example.inputCount()
                            && recipe.itemInputs().getFirst().ingredient().test(new ItemStack(example.input()))
                            && recipe.itemOutputs().size() == 1
                            && recipe.itemOutputs().getFirst().is(example.output().asItem())
                            && recipe.itemOutputs().getFirst().getCount() == example.outputCount(),
                    "Shard route differs from hotfix2: " + example.recipeId());
            ClayMachineBlockEntity machine = processingMachine(helper, example.method(), 2);
            machine.inventory().setStackInSlot(0, new ItemStack(example.input(), example.inputCount()));
            machine.receiveClayEnergy(ClayEnergy.ofInternal(10_000L));
            long initialEnergy = machine.energyStorage().stored().internalValue();
            int elapsed = run(machine, helper, 50);
            long expectedEnergy = TierManager.calculate(TierManager.general(2),
                    recipe.processingTicks(), recipe.clayEnergy(), 1.0D).totalEnergy().internalValue();
            require(elapsed == example.ticks(), "Wrong shard processing time: " + example.recipeId());
            require(initialEnergy - machine.energyStorage().stored().internalValue() == expectedEnergy,
                    "Wrong shard CE consumption: " + example.recipeId());
            require(machine.inventory().getStackInSlot(0).isEmpty()
                            && machine.inventory().getStackInSlot(machine.reservedSlot(0)).isEmpty(),
                    "Shard input was not consumed exactly once: " + example.recipeId());
            ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
            require(result.is(example.output().asItem()) && result.getCount() == example.outputCount(),
                    "Shard route did not execute exactly once: " + example.recipeId());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void metalAndSiliconeRecipesPreserveMaterialWithoutGrowth(GameTestHelper helper) {
        for (MaterialBlockDefinition definition : MaterialBlockDefinition.ORIGINAL_METAL_BLOCKS) {
            String material = definition.id().substring(0, definition.id().length() - "_block".length());
            Item ingot = MaterialRegistration.MATERIAL_ITEMS.get(material + "_ingot").get();
            Item block = MaterialRegistration.METAL_BLOCKS.get(definition.id()).get().asItem();
            List<ItemStack> ingots = java.util.Collections.nCopies(9, new ItemStack(ingot));
            ItemStack madeBlock;
            if (MaterialRegistration.isVanillaBacked(material + "_ingot")) {
                CraftingInput input = CraftingInput.of(3, 3, ingots);
                var recipe = helper.getLevel().getRecipeManager()
                        .byKey(ClayiumMod.id("material_block/" + definition.id()))
                        .orElseThrow(() -> new GameTestAssertException(
                                "Missing metal block recipe: " + definition.id()));
                require(recipe.value() instanceof net.minecraft.world.item.crafting.CraftingRecipe,
                        "Metal block recipe is not a crafting recipe: " + definition.id());
                madeBlock = ((net.minecraft.world.item.crafting.CraftingRecipe)recipe.value())
                        .assemble(input, helper.getLevel().registryAccess());
            } else {
                madeBlock = craft(helper, 3, 3, ingots);
            }
            require(madeBlock.is(block) && madeBlock.getCount() == 1,
                    "Nine ingots did not create their matching block: " + definition.id());
            ItemStack restored = craft(helper, 1, 1, List.of(new ItemStack(block)));
            require(restored.is(ingot) && restored.getCount() == 9,
                    "Metal block did not restore exactly nine matching ingots: " + definition.id());
            assertPlacedBlockDropsItself(helper, MaterialRegistration.METAL_BLOCKS.get(definition.id()).get());
        }
        Item silicone = MaterialRegistration.MATERIAL_ITEMS.get("silicone_block").get();
        for (SiliconeBlockDefinition definition : SiliconeBlockDefinition.ORIGINAL_COLORS) {
            List<ItemStack> ingredients = new ArrayList<>();
            for (int count = 0; count < 8; count++) {
                ingredients.add(new ItemStack(silicone));
            }
            ingredients.add(new ItemStack(DyeItem.byColor(definition.color())));
            ItemStack dyed = craft(helper, 3, 3, ingredients);
            Block colored = MaterialRegistration.SILICONE_BLOCKS.get(definition.id()).get();
            require(dyed.is(colored.asItem()) && dyed.getCount() == 8,
                    "Silicone dyeing changed the 8-to-8 balance: " + definition.id());
            require(colored.getStateDefinition().getProperties().stream()
                            .noneMatch(property -> property.getName().equals("color")),
                    "Silicone color was stored in mutable block state instead of the registry ID: " + definition.id());
            assertPlacedBlockDropsItself(helper, colored);
            ItemStack reloaded = ItemStack.parse(helper.getLevel().registryAccess(),
                    dyed.save(helper.getLevel().registryAccess())).orElseThrow();
            require(reloaded.is(colored.asItem()) && reloaded.getCount() == 8,
                    "Dyed silicone identity or quantity changed during item save/load: " + definition.id());
            var saved = NbtUtils.writeBlockState(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(1, 1, 1))));
            var loaded = NbtUtils.readBlockState(
                    helper.getLevel().registryAccess().lookupOrThrow(Registries.BLOCK), saved);
            require(loaded.is(colored)
                            && BuiltInRegistries.BLOCK.getKey(loaded.getBlock()).equals(ClayiumMod.id(definition.id())),
                    "Placed silicone color identity does not survive state serialization: " + definition.id());
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void elementalMillingMachineExecutesItsOriginalFirstCircuitRecipe(GameTestHelper helper) {
        ClayiumMachineRecipe recipe = recipe(helper, "elemental_milling_machine/clay_circuit_board");
        ItemLike input = MaterialRegistration.MATERIAL_ITEMS.get("dense_clay_plate").get();
        ItemLike output = MaterialRegistration.COMPONENTS.get("clay_circuit_board").get();
        require(recipe.processingMethod() == ClayiumRecipeMethod.MILLING_MACHINE
                        && recipe.requiredTier() == 0
                        && recipe.clayEnergy() == 1L
                        && recipe.processingTicks() == 32L
                        && recipe.itemInputs().getFirst().ingredient().test(new ItemStack(input))
                        && recipe.itemOutputs().getFirst().is(output.asItem()),
                "The elemental milling recipe differs from hotfix2");
        ClayMachineBlockEntity machine = new ClayMachineBlockEntity(
                helper.absolutePos(BlockPos.ZERO),
                FormingMachineRegistration.ELEMENTAL_MILLING_MACHINE.get().defaultBlockState());
        machine.setLevel(helper.getLevel());
        machine.inventory().setStackInSlot(0, new ItemStack(input));
        machine.receiveClayEnergy(ClayEnergy.ofInternal(10_000L));
        long initialEnergy = machine.energyStorage().stored().internalValue();
        int elapsed = run(machine, helper, 40);
        long expectedEnergy = TierManager.calculate(TierManager.general(1),
                recipe.processingTicks(), recipe.clayEnergy(), 1.0D).totalEnergy().internalValue();
        require(elapsed == 32 && initialEnergy - machine.energyStorage().stored().internalValue() == expectedEnergy,
                "Elemental milling time or CE differs from the dedicated recipe");
        require(machine.inventory().getStackInSlot(0).isEmpty()
                        && machine.inventory().getStackInSlot(machine.reservedSlot(0)).isEmpty(),
                "Elemental milling did not consume its input exactly once");
        require(machine.inventory().getStackInSlot(machine.outputSlot()).is(output.asItem()),
                "The independent elemental milling machine did not produce the first circuit board");
        require(FormingMachineRegistration.MACHINES.size() == 26
                        && !FormingMachineRegistration.MACHINES.containsKey("elemental_milling_machine"),
                "The independent elemental machine was folded into the ordinary 26-machine series");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allOriginalPngHashesAndTintLayersRemainUnmodified(GameTestHelper helper) {
        int count = 0;
        Set<String> required = Set.of(
                "textures/block/metalblock_base.png",
                "textures/block/metalblock_dark.png",
                "textures/block/metalblock_light.png",
                "textures/block/siliconecolored.png",
                "textures/block/claymillingmachine.png",
                "textures/item/compressedclay-shard-1.png",
                "textures/item/compressedclay-shard-2.png",
                "textures/item/compressedclay-shard-3.png");
        java.util.HashSet<String> manifestPaths = new java.util.HashSet<>();
        try (InputStream stream = resource("assets/clayium_neoforged/original_png.sha256");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                String[] fields = line.split("  ", 2);
                require(fields.length == 2, "Malformed original PNG manifest line");
                String path = fields[1];
                manifestPaths.add(path);
                try (InputStream png = resource("assets/clayium_neoforged/" + path)) {
                    String actual = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(png.readAllBytes()));
                    require(actual.equals(fields[0]), "Original PNG bytes changed: " + path);
                }
                count++;
            }
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new GameTestAssertException("Unable to verify original PNG hashes: " + exception.getMessage());
        }
        require(count == 416, "The original PNG manifest must contain exactly 416 entries");
        require(manifestPaths.containsAll(required), "An M38 texture is not tracked by the original PNG manifest");
        JsonObject metal = jsonResource("assets/clayium_neoforged/models/block/copper_block.json");
        require(metal.getAsJsonObject("textures").size() == 4
                        && metal.getAsJsonArray("elements").size() == 3,
                "Metal blocks do not use the original base/dark/light tint layers");
        JsonObject silicone = jsonResource("assets/clayium_neoforged/models/block/red_silicone_block.json");
        require(silicone.getAsJsonArray("elements").get(0).getAsJsonObject()
                        .getAsJsonObject("faces").getAsJsonObject("north").get("tintindex").getAsInt() == 0,
                "Silicone blocks do not tint the original silhouette texture");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void derivedOreTexturesUseTheRecordedModernStoneBackgrounds(GameTestHelper helper) {
        int count = 0;
        try (InputStream stream = resource("assets/clayium_neoforged/derived_ore_png.sha256");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                String[] fields = line.split("  ", 2);
                require(fields.length == 2, "Malformed derived ore PNG manifest line");
                try (InputStream png = resource("assets/clayium_neoforged/" + fields[1])) {
                    String actual = HexFormat.of().formatHex(
                            MessageDigest.getInstance("SHA-256").digest(png.readAllBytes()));
                    require(actual.equals(fields[0]), "Derived ore PNG bytes changed: " + fields[1]);
                }
                count++;
            }
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new GameTestAssertException("Unable to verify derived ore PNG hashes: " + exception.getMessage());
        }
        require(count == 6, "The derived ore PNG manifest must contain exactly six entries");
        helper.succeed();
    }

    private static MachineExample grinder(String oreId, ItemLike input, String shard, int count, int ticks) {
        return new MachineExample(
                "grinder/" + oreId + "_to_" + shard,
                ClayiumRecipeMethod.GRINDER,
                input,
                1,
                MaterialRegistration.SHARDS.get(shard).get(),
                count,
                ticks);
    }

    private static MachineExample condenser(String shard, String output, int ticks) {
        return new MachineExample(
                "condenser/" + shard,
                ClayiumRecipeMethod.CONDENSER,
                MaterialRegistration.SHARDS.get(shard).get(),
                4,
                MaterialRegistration.COMPRESSION_BLOCKS.get(output).get(),
                1,
                ticks);
    }

    private static ClayiumMachineRecipe recipe(GameTestHelper helper, String id) {
        var holder = helper.getLevel().getRecipeManager().byKey(ClayiumMod.id(id)).orElseThrow();
        require(holder.value() instanceof ClayiumMachineRecipe, id + " is not a Clayium machine recipe");
        return (ClayiumMachineRecipe)holder.value();
    }

    private static ClayMachineBlockEntity processingMachine(
            GameTestHelper helper, ClayiumRecipeMethod method, int tier) {
        var definition = MachineRegistration.MACHINES.values().stream()
                .filter(candidate -> candidate.series().recipeMethod() == method && candidate.tier() == tier)
                .findFirst().orElseThrow();
        ClayMachineBlockEntity machine = new ClayMachineBlockEntity(
                helper.absolutePos(BlockPos.ZERO), definition.block().get().defaultBlockState());
        machine.setLevel(helper.getLevel());
        return machine;
    }

    private static int run(ClayMachineBlockEntity machine, GameTestHelper helper, int maximumTicks) {
        ClayMachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
        int ticks = 0;
        while (ticks < maximumTicks && machine.activeRecipe() != null) {
            ClayMachineBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
            ticks++;
        }
        require(machine.activeRecipe() == null, "Machine recipe did not finish within " + maximumTicks + " ticks");
        return ticks;
    }

    private static void assertPlacedBlockDropsItself(GameTestHelper helper, Block block) {
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlock(position, block.defaultBlockState(), 3);
        require(helper.getLevel().getBlockState(position).is(block), "Block was not placed: " + block);
        List<ItemStack> drops = Block.getDrops(helper.getLevel().getBlockState(position),
                helper.getLevel(), position, null, null, new ItemStack(Items.IRON_PICKAXE));
        require(drops.size() == 1 && drops.getFirst().is(block.asItem()) && drops.getFirst().getCount() == 1,
                "Pickaxe mining did not return exactly one matching block: " + block);
    }

    private static ItemStack craft(
            GameTestHelper helper, int width, int height, List<ItemStack> ingredients) {
        CraftingInput input = CraftingInput.of(width, height, ingredients);
        var recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .orElseThrow(() -> new GameTestAssertException("Missing crafting recipe for M38 material conversion"));
        return recipe.value().assemble(input, helper.getLevel().registryAccess());
    }

    private static JsonObject jsonResource(String path) {
        try (InputStream stream = resource(path);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException exception) {
            throw new GameTestAssertException("Unable to read generated resource: " + path);
        }
    }

    private static InputStream resource(String path) {
        InputStream stream = MaterialBlockGameTests.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new GameTestAssertException("Missing resource: " + path);
        }
        return stream;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private record MachineExample(
            String recipeId,
            ClayiumRecipeMethod method,
            ItemLike input,
            int inputCount,
            ItemLike output,
            int outputCount,
            int ticks) {
    }

    private MaterialBlockGameTests() {
    }
}
