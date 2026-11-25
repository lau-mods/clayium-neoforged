package net.claustra01.clayium.gametest.logistics;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.tool.BufferConfigurationToolItem;
import net.claustra01.clayium.logistics.BufferControlPayload;
import net.claustra01.clayium.logistics.BufferMenu;
import net.claustra01.clayium.logistics.filter.ItemFilterRule;
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
    public static void bufferPagesExposeAllOneHundredStorageSlots(GameTestHelper helper) {
        BufferBlockEntity buffer = placedBuffer(helper, 9, new BlockPos(1, 1, 1));
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        BufferMenu menu = new BufferMenu(1, player.getInventory(), buffer, new SimpleContainerData(12));
        for (int page = 0; page < 4; page++) {
            require(menu.clickMenuButton(player, page), "A storage page could not be selected");
            for (int index = 0; index < BufferMenu.STORAGE_SLOTS; index++) {
                require(menu.slots.get(index).isActive() == (index / BufferMenu.PAGE_SIZE == page),
                        "Page " + page + " exposed the wrong storage slot " + index);
            }
        }
        require(!menu.clickMenuButton(player, 4) && menu.page() == 3,
                "An invalid storage page changed the menu");
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
        long[] expected = {12L, 100L, 800L, 6_400L, 12_800L, 19_200L, 25_600L, 51_200L, 102_400L, 640_000L};
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

        BufferBlockEntity limited = placedBuffer(helper, 13, new BlockPos(1, 1, 1));
        BufferBlockEntity limitedTarget = placedBuffer(helper, 13, new BlockPos(2, 1, 1));
        setOnlyFace(limited, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        setOnlyFace(limitedTarget, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        limited.setTransferLimit(37);
        long limitedTransferred = 0L;
        for (int tick = 0; tick < 100; tick++) {
            fill(limited.inventory(), Items.COBBLESTONE);
            BufferBlockEntity.serverTick(helper.getLevel(), limited.getBlockPos(), limited.getBlockState(), limited);
            limitedTransferred += countAndClear(limitedTarget.inventory());
        }
        require(limitedTransferred == 3_700L, "The configured quantity limit was not applied per transfer");

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
                require(total(buffer.inventory()) + inChests == 6400,
                        "Tier " + tier + " changed the item total during a chest capability transfer");
                totalMoved += inChests;
            }
            require(totalMoved == (100 / intervals[tier - 4]) * perCycle[tier - 4],
                    "Tier " + tier + " did not obey the original chest transfer rate");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void bufferDirectionsRedstoneAndSimulationPreserveItemTotals(GameTestHelper helper) {
        BufferBlockEntity source = placedBuffer(helper, 7, new BlockPos(1, 1, 1));
        BufferBlockEntity target = placedBuffer(helper, 7, new BlockPos(2, 1, 1));
        setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        setOnlyFace(target, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        source.setRedstoneCondition(BufferBlockEntity.RedstoneCondition.POWERED);
        source.inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));

        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(total(source.inventory()) == 64 && total(target.inventory()) == 0,
                "A powered-only buffer transferred without a redstone signal");
        helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 1, 2)), Blocks.REDSTONE_BLOCK.defaultBlockState());
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(total(source.inventory()) + total(target.inventory()) == 64,
                "Transfer simulation duplicated or lost an item");
        require(total(target.inventory()) == 64, "The configured output direction did not transfer");

        source.inventory().setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 16));
        source.setFaceMode(Direction.EAST, BufferBlockEntity.FaceMode.DISABLED);
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(source.inventory().getStackInSlot(0).getCount() == 16,
                "A disabled face sent items");
        source.setFaceMode(Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        source.setRedstoneCondition(BufferBlockEntity.RedstoneCondition.UNPOWERED);
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(source.inventory().getStackInSlot(0).getCount() == 16,
                "An unpowered-only buffer transferred while powered");
        helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(1, 1, 2)), Blocks.AIR.defaultBlockState());
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        require(source.inventory().getStackInSlot(0).isEmpty(),
                "An unpowered-only buffer did not resume transfer");

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
    public static void unifiedFilterMatchesModesTagsIdsNamesModsDamageComponentsAndNesting(GameTestHelper helper) {
        ItemStack oak = new ItemStack(Items.OAK_PLANKS);
        ItemStack stone = new ItemStack(Items.STONE);
        ItemStack namedStone = stone.copy();
        namedStone.set(DataComponents.CUSTOM_NAME, Component.literal("Selected stone"));
        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.setDamageValue(7);

        ItemFilterRule tag = ItemFilterRule.fromExample(ItemFilterRule.MatchType.TAG, oak);
        require(new ItemFilterSettings(ItemFilterSettings.Mode.WHITELIST, List.of(tag)).accepts(oak),
                "The item tag condition did not match");
        require(!new ItemFilterSettings(ItemFilterSettings.Mode.WHITELIST, List.of(tag)).accepts(stone),
                "The item tag condition matched an unrelated item");
        checkRule(ItemFilterRule.MatchType.INTERNAL_ID, stone, stone, oak, "internal ID");
        checkRule(ItemFilterRule.MatchType.DISPLAY_NAME, namedStone, namedStone, stone, "display name");
        checkRule(ItemFilterRule.MatchType.MOD_ID, stone, oak, LogisticsRegistration.ITEM_FILTER.toStack(), "mod ID");
        checkRule(ItemFilterRule.MatchType.DURABILITY, pickaxe, pickaxe, new ItemStack(Items.IRON_PICKAXE), "durability");
        checkRule(ItemFilterRule.MatchType.COMPONENTS, namedStone, namedStone, stone, "data components");

        ItemStack differentlyDamaged = pickaxe.copy();
        differentlyDamaged.setDamageValue(11);
        checkRule(ItemFilterRule.MatchType.FUZZY, pickaxe, differentlyDamaged, stone, "fuzzy item");

        ItemStack nestedFilter = LogisticsRegistration.ITEM_FILTER.toStack();
        nestedFilter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), new ItemFilterSettings(
                ItemFilterSettings.Mode.WHITELIST,
                List.of(ItemFilterRule.fromExample(ItemFilterRule.MatchType.INTERNAL_ID, new ItemStack(Items.DIAMOND)))));
        ItemFilterRule nested = ItemFilterRule.fromExample(ItemFilterRule.MatchType.NESTED, nestedFilter);
        ItemFilterSettings outer = new ItemFilterSettings(ItemFilterSettings.Mode.WHITELIST, List.of(nested));
        require(outer.accepts(new ItemStack(Items.DIAMOND)) && !outer.accepts(stone),
                "A nested filter did not preserve its inner allow list");

        ItemFilterSettings blacklist = new ItemFilterSettings(
                ItemFilterSettings.Mode.BLACKLIST,
                List.of(ItemFilterRule.fromExample(ItemFilterRule.MatchType.INTERNAL_ID, stone)));
        require(!blacklist.accepts(stone) && blacklist.accepts(oak),
                "The shared black/white evaluator did not invert the matched condition");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void filterSettingsSurviveSaveGuiSwitchAndConfigurationCopy(GameTestHelper helper) {
        BufferBlockEntity source = placedBuffer(helper, 9, new BlockPos(1, 1, 1));
        ItemStack filter = LogisticsRegistration.ITEM_FILTER.toStack();
        ItemFilterSettings settings = new ItemFilterSettings(
                ItemFilterSettings.Mode.WHITELIST,
                List.of(
                        ItemFilterRule.fromExample(ItemFilterRule.MatchType.INTERNAL_ID, new ItemStack(Items.DIAMOND)),
                        ItemFilterRule.fromExample(ItemFilterRule.MatchType.TAG, new ItemStack(Items.OAK_PLANKS))));
        filter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), settings);
        source.setFilterStack(filter);
        source.setFaceMode(Direction.NORTH, BufferBlockEntity.FaceMode.OUTPUT);
        source.setRedstoneCondition(BufferBlockEntity.RedstoneCondition.UNPOWERED);
        source.setTransferLimit(111);

        CompoundTag saved = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        BufferBlockEntity loaded = new BufferBlockEntity(source.getBlockPos(), source.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.filterSettings().equals(settings), "The filter component changed during block entity saving");
        require(loaded.faceMode(Direction.NORTH) == BufferBlockEntity.FaceMode.OUTPUT
                        && loaded.redstoneCondition() == BufferBlockEntity.RedstoneCondition.UNPOWERED
                        && loaded.transferLimit() == 111,
                "Buffer settings changed during block entity saving");

        ItemStack memoryCard = WorkbenchRegistration.DIRECTION_MEMORY_CARD.toStack();
        BufferConfigurationToolItem.copyFrom(memoryCard, loaded);
        BufferBlockEntity destination = placedBuffer(helper, 13, new BlockPos(3, 1, 1));
        require(BufferConfigurationToolItem.pasteTo(memoryCard, destination),
                "The memory card rejected a stored buffer configuration");
        require(destination.filterSettings().equals(settings)
                        && destination.faceMode(Direction.NORTH) == BufferBlockEntity.FaceMode.OUTPUT
                        && destination.redstoneCondition() == BufferBlockEntity.RedstoneCondition.UNPOWERED
                        && destination.transferLimit() == 111,
                "Configuration copy omitted a filter, face, redstone condition, or quantity limit");

        var interactionPlayer = helper.makeMockPlayer(GameType.CREATIVE);
        interactionPlayer.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, memoryCard);
        interactionPlayer.setShiftKeyDown(true);
        helper.useBlock(new BlockPos(1, 1, 1), interactionPlayer);
        require(memoryCard.has(LogisticsRegistration.BUFFER_CONFIGURATION.get()),
                "Sneak-use did not copy through the block interaction path");
        interactionPlayer.setShiftKeyDown(false);
        helper.useBlock(new BlockPos(3, 1, 1), interactionPlayer);
        require(destination.filterSettings().equals(settings),
                "Use did not paste through the block interaction path");

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        player.setPos(destination.getBlockPos().getX() + 0.5D, destination.getBlockPos().getY() + 0.5D,
                destination.getBlockPos().getZ() + 0.5D);
        BufferMenu menu = new BufferMenu(1, player.getInventory(), destination, new SimpleContainerData(12));
        player.containerMenu = menu;
        BufferControlPayload.apply(player, new BufferControlPayload(
                destination.getBlockPos(), BufferControlPayload.FILTER_MODE, 0));
        require(destination.filterSettings().mode() == ItemFilterSettings.Mode.BLACKLIST,
                "The integrated GUI action did not switch allow/deny mode");
        player.setPos(destination.getBlockPos().getX() + 9.0D, destination.getBlockPos().getY(), destination.getBlockPos().getZ());
        BufferControlPayload.apply(player, new BufferControlPayload(
                destination.getBlockPos(), BufferControlPayload.FILTER_MODE, 0));
        require(destination.filterSettings().mode() == ItemFilterSettings.Mode.BLACKLIST,
                "A remote GUI payload changed the filter mode");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void installedFiltersControlChestTransfersAfterSaveAndPaste(GameTestHelper helper) {
        BufferBlockEntity source = placedBuffer(helper, 7, new BlockPos(1, 1, 1));
        BlockPos chestPos = helper.absolutePos(new BlockPos(2, 1, 1));
        helper.getLevel().setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        setOnlyFace(source, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        ItemStack filter = LogisticsRegistration.ITEM_FILTER.toStack();
        filter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), new ItemFilterSettings(
                ItemFilterSettings.Mode.WHITELIST,
                List.of(ItemFilterRule.fromExample(ItemFilterRule.MatchType.INTERNAL_ID, new ItemStack(Items.DIAMOND)))));
        source.setFilterStack(filter);
        source.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
        source.inventory().setStackInSlot(1, new ItemStack(Items.COBBLESTONE, 8));
        BufferBlockEntity.serverTick(helper.getLevel(), source.getBlockPos(), source.getBlockState(), source);
        ChestBlockEntity chest = (ChestBlockEntity)helper.getLevel().getBlockEntity(chestPos);
        require(chest != null && chest.getItem(0).is(Items.DIAMOND) && chest.getItem(0).getCount() == 8,
                "The allow list did not transfer only diamonds");
        require(source.inventory().getStackInSlot(1).getCount() == 8, "The allow list consumed a rejected item");

        CompoundTag saved = source.saveWithFullMetadata(helper.getLevel().registryAccess());
        BufferBlockEntity loaded = new BufferBlockEntity(source.getBlockPos(), source.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.filterSettings().equals(source.filterSettings()), "The allow list was lost on reload");
        ItemStack card = WorkbenchRegistration.DIRECTION_MEMORY_CARD.toStack();
        BufferConfigurationToolItem.copyFrom(card, loaded);
        BufferBlockEntity pasted = placedBuffer(helper, 7, new BlockPos(1, 1, 3));
        helper.getLevel().setBlockAndUpdate(helper.absolutePos(new BlockPos(2, 1, 3)), Blocks.CHEST.defaultBlockState());
        BufferConfigurationToolItem.pasteTo(card, pasted);
        pasted.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
        pasted.inventory().setStackInSlot(1, new ItemStack(Items.COBBLESTONE, 8));
        BufferBlockEntity.serverTick(helper.getLevel(), pasted.getBlockPos(), pasted.getBlockState(), pasted);
        ChestBlockEntity pastedChest = (ChestBlockEntity)helper.getLevel().getBlockEntity(
                helper.absolutePos(new BlockPos(2, 1, 3)));
        require(pastedChest != null && pastedChest.getItem(0).is(Items.DIAMOND)
                        && pasted.inventory().getStackInSlot(1).getCount() == 8,
                "The copied allow list did not control transfer");
        pasted.cycleFilterMode();
        pastedChest.clearContent();
        pasted.inventory().setStackInSlot(0, new ItemStack(Items.DIAMOND, 8));
        BufferBlockEntity.serverTick(helper.getLevel(), pasted.getBlockPos(), pasted.getBlockState(), pasted);
        require(pastedChest.getItem(0).is(Items.COBBLESTONE)
                        && pasted.inventory().getStackInSlot(0).getCount() == 8,
                "The deny list did not reverse transfer selection");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void filterRuleValuesAndChangingHandlerRemainSafe(GameTestHelper helper) {
        ItemStack example = new ItemStack(Items.IRON_PICKAXE);
        example.setDamageValue(7);
        ItemFilterRule first = ItemFilterRule.fromExample(ItemFilterRule.MatchType.COMPONENTS, example);
        ItemFilterRule second = ItemFilterRule.fromExample(ItemFilterRule.MatchType.COMPONENTS, example.copy());
        require(first.equals(second) && first.hashCode() == second.hashCode(),
                "Equal component rules have different values");
        ItemStack copiedFilter = LogisticsRegistration.ITEM_FILTER.toStack();
        var firstConfiguration = new net.claustra01.clayium.logistics.BufferConfiguration(
                List.of(0, 0, 0, 0, 0, 0), BufferBlockEntity.RedstoneCondition.IGNORE, 8, copiedFilter);
        var secondConfiguration = new net.claustra01.clayium.logistics.BufferConfiguration(
                List.of(0, 0, 0, 0, 0, 0), BufferBlockEntity.RedstoneCondition.IGNORE, 8, copiedFilter.copy());
        require(firstConfiguration.equals(secondConfiguration)
                        && firstConfiguration.hashCode() == secondConfiguration.hashCode(),
                "Equal copied configurations have different values");
        BufferBlockEntity buffer = placedBuffer(helper, 4, new BlockPos(1, 1, 1));
        buffer.exampleInventory().setStackInSlot(0, new ItemStack(Items.DIAMOND));
        buffer.setFilterStack(LogisticsRegistration.ITEM_FILTER.toStack());
        buffer.addSelectedRule();
        require(buffer.filterSettings().rules().getFirst().reference().is(Items.DIAMOND),
                "The GUI example did not supply the rule");
        require(ItemFilterRule.MatchType.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE,
                        new com.google.gson.JsonPrimitive("unknown")).error().isPresent(),
                "An unknown rule type did not return a codec error");
        helper.succeed();
    }

    private static void checkRule(
            ItemFilterRule.MatchType type,
            ItemStack example,
            ItemStack accepted,
            ItemStack rejected,
            String label) {
        ItemFilterSettings settings = new ItemFilterSettings(
                ItemFilterSettings.Mode.WHITELIST, List.of(ItemFilterRule.fromExample(type, example)));
        require(settings.accepts(accepted), "The " + label + " condition did not match");
        require(!settings.accepts(rejected), "The " + label + " condition matched an unrelated item");
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

    private LogisticsGameTests() {
    }
}
