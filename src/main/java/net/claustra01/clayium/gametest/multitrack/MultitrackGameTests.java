package net.claustra01.clayium.gametest.multitrack;

import java.util.List;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.logistics.BufferBlockEntity;
import net.claustra01.clayium.logistics.distributor.DistributorBlockEntity;
import net.claustra01.clayium.logistics.filter.ItemFilterRule;
import net.claustra01.clayium.logistics.filter.ItemFilterSettings;
import net.claustra01.clayium.logistics.multitrack.MultitrackBufferBlockEntity;
import net.claustra01.clayium.registry.LogisticsRegistration;
import net.claustra01.clayium.registry.MultitrackRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MultitrackGameTests {
    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void matchingColorsCrossWithoutMixingAndKeepTierSpeedFiltersAndState(GameTestHelper helper) {
        require(MultitrackRegistration.MULTITRACK_BUFFERS.size() == 10,
                "The ten original multitrack tiers were not registered");
        MultitrackBufferBlockEntity west = multitrack(helper, 4, new BlockPos(1, 1, 2));
        MultitrackBufferBlockEntity north = multitrack(helper, 4, new BlockPos(2, 1, 1));
        MultitrackBufferBlockEntity junction = multitrack(helper, 4, new BlockPos(2, 1, 2));
        MultitrackBufferBlockEntity east = multitrack(helper, 4, new BlockPos(3, 1, 2));
        MultitrackBufferBlockEntity south = multitrack(helper, 4, new BlockPos(2, 1, 3));
        for (MultitrackBufferBlockEntity buffer : List.of(west, north, junction, east, south)) {
            for (int track = 0; track < buffer.trackCount(); track++) {
                for (Direction direction : Direction.values())
                    buffer.setFaceMode(track, direction, BufferBlockEntity.FaceMode.DISABLED);
            }
        }
        west.setFaceMode(0, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        north.setFaceMode(1, Direction.SOUTH, BufferBlockEntity.FaceMode.OUTPUT);
        junction.setFaceMode(0, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        junction.setFaceMode(0, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        junction.setFaceMode(1, Direction.NORTH, BufferBlockEntity.FaceMode.INPUT);
        junction.setFaceMode(1, Direction.SOUTH, BufferBlockEntity.FaceMode.OUTPUT);
        east.setFaceMode(0, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        south.setFaceMode(1, Direction.NORTH, BufferBlockEntity.FaceMode.INPUT);
        west.track(0).inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 8));
        west.track(0).inventory().setStackInSlot(1, new ItemStack(Items.DIAMOND, 8));
        north.track(1).inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 8));
        junction.track(0).filter().setStackInSlot(0, whitelist(Items.CLAY_BALL));
        junction.track(1).filter().setStackInSlot(0, whitelist(Items.IRON_INGOT));
        tick(helper, west, 7);
        tick(helper, north, 7);
        require(total(junction.track(0).inventory()) == 0 && total(junction.track(1).inventory()) == 0,
                "Tier 4 transferred before its eight tick interval");
        for (int round = 0; round < 4; round++) {
            tick(helper, west, round == 0 ? 1 : 8);
            tick(helper, north, round == 0 ? 1 : 8);
            tick(helper, junction, 8);
        }
        require(count(east.track(0).inventory(), Items.CLAY_BALL) == 4
                        && count(east.track(0).inventory(), Items.IRON_INGOT) == 0
                        && count(south.track(1).inventory(), Items.IRON_INGOT) == 4
                        && count(south.track(1).inventory(), Items.CLAY_BALL) == 0,
                "The crossing mixed colors or missed the tier 4 rate");
        require(count(west.track(0).inventory(), Items.DIAMOND) == 8
                        && count(east.track(0).inventory(), Items.DIAMOND) == 0,
                "The junction filter transferred a rejected item");

        CompoundTag saved = junction.saveWithFullMetadata(helper.getLevel().registryAccess());
        MultitrackBufferBlockEntity loaded = new MultitrackBufferBlockEntity(junction.getBlockPos(), junction.getBlockState());
        loaded.setLevel(helper.getLevel());
        loaded.loadWithComponents(saved, helper.getLevel().registryAccess());
        require(count(loaded.track(0).inventory(), Items.CLAY_BALL)
                        == count(junction.track(0).inventory(), Items.CLAY_BALL),
                "Track inventory was not persistent");
        require(loaded.track(0).faceMode(Direction.WEST) == BufferBlockEntity.FaceMode.INPUT,
                "Track face state was not persistent");
        require(loaded.track(0).filter().getStackInSlot(0).is(LogisticsRegistration.ITEM_FILTER.get()),
                "Track filter was not persistent");

        MultitrackBufferBlockEntity fastSource = multitrack(helper, 7, new BlockPos(4, 1, 1));
        MultitrackBufferBlockEntity fastTarget = multitrack(helper, 7, new BlockPos(5, 1, 1));
        configureLink(fastSource, 2, Direction.EAST, BufferBlockEntity.FaceMode.OUTPUT);
        configureLink(fastTarget, 2, Direction.WEST, BufferBlockEntity.FaceMode.INPUT);
        fastSource.track(2).inventory().setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 64));
        tick(helper, fastSource, 1);
        require(count(fastTarget.track(2).inventory(), Items.IRON_INGOT) == 64,
                "Tier 7 did not retain the original 64 item per tick speed");
        require(total(fastTarget.track(0).inventory()) == 0 && total(fastTarget.track(1).inventory()) == 0,
                "A crossing transfer leaked into an inactive color");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void distributorsShareFairlyAcrossThreeTiersAndSkipFullOutputs(GameTestHelper helper) {
        require(MultitrackRegistration.DISTRIBUTORS.size() == 3,
                "The three original distributor tiers were not registered");
        for (int tier = 7; tier <= 9; tier++) {
            BlockPos center = new BlockPos(4, 1, 2 + (tier - 7) * 4);
            DistributorBlockEntity distributor = distributor(helper, tier, center);
            for (Direction direction : Direction.values())
                distributor.setFaceMode(direction, BufferBlockEntity.FaceMode.DISABLED);
            setOutputs(distributor, Direction.NORTH, Direction.EAST, Direction.WEST);
            distributor.setFaceMode(Direction.SOUTH, BufferBlockEntity.FaceMode.INPUT);
            ChestBlockEntity source = chest(helper, center.relative(Direction.SOUTH));
            source.setItem(0, new ItemStack(Items.CLAY_BALL, 64));
            source.setItem(1, new ItemStack(Items.CLAY_BALL, 53));
            distributor.inventory().setStackInSlot(0, new ItemStack(Items.CLAY_BALL, 3));
            helper.setBlock(center.relative(Direction.NORTH), Blocks.HOPPER);
            net.minecraft.world.Container north = (net.minecraft.world.Container)helper.getLevel()
                    .getBlockEntity(helper.absolutePos(center.relative(Direction.NORTH)));
            for (int slot = 0; slot < 4; slot++) north.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            north.setItem(4, new ItemStack(Items.CLAY_BALL, 60));
            ChestBlockEntity east = chest(helper, center.relative(Direction.EAST));
            ChestBlockEntity west = chest(helper, center.relative(Direction.WEST));
            for (int slot = 0; slot < 26; slot++) west.setItem(slot, new ItemStack(Items.COBBLESTONE, 64));
            west.setItem(26, new ItemStack(Items.CLAY_BALL, 40));
            for (int step = 0; step < 6; step++) {
                DistributorBlockEntity.serverTick(helper.getLevel(), distributor.getBlockPos(),
                        distributor.getBlockState(), distributor);
                if (step == 0) {
                    int n = count(north, Items.CLAY_BALL) - 60;
                    int e = count(east, Items.CLAY_BALL);
                    int w = count(west, Items.CLAY_BALL) - 40;
                    require(Math.max(n, Math.max(e, w)) - Math.min(n, Math.min(e, w)) <= 1,
                            "Tier " + tier + " was unfair while all outputs accepted items");
                }
            }
            int delivered = count(north, Items.CLAY_BALL) - 60
                    + count(east, Items.CLAY_BALL) + count(west, Items.CLAY_BALL) - 40;
            require(count(north, Items.CLAY_BALL) == 64 && count(west, Items.CLAY_BALL) == 64,
                    "Tier " + tier + " did not stop at each output capacity");
            require(count(east, Items.CLAY_BALL) > 24,
                    "Tier " + tier + " stopped instead of serving the available output");
            require(delivered + count(source, Items.CLAY_BALL) + total(distributor.inventory()) == 120,
                    "Tier " + tier + " lost items while distributing");
        }
        helper.succeed();
    }

    private static MultitrackBufferBlockEntity multitrack(GameTestHelper helper, int tier, BlockPos relative) {
        var definition = MultitrackRegistration.MULTITRACK_BUFFERS.get(tier);
        helper.setBlock(relative, definition.block().get());
        return (MultitrackBufferBlockEntity)helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static DistributorBlockEntity distributor(GameTestHelper helper, int tier, BlockPos relative) {
        var definition = MultitrackRegistration.DISTRIBUTORS.get(tier);
        helper.setBlock(relative, definition.block().get());
        return (DistributorBlockEntity)helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static ChestBlockEntity chest(GameTestHelper helper, BlockPos relative) {
        helper.setBlock(relative, Blocks.CHEST);
        return (ChestBlockEntity)helper.getLevel().getBlockEntity(helper.absolutePos(relative));
    }

    private static void configureLink(MultitrackBufferBlockEntity buffer, int track, Direction active,
                                      BufferBlockEntity.FaceMode activeMode) {
        for (Direction direction : Direction.values()) {
            buffer.setFaceMode(track, direction,
                    direction == active ? activeMode : BufferBlockEntity.FaceMode.DISABLED);
        }
    }

    private static void setOutputs(DistributorBlockEntity distributor, Direction... outputs) {
        for (Direction direction : Direction.values()) distributor.setFaceMode(direction, BufferBlockEntity.FaceMode.DISABLED);
        for (Direction output : outputs) distributor.setFaceMode(output, BufferBlockEntity.FaceMode.OUTPUT);
    }

    private static ItemStack whitelist(Item item) {
        ItemStack filter = LogisticsRegistration.ITEM_FILTER.toStack();
        filter.set(LogisticsRegistration.ITEM_FILTER_SETTINGS.get(), new ItemFilterSettings(
                ItemFilterSettings.Mode.WHITELIST,
                List.of(ItemFilterRule.fromExample(ItemFilterRule.MatchType.FUZZY, new ItemStack(item)))));
        return filter;
    }

    private static void tick(GameTestHelper helper, MultitrackBufferBlockEntity buffer, int ticks) {
        for (int tick = 0; tick < ticks; tick++) MultitrackBufferBlockEntity.serverTick(
                helper.getLevel(), buffer.getBlockPos(), buffer.getBlockState(), buffer);
    }

    private static int total(IItemHandler handler) {
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) total += handler.getStackInSlot(slot).getCount();
        return total;
    }

    private static int count(IItemHandler handler, Item item) {
        int total = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++)
            if (handler.getStackInSlot(slot).is(item)) total += handler.getStackInSlot(slot).getCount();
        return total;
    }

    private static int count(net.minecraft.world.Container chest, Item item) {
        int total = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++)
            if (chest.getItem(slot).is(item)) total += chest.getItem(slot).getCount();
        return total;
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    private MultitrackGameTests() { }
}
