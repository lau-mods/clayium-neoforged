package net.claustra01.clayium.gametest.ca;

import java.util.Arrays;
import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.fe.EnergyConverterBlockEntity;
import net.claustra01.clayium.registry.EnergyConverterRegistration;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.augment.AugmentScanner;
import net.claustra01.clayium.machine.ca.CaCollectorBlockEntity;
import net.claustra01.clayium.machine.ca.CaSeries;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeEnergy;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.CaRegistration;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.FoundationDataComponentRegistration;
import net.claustra01.clayium.machine.ClayMachineBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CaMachineGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allEightCaMachinesPreserveOriginalTiersRecipesAndResonance(GameTestHelper helper) {
        require(Arrays.equals(CaSeries.CONDENSER.tiers(), new int[] {9, 10, 11}),
                "CA condenser tiers differ from the original ledger");
        require(Arrays.equals(CaSeries.INJECTOR.tiers(), new int[] {9, 10, 11, 12, 13}),
                "CA injector tiers differ from the original ledger");
        require(CaRegistration.MACHINES.size() == 8, "The eight CA machines are not registered");

        ClayiumMachineRecipe condenserRecipe = recipe(helper, "ca_condenser/antimatter");
        require(condenserRecipe.processingMethod() == ClayiumRecipeMethod.CA_CONDENSER
                        && condenserRecipe.requiredTier() == 9
                        && condenserRecipe.clayEnergy() == ClayiumRecipeEnergy.e(2.5D, 9)
                        && condenserRecipe.processingTicks() == 2_000
                        && condenserRecipe.itemOutputs().getFirst().is(
                                MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get()),
                "The original antimatter condensation recipe changed");

        String[] injectorRecipes = {
                "ca_injector/antimatter_resonator",
                "ca_injector/resonating_collector",
                "ca_injector/pure_antimatter_resonator",
                "ca_injector/oec_resonator",
                "ca_injector/opa_resonator"
        };
        int[] tiers = {9, 10, 11, 12, 13};
        for (int index = 0; index < tiers.length; index++) {
            ClayiumMachineRecipe recipe = recipe(helper, injectorRecipes[index]);
            require(recipe.processingMethod() == ClayiumRecipeMethod.CA_INJECTOR
                            && recipe.requiredTier() == tiers[index]
                            && recipe.clayEnergy() == ClayiumRecipeEnergy.e(2.0D, tiers[index])
                            && recipe.processingTicks() == 4_000,
                    injectorRecipes[index] + " differs from hotfix2");
        }
        String[] overclockers = {"antimatter", "pure_antimatter", "oec", "opa"};
        long[] originalTimes = {10_000_000_000_000L, 100_000_000_000_000L,
                1_000_000_000_000_000L, 1_000_000_000_000_000L};
        int[] hullCounts = {1, 4, 16, 64};
        int[] resonatorCounts = {8, 16, 32, 64};
        for (int index = 0; index < overclockers.length; index++) {
            ClayiumMachineRecipe reactor = recipe(helper, "reactor/" + overclockers[index] + "_overclocker");
            require(reactor.requiredTier() == index + 10
                            && reactor.processingTicks() == originalTimes[index]
                            && reactor.itemInputs().get(0).count() == hullCounts[index]
                            && reactor.itemInputs().get(1).count() == resonatorCounts[index],
                    overclockers[index] + " overclocker lost the hotfix2 reactor balance");
        }

        for (CaRegistration.MachineDefinition definition : CaRegistration.MACHINES.values()) {
            ClayMachineBlock block = definition.block().get();
            require(block.tier() == definition.tier()
                            && block.series() == definition.series()
                            && new ItemStack(block).get(FoundationDataComponentRegistration.TIER.get())
                                    == definition.tier(),
                    definition.id() + " has the wrong registered tier");
        }
        for (int tier : CaSeries.CONDENSER.tiers()) {
            runCaRecipe(helper, CaSeries.CONDENSER, tier, condenserRecipe, false);
            runCaRecipe(helper, CaSeries.CONDENSER, tier, condenserRecipe, true);
        }
        for (int index = 0; index < tiers.length; index++) {
            ClayiumMachineRecipe injectorRecipe = recipe(helper, injectorRecipes[index]);
            runCaRecipe(helper, CaSeries.INJECTOR, tiers[index], injectorRecipe, false);
            runCaRecipe(helper, CaSeries.INJECTOR, tiers[index], injectorRecipe, true);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void resonatingCollectorStopsWithoutResonanceAndNeverConsumesWhenFull(GameTestHelper helper) {
        BlockPos collectorPosition = new BlockPos(3, 3, 3);
        helper.setBlock(collectorPosition, CaRegistration.RESONATING_COLLECTOR.get());
        CaCollectorBlockEntity collector = helper.getBlockEntity(collectorPosition);
        for (int tick = 0; tick < 20; tick++) {
            CaCollectorBlockEntity.serverTick(
                    helper.getLevel(), collector.getBlockPos(), collector.getBlockState(), collector);
        }
        require(collector.progress() == 0L && collector.output().getStackInSlot(0).isEmpty(),
                "The collector advanced without resonance");

        helper.setBlock(collectorPosition.offset(2, 2, 2), augment("antimatter_resonator"));
        collector.invalidateResonance();
        for (int tick = 0; tick < 20; tick++) {
            CaCollectorBlockEntity.serverTick(
                    helper.getLevel(), collector.getBlockPos(), collector.getBlockState(), collector);
        }
        require(collector.progress() == 0L,
                "The collector accumulated fractional progress discarded by the original implementation");

        CompoundTag saved = collector.saveWithFullMetadata(helper.getLevel().registryAccess());
        saved.putLong("progress", CaCollectorBlockEntity.BASE_CRAFT_TIME - 1L);
        collector.loadWithComponents(saved, helper.getLevel().registryAccess());
        helper.setBlock(collectorPosition.offset(2, 2, 2), augment("opa_resonator"));
        collector.invalidateResonance();
        CaCollectorBlockEntity.serverTick(
                helper.getLevel(), collector.getBlockPos(), collector.getBlockState(), collector);
        require(collector.output().getStackInSlot(0).is(
                        MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get())
                        && collector.output().getStackInSlot(0).getCount() == 1,
                "Sufficient resonance did not produce antimatter");

        ItemStack full = new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("antimatter").get(), 64);
        for (int slot = 0; slot < collector.output().getSlots(); slot++) {
            collector.output().setStackInSlot(slot, full.copy());
        }
        long blockedProgress = collector.progress();
        CaCollectorBlockEntity.serverTick(
                helper.getLevel(), collector.getBlockPos(), collector.getBlockState(), collector);
        require(collector.progress() == blockedProgress
                        && collector.output().getStackInSlot(0).getCount() == 64,
                "A full collector consumed progress or changed its output");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void overclockAndResonanceUseOriginalProductsAndBoundaries(GameTestHelper helper) {
        BlockPos machinePosition = new BlockPos(5, 5, 5);
        ClayMachineBlockEntity machine = machine(helper, ProcessingSeries.GRINDER, 5, machinePosition);
        String[] overclockers = {
                "antimatter_overclocker", "pure_antimatter_overclocker",
                "oec_overclocker", "opa_overclocker"
        };
        net.minecraft.core.Direction[] directions = {
                net.minecraft.core.Direction.UP, net.minecraft.core.Direction.DOWN,
                net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.SOUTH
        };
        for (int index = 0; index < overclockers.length; index++) {
            helper.setBlock(machinePosition.relative(directions[index]), augment(overclockers[index]));
        }
        double factor = 1.5D * 2.3D * 3.5D * 5.0D;
        requireClose(machine.overclockFactor(), factor, "Adjacent overclock factors did not multiply");
        helper.setBlock(machinePosition.offset(2, 2, 2), augment("antimatter_resonator"));
        helper.setBlock(machinePosition.offset(-2, -2, -2), augment("oec_resonator"));
        helper.setBlock(machinePosition.offset(3, 0, 0), augment("opa_resonator"));
        requireClose(AugmentScanner.resonance(helper.getLevel(), machine.getBlockPos()), 2.16D,
                "Resonance did not use the inclusive 5x5x5 boundary");

        for (Direction direction : directions) {
            helper.setBlock(machinePosition.relative(direction), Blocks.AIR);
        }
        helper.setBlock(machinePosition.above(), augment("antimatter_overclocker"));
        machine.receiveClayEnergy(ClayEnergy.ofInternal(100_000L));
        machine = assertGrinderTick(helper, machine, 10L, 50L, 1.5D, 50L);
        helper.setBlock(machinePosition.relative(Direction.DOWN), augment("pure_antimatter_overclocker"));
        helper.setBlock(machinePosition.relative(Direction.NORTH), augment("oec_overclocker"));
        helper.setBlock(machinePosition.relative(Direction.SOUTH), augment("opa_overclocker"));
        machine = assertGrinderTick(helper, machine, 1L, 741L, 10.0D, 741L);

        BlockPos converterPosition = new BlockPos(12, 5, 5);
        helper.setBlock(converterPosition, EnergyConverterRegistration.CONVERTERS.get(6).block().get());
        EnergyConverterBlockEntity converter = helper.getBlockEntity(converterPosition);
        helper.setBlock(converterPosition.above(), augment("antimatter_overclocker"));
        converter.clayEnergyStorage().add(ClayEnergy.ofInternal(100_000L));
        EnergyConverterBlockEntity.serverTick(helper.getLevel(), converterPosition,
                converter.getBlockState(), converter);
        require(converter.clayEnergyStorage().stored().internalValue() == 85_000L
                        && converter.storedFe() == 135,
                "The converter did not apply its linear CE and FE overclock factors");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void energyStorageUpgradesCapAtSixtyFourAndDropOnlyTheExcess(GameTestHelper helper) {
        BlockPos machinePosition = new BlockPos(5, 4, 5);
        ClayMachineBlockEntity machine = machine(helper, ProcessingSeries.GRINDER, 4, machinePosition);
        BlockPos augmentPosition = machinePosition.above();
        String[] upgrades = {
                "antimatter_energy_storage_upgrade", "pure_antimatter_energy_storage_upgrade",
                "oec_energy_storage_upgrade", "opa_energy_storage_upgrade"
        };
        int[] limits = {2, 4, 8, 64};
        for (int index = 0; index < upgrades.length; index++) {
            helper.setBlock(augmentPosition, augment(upgrades[index]));
            require(machine.fuelLimit() == limits[index]
                            && machine.inventory().getSlotLimit(machine.fuelSlot()) == limits[index],
                    upgrades[index] + " exposes the wrong fuel limit");
        }

        helper.setBlock(machinePosition.relative(Direction.EAST), augment("antimatter_energy_storage_upgrade"));
        helper.setBlock(augmentPosition, augment("pure_antimatter_energy_storage_upgrade"));
        require(machine.fuelLimit() == 5, "Storage upgrades did not add below the cap");
        helper.setBlock(augmentPosition, augment("opa_energy_storage_upgrade"));
        helper.setBlock(machinePosition.relative(Direction.WEST), augment("oec_energy_storage_upgrade"));
        require(machine.fuelLimit() == 64, "Storage upgrades exceeded the 64 item cap");
        helper.setBlock(machinePosition.relative(Direction.EAST), Blocks.AIR);
        helper.setBlock(machinePosition.relative(Direction.WEST), Blocks.AIR);

        ItemStack fuel = new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS.get("energetic_clay").get(), 64);
        machine.inventory().setStackInSlot(machine.fuelSlot(), fuel);
        helper.setBlock(augmentPosition, net.minecraft.world.level.block.Blocks.AIR);
        tick(machine);
        require(machine.inventory().getStackInSlot(machine.fuelSlot()).getCount() == 1,
                "Removing the upgrade did not restore the base one-item limit");
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(
                ItemEntity.class, new AABB(machine.getBlockPos()).inflate(2.0D));
        int dropped = drops.stream().map(ItemEntity::getItem).mapToInt(ItemStack::getCount).sum();
        require(dropped == 63, "Upgrade removal duplicated or destroyed energetic clay: " + dropped);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void activeCaRecipeKeepsItsStartValuesWhenAugmentsChangeAcrossReload(GameTestHelper helper) {
        BlockPos position = new BlockPos(3, 3, 3);
        ClayMachineBlockEntity condenser = machine(helper, CaSeries.CONDENSER, 9, position);
        condenser.inventory().setStackInSlot(0,
                new ItemStack(MaterialRegistration.COMPONENTS.get("antimatter_seed").get()));
        condenser.receiveClayEnergy(ClayEnergy.ofInternal(100_000_000_000L));
        tick(condenser);
        CompoundTag saved = condenser.saveWithFullMetadata(helper.getLevel().registryAccess());
        require(saved.getLong("required_progress") == 2_000L
                        && saved.getList("pending_outputs", 10).size() == 1,
                "The condenser did not save its initial recipe parameters");
        helper.setBlock(position.offset(2, 0, 0), augment("opa_resonator"));
        condenser.loadWithComponents(saved, helper.getLevel().registryAccess());
        tick(condenser);
        require(condenser.activeRecipe() != null && condenser.progress() > 0L,
                "Adding a resonator aborted the saved process");
        CompoundTag nearlyComplete = condenser.saveWithFullMetadata(helper.getLevel().registryAccess());
        nearlyComplete.putLong("progress", 1_999L);
        condenser.loadWithComponents(nearlyComplete, helper.getLevel().registryAccess());
        tick(condenser);
        require(condenser.inventory().getStackInSlot(condenser.outputSlot()).getCount() == 1,
                "The saved process changed its output when resonance was added");
        helper.succeed();
    }

    private static void runCaRecipe(GameTestHelper helper, CaSeries series, int tier,
                                    ClayiumMachineRecipe recipe, boolean resonating) {
        BlockPos position = new BlockPos(3, 3, 3);
        helper.setBlock(position.offset(2, 0, 0),
                resonating ? augment("opa_resonator") : Blocks.AIR);
        helper.setBlock(position, Blocks.AIR);
        ClayMachineBlockEntity machine = machine(helper, series, tier, position);
        for (int slot = 0; slot < recipe.itemInputs().size(); slot++) {
            var ingredient = recipe.itemInputs().get(slot);
            machine.inventory().setStackInSlot(slot,
                    ingredient.ingredient().getItems()[0].copyWithCount(ingredient.count()));
        }
        long startingEnergy = 4_000_000_000_000_000L;
        machine.receiveClayEnergy(ClayEnergy.ofInternal(startingEnergy));
        tick(machine);
        require(machine.activeRecipe() != null, series + " tier " + tier + " did not start");
        CompoundTag started = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
        long[] condenserProgress = {2_000L, 200L, 20L};
        long expectedProgress = series == CaSeries.CONDENSER ? condenserProgress[tier - 9]
                : tier == 10 ? 40L : 4_000L;
        require(started.getLong("required_progress") == expectedProgress,
                series + " tier " + tier + " has the wrong processing time");
        double expectedMultiplier = series == CaSeries.INJECTOR && resonating
                ? Math.pow(20.0D, switch (tier) {
                    case 9 -> 0.2D;
                    case 10 -> 0.9D;
                    case 11 -> 3.0D;
                    default -> 1.0D;
                }) : 1.0D;
        requireClose(started.getDouble("active_progress_multiplier"), expectedMultiplier,
                series + " tier " + tier + " has the wrong injector progress factor");
        long expectedEnergy = recipe.clayEnergy() * (series == CaSeries.CONDENSER
                ? tier == 9 ? 1L : tier == 10 ? 10L : 100L
                : tier == 10 ? 250L : 1L);
        require(started.getLong("energy_per_progress") == expectedEnergy,
                series + " tier " + tier + " has the wrong CE cost");
        for (int step = 0; step <= 4_000 && machine.inventory().getStackInSlot(machine.outputSlot()).isEmpty(); step++) {
            tick(machine);
        }
        ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
        require(result.is(recipe.itemOutputs().getFirst().getItem())
                        && result.getCount() == (series == CaSeries.CONDENSER && resonating ? 3 : 1),
                series + " tier " + tier + " produced the wrong result");
        require(machine.energyStorage().stored().internalValue()
                        == startingEnergy - expectedProgress * expectedEnergy,
                series + " tier " + tier + " consumed the wrong CE");
    }

    private static ClayMachineBlockEntity assertGrinderTick(GameTestHelper helper, ClayMachineBlockEntity machine,
                                          long ticks, long energy, double progressPerTick,
                                          long consumedEnergy) {
        machine.inventory().setStackInSlot(0, new ItemStack(Items.QUARTZ));
        tick(machine);
        CompoundTag started = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
        require(started.getLong("required_progress") == ticks
                        && started.getLong("energy_per_progress") == energy,
                "The running grinder has the wrong time or CE parameters");
        requireClose(started.getDouble("progress_per_tick"), progressPerTick,
                "The running grinder has the wrong progress increment");
        long before = machine.energyStorage().stored().internalValue();
        tick(machine);
        require(before - machine.energyStorage().stored().internalValue() == consumedEnergy,
                "The running grinder consumed the wrong CE this tick");
        BlockPos position = new BlockPos(5, 5, 5);
        helper.setBlock(position, Blocks.AIR);
        ClayMachineBlockEntity replacement = machine(helper, ProcessingSeries.GRINDER, 5, position);
        replacement.receiveClayEnergy(ClayEnergy.ofInternal(100_000L));
        return replacement;
    }

    private static ClayiumMachineRecipe recipe(GameTestHelper helper, String id) {
        return (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                .byKey(ClayiumMod.id(id)).orElseThrow().value();
    }

    private static ClayMachineBlockEntity machine(
            GameTestHelper helper,
            net.claustra01.clayium.machine.series.MachineSeries series,
            int tier,
            BlockPos position) {
        var definition = series instanceof CaSeries
                ? CaRegistration.MACHINES.values().stream()
                        .filter(candidate -> candidate.series() == series && candidate.tier() == tier)
                        .findFirst().orElseThrow().block()
                : MachineRegistration.MACHINES.values().stream()
                        .filter(candidate -> candidate.series() == series && candidate.tier() == tier)
                        .findFirst().orElseThrow().block();
        helper.setBlock(position, definition.get());
        return helper.getBlockEntity(position);
    }

    private static net.minecraft.world.level.block.Block augment(String id) {
        return CaRegistration.AUGMENTS.get(id).block().get();
    }

    private static void tick(ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                machine.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void requireClose(double actual, double expected, String message) {
        require(Math.abs(actual - expected) < 0.0000001D, message + ": " + actual + " != " + expected);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private CaMachineGameTests() {
    }
}
