package net.claustra01.clayium.gametest.machine;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.energy.ClayEnergy;
import net.claustra01.clayium.energy.TierManager;
import net.claustra01.clayium.machine.ClayMachineBlockEntity;
import net.claustra01.clayium.machine.ClayMachineMenu;
import net.claustra01.clayium.machine.series.ProcessingSeries;
import net.claustra01.clayium.registry.MachineRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.PanRegistration;
import net.claustra01.clayium.registry.FoundationItemRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ClayMachineGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void processingMachinesUseOriginalRelativeDefaultRoutes(GameTestHelper helper) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get("condenser_tier_4");
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState state = definition.block().get().defaultBlockState()
                .setValue(net.claustra01.clayium.machine.ClayMachineBlock.FACING, Direction.EAST);
        helper.getLevel().setBlockAndUpdate(position, state);
        ClayMachineBlockEntity machine = (ClayMachineBlockEntity) helper.getLevel().getBlockEntity(position);
        require(machine != null, "Placed processing machine did not create its shared block entity");
        require(machine.faceMode(Direction.DOWN) == ClayMachineBlockEntity.FaceMode.OUTPUT,
                "The original lower output route was not restored");
        require(machine.faceMode(Direction.UP) == ClayMachineBlockEntity.FaceMode.INPUT,
                "The original upper normal-input route was not restored");
        require(machine.faceMode(Direction.WEST) == ClayMachineBlockEntity.FaceMode.FUEL_INPUT,
                "The original rear fuel-input route did not follow the machine facing");
        require(machine.faceMode(Direction.EAST) == ClayMachineBlockEntity.FaceMode.DISABLED
                        && machine.faceMode(Direction.NORTH) == ClayMachineBlockEntity.FaceMode.DISABLED
                        && machine.faceMode(Direction.SOUTH) == ClayMachineBlockEntity.FaceMode.DISABLED,
                "A processing-machine face without an original route was enabled by default");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void ioFacesUseOriginalRouteTexturesAndPipeShapesDoNotCullNeighbors(GameTestHelper helper) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get("condenser_tier_4");
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        BlockState normal = definition.block().get().defaultBlockState();
        helper.getLevel().setBlockAndUpdate(position, normal);
        ClayMachineBlockEntity machine = (ClayMachineBlockEntity)helper.getLevel().getBlockEntity(position);
        require(machine != null, "Placed processing machine did not create its shared block entity");
        require("import".equals(machine.insertionFaceTexture(0)),
                "The normal input route no longer uses the original import texture");
        require("import_energy".equals(machine.insertionFaceTexture(1)),
                "The energetic-clay route no longer uses the original yellow texture");
        require("export".equals(machine.extractionFaceTexture(0)),
                "The output route no longer uses the original export texture");

        BlockState pipe = normal.setValue(net.claustra01.clayium.machine.ClayMachineBlock.PIPE, true);
        helper.getLevel().setBlockAndUpdate(position, pipe);
        require(pipe.getOcclusionShape(helper.getLevel(), position).isEmpty(),
                "A pipe-shaped machine still culls the adjacent block face");
        require(!normal.getOcclusionShape(helper.getLevel(), position).isEmpty(),
                "A normal full-block machine lost its occlusion shape");

        BlockPos cablePosition = helper.absolutePos(new BlockPos(3, 1, 1));
        BlockState cable = PanRegistration.PAN_CABLE.get().defaultBlockState();
        helper.getLevel().setBlockAndUpdate(cablePosition, cable);
        require(cable.getOcclusionShape(helper.getLevel(), cablePosition).isEmpty(),
                "PAN cable still culls the adjacent block face");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void sharedMachineStateSurvivesStopsSavesAndRecipeChanges(GameTestHelper helper) {
        assertStateMachine(
                helper,
                ProcessingSeries.CONDENSER,
                new ItemStack(Items.CLAY_BALL, 4),
                Items.CLAY,
                1,
                20L,
                20L);
        assertStateMachine(
                helper,
                ProcessingSeries.GRINDER,
                new ItemStack(Items.IRON_INGOT),
                MaterialRegistration.MATERIAL_ITEMS.get("iron_dust").get(),
                1,
                40L,
                4_000L);

        ClayMachineBlockEntity changed = machine(helper, ProcessingSeries.GRINDER, 4);
        changed.inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
        changed.receiveClayEnergy(ClayEnergy.ofInternal(10_000L));
        tick(helper, changed);
        require(changed.activeRecipe() != null, "The recipe-change fixture did not reserve its input");
        CompoundTag changedTag = changed.saveWithFullMetadata(helper.getLevel().registryAccess());
        changedTag.putString("active_recipe", "clayium:missing_after_reload");
        ClayMachineBlockEntity reloaded = load(helper, changed, changedTag);
        tick(helper, reloaded);
        require(reloaded.activeRecipe() == null, "A removed recipe remained active after reload");
        require(reloaded.inventory().getStackInSlot(0).is(Items.IRON_INGOT),
                "A removed recipe did not return its reserved input");
        require(reloaded.inventory().getStackInSlot(reloaded.reservedSlot(0)).isEmpty(),
                "A removed recipe left a duplicate reserved input");
        require(reloaded.inventory().getStackInSlot(reloaded.outputSlot()).isEmpty(),
                "A removed recipe produced an output");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void grinderTierThreeFourAndSixUseSharedTierParameters(GameTestHelper helper) {
        ClayMachineBlockEntity tierThree = machine(helper, ProcessingSeries.GRINDER, 3);
        ClayMachineBlockEntity tierFour = machine(helper, ProcessingSeries.GRINDER, 4);
        ClayMachineBlockEntity tierSix = machine(helper, ProcessingSeries.GRINDER, 6);
        for (ClayMachineBlockEntity machine : List.of(tierThree, tierFour, tierSix)) {
            machine.inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT));
            machine.receiveClayEnergy(ClayEnergy.ofInternal(100_000L));
            tick(helper, machine);
        }

        for (ClayMachineBlockEntity machine : List.of(tierThree, tierFour, tierSix)) {
            IItemHandler input = machine.itemHandler(Direction.UP);
            IItemHandler output = machine.itemHandler(Direction.DOWN);
            require(input.getSlots() == 1 && output.getSlots() == 1,
                    "Tier changed the common sided slot layout");
            require(output.insertItem(0, new ItemStack(Items.CLAY), true).getCount() == 1,
                    "Output face accepted insertion");
            require(input.extractItem(0, 1, true).isEmpty(),
                    "Input face allowed extraction");
            ClayMachineBlockEntity ioMachine = machine(helper, ProcessingSeries.GRINDER, machine.tier());
            IItemHandler ioInput = ioMachine.itemHandler(Direction.UP);
            IItemHandler ioOutput = ioMachine.itemHandler(Direction.DOWN);
            require(ioInput.insertItem(0, new ItemStack(Items.CLAY), false).isEmpty()
                            && ioMachine.inventory().getStackInSlot(0).is(Items.CLAY),
                    "An input face rejected its item");
            IItemHandler fuelInput = ioMachine.itemHandler(Direction.SOUTH);
            require(fuelInput.getSlots() == 1
                            && !fuelInput.insertItem(0, new ItemStack(Items.CLAY), true).isEmpty(),
                    "The rear fuel route accepted a normal input item");
            ioMachine.inventory().setStackInSlot(ioMachine.outputSlot(), new ItemStack(Items.CLAY));
            require(ioOutput.extractItem(0, 1, false).is(Items.CLAY)
                            && ioMachine.inventory().getStackInSlot(ioMachine.outputSlot()).isEmpty(),
                    "An output face rejected extraction");
        }
        ClayMachineBlockEntity condenserThree = machine(helper, ProcessingSeries.CONDENSER, 3);
        ClayMachineBlockEntity condenserFive = machine(helper, ProcessingSeries.CONDENSER, 5);
        for (ClayMachineBlockEntity machine : List.of(condenserThree, condenserFive)) {
            machine.inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 4));
            machine.receiveClayEnergy(ClayEnergy.ofInternal(100_000L));
            tick(helper, machine);
        }
        require(condenserFive.requiredProgress() < condenserThree.requiredProgress(),
                "The second series did not use shared TierManager speed parameters");

        require(tierThree.activeRecipe() == null
                        && tierThree.inventory().getStackInSlot(0).is(Items.IRON_INGOT),
                "Tier 3 started the Tier 4 iron grinding recipe");
        require(tierFour.requiredProgress() == 40L,
                "Tier 4 did not use the common base processing time");
        require(tierSix.requiredProgress() == 2L,
                "Tier 6 did not use the common TierManager speed multiplier");

        tick(helper, tierFour);
        tick(helper, tierFour);
        tick(helper, tierSix);
        tick(helper, tierSix);
        require(tierFour.inventory().getStackInSlot(tierFour.outputSlot()).isEmpty(),
                "Tier 4 completed as quickly as Tier 6");
        require(tierSix.inventory().getStackInSlot(tierSix.outputSlot())
                        .is(MaterialRegistration.MATERIAL_ITEMS.get("iron_dust").get()),
                "Tier 6 did not emit the shared recipe output");
        require(tierSix.energyStorage().stored().internalValue() == 95_000L,
                "Tier 6 did not apply its shared CE multiplier");

        runUntilIdle(helper, tierFour, 50);
        require(tierFour.inventory().getStackInSlot(tierFour.outputSlot())
                        .is(MaterialRegistration.MATERIAL_ITEMS.get("iron_dust").get()),
                "Tier 4 did not emit the shared recipe output");
        require(tierFour.energyStorage().stored().internalValue() == 96_000L,
                "Tier 4 consumed the wrong amount of CE");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void machineMenuAndFacesEnforceServerOwnedAccess(GameTestHelper helper) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get("condenser_tier_4");
        BlockPos machinePos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(machinePos, definition.block().get().defaultBlockState());
        ClayMachineBlockEntity machine = (ClayMachineBlockEntity)helper.getLevel().getBlockEntity(machinePos);
        require(machine != null, "Placed processing machine did not create its shared block entity");
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setPos(machinePos.getX() + 0.5D, machinePos.getY() + 0.5D, machinePos.getZ() + 0.5D);
        ClayMachineMenu menu = new ClayMachineMenu(
                1, player.getInventory(), machine, new SimpleContainerData(18));
        require(menu.stillValid(player), "A nearby player could not use the open machine menu");
        player.setPos(machinePos.getX() + 9.0D, machinePos.getY(), machinePos.getZ());
        require(!menu.stillValid(player), "A remote player retained machine menu access");

        machine.setFaceMode(Direction.NORTH, ClayMachineBlockEntity.FaceMode.DISABLED);
        IItemHandler disabled = machine.itemHandler(Direction.NORTH);
        require(disabled.getSlots() == 0, "A disabled face still exposed machine slots");
        machine.setFaceMode(Direction.NORTH, ClayMachineBlockEntity.FaceMode.OUTPUT);
        IItemHandler output = machine.itemHandler(Direction.NORTH);
        require(output.getSlots() == 1
                        && !output.insertItem(0, new ItemStack(Items.CLAY), false).isEmpty(),
                "An output face accepted inserted items");
        machine.inventory().setStackInSlot(machine.reservedSlot(0), new ItemStack(Items.BRICK));
        IItemHandler unsided = machine.itemHandler(null);
        require(unsided.getSlots() == 3, "Unsided access exposed an internal machine slot");
        for (int slot = 0; slot < unsided.getSlots(); slot++) {
            require(!unsided.getStackInSlot(slot).is(Items.BRICK),
                    "Unsided access exposed the processing reservation");
        }

        CompoundTag tag = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
        ClayMachineBlockEntity loaded = load(helper, machine, tag);
        require(loaded.faceMode(Direction.NORTH) == ClayMachineBlockEntity.FaceMode.OUTPUT,
                "The server-owned face setting did not survive saving");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void originalMachineToolsConfigureRotateAndRestoreFaces(GameTestHelper helper) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get("condenser_tier_4");
        BlockPos position = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlockAndUpdate(position, definition.block().get().defaultBlockState());
        ClayMachineBlockEntity machine = (ClayMachineBlockEntity) helper.getLevel().getBlockEntity(position);
        require(machine != null, "Placed processing machine did not create its shared block entity");

        var player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(Vec3.atCenterOf(position));

        ItemStack ioTool = WorkbenchRegistration.CLAY_IO_CONFIGURATOR.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, ioTool);
        useTool(player, position, Direction.UP, "I/O fuel insertion select");
        require(machine.faceMode(Direction.UP) == ClayMachineBlockEntity.FaceMode.FUEL_INPUT,
                "A normal I/O-tool click did not advance to the fuel insertion route");
        useTool(player, position, Direction.UP, "I/O insertion disable");
        require(machine.faceMode(Direction.UP) == ClayMachineBlockEntity.FaceMode.DISABLED,
                "A second normal I/O-tool click did not disable insertion");
        useTool(player, position, Direction.UP, "I/O insertion restore");
        require(machine.faceMode(Direction.UP) == ClayMachineBlockEntity.FaceMode.INPUT,
                "A third normal I/O-tool click did not restore the item insertion route");
        player.setShiftKeyDown(true);
        useTool(player, position, Direction.UP, "I/O extraction select");
        player.setShiftKeyDown(false);
        require(machine.faceMode(Direction.UP) == ClayMachineBlockEntity.FaceMode.INPUT_OUTPUT,
                "A sneaking I/O-tool click did not add extraction independently of insertion");
        require(machine.itemHandler(Direction.UP).getSlots() == 2,
                "The combined I/O state did not expose its input and output routes");
        require(ioTool.getDamageValue() == 4, "Successful I/O-tool clicks consumed the wrong durability");

        ItemStack wrench = FoundationItemRegistration.CLAY_WRENCH.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        useTool(player, position, Direction.EAST, "wrench turn");
        require(helper.getLevel().getBlockState(position).getValue(net.claustra01.clayium.machine.ClayMachineBlock.FACING)
                        == Direction.EAST,
                "The wrench did not turn the machine toward the clicked side");
        useTool(player, position, Direction.EAST, "wrench reverse");
        require(helper.getLevel().getBlockState(position).getValue(net.claustra01.clayium.machine.ClayMachineBlock.FACING)
                        == Direction.WEST,
                "Clicking the current front with the wrench did not reverse it");

        ItemStack pipingTool = WorkbenchRegistration.CLAY_PIPING_CONFIGURATOR.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, pipingTool);
        player.setShiftKeyDown(true);
        useTool(player, position, Direction.SOUTH, "piping-tool turn");
        player.setShiftKeyDown(false);
        require(helper.getLevel().getBlockState(position).getValue(net.claustra01.clayium.machine.ClayMachineBlock.FACING)
                        == Direction.SOUTH,
                "A sneaking piping-tool click did not perform the original rotation action");
        require(pipingTool.getDamageValue() == 1,
                "A successful piping-tool rotation consumed the wrong durability");
        useTool(player, position, Direction.NORTH, "pipe shape toggle");
        require(helper.getLevel().getBlockState(position)
                        .getValue(net.claustra01.clayium.machine.ClayMachineBlock.PIPE),
                "A normal piping-tool click did not enable the shared pipe shape");
        require(pipingTool.getDamageValue() == 2,
                "A successful pipe toggle consumed the wrong durability");

        machine.setFaceMode(Direction.UP, ClayMachineBlockEntity.FaceMode.DISABLED);
        ItemStack memory = WorkbenchRegistration.MEMORY_CARD.toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, memory);
        player.setShiftKeyDown(true);
        useTool(player, position, Direction.NORTH, "memory save");
        player.setShiftKeyDown(false);
        machine.setFaceMode(Direction.UP, ClayMachineBlockEntity.FaceMode.INPUT);
        helper.getLevel().setBlockAndUpdate(position, helper.getLevel().getBlockState(position)
                .setValue(net.claustra01.clayium.machine.ClayMachineBlock.FACING, Direction.NORTH)
                .setValue(net.claustra01.clayium.machine.ClayMachineBlock.PIPE, false));
        useTool(player, position, Direction.NORTH, "memory load");
        require(machine.faceMode(Direction.UP) == ClayMachineBlockEntity.FaceMode.DISABLED,
                "The memory card did not restore the saved face setting");
        require(helper.getLevel().getBlockState(position).getValue(net.claustra01.clayium.machine.ClayMachineBlock.FACING)
                        == Direction.SOUTH,
                "The memory card did not restore the saved front");
        require(helper.getLevel().getBlockState(position)
                        .getValue(net.claustra01.clayium.machine.ClayMachineBlock.PIPE),
                "The memory card did not restore the shared pipe state");
        helper.succeed();
    }

    private static void useTool(
            net.minecraft.world.entity.player.Player player, BlockPos position, Direction side, String action) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(position), side, position, false);
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        require(player.getMainHandItem().onItemUseFirst(context).consumesAction(),
                "The machine configuration tool did not consume its " + action + " click");
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allFourteenProcessingMachinesRegisterAndProcess(GameTestHelper helper) {
        long processingMachines = MachineRegistration.MACHINES.values().stream()
                .filter(definition -> definition.series() instanceof ProcessingSeries)
                .count();
        require(processingMachines == 14, "The milestone did not retain exactly 14 processing machines");
        for (MachineRegistration.MachineDefinition definition : MachineRegistration.MACHINES.values()) {
            if (!(definition.series() instanceof ProcessingSeries series)) {
                continue;
            }
            int tier = definition.tier();
            RecipeExample example = example(series, tier);
            ClayMachineBlockEntity machine = machine(helper, series, tier);
            machine.inventory().setStackInSlot(0, example.input().copy());
            machine.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000_000_000L));
            tick(helper, machine);
            long requiredTicks = TierManager.calculate(TierManager.general(tier), example.ticks(),
                    example.energy(), 1.0D).requiredProgress();
            long energyPerTick = TierManager.calculate(TierManager.general(tier), example.ticks(),
                    example.energy(), 1.0D).energyPerProgress().internalValue();
            require(machine.activeRecipe() != null && machine.requiredProgress() == requiredTicks,
                    definition.id() + " did not start its own-tier recipe with the expected time");
            runUntilIdle(helper, machine, (int)requiredTicks + 2);
            ItemStack result = machine.inventory().getStackInSlot(machine.outputSlot());
            require(result.is(example.output().asItem()) && result.getCount() == example.outputCount(),
                    definition.id() + " produced the wrong amount");
            require(machine.inventory().getStackInSlot(0).isEmpty()
                            && machine.inventory().getStackInSlot(machine.reservedSlot(0)).isEmpty(),
                    definition.id() + " did not consume the required input");
            require(machine.energyStorage().stored().internalValue()
                            == 1_000_000_000_000_000L - requiredTicks * energyPerTick,
                    definition.id() + " consumed the wrong CE amount");
            int lowerTier = 0;
            for (int registeredTier : series.tiers()) {
                if (registeredTier < tier) {
                    lowerTier = registeredTier;
                }
            }
            if (lowerTier != 0 && series != ProcessingSeries.DECOMPOSER) {
                ClayMachineBlockEntity lower = machine(helper, series, lowerTier);
                lower.inventory().setStackInSlot(0, example.input().copy());
                lower.receiveClayEnergy(ClayEnergy.ofInternal(1_000_000_000_000_000L));
                tick(helper, lower);
                require(lower.activeRecipe() == null && lower.inventory().getStackInSlot(lower.outputSlot()).isEmpty(),
                        series + " tier " + lowerTier + " started a tier " + tier + " recipe");
            }
        }
        helper.succeed();
    }

    private static RecipeExample example(ProcessingSeries series, int tier) {
        return switch (series) {
            case CONDENSER -> switch (tier) {
                case 2 -> new RecipeExample(new ItemStack(Items.CLAY_BALL, 4), Items.CLAY, 1, 20, 1);
                case 3 -> new RecipeExample(new ItemStack(material("dense_clay_dust"), 9), compression("dense_clay"), 1, 20, 10);
                case 4 -> new RecipeExample(new ItemStack(material("industrial_clay_dust"), 9), compression("industrial_clay"), 1, 20, 100);
                case 5 -> new RecipeExample(new ItemStack(material("silicon_dust"), 9), material("silicon_block"), 1, 20, 1_000);
                case 10 -> new RecipeExample(new ItemStack(material("antimatter_dust"), 9), material("antimatter_block"), 1, 20, 100_000_000);
                default -> throw new IllegalArgumentException();
            };
            case GRINDER -> switch (tier) {
                case 2 -> new RecipeExample(new ItemStack(Items.CLAY), material("clay_dust"), 4, 20, 1);
                case 3 -> new RecipeExample(new ItemStack(Items.QUARTZ), material("quartz_dust"), 1, 40, 10);
                case 4 -> new RecipeExample(new ItemStack(Items.IRON_INGOT), material("iron_dust"), 1, 40, 100);
                case 5 -> new RecipeExample(new ItemStack(material("aluminium_ingot")), material("aluminium_dust"), 1, 40, 1_000);
                case 6 -> new RecipeExample(new ItemStack(material("clay_steel_ingot")), material("clay_steel_dust"), 1, 40, 10_000);
                case 10 -> new RecipeExample(new ItemStack(material("antimatter_block")), material("antimatter_dust"), 9, 40, 100_000_000);
                default -> throw new IllegalArgumentException();
            };
            case DECOMPOSER -> switch (tier) {
                case 2 -> new RecipeExample(new ItemStack(Items.CLAY), Items.CLAY_BALL, 4, 3, 1);
                case 3 -> new RecipeExample(new ItemStack(compression("compressed_clay")), compression("dense_clay"), 9, 3, 1);
                case 4 -> new RecipeExample(new ItemStack(compression("industrial_clay")), compression("compressed_clay"), 9, 10, 1);
                default -> throw new IllegalArgumentException();
            };
        };
    }

    private static ItemLike material(String id) {
        return MaterialRegistration.MATERIAL_ITEMS.get(id).get();
    }

    private static ItemLike compression(String id) {
        return MaterialRegistration.COMPRESSION_BLOCKS.get(id).get();
    }

    private record RecipeExample(ItemStack input, ItemLike output, int outputCount, int ticks, long energy) {
    }

    private static void assertStateMachine(
            GameTestHelper helper,
            ProcessingSeries series,
            ItemStack input,
            ItemLike expectedOutput,
            int expectedCount,
            long expectedProgress,
            long expectedEnergyUse) {
        ClayMachineBlockEntity machine = machine(helper, series, 4);
        machine.inventory().setStackInSlot(0, input.copy());
        machine.receiveClayEnergy(ClayEnergy.ofInternal(100_000L));
        tick(helper, machine);
        require(machine.state() == ClayMachineBlockEntity.MachineState.RUNNING,
                series + " did not enter the shared running state");
        require(machine.requiredProgress() == expectedProgress,
                series + " did not retain the recipe progress target");
        require(machine.inventory().getStackInSlot(0).isEmpty(),
                series + " did not reserve its input");
        require(machine.inventory().getStackInSlot(machine.reservedSlot(0)).getCount() == input.getCount(),
                series + " reserved the wrong input count");

        tick(helper, machine);
        long beforeBlockedTick = machine.energyStorage().stored().internalValue();
        machine.inventory().setStackInSlot(
                machine.outputSlot(), new ItemStack(Items.COBBLESTONE, 64));
        tick(helper, machine);
        require(machine.state() == ClayMachineBlockEntity.MachineState.OUTPUT_FULL,
                series + " did not stop for a full output slot");
        require(machine.energyStorage().stored().internalValue() == beforeBlockedTick,
                series + " consumed CE while its output was full");
        machine.inventory().setStackInSlot(machine.outputSlot(), ItemStack.EMPTY);
        tick(helper, machine);
        require(machine.state() == ClayMachineBlockEntity.MachineState.RUNNING,
                series + " did not resume after the output was cleared");

        CompoundTag saved = machine.saveWithFullMetadata(helper.getLevel().registryAccess());
        ClayMachineBlockEntity loaded = load(helper, machine, saved);
        require(loaded.progress() == machine.progress(), series + " lost progress while saving");
        require(loaded.energyStorage().stored().equals(machine.energyStorage().stored()),
                series + " lost CE while saving");
        runUntilIdle(helper, loaded, 80);
        ItemStack result = loaded.inventory().getStackInSlot(loaded.outputSlot());
        require(result.is(expectedOutput.asItem()) && result.getCount() == expectedCount,
                series + " produced the wrong output after reloading");
        require(loaded.inventory().getStackInSlot(loaded.reservedSlot(0)).isEmpty(),
                series + " duplicated its reserved input after reloading");
        require(loaded.energyStorage().stored().internalValue() == 100_000L - expectedEnergyUse,
                series + " consumed the wrong total CE across saving and output blocking");
    }

    private static ClayMachineBlockEntity machine(GameTestHelper helper, ProcessingSeries series, int tier) {
        MachineRegistration.MachineDefinition definition = MachineRegistration.MACHINES.get(
                series.recipeMethod().id() + "_tier_" + tier);
        require(definition != null, "Missing machine definition for " + series + " tier " + tier);
        BlockState state = definition.block().get().defaultBlockState();
        ClayMachineBlockEntity machine = new ClayMachineBlockEntity(helper.absolutePos(BlockPos.ZERO), state);
        machine.setLevel(helper.getLevel());
        return machine;
    }

    private static ClayMachineBlockEntity load(
            GameTestHelper helper, ClayMachineBlockEntity source, CompoundTag tag) {
        ClayMachineBlockEntity loaded = new ClayMachineBlockEntity(source.getBlockPos(), source.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(tag, helper.getLevel().registryAccess());
        return loaded;
    }

    private static void tick(GameTestHelper helper, ClayMachineBlockEntity machine) {
        ClayMachineBlockEntity.serverTick(
                helper.getLevel(), machine.getBlockPos(), machine.getBlockState(), machine);
    }

    private static void runUntilIdle(GameTestHelper helper, ClayMachineBlockEntity machine, int maximumTicks) {
        for (int tick = 0; tick < maximumTicks && machine.activeRecipe() != null; tick++) {
            tick(helper, machine);
        }
        require(machine.activeRecipe() == null, "Machine did not complete within " + maximumTicks + " ticks");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private ClayMachineGameTests() {
    }
}
