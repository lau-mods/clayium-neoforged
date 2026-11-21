package net.claustra01.clayium.gametest.area;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.logistics.filter.ItemFilterRule;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
import net.claustra01.clayium.registry.AreaMachineRegistration;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.registry.WorldgenRegistration;
import net.claustra01.clayium.world.area.AreaBounds;
import net.claustra01.clayium.world.area.AreaMachineBlock;
import net.claustra01.clayium.world.area.AreaMachineBlockEntity;
import net.claustra01.clayium.world.area.AreaMachineMenu;
import net.claustra01.clayium.world.area.AreaMachineType;
import net.claustra01.clayium.world.area.AreaMarkerBlockEntity;
import net.claustra01.clayium.world.area.AreaMarkerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AreaMachineGameTests {
    private static final UUID OWNER = UUID.fromString("6bf4e40d-a817-4f17-aea4-b89c37d52d70");

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void markerHeightExpansionUsesWorldBoundsAndNeverScansOutside(GameTestHelper helper) {
        int minimumY = helper.getLevel().getMinBuildHeight();
        int maximumY = helper.getLevel().getMaxBuildHeight() - 1;
        require(minimumY == -64 && maximumY == 319,
                "The standard overworld did not expose the expected -64 through 319 build range");

        int baseX = helper.absolutePos(new BlockPos(2, 2, 2)).getX();
        int baseZ = helper.absolutePos(new BlockPos(2, 2, 2)).getZ();
        int[] heights = {minimumY, 0, 255, maximumY};
        AreaMarkerType[] types = AreaMarkerType.values();
        AreaBounds[] selections = new AreaBounds[types.length];
        try {
            for (int index = 0; index < types.length; index++) {
                BlockPos markerPosition = new BlockPos(baseX + index * 2, heights[index], baseZ);
                BlockPos other = new BlockPos(baseX + index * 2 + 1, heights[index], baseZ);
                var markerBlock = AreaMachineRegistration.MARKERS.get(types[index]).block().get();
                helper.getLevel().setBlockAndUpdate(markerPosition, markerBlock.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(other, markerBlock.defaultBlockState());
                AreaMarkerBlockEntity marker = (AreaMarkerBlockEntity)helper.getLevel().getBlockEntity(markerPosition);
                require(marker != null, "An area marker was not present at a requested height boundary");
                marker.select(other);
                selections[index] = marker.selectedArea();
            }
        } finally {
            for (int index = 0; index < types.length; index++) {
                helper.getLevel().removeBlock(new BlockPos(baseX + index * 2, heights[index], baseZ), false);
                helper.getLevel().removeBlock(new BlockPos(baseX + index * 2 + 1, heights[index], baseZ), false);
            }
        }
        AreaBounds normal = selections[0];
        AreaBounds openPit = selections[1];
        AreaBounds leveling = selections[2];
        AreaBounds prism = selections[3];
        require(normal.minimum().getY() == minimumY && normal.maximum().getY() == minimumY,
                "The normal marker changed the selected Y bounds");
        require(openPit.minimum().getY() == minimumY && openPit.maximum().getY() == 0,
                "The open-pit marker used the old fixed lower bound");
        require(leveling.minimum().getY() == 255 && leveling.maximum().getY() == maximumY,
                "The leveling marker used the old fixed upper bound");
        require(prism.minimum().getY() == minimumY && prism.maximum().getY() == maximumY,
                "The prism marker did not span the actual build height");
        AreaBounds clamped = new AreaBounds(
                new BlockPos(baseX, -100, baseZ), new BlockPos(baseX, 400, baseZ)).clampHeight(helper.getLevel());
        require(clamped.minimum().getY() == minimumY && clamped.maximum().getY() == maximumY,
                "An out-of-range selection was not clamped to both build boundaries");
        require(prism.positionAt(0).equals(prism.minimum())
                        && prism.positionAt(prism.volume() - 1L).equals(prism.maximum())
                        && prism.positionAt(prism.volume()).equals(prism.minimum()),
                "The sequential area cursor visited a coordinate outside its inclusive bounds");
        AreaMachineBlockEntity miner = placeMachine(helper, new BlockPos(2, 2, 2), AreaMachineType.MINER);
        BlockPos low = new BlockPos(prism.minimum().getX(), minimumY, baseZ);
        BlockPos high = new BlockPos(prism.minimum().getX(), maximumY, baseZ);
        BlockPos sentinel = new BlockPos(prism.minimum().getX() - 1, 0, baseZ);
        Map<BlockPos, BlockState> previous = new HashMap<>();
        try {
            for (int y = minimumY; y <= maximumY; y++) {
                for (int x = prism.minimum().getX(); x <= prism.maximum().getX(); x++) {
                    BlockPos point = new BlockPos(x, y, baseZ);
                    previous.put(point, helper.getLevel().getBlockState(point));
                    helper.getLevel().setBlockAndUpdate(point, Blocks.AIR.defaultBlockState());
                }
            }
            previous.put(sentinel, helper.getLevel().getBlockState(sentinel));
            helper.getLevel().setBlockAndUpdate(low, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(high, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(sentinel, Blocks.STONE.defaultBlockState());
            miner.configureArea(prism);
            int mined = miner.runOperationsNow((int)prism.volume());
            require(mined == 2
                            && helper.getLevel().getBlockState(low).isAir()
                            && helper.getLevel().getBlockState(high).isAir()
                            && helper.getLevel().getBlockState(sentinel).is(Blocks.STONE),
                    "The machine did not scan only the selected build-height range: " + mined
                            + ", status=" + miner.status() + ", cursor=" + miner.cursor());
        } finally {
            previous.forEach((point, state) -> helper.getLevel().setBlockAndUpdate(point, state));
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID, timeoutTicks = 100)
    public static void areaMachinesPreserveCountsProtectionTierSpeedAndLaserAcceleration(GameTestHelper helper) {
        require(AreaMachineRegistration.MARKERS.size() == 4 && AreaMachineRegistration.MACHINES.size() == 5,
                "The four markers and five original area machines are not registered");
        require(AreaMachineType.MINER.progressPerJob() == 400
                        && AreaMachineType.ADVANCED_MINER.progressPerJob() == 400
                        && AreaMachineType.ITEM_COLLECTOR.progressPerJob() == 10
                        && AreaMachineType.MINER.energyPerTick() == 1000
                        && AreaMachineType.ITEM_COLLECTOR.energyPerTick() == 100,
                "Original area machine progress or energy constants changed");

        BlockPos minerPosition = new BlockPos(2, 2, 2);
        BlockPos orePosition = new BlockPos(3, 2, 2);
        AreaMachineBlockEntity miner = placeMachine(helper, minerPosition, AreaMachineType.MINER);
        var operator = helper.makeMockPlayer(GameType.CREATIVE);
        operator.setPos(miner.getBlockPos().getX() + 0.5D, miner.getBlockPos().getY() + 0.5D,
                miner.getBlockPos().getZ() + 0.5D);
        AreaMachineMenu menu = (AreaMachineMenu)miner.createMenu(1, operator.getInventory(), operator);
        ItemStack menuFilter = new ItemStack(LogisticsRegistration.ITEM_FILTER.get());
        operator.getInventory().setItem(0, menuFilter);
        ItemStack transferred = menu.quickMoveStack(operator, 39);
        require(menu.stillValid(operator) && menu.slots.size() == 48
                        && !transferred.isEmpty()
                        && miner.inventory().getStackInSlot(AreaMachineBlockEntity.FILTER_SLOT)
                                .is(LogisticsRegistration.ITEM_FILTER.get())
                        && !miner.inventory().isItemValid(AreaMachineBlockEntity.OPERATION_SLOT,
                                new ItemStack(LogisticsRegistration.ITEM_FILTER.get())),
                "The area menu did not expose and route its operation, filter, fuel and output slots");
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.FILTER_SLOT, ItemStack.EMPTY);
        Holder<Enchantment> silkTouch = helper.getLevel().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        ItemStack silkTool = new ItemStack(Items.DIAMOND_PICKAXE);
        silkTool.enchant(silkTouch, 1);
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT, silkTool);
        miner.configureArea(absoluteArea(helper, orePosition, orePosition));
        helper.setBlock(orePosition, WorldgenRegistration.CLAY_ORE.get());
        require(miner.runOperationsNow(1) == 1
                        && outputCount(miner, WorldgenRegistration.CLAY_ORE.get().asItem()) == 1,
                "The area miner did not retain M27 Silk Touch loot behavior");

        BlockPos filteredTarget = new BlockPos(4, 2, 2);
        helper.setBlock(filteredTarget, Blocks.STONE);
        miner.configureArea(absoluteArea(helper, filteredTarget, filteredTarget));
        ItemStack filter = new ItemStack(LogisticsRegistration.ITEM_FILTER.get());
        filter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), new ItemFilterSettings(
                ItemFilterSettings.Mode.WHITELIST,
                List.of(ItemFilterRule.fromExample(ItemFilterRule.MatchType.FUZZY, new ItemStack(Items.DIAMOND)))));
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.FILTER_SLOT, filter);
        require(miner.runOperationsNow(1) == 0 && helper.getBlockState(filteredTarget).is(Blocks.STONE),
                "The M10 item filter did not exclude a block from area mining");
        miner.inventory().setStackInSlot(AreaMachineBlockEntity.FILTER_SLOT, ItemStack.EMPTY);

        BlockPos collectorPosition = new BlockPos(2, 2, 5);
        BlockPos collectedPosition = new BlockPos(3, 2, 5);
        AreaMachineBlockEntity collector = placeMachine(helper, collectorPosition, AreaMachineType.ITEM_COLLECTOR);
        collector.configureArea(absoluteArea(helper, collectedPosition, collectedPosition));
        BlockPos collectedAbsolute = helper.absolutePos(collectedPosition);
        ItemEntity dropped = new ItemEntity(
                helper.getLevel(), collectedAbsolute.getX() + 0.5D, collectedAbsolute.getY() + 0.25D,
                collectedAbsolute.getZ() + 0.5D, new ItemStack(Items.IRON_INGOT, 3));
        helper.getLevel().addFreshEntity(dropped);
        require(collector.runOperationsNow(1) == 1
                        && !dropped.isAlive()
                        && outputCount(collector, Items.IRON_INGOT) == 3,
                "Area collection did not preserve the exact dropped-item count");

        BlockPos replacerPosition = new BlockPos(2, 2, 8);
        BlockPos replaceFirst = new BlockPos(3, 2, 8);
        BlockPos replaceSecond = new BlockPos(4, 2, 8);
        AreaMachineBlockEntity replacer = placeMachine(helper, replacerPosition, AreaMachineType.REPLACER);
        replacer.configureArea(absoluteArea(helper, replaceFirst, replaceSecond));
        replacer.inventory().setStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT, new ItemStack(Items.DIRT, 2));
        helper.setBlock(replaceFirst, Blocks.STONE);
        helper.setBlock(replaceSecond, Blocks.STONE);
        require(replacer.runOperationsNow(2) == 2
                        && helper.getBlockState(replaceFirst).is(Blocks.DIRT)
                        && helper.getBlockState(replaceSecond).is(Blocks.DIRT)
                        && replacer.inventory().getStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT).isEmpty(),
                "Area replacement did not consume one replacement item per changed block");
        helper.setBlock(replaceFirst, Blocks.STONE);
        helper.setBlock(replaceSecond, Blocks.STONE);
        replacer.configureArea(absoluteArea(helper, replaceFirst, replaceSecond));
        require(replacer.runOperationsNow(2) == 0 && replacer.cursor() == 0
                        && replacer.status() == AreaMachineBlockEntity.OperationStatus.NO_MATERIAL,
                "An empty replacement slot advanced the cursor");
        helper.setBlock(replaceFirst, Blocks.DIRT);
        helper.setBlock(replaceSecond, Blocks.DIRT);
        replacer.inventory().setStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT,
                new ItemStack(Items.STONE, 2));
        for (int slot = AreaMachineBlockEntity.OUTPUT_START;
                slot < AreaMachineBlockEntity.OUTPUT_START + AreaMachineBlockEntity.OUTPUT_SLOTS; slot++) {
            replacer.inventory().setStackInSlot(slot, new ItemStack(Items.DIRT, 64));
        }
        require(replacer.runOperationsNow(2) == 0 && replacer.cursor() == 0
                        && replacer.status() == AreaMachineBlockEntity.OperationStatus.OUTPUT_FULL,
                "A full output advanced past an unprocessed replacement target");
        replacer.inventory().setStackInSlot(AreaMachineBlockEntity.OUTPUT_START, ItemStack.EMPTY);
        require(replacer.runOperationsNow(2) == 2
                        && replacer.inventory().getStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT).isEmpty(),
                "Replacement did not resume at the blocked coordinate");

        BlockPos activatorPosition = new BlockPos(7, 2, 2);
        BlockPos activatedPosition = new BlockPos(8, 2, 2);
        AreaMachineBlockEntity activator = placeMachine(helper, activatorPosition, AreaMachineType.ACTIVATOR);
        activator.configureArea(absoluteArea(helper, activatedPosition, activatedPosition));
        activator.inventory().setStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT, new ItemStack(Items.IRON_AXE));
        helper.setBlock(activatedPosition, Blocks.OAK_LOG);
        require(activator.runOperationsNow(1) == 1
                        && helper.getBlockState(activatedPosition).is(Blocks.STRIPPED_OAK_LOG)
                        && activator.inventory().getStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT).getDamageValue() == 1,
                "Area activation did not match one server-side player use");

        BlockPos protectedPosition = new BlockPos(8, 2, 5);
        AreaMachineBlockEntity advanced = placeMachine(helper, new BlockPos(7, 2, 5), AreaMachineType.ADVANCED_MINER);
        advanced.configureArea(absoluteArea(helper, protectedPosition, protectedPosition));
        advanced.inventory().setStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.setBlock(protectedPosition, Blocks.STONE);
        BlockPos protectedAbsolute = helper.absolutePos(protectedPosition);
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getPos().equals(protectedAbsolute)) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, protect);
        try {
            require(advanced.runOperationsNow(4) == 0
                            && helper.getBlockState(protectedPosition).is(Blocks.STONE)
                            && advanced.status() == AreaMachineBlockEntity.OperationStatus.PROTECTED,
                    "A protected coordinate was changed by an area operation");
        } finally {
            NeoForge.EVENT_BUS.unregister(protect);
        }

        BlockPos deniedReplacement = new BlockPos(4, 2, 8);
        replacer.configureArea(absoluteArea(helper, deniedReplacement, deniedReplacement));
        replacer.inventory().setStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT, new ItemStack(Items.DIRT));
        helper.setBlock(deniedReplacement, Blocks.STONE);
        Consumer<BlockEvent.EntityPlaceEvent> denyPlacement = event -> {
            if (event.getPos().equals(helper.absolutePos(deniedReplacement))) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(BlockEvent.EntityPlaceEvent.class, denyPlacement);
        try {
            require(replacer.runOperationsNow(1) == 0
                            && helper.getBlockState(deniedReplacement).is(Blocks.STONE)
                            && replacer.inventory().getStackInSlot(AreaMachineBlockEntity.OPERATION_SLOT).getCount() == 1
                            && replacer.status() == AreaMachineBlockEntity.OperationStatus.PROTECTED,
                    "Cancelled placement changed the original block or replacement count");
        } finally {
            NeoForge.EVENT_BUS.unregister(denyPlacement);
        }

        helper.setBlock(activatedPosition, Blocks.OAK_LOG);
        activator.configureArea(absoluteArea(helper, activatedPosition, activatedPosition));
        Consumer<PlayerInteractEvent.RightClickBlock> denyUse = event -> {
            if (event.getPos().equals(helper.absolutePos(activatedPosition))) {
                event.setCanceled(true);
            }
        };
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, denyUse);
        try {
            require(activator.runOperationsNow(1) == 0
                            && helper.getBlockState(activatedPosition).is(Blocks.OAK_LOG),
                    "Cancelled activation changed a protected block");
        } finally {
            NeoForge.EVENT_BUS.unregister(denyUse);
        }

        require(helper.getLevel().getCapability(InterfaceCapabilities.LASER_RECEIVER,
                        advanced.getBlockPos(), Direction.UP) == advanced,
                "The area machine did not expose the laser receiver capability");
        BlockPos speedFirst = new BlockPos(1, 2, 10);
        BlockPos speedLast = new BlockPos(8, 2, 10);
        BlockPos outside = new BlockPos(9, 2, 10);
        for (int x = speedFirst.getX(); x <= speedLast.getX(); x++) {
            helper.setBlock(new BlockPos(x, 2, 10), Blocks.STONE);
        }
        helper.setBlock(outside, Blocks.STONE);
        advanced.configureArea(absoluteArea(helper, speedFirst, speedLast));
        advanced.inventory().setStackInSlot(AreaMachineBlockEntity.FUEL_SLOT,
                new ItemStack(MaterialRegistration.COMPRESSION_BLOCKS.get("energetic_clay").get()));
        for (int tick = 0; tick < 4; tick++) {
            AreaMachineBlockEntity.serverTick(helper.getLevel(), advanced.getBlockPos(),
                    advanced.getBlockState(), advanced);
        }
        require(helper.getBlockState(speedFirst).isAir()
                        && helper.getBlockState(new BlockPos(2, 2, 10)).is(Blocks.STONE),
                "Unaccelerated mining did not take four ticks per block");
        var receiver = helper.getLevel().getCapability(InterfaceCapabilities.LASER_RECEIVER,
                advanced.getBlockPos(), Direction.UP);
        receiver.receiveLaser(new LaserBeam(10L, 10L, 10L));
        AreaMachineBlockEntity.serverTick(helper.getLevel(), advanced.getBlockPos(),
                advanced.getBlockState(), advanced);
        for (int x = 1; x <= 6; x++) {
            require(helper.getBlockState(new BlockPos(x, 2, 10)).isAir(),
                    "Laser acceleration did not process five additional blocks");
        }
        require(helper.getBlockState(outside).is(Blocks.STONE),
                "Mining changed a block outside the configured area");

        CompoundTag saved = replacer.saveWithFullMetadata(helper.getLevel().registryAccess());
        AreaMachineBlockEntity loaded = new AreaMachineBlockEntity(
                replacer.getBlockPos(), replacer.getBlockState());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(loaded.cursor() == replacer.cursor() && loaded.workArea().equals(replacer.workArea()),
                "The sequential scan cursor or selected area did not survive NBT");
        helper.succeed();
    }

    private static AreaMachineBlockEntity placeMachine(
            GameTestHelper helper, BlockPos relativePosition, AreaMachineType type) {
        AreaMachineBlock block = AreaMachineRegistration.MACHINES.get(type).block().get();
        helper.setBlock(relativePosition, block.defaultBlockState()
                .setValue(AreaMachineBlock.FACING, Direction.EAST));
        AreaMachineBlockEntity machine = (AreaMachineBlockEntity)helper.getBlockEntity(relativePosition);
        machine.setOwner(OWNER);
        return machine;
    }

    private static AreaBounds absoluteArea(GameTestHelper helper, BlockPos first, BlockPos second) {
        return new AreaBounds(helper.absolutePos(first), helper.absolutePos(second));
    }

    private static int outputCount(AreaMachineBlockEntity machine, Item item) {
        int count = 0;
        for (int slot = AreaMachineBlockEntity.OUTPUT_START;
                slot < AreaMachineBlockEntity.OUTPUT_START + AreaMachineBlockEntity.OUTPUT_SLOTS;
                slot++) {
            ItemStack stack = machine.inventory().getStackInSlot(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private AreaMachineGameTests() {
    }
}
