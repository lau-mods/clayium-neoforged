package net.claustra01.clayium.gametest.autocrafter;

import java.util.Arrays;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterBlockEntity;
import net.claustra01.clayium.machine.autocrafter.AutoCrafterSpecification;
import net.claustra01.clayium.recipe.ClayiumMachineRecipe;
import net.claustra01.clayium.recipe.ClayiumRecipeMethod;
import net.claustra01.clayium.registry.AutoCrafterRegistration;
import net.claustra01.clayium.registry.OriginalRegistryCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AutoCrafterGameTests {
    private static final long STARTING_ENERGY = 1_000_000L;

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allFiveTiersCraftAtOriginalCyclesWithoutInfiniteOutput(GameTestHelper helper) {
        OriginalRegistryCatalog.MachineFamily family = OriginalRegistryCatalog.MACHINE_FAMILIES.stream()
                .filter(candidate -> candidate.legacyName().equals("AutoCrafter"))
                .findFirst().orElseThrow();
        require(Arrays.equals(family.tiers(), new int[] {5, 6, 7, 8, 9}),
                "The original auto crafter tier ledger changed");
        require(AutoCrafterRegistration.AUTO_CRAFTERS.size() == 5,
                "Exactly five auto crafters must be registered");

        for (AutoCrafterSpecification specification : AutoCrafterSpecification.VALUES) {
            AutoCrafterBlockEntity autoCrafter = machine(helper, specification.tier());
            autoCrafter.setPatternStack(0, new ItemStack(Items.SUGAR_CANE));
            fillInputs(autoCrafter, Items.SUGAR_CANE, specification.craftsPerCycle());
            autoCrafter.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
            for (int tick = 1; tick < specification.intervalTicks(); tick++) {
                tick(helper, autoCrafter);
                require(outputCount(autoCrafter, Items.SUGAR) == 0,
                        "Tier " + specification.tier() + " crafted before its cycle elapsed");
            }
            tick(helper, autoCrafter);
            int crafts = specification.craftsPerCycle();
            require(outputCount(autoCrafter, Items.SUGAR) == crafts,
                    "Tier " + specification.tier() + " produced the wrong batch");
            require(inputCount(autoCrafter, Items.SUGAR_CANE) == 0,
                    "Tier " + specification.tier() + " consumed the wrong material count");
            require(autoCrafter.energyStorage().stored().internalValue()
                            == STARTING_ENERGY - crafts * specification.energyPerCraft(),
                    "Tier " + specification.tier() + " consumed the wrong CE amount");
            for (int tick = 0; tick < specification.intervalTicks() * 2; tick++) {
                tick(helper, autoCrafter);
            }
            require(outputCount(autoCrafter, Items.SUGAR) == crafts,
                    "Tier " + specification.tier() + " generated output without materials");

            String id = OriginalRegistryCatalog.machineId(family, specification.tier());
            require(AutoCrafterRegistration.AUTO_CRAFTERS.get(specification.tier()).id().equals(id),
                    "Tier " + specification.tier() + " has the wrong named ID");
            ClayiumMachineRecipe recipe = (ClayiumMachineRecipe)helper.getLevel().getRecipeManager()
                    .byKey(ClayiumMod.id("auto_crafter/" + id)).orElseThrow().value();
            require(recipe.processingMethod() == ClayiumRecipeMethod.ASSEMBLER
                            && recipe.processingTicks() == 40,
                    id + " does not use the assembly construction path");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void blockedAndPartialCraftsNeverConsumeMaterialsOrEnergy(GameTestHelper helper) {
        AutoCrafterBlockEntity blocked = machine(helper, 6);
        blocked.setPatternStack(0, new ItemStack(Items.OAK_LOG));
        blocked.inventory().setStackInSlot(0, new ItemStack(Items.OAK_LOG));
        for (int slot = 0; slot < AutoCrafterBlockEntity.OUTPUT_SLOTS; slot++) {
            blocked.inventory().setStackInSlot(
                    AutoCrafterBlockEntity.OUTPUT_START + slot, new ItemStack(Blocks.COBBLESTONE, 64));
        }
        blocked.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, blocked);
        require(blocked.inventory().getStackInSlot(0).getCount() == 1
                        && blocked.energyStorage().stored().internalValue() == STARTING_ENERGY,
                "A full output consumed input or CE");

        AutoCrafterBlockEntity blockedRemainders = machine(helper, 6);
        for (int slot : new int[] {0, 1, 3, 4}) {
            blockedRemainders.setPatternStack(slot, new ItemStack(Items.HONEY_BOTTLE));
        }
        blockedRemainders.inventory().setStackInSlot(0, new ItemStack(Items.HONEY_BOTTLE, 4));
        for (int slot = 0; slot < AutoCrafterBlockEntity.OUTPUT_SLOTS - 1; slot++) {
            blockedRemainders.inventory().setStackInSlot(
                    AutoCrafterBlockEntity.OUTPUT_START + slot, new ItemStack(Blocks.COBBLESTONE, 64));
        }
        blockedRemainders.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, blockedRemainders);
        require(blockedRemainders.inventory().getStackInSlot(0).getCount() == 4
                        && outputCount(blockedRemainders, Items.HONEY_BLOCK) == 0
                        && blockedRemainders.energyStorage().stored().internalValue() == STARTING_ENERGY,
                "A recipe whose containers do not fit consumed input or CE");

        AutoCrafterBlockEntity partial = machine(helper, 6);
        partial.setPatternStack(0, new ItemStack(Items.OAK_PLANKS));
        partial.setPatternStack(3, new ItemStack(Items.OAK_PLANKS));
        partial.inventory().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS));
        partial.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, partial);
        require(partial.inventory().getStackInSlot(0).getCount() == 1
                        && outputCount(partial, Items.STICK) == 0
                        && partial.energyStorage().stored().internalValue() == STARTING_ENERGY,
                "A partial recipe consumed input, CE, or generated output");

        ItemStack namedPattern = new ItemStack(Items.OAK_LOG);
        namedPattern.set(DataComponents.CUSTOM_NAME, Component.literal("component locked"));
        AutoCrafterBlockEntity components = machine(helper, 6);
        components.setPatternStack(0, namedPattern);
        components.inventory().setStackInSlot(0, new ItemStack(Items.OAK_LOG));
        components.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, components);
        require(components.inventory().getStackInSlot(0).getCount() == 1,
                "A material with different data components matched the ghost pattern");
        components.inventory().setStackInSlot(0, namedPattern.copy());
        tick(helper, components);
        require(outputCount(components, Items.OAK_PLANKS) == 4,
                "A material with matching data components was rejected");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void vanillaRemainingItemsAndDamagedRecipesCommitExactlyOnce(GameTestHelper helper) {
        AutoCrafterBlockEntity honey = machine(helper, 6);
        for (int slot : new int[] {0, 1, 3, 4}) {
            honey.setPatternStack(slot, new ItemStack(Items.HONEY_BOTTLE));
        }
        honey.inventory().setStackInSlot(0, new ItemStack(Items.HONEY_BOTTLE, 4));
        honey.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, honey);
        require(outputCount(honey, Items.HONEY_BLOCK) == 1,
                "The vanilla honey recipe did not produce its result");
        require(outputCount(honey, Items.GLASS_BOTTLE) == 4,
                "Vanilla getRemainingItems did not return all containers");
        require(honey.inventory().getStackInSlot(0).isEmpty(),
                "The honey recipe did not consume exactly four bottles");

        AutoCrafterBlockEntity repair = machine(helper, 6);
        ItemStack firstTool = new ItemStack(Items.WOODEN_PICKAXE);
        firstTool.set(DataComponents.DAMAGE, 30);
        ItemStack secondTool = new ItemStack(Items.WOODEN_PICKAXE);
        secondTool.set(DataComponents.DAMAGE, 35);
        repair.setPatternStack(0, firstTool);
        repair.setPatternStack(1, secondTool);
        repair.inventory().setStackInSlot(0, firstTool.copy());
        repair.inventory().setStackInSlot(1, secondTool.copy());
        repair.receiveClayEnergy(ClayEnergy.ofInternal(STARTING_ENERGY));
        tick(helper, repair);
        require(repair.inventory().getStackInSlot(0).isEmpty()
                        && repair.inventory().getStackInSlot(1).isEmpty(),
                "The damaged repair ingredients were not consumed once");
        require(outputCount(repair, Items.WOODEN_PICKAXE) == 1,
                "The vanilla damaged-item recipe did not return exactly one tool");
        helper.succeed();
    }

    private static AutoCrafterBlockEntity machine(GameTestHelper helper, int tier) {
        BlockState state = AutoCrafterRegistration.AUTO_CRAFTERS.get(tier).block().get().defaultBlockState();
        AutoCrafterBlockEntity machine = new AutoCrafterBlockEntity(helper.absolutePos(BlockPos.ZERO), state);
        machine.setLevel(helper.getLevel());
        return machine;
    }

    private static void tick(GameTestHelper helper, AutoCrafterBlockEntity machine) {
        AutoCrafterBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static int outputCount(AutoCrafterBlockEntity machine, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < AutoCrafterBlockEntity.OUTPUT_SLOTS; slot++) {
            ItemStack stack = machine.inventory().getStackInSlot(AutoCrafterBlockEntity.OUTPUT_START + slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int inputCount(AutoCrafterBlockEntity machine, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < AutoCrafterBlockEntity.INPUT_SLOTS; slot++) {
            ItemStack stack = machine.inventory().getStackInSlot(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void fillInputs(
            AutoCrafterBlockEntity machine, net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        for (int slot = 0; slot < AutoCrafterBlockEntity.INPUT_SLOTS && remaining > 0; slot++) {
            int inserted = Math.min(item.getDefaultMaxStackSize(), remaining);
            machine.inventory().setStackInSlot(slot, new ItemStack(item, inserted));
            remaining -= inserted;
        }
        require(remaining == 0, "The test input does not fit in the auto crafter");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private AutoCrafterGameTests() {
    }
}
