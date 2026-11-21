package net.claustra01.clayium.gametest.clayproduction;

import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.clayproduction.ClayProductionBlockEntity;
import net.claustra01.clayium.machine.clayproduction.ClayProductionMenu;
import net.claustra01.clayium.machine.clayproduction.ClayProductionRules;
import net.claustra01.clayium.machine.clayproduction.ClayProductionType;
import net.claustra01.clayium.registry.ClayProductionRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ClayProductionGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 120)
    public static void condensersRequireNineItemsAndPreserveOriginalTiersAndTimes(GameTestHelper helper) {
        ClayProductionBlockEntity[] machines = new ClayProductionBlockEntity[9];
        long[] energyPerTick = {1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000};
        for (int level = 4; level <= 12; level++) {
            ClayProductionType type = level <= 6 ? ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I
                    : ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_II;
            ClayProductionBlockEntity machine = machine(helper, type, new BlockPos(level - 3, 2, 1));
            machines[level - 4] = machine;
            machine.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                    new ItemStack(ClayProductionRules.itemForLevel(level), 9));
            if (level == 4) {
                machine.inventory().setStackInSlot(ClayProductionBlockEntity.FUEL_SLOT,
                        new ItemStack(ClayProductionRules.itemForLevel(5)));
            } else {
                machine.receiveClayEnergy(ClayEnergy.ofInternal(energyPerTick[level - 4] * 64));
            }
        }
        ClayProductionBlockEntity insufficient = machine(helper,
                ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I, new BlockPos(11, 2, 1));
        insufficient.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(7), 9));
        insufficient.receiveClayEnergy(ClayEnergy.ofInternal(100_000));
        ClayProductionBlockEntity oneItem = machine(helper,
                ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I, new BlockPos(12, 2, 1));
        oneItem.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(4)));
        oneItem.receiveClayEnergy(ClayEnergy.ofInternal(100_000));
        for (int tick = 0; tick < 64; tick++) {
            for (ClayProductionBlockEntity machine : machines) {
                ClayProductionBlockEntity.serverTick(helper.getLevel(), machine.getBlockPos(),
                        machine.getBlockState(), machine);
            }
            ClayProductionBlockEntity.serverTick(helper.getLevel(), insufficient.getBlockPos(),
                    insufficient.getBlockState(), insufficient);
            ClayProductionBlockEntity.serverTick(helper.getLevel(), oneItem.getBlockPos(),
                    oneItem.getBlockState(), oneItem);
            if (tick == 1) {
                for (int level = 4; level <= 12; level++) {
                    ClayProductionBlockEntity machine = machines[level - 4];
                    require(machine.requiredProgress() == (level == 4 ? 16 : level == 5 ? 32 : 64),
                            "Incorrect recipe time at level " + level);
                    require(machine.activeInputLevel() == level, "Incorrect recipe input at level " + level);
                }
            }
        }
        require(insufficient.inventory().getStackInSlot(ClayProductionBlockEntity.RESERVED_SLOT).isEmpty(),
                "Mk I accepted a Mk II recipe");
        require(oneItem.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).isEmpty(),
                "One item produced a nine-item compression");
        for (int level = 4; level <= 12; level++) {
            ClayProductionBlockEntity machine = machines[level - 4];
            require(machine.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT)
                    .is(ClayProductionRules.itemForLevel(level + 1)), "Wrong output at level " + level);
            require(machine.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).getCount() == 1,
                    "Wrong output count at level " + level);
            require(machine.energyStorage().stored().internalValue()
                    == (level == 4 ? 100_000 - 16
                            : energyPerTick[level - 4] * (64 - (level == 5 ? 32 : 64))),
                    "Wrong CE use at level " + level);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 30)
    public static void automaticCondensersPreserveNineToOneAndLimits(GameTestHelper helper) {
        ClayProductionBlockEntity[] machines = new ClayProductionBlockEntity[13];
        for (int level = 0; level < 13; level++) {
            ClayProductionBlockEntity machine = machine(helper,
                    ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_II, new BlockPos(level + 1, 2, 1));
            machines[level] = machine;
            machine.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                    new ItemStack(ClayProductionRules.itemForLevel(level), 9));
        }
        ClayProductionBlockEntity limited = machine(helper,
                ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_I, new BlockPos(15, 2, 1));
        limited.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(2), 9));
        limited.inventory().setStackInSlot(ClayProductionBlockEntity.LIMIT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(2)));
        helper.runAfterDelay(3, () -> {
            for (int level = 0; level < 13; level++) {
                ItemStack output = machines[level].inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT);
                require(output.is(ClayProductionRules.itemForLevel(level + 1)) && output.getCount() == 1,
                        "Automatic compression failed at level " + level);
            }
            require(limited.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "Compression limit was ignored");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void productionMenusExposeOnlyUsableSlotsAndShiftInsert(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        ClayProductionBlockEntity energetic = machine(helper,
                ClayProductionType.ENERGETIC_CLAY_CONDENSER_MK_I, new BlockPos(1, 2, 1));
        ClayProductionMenu fuelMenu = (ClayProductionMenu)energetic.createMenu(1, player.getInventory(), player);
        require(fuelMenu.slots.size() == 40 && fuelMenu.hasFuelSlot() && !fuelMenu.hasLimitSlot(),
                "Energetic condenser exposed the wrong slots");
        player.getInventory().setItem(9, new ItemStack(ClayProductionRules.itemForLevel(5)));
        fuelMenu.quickMoveStack(player, 4);
        require(energetic.inventory().getStackInSlot(ClayProductionBlockEntity.FUEL_SLOT).getCount() == 1,
                "Shift-click failed to fill the CE fuel slot");

        ClayProductionBlockEntity automatic = machine(helper,
                ClayProductionType.AUTOMATIC_CLAY_CONDENSER_MK_I, new BlockPos(3, 2, 1));
        automatic.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT, new ItemStack(Items.CLAY));
        ClayProductionMenu limitMenu = (ClayProductionMenu)automatic.createMenu(2, player.getInventory(), player);
        require(limitMenu.slots.size() == 40 && limitMenu.hasLimitSlot() && !limitMenu.hasFuelSlot(),
                "Automatic condenser exposed the wrong slots");
        player.getInventory().setItem(9, new ItemStack(ClayProductionRules.itemForLevel(1)));
        limitMenu.quickMoveStack(player, 4);
        require(automatic.inventory().getStackInSlot(ClayProductionBlockEntity.LIMIT_SLOT).getCount() == 1,
                "Shift-click failed to fill the compression limit slot");

        ClayProductionBlockEntity solar = machine(helper,
                ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_I, new BlockPos(5, 2, 1));
        ClayProductionMenu solarMenu = (ClayProductionMenu)solar.createMenu(3, player.getInventory(), player);
        require(solarMenu.slots.size() == 39 && !solarMenu.hasFuelSlot() && !solarMenu.hasLimitSlot(),
                "Solar machine exposed unused slots");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 16800)
    public static void solarMachinesRequireOpenSkyRespectCapsAndUseLithium(GameTestHelper helper) {
        helper.getLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, helper.getLevel().getServer());
        helper.getLevel().setDayTime(6000);
        helper.getLevel().setWeatherParameters(0, 0, false, false);
        ClayProductionBlockEntity outdoor = machine(helper,
                ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_I, new BlockPos(1, 2, 1));
        ClayProductionBlockEntity indoor = machine(helper,
                ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_I, new BlockPos(3, 2, 1));
        helper.setBlock(new BlockPos(3, 3, 1), Blocks.STONE);
        outdoor.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT, new ItemStack(Items.CLAY));
        indoor.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT, new ItemStack(Items.CLAY));
        ClayProductionBlockEntity capped = machine(helper,
                ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_II, new BlockPos(5, 2, 1));
        capped.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(7)));
        ClayProductionBlockEntity lithium = machine(helper,
                ClayProductionType.LITHIUM_SOLAR_CLAY_FABRICATOR, new BlockPos(7, 2, 1));
        lithium.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(MaterialRegistration.MATERIAL_ITEMS.get("lithium_ingot").get()));
        long[] pausedProgress = new long[1];
        helper.runAfterDelay(3, () -> {
            require(outdoor.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT)
                    .is(ClayProductionRules.itemForLevel(1)), "Outdoor machine did not manufacture clay");
            require(indoor.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).isEmpty()
                    && indoor.inventory().getStackInSlot(ClayProductionBlockEntity.INPUT_SLOT).is(Items.CLAY),
                    "Roofed machine processed clay");
            require(capped.inventory().getStackInSlot(ClayProductionBlockEntity.RESERVED_SLOT).isEmpty(),
                    "Solar Mk II exceeded its compression cap");
            require(lithium.activeInputLevel() == 8 && lithium.requiredProgress() == 16699,
                    "Lithium route did not start at its original stage");
            pausedProgress[0] = lithium.progress();
            helper.getLevel().setDayTime(18000);
        });
        helper.runAfterDelay(5, () -> {
            require(lithium.progress() == pausedProgress[0], "Solar machine ran at night");
            helper.getLevel().setDayTime(6000);
            helper.getLevel().setWeatherParameters(0, 200, true, false);
            helper.getLevel().setRainLevel(1.0F);
            pausedProgress[0] = lithium.progress();
        });
        helper.runAfterDelay(8, () -> {
            require(lithium.progress() == pausedProgress[0], "Solar machine ran in rain");
            helper.getLevel().setWeatherParameters(0, 0, false, false);
            helper.getLevel().setRainLevel(0.0F);
        });
        helper.runAfterDelay(16720, () -> {
            require(lithium.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT)
                    .is(ClayProductionRules.itemForLevel(9)), "Lithium route did not produce energetic clay");
            helper.getLevel().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, helper.getLevel().getServer());
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 30)
    public static void solarCePerTickUsesInputOutputDifference(GameTestHelper helper) {
        helper.getLevel().setDayTime(6000);
        helper.getLevel().setWeatherParameters(0, 0, false, false);
        ClayProductionBlockEntity machine = machine(helper,
                ClayProductionType.SOLAR_CLAY_FABRICATOR_MK_II, new BlockPos(1, 2, 1));
        machine.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(5)));
        helper.runAfterDelay(2, () -> {
            var player = helper.makeMockPlayer(GameType.CREATIVE);
            ClayProductionMenu menu = (ClayProductionMenu)machine.createMenu(1, player.getInventory(), player);
            require(menu.requiredProgress() == 890 && menu.cePerTick() == 1011,
                    "GUI CE/t did not show the 900000 internal CE difference divided by 890 ticks");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 950)
    public static void clayFabricatorsKeepSamplesDuplicateCountsAndUseOriginalTiming(GameTestHelper helper) {
        ClayProductionBlockEntity mkOne = machine(helper,
                ClayProductionType.CLAY_FABRICATOR_MK_I, new BlockPos(1, 2, 1));
        mkOne.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(1), 2));
        ClayProductionBlockEntity mkTwo = machine(helper,
                ClayProductionType.CLAY_FABRICATOR_MK_II, new BlockPos(3, 2, 1));
        mkTwo.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(1)));
        ClayProductionBlockEntity mkOneCap = machine(helper,
                ClayProductionType.CLAY_FABRICATOR_MK_I, new BlockPos(5, 2, 1));
        mkOneCap.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(12)));
        ClayProductionBlockEntity mkTwoTop = machine(helper,
                ClayProductionType.CLAY_FABRICATOR_MK_II, new BlockPos(7, 2, 1));
        mkTwoTop.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(13)));
        ClayProductionBlockEntity mkThree = machine(helper,
                ClayProductionType.CLAY_FABRICATOR_MK_III, new BlockPos(9, 2, 1));
        mkThree.inventory().setStackInSlot(ClayProductionBlockEntity.INPUT_SLOT,
                new ItemStack(ClayProductionRules.itemForLevel(13), 3));
        helper.runAfterDelay(2, () -> {
            require(mkTwo.requiredProgress() == 897,
                    "Fabricator original low-stage timing changed");
            require(mkOne.inventory().getStackInSlot(ClayProductionBlockEntity.INPUT_SLOT).getCount() == 2
                    && mkOne.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).getCount() >= 2,
                    "Mk I did not retain and duplicate its sample");
            require(mkOneCap.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).isEmpty(),
                    "Mk I exceeded sextuple energetic clay");
            require(mkTwoTop.requiredProgress() == 3675834,
                    "Mk II did not accept octuple energetic clay");
            require(mkThree.inventory().getStackInSlot(ClayProductionBlockEntity.INPUT_SLOT).getCount() == 3
                    && mkThree.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).getCount() >= 3,
                    "Mk III did not duplicate immediately");
        });
        helper.runAfterDelay(895, () -> require(
                mkTwo.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).isEmpty(),
                "Mk II completed before the original 897 ticks"));
        helper.runAfterDelay(900, () -> {
            require(mkTwo.inventory().getStackInSlot(ClayProductionBlockEntity.INPUT_SLOT).getCount() == 1
                    && mkTwo.inventory().getStackInSlot(ClayProductionBlockEntity.OUTPUT_SLOT).getCount() == 1,
                    "Mk II failed to duplicate after 897 ticks");
            helper.succeed();
        });
    }

    private static ClayProductionBlockEntity machine(
            GameTestHelper helper, ClayProductionType type, BlockPos position) {
        helper.setBlock(position, ClayProductionRegistration.BLOCKS.get(type).get());
        return helper.getBlockEntity(position);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private ClayProductionGameTests() {
    }
}
