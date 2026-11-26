package net.claustra01.clayium.gametest.config;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.compat.RecipeViewerPage;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.environment.CobblestoneGeneratorBlockEntity;
import net.claustra01.clayium.recipe.ConfigEnabledCondition.Option;
import net.claustra01.clayium.recipe.ProgressionRateRules;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.claustra01.clayium.tool.ClayCraftingToolItem;
import net.claustra01.clayium.tool.advanced.ClayToolDropEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ConfigBehaviorGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void progressionRateAffectsOnlyOriginalCallSites(GameTestHelper helper) {
        double saved = ClayiumConfig.PROGRESSION_RATE.get();
        try {
            ClayiumConfig.PROGRESSION_RATE.set(2.0D);
            require(ClayToolDropEvents.progressionScaledBaseDrop(7) == 14,
                    "Clay ore base drop did not use ProgressionRate");
            require(CobblestoneGeneratorBlockEntity.intervalTicks(1) == 20,
                    "Cobblestone generator interval did not use ProgressionRate");
            require(ClayiumConfig.divideProgressionTicks(101) == 50,
                    "Fabricator tick scaling did not preserve original truncation");

            ClayiumMachineRecipe bending = machineRecipe(helper, "bending_machine/dense_clay_blade");
            require(ProgressionRateRules.adjustedRecipeTicks(bending) == 4,
                    "Original forming recipe was not scaled");
            ClayiumMachineRecipe fixedBending = machineRecipe(helper, "bending_machine/aluminium_plate");
            require(ProgressionRateRules.adjustedRecipeTicks(fixedBending) == 20,
                    "A fixed-time advanced recipe was scaled");
            ClayiumMachineRecipe workTable = machineRecipe(helper, "workbench/roll_clay_plate");
            require(ProgressionRateRules.adjustedRecipeTicks(workTable) == 2,
                    "Clay Work Table recipe was not scaled");

            ClayiumMachineRecipe shardRecipe = machineRecipe(
                    helper, "grinder/clay_ore_to_compressed_clay_shard");
            ItemStack shard = shardRecipe.itemOutputs().getFirst();
            List<ItemStack> outputs = ProgressionRateRules.adjustedRecipeOutputs(shardRecipe);
            require(outputs.getFirst().getCount() == 4 && shard.getCount() == 2,
                    "Ore grinder output scaling was not applied without mutating recipe data");

            ClayiumMachineRecipe batch = (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                    .byKey(ClayiumMod.id("grinder/cobblestone_to_gravel_4")).orElseThrow().value();
            ClayiumMachineRecipe adjustedBatch = ProgressionRateRules.adjustedRecipe(batch);
            require(adjustedBatch.itemInputs().getFirst().count() == 8
                            && adjustedBatch.itemOutputs().getFirst().getCount() == 8,
                    "Original batch recipe input and output counts did not use ProgressionRate");
            var displayedBatch = RecipeViewerPage.forRecipe(new net.minecraft.world.item.crafting.RecipeHolder<>(
                    ClayiumMod.id("grinder/cobblestone_to_gravel_4"), batch)).findFirst().orElseThrow();
            require(displayedBatch.holder().value().itemInputs().getFirst().count() == 8
                            && displayedBatch.holder().value().itemOutputs().getFirst().getCount() == 8,
                    "JEI page inputs and outputs differ from ProgressionRate execution values");
            var bendingId = ClayiumMod.id("bending_machine/dense_clay_blade");
            var displayedBending = RecipeViewerPage.forRecipe(
                    new net.minecraft.world.item.crafting.RecipeHolder<>(bendingId, bending))
                    .findFirst().orElseThrow().holder().value();
            require(displayedBending.processingTicks()
                            == ProgressionRateRules.adjustedRecipeTicks(bending),
                    "JEI page time differs from ProgressionRate execution time");

            ClayCraftingToolItem rollingPin = WorkbenchRegistration.CLAY_ROLLING_PIN.get();
            ItemStack freshTool = new ItemStack(rollingPin);
            require(freshTool.getMaxDamage() == 120,
                    "Fresh manual tool durability did not use ProgressionRate");
            ItemStack remainder = rollingPin.getCraftingRemainingItem(freshTool);
            require(remainder.getMaxDamage() == 120 && remainder.getDamageValue() == 1,
                    "Manual tool durability did not use ProgressionRate");
        } finally {
            ClayiumConfig.PROGRESSION_RATE.set(saved);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void recipeSwitchesApplyOnInitialDatapackLoad(GameTestHelper helper) {
        require(Option.HARDCORE_ALUMINIUM.enabled() == ClayiumConfig.HARDCORE_ALUMINIUM.get(),
                "HardcoreAluminium was unavailable during the initial datapack load");
        require(Option.HARDCORE_OSMIUM.enabled() == ClayiumConfig.HARDCORE_OSMIUM.get(),
                "HardcoreOsmium was unavailable during the initial datapack load");
        require(Option.INJECTOR_INTERFACE.enabled() == ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.get(),
                "EnableInjectorRecipeOfInterface was unavailable during the initial datapack load");
        assertRecipeConditionState(helper.getLevel().getRecipeManager(),
                ClayiumConfig.HARDCORE_ALUMINIUM.get(),
                ClayiumConfig.HARDCORE_OSMIUM.get(),
                ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.get());
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "clayium_config_reload", timeoutTicks = 600)
    public static void optionalRecipeSwitchesDriveConditions(GameTestHelper helper) {
        boolean savedAluminium = ClayiumConfig.HARDCORE_ALUMINIUM.get();
        boolean savedOsmium = ClayiumConfig.HARDCORE_OSMIUM.get();
        boolean savedInterfaces = ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.get();
        MinecraftServer server = helper.getLevel().getServer();
        AtomicInteger phase = new AtomicInteger();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicReference<Throwable> assertionFailure = new AtomicReference<>();
        AtomicReference<Throwable> restorationFailure = new AtomicReference<>();

        ClayiumConfig.HARDCORE_ALUMINIUM.set(false);
        ClayiumConfig.HARDCORE_OSMIUM.set(false);
        ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.set(false);
        try {
            reload(server, phase, 1, failure);
        } catch (Throwable error) {
            assertionFailure.set(error);
            restoreAndReload(server, phase, failure,
                    savedAluminium, savedOsmium, savedInterfaces);
        }
        helper.succeedWhen(() -> {
            if (failure.get() != null && phase.get() != 5 && phase.get() != 7) {
                assertionFailure.compareAndSet(null, failure.getAndSet(null));
                restoreAndReload(server, phase, failure,
                        savedAluminium, savedOsmium, savedInterfaces);
                throw new GameTestAssertException("Restoring configuration after a reload failure");
            }
            if (phase.get() == 1) {
                try {
                    assertRecipeConditionState(server.getRecipeManager(), false, false, false);
                    ClayiumConfig.HARDCORE_ALUMINIUM.set(true);
                    ClayiumConfig.HARDCORE_OSMIUM.set(true);
                    ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.set(true);
                    require(Option.HARDCORE_ALUMINIUM.enabled(),
                            "HardcoreAluminium condition ignored configuration");
                    require(Option.HARDCORE_OSMIUM.enabled(),
                            "HardcoreOsmium condition ignored configuration");
                    require(Option.INJECTOR_INTERFACE.enabled(),
                            "EnableInjectorRecipeOfInterface condition ignored configuration");
                    phase.set(2);
                    reload(server, phase, 3, failure);
                } catch (Throwable error) {
                    assertionFailure.compareAndSet(null, error);
                    restoreAndReload(server, phase, failure,
                            savedAluminium, savedOsmium, savedInterfaces);
                    throw new GameTestAssertException("Restoring configuration after a condition assertion");
                }
            } else if (phase.get() == 3) {
                try {
                    assertRecipeConditionState(server.getRecipeManager(), true, true, true);
                } catch (Throwable error) {
                    assertionFailure.compareAndSet(null, error);
                    restoreAndReload(server, phase, failure,
                            savedAluminium, savedOsmium, savedInterfaces);
                    throw new GameTestAssertException("Restoring configuration after a condition assertion");
                }
                restoreAndReload(server, phase, failure,
                        savedAluminium, savedOsmium, savedInterfaces);
            } else if (phase.get() == 5 && failure.get() != null) {
                restorationFailure.compareAndSet(null, failure.getAndSet(null));
                phase.set(6);
                try {
                    reload(server, phase, 7, failure);
                } catch (Throwable error) {
                    failure.set(error);
                    phase.set(7);
                }
                throw new GameTestAssertException("Retrying final configuration restoration reload");
            } else if (phase.get() == 5 || phase.get() == 7) {
                require(failure.get() == null, "Final config restoration reload failed: " + failure.get());
                assertRecipeConditionState(server.getRecipeManager(),
                        savedAluminium, savedOsmium, savedInterfaces);
                require(restorationFailure.get() == null,
                        "Initial config restoration reload failed: " + restorationFailure.get());
                require(assertionFailure.get() == null,
                        "Conditional recipe integration failed before restoration: " + assertionFailure.get());
                return;
            }
            throw new GameTestAssertException("Waiting for config recipe reload phase " + phase.get());
        });
    }

    private static void reload(
            MinecraftServer server, AtomicInteger phase, int completedPhase,
            AtomicReference<Throwable> failure) {
        server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, error) -> {
            failure.set(error);
            phase.set(completedPhase);
        });
    }

    private static void restoreRecipeOptions(boolean aluminium, boolean osmium, boolean interfaces) {
        ClayiumConfig.HARDCORE_ALUMINIUM.set(aluminium);
        ClayiumConfig.HARDCORE_OSMIUM.set(osmium);
        ClayiumConfig.ENABLE_INJECTOR_RECIPE_OF_INTERFACE.set(interfaces);
    }

    private static void restoreAndReload(
            MinecraftServer server, AtomicInteger phase, AtomicReference<Throwable> failure,
            boolean aluminium, boolean osmium, boolean interfaces) {
        restoreRecipeOptions(aluminium, osmium, interfaces);
        phase.set(4);
        try {
            reload(server, phase, 5, failure);
        } catch (Throwable error) {
            failure.set(error);
            phase.set(5);
        }
    }

    private static void assertRecipeConditionState(
            RecipeManager manager, boolean aluminium, boolean osmium, boolean interfaces) {
        for (String path : List.of(
                "bending_machine/impure_aluminium_plate",
                "bending_machine/large_impure_aluminium_plate",
                "grinder/impure_aluminium_ingot",
                "grinder/impure_aluminium_plate",
                "grinder/large_impure_aluminium_plate",
                "smelter/impure_aluminium")) {
            require(manager.byKey(ClayiumMod.id(path)).isPresent() == aluminium,
                    path + " did not follow HardcoreAluminium after datapack reload");
        }
        require(manager.byKey(ClayiumMod.id("blast_furnace/osmium_from_impure_osmium")).isPresent() == osmium,
                "Osmium recipe did not follow HardcoreOsmium after datapack reload");
        int injectorRecipes = 0;
        for (String type : List.of("clay_interface", "redstone_interface", "clay_laser_interface")) {
            int firstTier = type.equals("clay_laser_interface") ? 8 : 6;
            for (int tier = firstTier; tier <= 13; tier++) {
                ResourceLocation id = ClayiumMod.id(
                        "progression/ca_injector/interface/" + type + "_tier_" + tier);
                require(manager.byKey(id).isPresent() == interfaces,
                        id + " did not follow EnableInjectorRecipeOfInterface after datapack reload");
                injectorRecipes++;
            }
        }
        require(injectorRecipes == 22, "The config integration test did not inspect all injector recipes");
    }

    private static ClayiumMachineRecipe machineRecipe(GameTestHelper helper, String path) {
        return (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(path)).orElseThrow().value();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void machineTransportSettingsDriveAutomaticTransfers(GameTestHelper helper) {
        var insertInterval = ClayiumConfig.MACHINE_TRANSPORT.insertIntervalValue(4);
        var extractInterval = ClayiumConfig.MACHINE_TRANSPORT.extractIntervalValue(4);
        var maximumInsert = ClayiumConfig.MACHINE_TRANSPORT.maximumInsertValue(4);
        var maximumExtract = ClayiumConfig.MACHINE_TRANSPORT.maximumExtractValue(4);
        int savedInsertInterval = insertInterval.get();
        int savedExtractInterval = extractInterval.get();
        int savedMaximumInsert = maximumInsert.get();
        int savedMaximumExtract = maximumExtract.get();
        try {
            insertInterval.set(1);
            extractInterval.set(1);
            maximumInsert.set(3);
            maximumExtract.set(5);

            BlockPos machinePosition = helper.absolutePos(new BlockPos(3, 2, 3));
            BlockPos sourcePosition = machinePosition.west();
            BlockPos targetPosition = machinePosition.east();
            helper.getLevel().setBlockAndUpdate(sourcePosition, Blocks.CHEST.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(targetPosition, Blocks.CHEST.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(machinePosition,
                    MachineRegistration.MACHINES.get("grinder_tier_4").block().get().defaultBlockState());
            ChestBlockEntity source = (ChestBlockEntity)helper.getLevel().getBlockEntity(sourcePosition);
            ChestBlockEntity target = (ChestBlockEntity)helper.getLevel().getBlockEntity(targetPosition);
            ClayMachineBlockEntity machine = (ClayMachineBlockEntity)helper.getLevel().getBlockEntity(machinePosition);
            require(source != null && target != null && machine != null,
                    "Machine transport test fixtures did not create block entities");
            for (Direction direction : Direction.values()) {
                machine.setFaceMode(direction, ClayMachineBlockEntity.FaceMode.DISABLED);
            }
            machine.setFaceMode(Direction.WEST, ClayMachineBlockEntity.FaceMode.INPUT);
            machine.setFaceMode(Direction.EAST, ClayMachineBlockEntity.FaceMode.OUTPUT);
            source.setItem(0, new ItemStack(Items.DIRT, 20));
            machine.inventory().setStackInSlot(machine.outputSlot(), new ItemStack(Items.DIAMOND, 20));

            ClayMachineBlockEntity.serverTick(
                    helper.getLevel(), machinePosition, machine.getBlockState(), machine);
            require(machine.inventory().getStackInSlot(0).getCount() == 3
                            && source.getItem(0).getCount() == 17,
                    "Machine auto-insert did not use the configured three-item limit");
            require(target.getItem(0).getCount() == 5
                            && machine.inventory().getStackInSlot(machine.outputSlot()).getCount() == 15,
                    "Machine auto-extract did not use the configured five-item limit");

            maximumInsert.set(1);
            maximumExtract.set(2);
            ClayMachineBlockEntity.serverTick(
                    helper.getLevel(), machinePosition, machine.getBlockState(), machine);
            require(machine.inventory().getStackInSlot(0).getCount() == 4
                            && source.getItem(0).getCount() == 16,
                    "Changed machine auto-insert setting was not applied on the next transfer");
            require(target.getItem(0).getCount() == 7
                            && machine.inventory().getStackInSlot(machine.outputSlot()).getCount() == 13,
                    "Changed machine auto-extract setting was not applied on the next transfer");
        } finally {
            insertInterval.set(savedInsertInterval);
            extractInterval.set(savedExtractInterval);
            maximumInsert.set(savedMaximumInsert);
            maximumExtract.set(savedMaximumExtract);
        }
        helper.succeed();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private ConfigBehaviorGameTests() {
    }
}
