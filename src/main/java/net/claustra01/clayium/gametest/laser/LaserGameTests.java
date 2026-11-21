package net.claustra01.clayium.gametest.laser;

import java.util.HashMap;
import java.util.Map;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.claustra01.clayium.laser.ClayEnergyLaserBlock;
import net.claustra01.clayium.laser.ClayEnergyLaserBlockEntity;
import net.claustra01.clayium.laser.LaserBeam;
import net.claustra01.clayium.laser.LaserReflectorBlock;
import net.claustra01.clayium.laser.LaserReflectorBlockEntity;
import net.claustra01.clayium.laser.LaserSpecification;
import net.claustra01.clayium.machine.iface.InterfaceCapabilities;
import net.claustra01.clayium.machine.iface.LaserReceiver;
import net.claustra01.clayium.registry.LaserRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ClayiumMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class LaserGameTests {
    private static final Map<BlockPos, LaserReceiver> RECEIVERS = new HashMap<>();

    public static void registerTestCapability(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                InterfaceCapabilities.LASER_RECEIVER,
                (level, pos, state, blockEntity, side) -> RECEIVERS.get(pos),
                Blocks.BARREL);
    }

    @GameTest(template = "empty", batch = "clayium_laser_reflection")
    public static void reflectorStopsAtTargetWhenEmitterTicksFirst(GameTestHelper helper) {
        RECEIVERS.clear();
        BlockPos reflectorPosition = new BlockPos(3, 2, 1);
        BlockPos target = new BlockPos(3, 2, 7);
        helper.setBlock(target, Blocks.BARREL);
        LaserBeam[] received = {new LaserBeam(0L, 0L, 0L)};
        int[] deliveries = {0};
        RECEIVERS.put(helper.absolutePos(target), beam -> {
            received[0] = beam;
            deliveries[0]++;
            return true;
        });
        helper.getLevel().invalidateCapabilities(helper.absolutePos(target));
        helper.setBlock(reflectorPosition, LaserRegistration.LASER_REFLECTOR.get().defaultBlockState()
                .setValue(LaserReflectorBlock.FACING, Direction.SOUTH));
        LaserReflectorBlockEntity reflector = helper.getBlockEntity(reflectorPosition);
        ClayEnergyLaserBlockEntity source = placeLaser(helper, 7, new BlockPos(1, 2, 1), Direction.EAST);
        fuel(helper, source);
        tick(helper, source);
        LaserReflectorBlockEntity.serverTick(
                helper.getLevel(), reflector.getBlockPos(), reflector.getBlockState(), reflector);
        require(!reflector.isIrradiating() && reflector.laserLength() == 0,
                "A reflector displayed an untraced beam in its receiving tick");
        helper.runAfterDelay(3, () -> {
            require(reflector.isIrradiating() && reflector.laserLength() == 6,
                    "The reflected beam passed through its first blocking target");
            require(received[0].equals(new LaserBeam(1L, 0L, 0L, 1)),
                    "The reflected target did not receive the delayed beam");
            require(deliveries[0] >= 2, "Continuous input did not reach the reflected target every tick");
            RECEIVERS.clear();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = ClayiumMod.MOD_ID)
    public static void fourLasersConsumeMergeReflectAndHonorRedstone(GameTestHelper helper) {
        RECEIVERS.clear();
        require(LaserRegistration.LASERS.size() == 4, "The four original Clay Energy Lasers are not registered");
        long[] costs = {40_000L, 400_000L, 4_000_000L, 40_000_000L};
        long[][] components = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {3, 3, 3}};
        for (int index = 0; index < LaserSpecification.VALUES.size(); index++) {
            LaserSpecification specification = LaserSpecification.VALUES.get(index);
            require(specification.tier() == index + 7
                            && specification.energyPerTick().internalValue() == costs[index]
                            && specification.beam().blue() == components[index][0]
                            && specification.beam().green() == components[index][1]
                            && specification.beam().red() == components[index][2],
                    "Laser Tier " + (index + 7) + " does not preserve its original cost and components");
        }

        BlockPos target = new BlockPos(2, 2, 2);
        helper.setBlock(target, Blocks.BARREL);
        LaserBeam[] received = {new LaserBeam(0L, 0L, 0L)};
        RECEIVERS.put(helper.absolutePos(target), (beam) -> {
            received[0] = LaserBeam.merge(received[0], beam);
            return true;
        });
        helper.getLevel().invalidateCapabilities(helper.absolutePos(target));

        BlockPos[] positions = {
                new BlockPos(1, 2, 2), new BlockPos(3, 2, 2),
                new BlockPos(2, 2, 1), new BlockPos(2, 2, 3)};
        Direction[] directions = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH};
        ClayEnergyLaserBlockEntity[] lasers = new ClayEnergyLaserBlockEntity[4];
        for (int index = 0; index < lasers.length; index++) {
            lasers[index] = placeLaser(helper, index + 7, positions[index], directions[index]);
            fuel(helper, lasers[index]);
            tick(helper, lasers[index]);
            require(lasers[index].energyStorage().stored().internalValue() == 10_000_000_000_000L - costs[index],
                    "Laser Tier " + (index + 7) + " did not consume its exact CE/t");
        }
        require(received[0].equals(new LaserBeam(4L, 4L, 4L)),
                "Multiple lasers did not add their components at one receiver");
        require(Math.abs(new LaserBeam(1, 0, 0).energy() - 1.4602261494524762D) < 0.000001D
                        && Math.abs(new LaserBeam(3, 3, 3).energy() - 598.3958579893288D) < 0.000001D
                        && Math.abs(received[0].energy() - 5850.774454111925D) < 0.000001D,
                "The receiver did not produce the original nonlinear energy values");
        require(LaserBeam.merge(new LaserBeam(5, 1, 0, 10), new LaserBeam(2, 4, 1, 2))
                        .equals(new LaserBeam(5, 4, 1, 10)),
                "Long reflector chains did not switch to the original maximum-component merge");

        ClayEnergyLaserBlockEntity blue = lasers[0];
        long beforeRedstone = blue.energyStorage().stored().internalValue();
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
        LaserBeam beforeStopped = received[0];
        tick(helper, blue);
        require(received[0].equals(beforeStopped), "A powered laser still reached its receiver");
        require(blue.energyStorage().stored().internalValue() == beforeRedstone,
                "A powered laser ignored its standard redstone stop condition");
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.AIR);
        ClayiumConfig.INVERT_CLAY_LASER_RS_CONDITION.set(true);
        try {
            tick(helper, blue);
            require(received[0].equals(beforeStopped), "An unpowered inverted laser still reached its receiver");
            require(blue.energyStorage().stored().internalValue() == beforeRedstone,
                    "An unpowered laser ignored the inverted redstone stop condition");
            helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
            tick(helper, blue);
            require(blue.energyStorage().stored().internalValue() == beforeRedstone - costs[0],
                    "A powered laser did not run with the inverted redstone condition");
        } finally {
            ClayiumConfig.INVERT_CLAY_LASER_RS_CONDITION.set(false);
        }

        for (BlockPos position : positions) {
            helper.setBlock(position, Blocks.AIR);
        }
        received[0] = new LaserBeam(0L, 0L, 0L);
        BlockPos straightTarget = new BlockPos(4, 4, 1);
        helper.setBlock(straightTarget, Blocks.BARREL);
        RECEIVERS.put(helper.absolutePos(straightTarget), beam -> {
            received[0] = LaserBeam.merge(received[0], beam);
            return true;
        });
        helper.getLevel().invalidateCapabilities(helper.absolutePos(straightTarget));
        BlockPos reflectedTarget = new BlockPos(2, 4, 2);
        helper.setBlock(reflectedTarget, Blocks.BARREL);
        LaserBeam[] reflected = {new LaserBeam(0L, 0L, 0L)};
        RECEIVERS.put(helper.absolutePos(reflectedTarget), (beam) -> {
            reflected[0] = LaserBeam.merge(reflected[0], beam);
            return true;
        });
        helper.getLevel().invalidateCapabilities(helper.absolutePos(reflectedTarget));
        BlockPos reflectorPosition = new BlockPos(2, 4, 1);
        helper.setBlock(reflectorPosition, LaserRegistration.LASER_REFLECTOR.get().defaultBlockState()
                .setValue(LaserReflectorBlock.FACING, Direction.SOUTH));
        ClayEnergyLaserBlockEntity reflectedSource = placeLaser(
                helper, 7, new BlockPos(1, 4, 1), Direction.EAST);
        ClayEnergyLaserBlockEntity secondReflectedSource = placeLaser(
                helper, 8, new BlockPos(2, 5, 1), Direction.DOWN);
        fuel(helper, reflectedSource);
        fuel(helper, secondReflectedSource);
        tick(helper, reflectedSource);
        tick(helper, secondReflectedSource);
        helper.setBlock(new BlockPos(1, 4, 1), Blocks.AIR);
        helper.setBlock(new BlockPos(2, 5, 1), Blocks.AIR);
        BlockPos upwardTarget = new BlockPos(5, 4, 5);
        helper.setBlock(upwardTarget, Blocks.BARREL);
        LaserBeam[] upward = {new LaserBeam(0L, 0L, 0L)};
        RECEIVERS.put(helper.absolutePos(upwardTarget), beam -> {
            upward[0] = LaserBeam.merge(upward[0], beam);
            return true;
        });
        helper.getLevel().invalidateCapabilities(helper.absolutePos(upwardTarget));
        ClayEnergyLaserBlockEntity upwardSource = placeLaser(
                helper, 9, new BlockPos(5, 2, 5), Direction.UP);
        fuel(helper, upwardSource);
        tick(helper, upwardSource);
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.AIR);
        helper.runAfterDelay(3, () -> {
            require(reflected[0].equals(new LaserBeam(1L, 1L, 0L, 1)),
                    "The reflector did not merge and redirect both beams toward its facing");
            require(received[0].isEmpty(), "The old straight-line receiver still received the reflected beam");
            require(upward[0].equals(new LaserBeam(0L, 0L, 1L)),
                    "The upward laser did not deliver its beam above the emitter");
            RECEIVERS.clear();
            helper.succeed();
        });
    }

    private static void fuel(GameTestHelper helper, ClayEnergyLaserBlockEntity laser) {
        IItemHandler handler = helper.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, laser.getBlockPos(), Direction.UP);
        require(handler != null && handler.insertItem(0, new ItemStack(
                MaterialRegistration.COMPRESSION_BLOCKS.get("octuple_compressed_energetic_clay").get()), false).isEmpty(),
                "The fuel slot rejected compressed energetic clay");
    }

    private static ClayEnergyLaserBlockEntity placeLaser(
            GameTestHelper helper, int tier, BlockPos position, Direction direction) {
        var definition = LaserRegistration.LASERS.get(tier);
        helper.setBlock(position, definition.block().get().defaultBlockState()
                .setValue(ClayEnergyLaserBlock.FACING, direction));
        return helper.getBlockEntity(position);
    }

    private static void tick(GameTestHelper helper, ClayEnergyLaserBlockEntity laser) {
        ClayEnergyLaserBlockEntity.serverTick(
                helper.getLevel(), laser.getBlockPos(), laser.getBlockState(), laser);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    private LaserGameTests() {
    }
}
