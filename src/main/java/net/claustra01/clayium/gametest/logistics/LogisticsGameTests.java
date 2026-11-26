package net.claustra01.clayium.gametest.logistics;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.BufferMenu;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.WorkbenchRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LogisticsGameTests {
    private static final Map<BlockPos, IItemHandler> TEST_HANDLERS = new HashMap<>();

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void buffersUseOriginalRearInputOnlyDefault(GameTestHelper helper) {
        BufferBlockEntity buffer = placedBuffer(helper, 4, new BlockPos(1, 1, 1));
        require(buffer.getBlockState().getValue(net.claustra01.clayium.logistics.BufferBlock.FACING)
                        == Direction.NORTH,
                "The default buffer fixture no longer faces north");
        for (Direction direction : Direction.values()) {
            BufferBlockEntity.FaceMode expected = direction == Direction.SOUTH
                    ? BufferBlockEntity.FaceMode.INPUT
                    : BufferBlockEntity.FaceMode.DISABLED;
            require(buffer.faceMode(direction) == expected,
                    "The buffer has the wrong original default on " + direction.getName());
        }

        net.claustra01.clayium.logistics.BufferBlock block =
                (net.claustra01.clayium.logistics.BufferBlock) buffer.getBlockState().getBlock();
        net.minecraft.world.entity.player.Player placer = helper.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        placer.setYRot(90.0F);
        net.minecraft.world.item.context.BlockPlaceContext eastFacingPlacement =
                new net.minecraft.world.item.context.BlockPlaceContext(
                        helper.getLevel(), placer, net.minecraft.world.InteractionHand.MAIN_HAND,
                        new ItemStack(block), new net.minecraft.world.phys.BlockHitResult(
                                net.minecraft.world.phys.Vec3.atCenterOf(buffer.getBlockPos()),
                                Direction.WEST, buffer.getBlockPos(), false));
        net.minecraft.world.level.block.state.BlockState eastFacingState = block.getStateForPlacement(eastFacingPlacement);
        require(eastFacingState != null && eastFacingState.getValue(
                        net.claustra01.clayium.logistics.BufferBlock.FACING) == Direction.EAST,
                "The placement state did not face east");
        for (Direction direction : Direction.values()) {
            BufferBlockEntity.FaceMode expected = direction == Direction.WEST
                    ? BufferBlockEntity.FaceMode.INPUT
                    : BufferBlockEntity.FaceMode.DISABLED;
            require(eastFacingState.getValue(
                            net.claustra01.clayium.logistics.BufferBlock.FACES.get(direction.get3DDataValue())) == expected,
                    "The east-facing placement state did not keep the original rear-only route: " + direction);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void pipesUseActiveRoutesAndOriginalPassiveBufferEndpoints(GameTestHelper helper) {
        BufferBlockEntity source = placedBuffer(helper, 7, new BlockPos(1, 1, 1));
        BufferBlockEntity target = placedBuffer(helper, 7, new BlockPos(2, 1, 1));
        setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.DISABLED);
        setOnlyFace(target, Direction.WEST, BufferBlockEntity.FaceMode.DISABLED);
        helper.getLevel().setBlockAndUpdate(source.getBlockPos(),
                source.getBlockState().setValue(net.claustra01.clayium.logistics.BufferBlock.PIPE, true));
        helper.getLevel().setBlockAndUpdate(target.getBlockPos(),
                target.getBlockState().setValue(net.claustra01.clayium.logistics.BufferBlock.PIPE, true));

        require(!source.pipeConnects(Direction.EAST) && !target.pipeConnects(Direction.WEST),
                "Two passive buffer faces connected without an active import or export route");

        source.setFaceMode(Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        require(source.pipeConnects(Direction.EAST) && target.pipeConnects(Direction.WEST),
                "An active export did not join the neighboring passive buffer pipe");
        source.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 16));
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(total(source.inventory()) == 0 && total(target.inventory()) == 16,
                "The connected export could not insert into the passive buffer capability");

        target.inventory().setStackInSlot(0, ItemStack.EMPTY);
        source.inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 16));
        source.setFaceMode(Direction.EAST, BufferBlockEntity.FaceMode.DISABLED);
        target.setFaceMode(Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        require(source.pipeConnects(Direction.EAST) && target.pipeConnects(Direction.WEST),
                "An active import did not join the neighboring passive buffer pipe");
        BufferBlockEntity.serverTick(helper.getLevel(), target.getBlockPos(), target.getBlockState(), target);
        require(total(source.inventory()) == 0 && total(target.inventory()) == 16,
                "The connected import could not extract from the passive buffer capability");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void buffersUseOriginalTierInventoriesAndNormalInventoryLayout(GameTestHelper helper) {
        int[][] sizes = {{2, 1}, {3, 2}, {4, 3}, {5, 4}, {9, 4}, {9, 6}, {9, 6}, {9, 6}, {9, 6}, {9, 6}};
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        for (int tier = 4; tier <= 13; tier++) {
            BufferBlockEntity buffer = placedBuffer(helper, tier, new BlockPos(1, 1, 1));
            int columns = sizes[tier - 4][0];
            int rows = sizes[tier - 4][1];
            require(buffer.inventory().getSlots() == columns * rows,
                    "Tier " + tier + " buffer does not keep the original " + columns + "x" + rows + " inventory");
            BufferMenu menu = new BufferMenu(1, player.getInventory(), buffer);
            require(menu.layout().machineHeight() == rows * 18 + 18,
                    "Tier " + tier + " buffer GUI height differs from ContainerNormalInventory");
            require(menu.slots.get(0).x == (176 - 18 * columns) / 2 + 1 && menu.slots.get(0).y == 18,
                    "Tier " + tier + " buffer slots are not centred like ContainerNormalInventory");
            require(menu.slots.size() == columns * rows + 36,
                    "Tier " + tier + " buffer GUI exposes extra slots");
        }
        BufferBlockEntity creative = (BufferBlockEntity)placedCreative(helper, new BlockPos(3, 1, 1)).entity();
        require(creative.inventory().getSlots() == 1, "The creative energy source does not keep its 1x1 inventory");
        helper.succeed();
    }

    public static void registerTestCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> TEST_HANDLERS.get(pos), Blocks.BARRIER);
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void customCapabilityTransfersThroughNeighborCache(GameTestHelper helper) {
        BufferBlockEntity sending = placedBuffer(helper, 7, new BlockPos(1, 1, 1));
        setOnlyFace(sending, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        sending.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 10));
        BlockPos outputPos = helper.absolutePos(new BlockPos(2, 1, 1));
        helper.getLevel().setBlockAndUpdate(outputPos, Blocks.BARRIER.defaultBlockState());
        LimitedHandler output = new LimitedHandler(3);
        TEST_HANDLERS.put(outputPos, output);
        helper.getLevel().invalidateCapabilities(outputPos);
        BufferBlockEntity.serverTick(helper.getLevel(), sending.getBlockPos(), sending.getBlockState(), sending);
        require(output.getStackInSlot(0).getCount() == 3 && total(sending.inventory()) == 7,
                "A custom block item capability did not respect its slot limit");

        BufferBlockEntity receiving = placedBuffer(helper, 7, new BlockPos(4, 1, 1));
        setOnlyFace(receiving, Direction.EAST, BufferBlockEntity.FaceMode.INPUT);
        BlockPos inputPos = helper.absolutePos(new BlockPos(5, 1, 1));
        helper.getLevel().setBlockAndUpdate(inputPos, Blocks.BARRIER.defaultBlockState());
        ItemStackHandler input = new ItemStackHandler(1);
        input.setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 12));
        TEST_HANDLERS.put(inputPos, input);
        helper.getLevel().invalidateCapabilities(inputPos);
        BufferBlockEntity.serverTick(helper.getLevel(), receiving.getBlockPos(), receiving.getBlockState(), receiving);
        require(total(receiving.inventory()) == 12 && total(input) == 0,
                "A custom block item capability did not supply the input face");
        TEST_HANDLERS.remove(outputPos);
        TEST_HANDLERS.remove(inputPos);
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void tenBufferTiersTransferExactAmountsOverOneHundredTicks(GameTestHelper helper) {
        // Tier 13 sends at most its whole 9x6 inventory (54 stacks) per transfer.
        long[] expected = {12L, 100L, 800L, 6_400L, 12_800L, 19_200L, 25_600L, 51_200L, 102_400L, 345_600L};
        for (int tier = 4; tier <= 13; tier++) {
            BufferBlockEntity source = placedBuffer(helper, tier, new BlockPos(1, 1, 1));
            BufferBlockEntity target = placedBuffer(helper, tier, new BlockPos(2, 1, 1));
            setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
            setOnlyFace(target, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
            long transferred = 0L;
            for (int tick = 0; tick < 100; tick++) {
                fill(source.inventory(), Items.COBBLESTONE);
                BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
                transferred += countAndClear(target.inventory());
            }
            require(transferred == expected[tier - 4],
                    "Tier " + tier + " moved " + transferred + " items instead of " + expected[tier - 4]);
        }

        ItemStackHandler genericSource = new ItemStackHandler(1);
        genericSource.setStackInSlot(0, new ItemStack(Items.DIAMOND, 10));
        LimitedHandler otherModHandler = new LimitedHandler(3);
        int moved = BufferBlockEntity.moveItems(genericSource, otherModHandler, stack -> true, 10, stack -> { throw new GameTestAssertException("Items were not restored"); });
        require(moved == 3 && genericSource.getStackInSlot(0).getCount() == 7
                        && otherModHandler.getStackInSlot(0).getCount() == 3,
                "A standard IItemHandler slot limit was ignored or changed the item total");

        helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 1, 1)), Blocks.CHEST.defaultBlockState());
        BufferBlockEntity chestSource = placedBuffer(helper, 4, new BlockPos(1, 1, 1));
        setOnlyFace(chestSource, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        chestSource.inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 8));
        for (int tick = 0; tick < 8; tick++) {
            BufferBlockEntity.serverTick(helper.getLevel(), chestSource.getBlockPos(), chestSource.getBlockState(), chestSource);
        }
        ChestBlockEntity chest = (ChestBlockEntity)helper.getLevel().getBlockEntity(
                helper.absolutePos(new BlockPos(2, 1, 1)));
        require(chest != null && chest.getItem(0).is(Items.CLAY_BALL) && chest.getItem(0).getCount() == 1,
                "The buffer did not transfer through the vanilla chest item capability");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void changingHandlersReturnRejectedItemsToRecovery(GameTestHelper helper) {
        IItemHandler source = new ItemStackHandler(1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }
        };
        ((ItemStackHandler)source).setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
        IItemHandler changingTarget = new ItemStackHandler(1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return simulate ? ItemStack.EMPTY : stack;
            }
        };
        java.util.List<ItemStack> recovered = new java.util.ArrayList<>();
        int moved = BufferBlockEntity.moveItems(source, changingTarget, stack -> true, 8, recovered::add);
        require(moved == 0 && total(source) == 0 && total(changingTarget) == 0
                        && recovered.size() == 1 && recovered.getFirst().getCount() == 8,
                "A changing item handler lost rejected items after simulation");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void allTiersUseChestCapabilitiesAndConserveItems(GameTestHelper helper) {
        int[] perCycle = {1, 4, 16, 64, 128, 192, 256, 512, 1024, 6400};
        int[] intervals = {8, 4, 2, 1, 1, 1, 1, 1, 1, 1};
        BlockPos center = new BlockPos(3, 1, 3);
        for (int tier = 4; tier <= 13; tier++) {
            BufferBlockEntity buffer = placedBuffer(helper, tier, center);
            for (Direction direction : Direction.values()) {
                buffer.setFaceMode(direction, direction.getAxis().isHorizontal()
                        ? BufferBlockEntity.FaceMode.OUTPUT : BufferBlockEntity.FaceMode.DISABLED);
                if (direction.getAxis().isHorizontal()) {
                    helper.getLevel().setBlockAndUpdate(helper.absolutePos(center.relative(direction)),
                            Blocks.CHEST.defaultBlockState());
                }
            }
            long totalMoved = 0;
            for (int tick = 0; tick < 100; tick++) {
                fill(buffer.inventory(), Items.COBBLESTONE);
                BufferBlockEntity.serverTick(helper.getLevel(), buffer.getBlockPos(), buffer.getBlockState(), buffer);
                int inChests = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    ChestBlockEntity chest = (ChestBlockEntity)helper.getLevel().getBlockEntity(
                            helper.absolutePos(center.relative(direction)));
                    require(chest != null, "A chest was not available to its buffer");
                    for (int slot = 0; slot < chest.getContainerSize(); slot++) {
                        inChests += chest.getItem(slot).getCount();
                        chest.setItem(slot, ItemStack.EMPTY);
                    }
                }
                require(total(buffer.inventory()) + inChests == buffer.inventory().getSlots() * 64,
                        "Tier " + tier + " changed the item total during a chest capability transfer");
                totalMoved += inChests;
            }
            require(totalMoved == (100 / intervals[tier - 4])
                            * (long)Math.min(perCycle[tier - 4], buffer.inventory().getSlots() * 64),
                    "Tier " + tier + " did not obey the original chest transfer rate");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void bufferDirectionsAndSimulationPreserveItemTotals(GameTestHelper helper) {
        BufferBlockEntity source = placedBuffer(helper, 7, new BlockPos(1, 1, 1));
        BufferBlockEntity target = placedBuffer(helper, 7, new BlockPos(2, 1, 1));
        setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        setOnlyFace(target, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        source.inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(total(source.inventory()) + total(target.inventory()) == 64,
                "Transfer simulation duplicated or lost an item");
        require(total(target.inventory()) == 64, "The configured output direction did not transfer");

        source.inventory().setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 16));
        source.setFaceMode(Direction.EAST, BufferBlockEntity.FaceMode.DISABLED);
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(source.inventory().getStackInSlot(0).getCount() == 16, "A disabled face sent items");

        BufferBlockEntity pulling = placedBuffer(helper, 7, new BlockPos(4, 1, 1));
        setOnlyFace(pulling, Direction.EAST, BufferBlockEntity.FaceMode.INPUT);
        BlockPos inputChestPos = helper.absolutePos(new BlockPos(5, 1, 1));
        helper.getLevel().setBlockAndUpdate(inputChestPos, Blocks.CHEST.defaultBlockState());
        ChestBlockEntity inputChest = (ChestBlockEntity)helper.getLevel().getBlockEntity(inputChestPos);
        require(inputChest != null, "The input chest was not placed");
        inputChest.setItem(0, new ItemStack(Items.CLAY_BALL, 32));
        BufferBlockEntity.serverTick(helper.getLevel(), pulling.getBlockPos(), pulling.getBlockState(), pulling);
        require(total(pulling.inventory()) == 32 && inputChest.getItem(0).isEmpty(),
                "The buffer did not pull from an input face through the chest capability");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void itemFilterMatchesRememberedItemsModesAndNesting(GameTestHelper helper) {
        ItemStack stone = new ItemStack(Items.STONE);
        ItemStack namedStone = stone.copy();
        namedStone.set(DataComponents.CUSTOM_NAME, Component.literal("Selected stone"));
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(7);
        ItemFilterSettings whitelist = ItemFilterSettings.EMPTY.withItem(0, namedStone).withItem(4, pickaxe);
        require(whitelist.items().size() == ItemFilterSettings.SIZE && whitelist.item(0).getCount() == 1,
                "The filter does not keep ten one-item memories");
        require(whitelist.accepts(stone) && whitelist.accepts(new ItemStack(Items.IRON_PICKAXE)),
                "ItemFilterTemp.matchBetweenItemstacks: the item type alone must match");
        require(!whitelist.accepts(new ItemStack(Items.DIAMOND)), "The whitelist accepted an unlisted item");
        require(!ItemFilterSettings.EMPTY.accepts(stone), "An empty whitelist accepted an item");
        ItemFilterSettings blacklist = whitelist.withMode(ItemFilterSettings.Mode.BLACKLIST);
        require(!blacklist.accepts(stone) && blacklist.accepts(new ItemStack(Items.DIAMOND)),
                "ItemFilterBlacklist did not invert the whitelist result");

        ItemStack nestedFilter = LogisticsRegistration.ITEM_FILTER.toStack();
        nestedFilter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(),
                ItemFilterSettings.EMPTY.withItem(0, new ItemStack(Items.DIAMOND)));
        ItemFilterSettings outer = ItemFilterSettings.EMPTY.withItem(0, nestedFilter);
        require(outer.accepts(new ItemStack(Items.DIAMOND)) && !outer.accepts(stone),
                "A remembered filter was not evaluated recursively");
        require(net.claustra01.clayium.logistics.filter.ItemFilter.accepts(ItemStack.EMPTY, stone),
                "An empty filter slot must accept every item");
        require(net.claustra01.clayium.logistics.filter.ItemFilter.matchesMemory(new ItemStack(Items.STONE), stone)
                        && !net.claustra01.clayium.logistics.filter.ItemFilter.matchesMemory(
                                new ItemStack(Items.STONE), new ItemStack(Items.DIAMOND)),
                "A remembered non-filter item must match only its own item type");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void filterGuiFacesAndConfigurationCopySurviveSave(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        ItemStack filter = LogisticsRegistration.ITEM_FILTER.toStack();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, filter);
        var menu = new net.claustra01.clayium.logistics.filter.FilterMenu(1, player.getInventory());
        player.containerMenu = menu;
        require(menu.slots.size() == ItemFilterSettings.SIZE + 36
                        && menu.slots.get(0).x == 43 && menu.slots.get(0).y == 18
                        && menu.slots.get(9).x == 115 && menu.slots.get(9).y == 36,
                "The filter GUI does not keep ContainerItemFilterWhiteList's 5x2 memory slots");
        menu.setCarried(new ItemStack(Items.DIAMOND, 12));
        menu.clicked(3, 0, net.minecraft.world.inventory.ClickType.PICKUP, player);
        require(menu.getCarried().getCount() == 12, "A memory slot consumed the carried stack");
        require(menu.clickMenuButton(player, net.claustra01.clayium.logistics.filter.FilterMenu.BUTTON_TOGGLE_MODE),
                "The whitelist/blacklist button was rejected");
        ItemFilterSettings settings = player.getMainHandItem().get(LogisticsRegistration.ITEM_FILTER_SETTINGS.get());
        require(settings != null && settings.item(3).is(Items.DIAMOND) && settings.item(3).getCount() == 1
                        && settings.mode() == ItemFilterSettings.Mode.BLACKLIST,
                "The filter GUI did not store the remembered item and mode on the held filter");
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        require(!menu.stillValid(player), "The filter GUI stayed open without the held filter");

        BufferBlockEntity source = placedBuffer(helper, 9, new BlockPos(1, 1, 1));
        source.setFaceMode(Direction.NORTH, BufferBlockEntity.FaceMode.OUTPUT);
        CompoundTag saved = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        BufferBlockEntity loaded = new BufferBlockEntity(source.getBlockPos(), source.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.faceMode(Direction.NORTH) == BufferBlockEntity.FaceMode.OUTPUT,
                "Buffer faces changed during block entity saving");

        ItemStack memoryCard = WorkbenchRegistration.DIRECTION_MEMORY_CARD.toStack();
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, memoryCard);
        player.setShiftKeyDown(true);
        useDirectionCard(player, source.getBlockPos(), Direction.NORTH, "copy");
        player.setShiftKeyDown(false);
        require(memoryCard.has(LogisticsRegistration.BUFFER_CONFIGURATION.get()),
                "The direction memory card did not copy from a buffer through item interaction");
        BufferBlockEntity destination = placedBuffer(helper, 13, new BlockPos(3, 1, 1));
        useDirectionCard(player, destination.getBlockPos(), Direction.NORTH, "paste");
        require(destination.faceMode(Direction.NORTH) == BufferBlockEntity.FaceMode.OUTPUT,
                "The memory card did not copy the buffer faces");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void faceFiltersControlChestTransfersAfterSave(GameTestHelper helper) {
        BufferBlockEntity source = placedBuffer(helper, 7, new BlockPos(1, 1, 1));
        BlockPos chestPos = helper.absolutePos(new BlockPos(2, 1, 1));
        helper.getLevel().setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        ItemStack filter = LogisticsRegistration.ITEM_FILTER.toStack();
        filter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(),
                ItemFilterSettings.EMPTY.withItem(0, new ItemStack(Items.DIAMOND)));
        source.setFilter(Direction.EAST, filter);
        source.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
        source.inventory().setStackInSlot(1, new ItemStack(Items.COBBLESTONE, 8));
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        ChestBlockEntity chest = (ChestBlockEntity)helper.getLevel().getBlockEntity(chestPos);
        require(chest != null && chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 8,
                "The face whitelist did not transfer only diamonds");
        require(source.inventory().getStackInSlot(1).getCount() == 8, "The face whitelist moved a rejected item");

        CompoundTag saved = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        BufferBlockEntity loaded = new BufferBlockEntity(source.getBlockPos(), source.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(ItemStack.matches(loaded.filter(Direction.EAST), source.filter(Direction.EAST)),
                "The face filter was lost on reload");

        ItemStack blacklist = filter.copy();
        blacklist.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(),
                ItemFilterSettings.EMPTY.withItem(0, new ItemStack(Items.DIAMOND))
                        .withMode(ItemFilterSettings.Mode.BLACKLIST));
        source.setFilter(Direction.EAST, blacklist);
        chest.clearContent();
        source.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(chest.getItem(0).is(Items.COBBLESTONE) && source.inventory().getStackInSlot(0).getCount() == 8,
                "The face blacklist did not reverse transfer selection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void filterSettingsAndConfigurationValuesRemainSafe(GameTestHelper helper) {
        ItemStack example = new ItemStack(Items.IRON_PICKAXE);
        example.setDamageValue(7);
        ItemFilterSettings first = ItemFilterSettings.EMPTY.withItem(2, example);
        ItemFilterSettings second = ItemFilterSettings.EMPTY.withItem(2, example.copy());
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "Equal filter settings have different values");
        var firstConfiguration = new net.claustra01.clayium.logistics.BufferConfiguration(List.of(0, 1, 2, 3, 0, 1));
        var secondConfiguration = new net.claustra01.clayium.logistics.BufferConfiguration(List.of(0, 1, 2, 3, 0, 1));
        require(firstConfiguration.equals(secondConfiguration)
                        && firstConfiguration.hashCode() == secondConfiguration.hashCode(),
                "Equal copied configurations have different values");
        var encoded = ItemFilterSettings.CODEC.encodeStart(
                helper.getLevel().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),
                first).getOrThrow();
        var decoded = ItemFilterSettings.CODEC.parse(
                helper.getLevel().registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),
                encoded).getOrThrow();
        require(decoded.equals(first), "The filter settings codec did not round-trip");
        require(ItemFilterSettings.Mode.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
                        new com.google.gson.JsonPrimitive("unknown")).error().isPresent(),
                "An unknown filter mode did not return a codec error");
        helper.succeed();
    }

    private static BlockEntityWrapper placedCreative(GameTestHelper helper, BlockPos relativePos) {
        BlockPos pos = helper.absolutePos(relativePos);
        helper.getLevel().setBlockAndUpdate(pos,
                net.claustra01.clayium.registry.CreativeSourceRegistration.CREATIVE_ENERGY_SOURCE.get().defaultBlockState());
        return new BlockEntityWrapper(helper.getLevel().getBlockEntity(pos));
    }

    private static BufferBlockEntity placedBuffer(GameTestHelper helper, int tier, BlockPos relativePos) {
        var definition = LogisticsRegistration.BUFFERS.get(tier);
        BlockPos pos = helper.absolutePos(relativePos);
        helper.getLevel().setBlockAndUpdate(pos, definition.block().get().defaultBlockState());
        BufferBlockEntity buffer = (BufferBlockEntity)helper.getLevel().getBlockEntity(pos);
        require(buffer != null, "Tier " + tier + " buffer did not create its block entity");
        return buffer;
    }

    private static void setOnlyFace(
            BufferBlockEntity buffer, Direction activeDirection, BufferBlockEntity.FaceMode mode) {
        for (Direction direction : Direction.values()) {
            buffer.setFaceMode(direction, direction == activeDirection ? mode : BufferBlockEntity.FaceMode.DISABLED);
        }
    }

    private static void fill(ItemStackHandler handler, net.minecraft.world.level.ItemLike item) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            handler.setStackInSlot(slot, new ItemStack(item, 64));
        }
    }

    private static int countAndClear(ItemStackHandler handler) {
        int count = total(handler);
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            handler.setStackInSlot(slot, ItemStack.EMPTY);
        }
        return count;
    }

    private static int total(IItemHandler handler) {
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            total += handler.getStackInSlot(slot).getCount();
        }
        return total;
    }

    private static void useDirectionCard(
            net.minecraft.world.entity.player.Player player, BlockPos position, Direction side, String action) {
        var hit = new net.minecraft.world.phys.BlockHitResult(
                net.minecraft.world.phys.Vec3.atCenterOf(position), side, position, false);
        var context = new net.minecraft.world.item.context.UseOnContext(
                player, net.minecraft.world.InteractionHand.MAIN_HAND, hit);
        require(player.getMainHandItem().onItemUseFirst(context).consumesAction(),
                "The direction memory card did not consume its buffer " + action + " interaction");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private static final class LimitedHandler implements IItemHandler {
        private final int limit;
        private ItemStack stack = ItemStack.EMPTY;

        private LimitedHandler(int limit) {
            this.limit = limit;
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return stack;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack inserted, boolean simulate) {
            int room = limit - stack.getCount();
            int accepted = Math.min(room, inserted.getCount());
            if (!simulate && accepted > 0) {
                stack = inserted.copyWithCount(accepted);
            }
            return accepted == inserted.getCount()
                    ? ItemStack.EMPTY : inserted.copyWithCount(inserted.getCount() - accepted);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return limit;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }
    }

    private record BlockEntityWrapper(net.minecraft.world.level.block.entity.BlockEntity entity) {
    }

    private LogisticsGameTests() {
    }
}
