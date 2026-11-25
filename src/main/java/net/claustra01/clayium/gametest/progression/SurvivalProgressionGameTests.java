package net.claustra01.clayium.gametest.progression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerData;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.gadget.GadgetType;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.iface.InterfaceType;
import net.claustra01.clayium.pan.PanBlock;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.NbtPreservingAutoEatRecipe;
import net.claustra01.clayium.registry.AdvancedToolRegistration;
import net.claustra01.clayium.registry.AssemblyMachineRegistration;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.CaReactorRegistration;
import net.claustra01.clayium.registry.EnvironmentMachineRegistration;
import net.claustra01.clayium.registry.FluidRegistration;
import net.claustra01.clayium.registry.FormingMachineRegistration;
import net.claustra01.clayium.registry.InterfaceRegistration;
import net.claustra01.clayium.registry.LaserRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.StorageRegistration;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.claustra01.clayium.storage.MetalChestType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SurvivalProgressionGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allOneHundredTwentyThreeCandidateMachinesHaveClassifiedRuntimePaths(
            GameTestHelper helper) {
        List<ResourceLocation> candidates = candidateRecipeIds();
        require(candidates.size() == 123, "The fixed candidate inventory no longer contains 123 machines");
        Map<String, String[]> ledger = readLedger();
        for (ResourceLocation id : candidates) {
            RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(id).orElseThrow();
            String expectedPath = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
            String[] row = ledger.get("clayium:" + expectedPath);
            require(row != null, id + " has no individual ledger row");
            require(List.of(row[3].split("\\|")).contains(id.toString()),
                    id + " is absent from its ledger row");
            require(row[2].equals("restored") || row[2].equals("explicit_change"),
                    id + " has an unexpected classification");
            require(holder.value().getResultItem(helper.getLevel().registryAccess()).is(
                            BuiltInRegistries.ITEM.get(ClayiumMod.id(expectedPath))),
                    id + " does not produce its classified machine");
            if (holder.value() instanceof ClayiumMachineRecipe recipe) {
                require(recipe.processingMethod().id().equals(row[4].substring("clayium:".length()))
                                && recipe.requiredTier() == Integer.parseInt(row[5])
                                && recipe.processingTicks() == Long.parseLong(row[6])
                                && recipe.clayEnergy() == Long.parseLong(row[7]),
                        id + " differs from the recorded method, Tier, time or CE");
                require(!recipe.itemInputs().isEmpty(), id + " has no input");
                String inputs = recipe.itemInputs().stream()
                        .map(input -> input.count() + "*" + BuiltInRegistries.ITEM.getKey(
                                input.ingredient().getItems()[0].getItem()))
                        .collect(java.util.stream.Collectors.joining("+"));
                require(inputs.equals(row[8]), id + " differs from the recorded input items or counts");
            }
        }
        helper.succeed();
    }

    private static Map<String, String[]> readLedger() {
        Map<String, String[]> rows = new HashMap<>();
        try (var stream = SurvivalProgressionGameTests.class.getResourceAsStream(
                        "/data/clayium/survival_recipe_ledger.tsv");
                var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            reader.readLine();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                String[] columns = line.split("\\t", -1);
                require(columns.length == 11, "Malformed ledger row for " + columns[1]);
                require(rows.put(columns[1], columns) == null, "Duplicate ledger row for " + columns[1]);
                require(!columns[2].equals("missing") && !columns[2].equals("unverified"),
                        "The survival ledger contains an unresolved classification");
            }
        } catch (IOException exception) {
            throw new GameTestAssertException("Could not read the survival recipe ledger: " + exception.getMessage());
        }
        return rows;
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void progressionRecipesForDifferentProductsNeverShareTheSameInputs(
            GameTestHelper helper) {
        Map<String, String> productsByInput = new HashMap<>();
        for (RecipeHolder<?> holder : helper.getLevel().getRecipeManager().getRecipes()) {
            if (!holder.id().getNamespace().equals(ClayiumMod.MOD_ID)
                    || !holder.id().getPath().startsWith("progression/")) {
                continue;
            }
            String signature;
            if (holder.value() instanceof ShapedRecipe shaped) {
                signature = "shaped:" + shaped.getWidth() + "x" + shaped.getHeight() + ":"
                        + shaped.getIngredients().stream().map(SurvivalProgressionGameTests::ingredientSignature)
                                .toList();
            } else if (holder.value() instanceof ShapelessRecipe shapeless) {
                signature = "shapeless:" + shapeless.getIngredients().stream()
                        .map(SurvivalProgressionGameTests::ingredientSignature).sorted().toList();
            } else if (holder.value() instanceof ClayiumMachineRecipe machine) {
                signature = machine.processingMethod().id() + ":" + machine.itemInputs().stream()
                        .map(input -> input.count() + "*" + ingredientSignature(input.ingredient()))
                        .sorted().toList();
            } else {
                continue;
            }
            String product = BuiltInRegistries.ITEM.getKey(
                    holder.value().getResultItem(helper.getLevel().registryAccess()).getItem()).toString();
            String previous = productsByInput.putIfAbsent(signature, product);
            require(previous == null || previous.equals(product),
                    holder.id() + " shadows the " + previous + " recipe with identical inputs");
        }
        helper.succeed();
    }

    private static String ingredientSignature(net.minecraft.world.item.crafting.Ingredient ingredient) {
        return java.util.Arrays.stream(ingredient.getItems())
                .map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .sorted().toList().toString();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void survivalDependencyGraphReachesNonTierCaPanAndContainersWithoutTierLeap(
            GameTestHelper helper) {
        List<String> representativePaths = List.of(
                "progression/power/clay_water_wheel",
                "progression/world/area_miner",
                "progression/laser/laser_reflector",
                "progression/environment/quartz_crucible",
                "progression/world/clay_3x3_chunk_loader",
                "progression/ca_reactor/ca_reactor_core_mk_iv",
                "progression/pan/pan_core",
                "progression/pan/pan_cable",
                "progression/storage/storage_container",
                "progression/storage/void_container");
        Set<net.minecraft.world.item.Item> reachable = new HashSet<>();
        BuiltInRegistries.ITEM.forEach(item -> {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft")
                    && isSurvivalVanillaSeed(item)) {
                reachable.add(item);
            }
        });
        for (String ore : List.of("clay_ore", "dense_clay_ore", "large_dense_clay_ore",
                "deepslate_clay_ore", "deepslate_dense_clay_ore", "deepslate_large_dense_clay_ore")) {
            reachable.add(BuiltInRegistries.ITEM.get(ClayiumMod.id(ore)));
        }
        boolean changed;
        do {
            changed = false;
            for (RecipeHolder<?> holder : helper.getLevel().getRecipeManager().getRecipes()) {
                if (holder.id().getNamespace().equals(ClayiumMod.MOD_ID)
                        && holder.id().getPath().startsWith("gametest/")) {
                    continue;
                }
                if (holder.value() instanceof ClayiumMachineRecipe recipe) {
                    if (!recipe.itemInputs().stream().allMatch(input ->
                                    reachableIngredient(input.ingredient(), reachable))
                            || !recipe.fluidInputs().isEmpty()
                            || !survivalEnvironmentAvailable(recipe.environment())
                            || !processingMachineAvailable(recipe, reachable)) {
                        continue;
                    }
                    for (ItemStack output : recipe.itemOutputs()) {
                        changed |= reachable.add(output.getItem());
                    }
                    for (var output : recipe.randomOutputs()) {
                        changed |= reachable.add(output.stack().getItem());
                    }
                } else if (holder.value().getIngredients().stream().allMatch(ingredient ->
                        reachableIngredient(ingredient, reachable))) {
                    changed |= reachable.add(holder.value()
                            .getResultItem(helper.getLevel().registryAccess()).getItem());
                }
            }
            if (reachable.contains(BuiltInRegistries.ITEM.get(ClayiumMod.id("quartz_crucible")))
                    && reachable.contains(BuiltInRegistries.ITEM.get(ClayiumMod.id("impure_silicon_ingot")))) {
                changed |= reachable.add(BuiltInRegistries.ITEM.get(ClayiumMod.id("silicon_ingot")));
            }
        } while (changed);
        for (ResourceLocation recipeId : candidateRecipeIds()) {
            String id = recipeId.getPath().substring(recipeId.getPath().lastIndexOf('/') + 1);
            if (!reachable.contains(BuiltInRegistries.ITEM.get(ClayiumMod.id(id)))) {
                RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(recipeId).orElseThrow();
                String blockedBy = holder.value() instanceof ClayiumMachineRecipe recipe
                        ? "inputs=" + recipe.itemInputs().stream()
                                .map(input -> input.count() + "*" + ingredientSignature(input.ingredient())
                                        + ":" + reachableIngredient(input.ingredient(), reachable))
                                .toList()
                                + ";method=" + recipe.processingMethod().id()
                                + ";machine=" + processingMachineAvailable(recipe, reachable)
                                + ";caMachines=" + CaRegistration.MACHINES.values().stream()
                                        .filter(machine -> machine.series().recipeMethod().id()
                                                .equals(recipe.processingMethod().id()))
                                        .map(machine -> machine.id() + ":"
                                                + reachable.contains(machine.block().get().asItem()))
                                        .toList()
                                + ";caTier9Inputs=" + List.of(
                                        "ultimate_machine_hull", "clay_reactor", "assembler_tier_6",
                                        "clay_laser_interface_tier_7").stream()
                                        .map(key -> key + ":" + reachable.contains(
                                                BuiltInRegistries.ITEM.get(ClayiumMod.id(key))))
                                        .toList()
                        : "ingredients=" + holder.value().getIngredients().stream()
                                .map(input -> ingredientSignature(input) + ":"
                                        + reachableIngredient(input, reachable))
                                .toList();
                throw new GameTestAssertException(id
                        + " is unreachable from world resources and obtainable machines; " + blockedBy);
            }
        }
        for (String path : representativePaths) {
            RecipeHolder<?> holder = helper.getLevel().getRecipeManager().byKey(ClayiumMod.id(path)).orElseThrow();
            require(reachable.contains(holder.value().getResultItem(helper.getLevel().registryAccess()).getItem()),
                    path + " is unreachable from world resources");
        }
        for (String[] row : readLedger().values()) {
            if (row[2].equals("restored") || row[2].equals("explicit_change")) {
                ResourceLocation id = ResourceLocation.parse(row[1]);
                require(reachable.contains(BuiltInRegistries.ITEM.get(id)),
                        id + " is listed as restored but has no survival dependency path");
            }
        }
        int machineVariants = 0;
        for (OriginalRegistryCatalog.MachineFamily family : OriginalRegistryCatalog.MACHINE_FAMILIES) {
            for (int tier : family.tiers()) {
                machineVariants++;
                String id = OriginalRegistryCatalog.machineId(family, tier);
                require(reachable.contains(BuiltInRegistries.ITEM.get(ClayiumMod.id(id))),
                        id + " is registered but unreachable from the survival dependency closure");
            }
        }
        require(machineVariants == 168, "The survival closure did not inspect all 168 machine variants");
        for (MetalChestType chest : MetalChestType.ALL) {
            require(reachable.contains(BuiltInRegistries.ITEM.get(ClayiumMod.id(chest.id()))),
                    chest.id() + " is registered but unreachable from the survival dependency closure");
        }
        require(MetalChestType.ALL.size() == 57, "The survival closure did not inspect all 57 metal chests");

        ShapelessRecipe wheel = (ShapelessRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id("progression/power/clay_water_wheel")).orElseThrow().value();
        CraftingInput input = CraftingInput.of(2, 1, wheel.getIngredients().stream()
                .map(ingredient -> ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0].copy())
                .toList());
        require(wheel.matches(input, helper.getLevel())
                        && wheel.assemble(input, helper.getLevel().registryAccess()).is(
                                BuiltInRegistries.ITEM.get(ClayiumMod.id("clay_water_wheel"))),
                "The first water wheel cannot actually be crafted from its listed inputs");
        processInPlacedAssembler(helper, "progression/laser/clay_energy_laser_tier_7", 6,
                new BlockPos(1, 2, 1));
        processInPlacedAssembler(helper, "progression/ca_reactor/ca_reactor_core_mk_i", 10,
                new BlockPos(3, 2, 1));
        processInPlacedAssembler(helper, "progression/pan/pan_cable", 10,
                new BlockPos(5, 2, 1));
        processInPlacedAssembler(helper, "progression/storage/storage_container", 4,
                new BlockPos(7, 2, 1));
        helper.succeed();
    }

    private static void processInPlacedAssembler(
            GameTestHelper helper, String path, int tier, BlockPos position) {
        ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(path)).orElseThrow().value();
        helper.setBlock(position, AssemblyMachineRegistration.MACHINES.get("assembler_tier_" + tier).block().get());
        ClayMachineBlockEntity machine = (ClayMachineBlockEntity)helper.getLevel()
                .getBlockEntity(helper.absolutePos(position));
        insertInputs(machine, recipe);
        machine.receiveClayEnergy(ClayEnergy.MAX);
        for (int index = 0; index < 10_000
                && machine.inventory().getStackInSlot(machine.outputSlot(0)).isEmpty(); index++) {
            tick(helper, machine);
        }
        ItemStack output = machine.inventory().getStackInSlot(machine.outputSlot(0));
        require(ItemStack.isSameItemSameComponents(output, recipe.itemOutputs().getFirst())
                        && output.getCount() == recipe.itemOutputs().getFirst().getCount(),
                path + " did not process in the placed Tier " + tier + " assembler");
    }

    private static boolean reachableIngredient(Ingredient ingredient, Set<net.minecraft.world.item.Item> reachable) {
        if (ingredient.isEmpty()) {
            return true;
        }
        return java.util.Arrays.stream(ingredient.getItems()).anyMatch(stack -> reachable.contains(stack.getItem()));
    }

    private static boolean survivalEnvironmentAvailable(
            ClayiumMachineRecipe.EnvironmentCondition condition) {
        if (condition.dimension().isPresent()
                && !condition.dimension().get().equals(net.minecraft.world.level.Level.OVERWORLD.location())) {
            return false;
        }
        int minimum = condition.minimumY().orElse(-64);
        int maximum = condition.maximumY().orElse(319);
        return minimum <= maximum && maximum >= -64 && minimum <= 319;
    }

    private static boolean isSurvivalVanillaSeed(net.minecraft.world.item.Item item) {
        return !Set.of(
                Items.AIR,
                Items.BARRIER,
                Items.BEDROCK,
                Items.COMMAND_BLOCK,
                Items.CHAIN_COMMAND_BLOCK,
                Items.REPEATING_COMMAND_BLOCK,
                Items.STRUCTURE_BLOCK,
                Items.STRUCTURE_VOID,
                Items.JIGSAW,
                Items.DEBUG_STICK,
                Items.KNOWLEDGE_BOOK,
                Items.LIGHT,
                Items.END_PORTAL_FRAME,
                Items.PETRIFIED_OAK_SLAB,
                Items.REINFORCED_DEEPSLATE).contains(item);
    }

    private static boolean processingMachineAvailable(
            ClayiumMachineRecipe recipe, Set<net.minecraft.world.item.Item> reachable) {
        String method = recipe.processingMethod().id();
        int tier = recipe.requiredTier();
        if (method.equals("clay_work_table")) {
            return reachable.contains(BuiltInRegistries.ITEM.get(ClayiumMod.id("clay_work_table")));
        }
        if (method.equals("reactor") || method.equals("blast_furnace")) {
            return reachable.contains(BuiltInRegistries.ITEM.get(
                    ClayiumMod.id(method.equals("reactor") ? "clay_reactor" : "clay_blast_furnace")))
                    && AssemblyMachineRegistration.MACHINE_HULLS.entrySet().stream()
                            .anyMatch(entry -> entry.getKey() >= tier
                                    && reachable.contains(entry.getValue().get().asItem()))
                    && (!method.equals("reactor") || interfaceAvailable(InterfaceType.LASER, tier, reachable));
        }
        if (method.equals("ca_reactor")) {
            return CaReactorRegistration.CORES.values().stream()
                    .anyMatch(core -> core.tier() >= tier
                            && reachable.contains(core.block().get().asItem())
                            && reachable.contains(CaReactorRegistration.COILS.get(core.tier()).block().get().asItem())
                            && CaReactorRegistration.HULLS.values().stream()
                                    .anyMatch(hull -> hull.rank() <= core.tier() - 3
                                            && reachable.contains(hull.block().get().asItem()))
                            && interfaceAvailable(InterfaceType.ITEM, core.tier(), reachable)
                            && interfaceAvailable(InterfaceType.LASER, core.tier(), reachable)
                    );
        }
        if (AssemblyMachineRegistration.MACHINES.values().stream()
                .anyMatch(machine -> machine.series().recipeMethod().id().equals(method)
                        && machine.tier() >= tier && reachable.contains(machine.block().get().asItem()))) {
            return true;
        }
        if (MachineRegistration.MACHINES.values().stream()
                .anyMatch(machine -> machine.series().recipeMethod().id().equals(method)
                        && machine.tier() >= tier && reachable.contains(machine.block().get().asItem()))) {
            return true;
        }
        if (FormingMachineRegistration.MACHINES.values().stream()
                .anyMatch(machine -> machine.series().recipeMethod().id().equals(method)
                        && machine.tier() >= tier && reachable.contains(machine.block().get().asItem()))) {
            return true;
        }
        if (CaRegistration.MACHINES.values().stream()
                .anyMatch(machine -> machine.series().recipeMethod().id().equals(method)
                        && machine.tier() >= tier && reachable.contains(machine.block().get().asItem()))) {
            return true;
        }
        return method.equals("milling_machine") && tier <= 1
                && reachable.contains(FormingMachineRegistration.ELEMENTAL_MILLING_MACHINE.get().asItem());
    }

    private static boolean interfaceAvailable(
            InterfaceType type, int minimumTier, Set<net.minecraft.world.item.Item> reachable) {
        return InterfaceRegistration.INTERFACES.values().stream()
                .anyMatch(definition -> definition.type() == type && definition.tier() >= minimumTier
                        && reachable.contains(definition.item().get()));
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void fiftySevenMetalChestsHaveBothOriginalCraftingRoutesAndEquipmentIsReachable(
            GameTestHelper helper) {
        require(MetalChestType.ALL.size() == 57, "The metal chest ledger no longer contains 57 materials");
        for (MetalChestType type : MetalChestType.ALL) {
            ItemStack expected = new ItemStack(StorageRegistration.METAL_CHESTS.get(type.id()).block().get());
            String material = type.id().substring(0, type.id().length() - "_chest".length());
            ItemStack ingot = new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get(material + "_ingot").get());
            for (String suffix : List.of("_from_wooden", "_from_metal")) {
                ShapedRecipe recipe = shaped(helper, "progression/metal_chest/" + type.id() + suffix);
                require(recipe.getWidth() == 3 && recipe.getHeight() == 3 && recipe.getIngredients().size() == 9,
                        type.id() + suffix + " does not preserve the eight-ingot center-chest pattern");
                for (int index = 0; index < 9; index++) {
                    if (index != 4) {
                        require(recipe.getIngredients().get(index).test(ingot),
                                type.id() + suffix + " does not use its own material ingot");
                    }
                }
                ItemStack center = suffix.equals("_from_wooden") ? new ItemStack(Items.CHEST)
                        : new ItemStack(StorageRegistration.METAL_CHESTS.get("silicon_chest").block().get());
                require(recipe.getIngredients().get(4).test(center),
                        type.id() + suffix + " rejects the original center chest kind");
                CompoundTag contents = new CompoundTag();
                CompoundTag diamond = (CompoundTag)new ItemStack(Items.DIAMOND)
                        .save(helper.getLevel().registryAccess());
                diamond.putByte("Slot", (byte)0);
                ListTag items = new ListTag();
                items.add(diamond);
                contents.put("Items", items);
                center.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(contents));
                List<ItemStack> grid = new ArrayList<>();
                for (int index = 0; index < 9; index++) {
                    grid.add(index == 4 ? center : ingot.copy());
                }
                CraftingInput input = CraftingInput.of(3, 3, grid);
                ItemStack actual = recipe.assemble(input, helper.getLevel().registryAccess());
                require(recipe.matches(input, helper.getLevel())
                                && ItemStack.isSameItemSameComponents(actual, expected)
                                && !actual.has(DataComponents.BLOCK_ENTITY_DATA),
                        type.id() + suffix + " cannot be crafted from its declared route");
            }
        }

        List<String> equipment = new ArrayList<>();
        AdvancedToolRegistration.ALL.forEach(item -> equipment.add(item.getId().getPath()));
        WeaponRegistration.ALL.forEach(item -> equipment.add(item.getId().getPath()));
        equipment.add("gadget_holder");
        for (GadgetType type : GadgetType.values()) {
            equipment.add(type.id());
        }
        equipment.addAll(List.of("white_list_item_filter", "fluid_filter", "synchronizer"));
        Map<String, String[]> ledger = readLedger();
        for (String id : equipment) {
            ResourceLocation recipeId = ClayiumMod.id("progression/equipment/" + id);
            var holder = helper.getLevel().getRecipeManager().byKey(recipeId);
            require(holder.isPresent(),
                    id + " has no distinct survival recipe");
            String[] row = ledger.get("clayium:" + id);
            require(row != null && List.of(row[3].split("\\|")).contains(recipeId.toString()),
                    id + " has no source recipe in the individual equipment ledger");
            require(row[4].equals("clayium:assembler") || row[4].equals("minecraft:crafting_shaped")
                            || row[4].equals("minecraft:crafting_shapeless")
                            || id.equals("clay_gadget_auto_eat")
                                    && row[4].equals("clayium:nbt_preserving_auto_eat"),
                    id + " has an unexpected equipment method in the ledger");
            require(BuiltInRegistries.ITEM.getKey(holder.get().value()
                            .getResultItem(helper.getLevel().registryAccess()).getItem()).toString().equals(row[1]),
                    id + " produces a different item from the ledger");
            require(holder.get().value().getResultItem(helper.getLevel().registryAccess()).getCount() == 1,
                    id + " produces a different quantity from the ledger");
            if (holder.get().value() instanceof ClayiumMachineRecipe machine) {
                require(row[4].equals("clayium:" + machine.processingMethod().id())
                                && machine.requiredTier() == Integer.parseInt(row[5])
                                && machine.processingTicks() == Long.parseLong(row[6])
                                && machine.clayEnergy() == Long.parseLong(row[7]),
                        id + " differs from the original method, Tier, time or CE");
                require(machine.itemInputs().stream().allMatch(input -> input.ingredient().getItems().length == 1),
                        id + " has unrecorded alternate ingredients");
                String inputs = machine.itemInputs().stream()
                        .map(input -> input.count() + "*" + BuiltInRegistries.ITEM.getKey(
                                input.ingredient().getItems()[0].getItem()))
                        .collect(java.util.stream.Collectors.joining("+"));
                require(inputs.equals(row[8]), id + " differs from the original ingredient quantities");
            } else if (holder.get().value() instanceof ShapedRecipe shaped) {
                require(row[4].equals("minecraft:crafting_shaped")
                                && row[5].equals("-") && row[6].equals("-") && row[7].equals("-"),
                        id + " differs from the original crafting method");
                String[] patternAndKeys = row[8].split(";", 2);
                String[] lines = patternAndKeys[0].split("/", -1);
                Map<Character, ItemStack> keys = new HashMap<>();
                for (String key : patternAndKeys[1].split(",")) {
                    keys.put(key.charAt(0), new ItemStack(BuiltInRegistries.ITEM.get(
                            ResourceLocation.parse(key.substring(2)))));
                }
                require(shaped.getHeight() == lines.length && shaped.getWidth() == lines[0].length(),
                        id + " has a different crafting grid from the ledger");
                for (int y = 0; y < shaped.getHeight(); y++) {
                    for (int x = 0; x < shaped.getWidth(); x++) {
                        char symbol = lines[y].charAt(x);
                        Ingredient actual = shaped.getIngredients().get(y * shaped.getWidth() + x);
                        require(symbol == ' ' ? actual.isEmpty() : actual.test(keys.get(symbol))
                                        && actual.getItems().length == 1,
                                id + " has a different ingredient at " + x + "," + y);
                    }
                }
            } else if (holder.get().value() instanceof ShapelessRecipe shapeless) {
                require(row[4].equals("minecraft:crafting_shapeless")
                                && row[5].equals("-") && row[6].equals("-") && row[7].equals("-"),
                        id + " differs from the original crafting method");
                require(shapeless.getIngredients().size() == 1
                                && shapeless.getIngredients().getFirst().getItems().length == 1
                                && shapeless.getIngredients().getFirst().test(new ItemStack(
                                        BuiltInRegistries.ITEM.get(ResourceLocation.parse(row[8])))),
                        id + " differs from the original shapeless ingredient");
            } else if (holder.get().value() instanceof NbtPreservingAutoEatRecipe) {
                require(id.equals("clay_gadget_auto_eat")
                                && row[4].equals("clayium:nbt_preserving_auto_eat"),
                        id + " differs from the original component-preserving conversion");
            } else {
                throw new GameTestAssertException(id + " has an unexpected recipe type");
            }
            require(row[9].equals("1*" + row[1]), id + " has a different output quantity");
        }
        for (String id : List.of(
                "raw_clay_rolling_pin", "raw_clay_slicer", "raw_clay_spatula",
                "clay_rolling_pin", "clay_slicer", "clay_spatula", "clay_wrench",
                "clay_io_configurator", "clay_piping_configurator", "memory_card",
                "direction_memory_card")) {
            require(helper.getLevel().getRecipeManager().byKey(ClayiumMod.id("workbench/" + id)).isPresent(),
                    id + " lost its existing low-tier survival route");
        }
        require(!BuiltInRegistries.ITEM.containsKey(ClayiumMod.id("black_list_item_filter")),
                "The obsolete black-list item was restored instead of using the unified filter");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void reloadedViewerDataMatchesMachineAcceptanceAndExecution(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        AtomicBoolean reloaded = new AtomicBoolean();
        AtomicReference<Throwable> error = new AtomicReference<>();
        server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, failure) -> {
            error.set(failure);
            reloaded.set(true);
        });
        helper.succeedWhen(() -> {
            require(reloaded.get(), "Datapack reload has not completed");
            require(error.get() == null, "Datapack reload failed: " + error.get());
            ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)server.getRecipeManager()
                    .byKey(ClayiumMod.id("progression/machines/transformer_tier_11"))
                    .orElseThrow().value();
            RecipeViewerData viewer = RecipeViewerData.from(recipe);
            require(viewer.requiredTier() == recipe.requiredTier()
                            && viewer.processingTicks() > 0L && viewer.clayEnergy() > 0L,
                    "The reloaded viewer omitted the construction recipe tier, time or CE");
            require(viewer.itemInputs().size() == recipe.itemInputs().size()
                            && viewer.itemOutputs().size() == recipe.itemOutputs().size(),
                    "The reloaded viewer omitted an ingredient or result");
            for (int index = 0; index < recipe.itemInputs().size(); index++) {
                require(viewer.itemInputs().get(index).count() == recipe.itemInputs().get(index).count()
                                && ingredientSignature(viewer.itemInputs().get(index).ingredient()).equals(
                                        ingredientSignature(recipe.itemInputs().get(index).ingredient())),
                        "The reloaded viewer changed an ingredient or its count");
            }
            for (int index = 0; index < recipe.itemOutputs().size(); index++) {
                require(ItemStack.isSameItemSameComponents(viewer.itemOutputs().get(index),
                                recipe.itemOutputs().get(index))
                                && viewer.itemOutputs().get(index).getCount()
                                        == recipe.itemOutputs().get(index).getCount(),
                        "The reloaded viewer changed an output or its count");
            }
            BlockPos lowerPos = new BlockPos(1, 2, 1);
            BlockPos upperPos = new BlockPos(3, 2, 1);
            helper.setBlock(lowerPos, AssemblyMachineRegistration.MACHINES.get("assembler_tier_6").block().get());
            helper.setBlock(upperPos, AssemblyMachineRegistration.MACHINES.get("assembler_tier_10").block().get());
            ClayMachineBlockEntity rejected = (ClayMachineBlockEntity)helper.getLevel()
                    .getBlockEntity(helper.absolutePos(lowerPos));
            ClayMachineBlockEntity accepted = (ClayMachineBlockEntity)helper.getLevel()
                    .getBlockEntity(helper.absolutePos(upperPos));
            insertInputs(rejected, recipe);
            rejected.receiveClayEnergy(ClayEnergy.MAX);
            tick(helper, rejected);
            require(rejected.activeRecipe() == null, "A Tier 6 assembler accepted a Tier 10 recipe");
            insertInputs(accepted, recipe);
            accepted.receiveClayEnergy(ClayEnergy.MAX);
            for (int index = 0; index < recipe.itemInputs().size(); index++) {
                require(accepted.inventory().getStackInSlot(index).getCount()
                                == recipe.itemInputs().get(index).count(),
                        "The placed assembler has different input counts from the displayed recipe");
            }
            tick(helper, accepted);
            require(accepted.activeRecipe() != null, "The placed assembler rejected the reloaded recipe");
            require(accepted.requiredProgress() == viewer.processingTicks()
                            && accepted.requiredEnergy() == viewer.clayEnergy(),
                    "The recipe viewer and executing machine disagree on time or total CE");
            for (int index = 0; index < 1000
                    && accepted.inventory().getStackInSlot(accepted.outputSlot(0)).isEmpty(); index++) {
                tick(helper, accepted);
            }
            require(ItemStack.isSameItemSameComponents(
                            accepted.inventory().getStackInSlot(accepted.outputSlot(0)),
                            viewer.itemOutputs().getFirst())
                            && accepted.inventory().getStackInSlot(accepted.outputSlot(0)).getCount()
                                    == viewer.itemOutputs().getFirst().getCount(),
                    "The placed assembler did not produce the result shown by the viewer");
        });
    }

    private static List<ResourceLocation> candidateRecipeIds() {
        List<ResourceLocation> ids = new ArrayList<>();
        MachineRegistration.MACHINES.values().stream()
                .filter(definition -> !definition.id().equals("chemical_metal_separator")
                        && !definition.id().equals("energetic_clay_decomposer"))
                .forEach(definition -> ids.add(ClayiumMod.id("progression/machines/" + definition.id())));
        LogisticsRegistration.BUFFERS.values().forEach(definition ->
                ids.add(ClayiumMod.id("progression/logistics/" + definition.id())));
        CaRegistration.MACHINES.values().forEach(definition ->
                ids.add(ClayiumMod.id("progression/ca/" + definition.id())));
        LaserRegistration.LASERS.values().forEach(definition -> ids.add(ClayiumMod.id(
                "progression/laser/" + definition.specification().id())));
        InterfaceRegistration.INTERFACES.values().forEach(definition ->
                ids.add(ClayiumMod.id("progression/interfaces/" + definition.id())));
        FluidRegistration.MACHINES.values().forEach(definition -> ids.add(ClayiumMod.id(
                "progression/fluid/" + definition.specification().id())));
        MultitrackRegistration.MULTITRACK_BUFFERS.values().forEach(definition ->
                ids.add(ClayiumMod.id("progression/logistics/" + definition.id())));
        MultitrackRegistration.DISTRIBUTORS.values().forEach(definition ->
                ids.add(ClayiumMod.id("progression/logistics/" + definition.id())));
        PanRegistration.DEFINITIONS.values().stream()
                .filter(definition -> definition.kind() == PanBlock.Kind.ADAPTER
                        || definition.kind() == PanBlock.Kind.DUPLICATOR)
                .forEach(definition -> ids.add(ClayiumMod.id("progression/pan/" + definition.id())));
        EnvironmentMachineRegistration.SALT_EXTRACTORS.values().forEach(definition ->
                ids.add(ClayiumMod.id("progression/environment/" + definition.id())));
        return ids;
    }

    private static ShapedRecipe shaped(GameTestHelper helper, String path) {
        var recipe = helper.getLevel().getRecipeManager().byKey(ClayiumMod.id(path)).orElseThrow().value();
        require(recipe instanceof ShapedRecipe, path + " is not a shaped crafting recipe");
        return (ShapedRecipe)recipe;
    }

    private static void insertInputs(ClayMachineBlockEntity machine, ClayiumMachineRecipe recipe) {
        for (int index = 0; index < recipe.itemInputs().size(); index++) {
            ItemStack stack = recipe.itemInputs().get(index).ingredient().getItems()[0].copyWithCount(
                    recipe.itemInputs().get(index).count());
            machine.inventory().setStackInSlot(index, stack);
        }
    }

    private static void tick(GameTestHelper helper, ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private SurvivalProgressionGameTests() {
    }
}
