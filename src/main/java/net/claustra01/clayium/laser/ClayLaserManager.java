package net.claustra01.clayium.laser;

import java.util.Map;
import javax.annotation.Nullable;
import net.claustra01.clayium.registry.ClayiumBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.common.Tags;

/**
 * Raycasts and evaluates world/machine effects of an emitted Clay Laser beam,
 * matching 1.7.10 {@code ClayLaserManager} (max length 32, 10-tick stability history,
 * rock/ore block conversion, sapling species cycling -> Clay Tree sapling at 300,000 energy,
 * and {@link IClayLaserMachine} delivery).
 */
public class ClayLaserManager {
    public static final int MAX_LASER_LENGTH = 32;
    public static final int HISTORY_LENGTH = 10;
    public static final long CLAY_TREE_SAPLING_ENERGY = 300_000L;

    private static final Map<Block, Block> ORE_TO_STORAGE_BLOCK = Map.ofEntries(
            Map.entry(Blocks.COAL_ORE, Blocks.COAL_BLOCK),
            Map.entry(Blocks.DEEPSLATE_COAL_ORE, Blocks.COAL_BLOCK),
            Map.entry(Blocks.IRON_ORE, Blocks.IRON_BLOCK),
            Map.entry(Blocks.DEEPSLATE_IRON_ORE, Blocks.IRON_BLOCK),
            Map.entry(Blocks.GOLD_ORE, Blocks.GOLD_BLOCK),
            Map.entry(Blocks.DEEPSLATE_GOLD_ORE, Blocks.GOLD_BLOCK),
            Map.entry(Blocks.DIAMOND_ORE, Blocks.DIAMOND_BLOCK),
            Map.entry(Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DIAMOND_BLOCK),
            Map.entry(Blocks.REDSTONE_ORE, Blocks.REDSTONE_BLOCK),
            Map.entry(Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.REDSTONE_BLOCK),
            Map.entry(Blocks.LAPIS_ORE, Blocks.LAPIS_BLOCK),
            Map.entry(Blocks.DEEPSLATE_LAPIS_ORE, Blocks.LAPIS_BLOCK),
            Map.entry(Blocks.EMERALD_ORE, Blocks.EMERALD_BLOCK),
            Map.entry(Blocks.DEEPSLATE_EMERALD_ORE, Blocks.EMERALD_BLOCK)
    );

    private static final Block[] SAPLING_CYCLE = {
            Blocks.OAK_SAPLING,
            Blocks.SPRUCE_SAPLING,
            Blocks.BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING,
            Blocks.ACACIA_SAPLING,
            Blocks.DARK_OAK_SAPLING
    };

    private int laserLength = MAX_LASER_LENGTH;
    private boolean hasTarget = false;
    private int targetBlockId = 0;
    private final long[] laserEnergyHistory = new long[HISTORY_LENGTH];
    private long totalIrradiatedEnergy = 0L;

    private boolean irradiating = false;
    private Direction direction = Direction.NORTH;
    @Nullable
    private ClayLaser clayLaser;

    public boolean isIrradiating() {
        return this.irradiating;
    }

    public int getLaserLength() {
        return this.laserLength;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public long getTotalIrradiatedEnergy() {
        return this.totalIrradiatedEnergy;
    }

    @Nullable
    public ClayLaser getClayLaser() {
        return this.clayLaser;
    }

    public boolean update(Level level, BlockPos origin, Direction dir, @Nullable ClayLaser laser, boolean emit) {
        boolean prevIrradiating = this.irradiating;
        int prevLength = this.laserLength;
        Direction prevDir = this.direction;
        ClayLaser prevLaser = this.clayLaser;

        this.direction = dir;
        if (!emit || laser == null) {
            this.irradiating = false;
            this.hasTarget = false;
            this.laserLength = MAX_LASER_LENGTH;
            this.clayLaser = null;
            resetHistory();
            return prevIrradiating != this.irradiating
                    || prevLength != this.laserLength
                    || prevDir != this.direction
                    || !sameLaser(prevLaser, this.clayLaser);
        }

        this.irradiating = true;
        this.clayLaser = laser;

        int foundLen = MAX_LASER_LENGTH;
        boolean hit = false;
        BlockPos targetPos = origin;
        for (int i = 1; i < MAX_LASER_LENGTH; i++) {
            BlockPos check = origin.relative(dir, i);
            if (!level.isInWorldBounds(check)) {
                foundLen = i;
                hit = false;
                break;
            }
            if (!canGoThrough(level, check)) {
                foundLen = i;
                targetPos = check;
                hit = true;
                break;
            }
        }

        if (hit != this.hasTarget || foundLen != this.laserLength) {
            resetHistory();
        }
        this.hasTarget = hit;
        this.laserLength = foundLen;

        if (!this.hasTarget) {
            resetHistory();
            this.targetBlockId = 0;
        } else {
            BlockState targetState = level.getBlockState(targetPos);
            int stateId = Block.getId(targetState);
            if (stateId != this.targetBlockId) {
                resetHistory();
                this.targetBlockId = stateId;
            }
            irradiateTarget(level, targetPos, targetState, dir, laser);
        }

        return prevIrradiating != this.irradiating
                || prevLength != this.laserLength
                || prevDir != this.direction
                || !sameLaser(prevLaser, this.clayLaser);
    }

    private void resetHistory() {
        this.totalIrradiatedEnergy = 0L;
        for (int i = 0; i < HISTORY_LENGTH; i++) {
            this.laserEnergyHistory[i] = 0L;
        }
    }

    private static boolean canGoThrough(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(Tags.Blocks.GLASS_BLOCKS)
                || state.is(Tags.Blocks.GLASS_PANES);
    }

    private void irradiateTarget(
            Level level,
            BlockPos targetPos,
            BlockState state,
            Direction dir,
            ClayLaser laser) {
        long n = (long) laser.getEnergy();
        if (n > 0L) {
            this.totalIrradiatedEnergy += n;
            long minHistory = n;
            for (int i = 0; i < HISTORY_LENGTH; i++) {
                minHistory = Math.min(minHistory, this.laserEnergyHistory[i]);
            }
            this.totalIrradiatedEnergy = (long) ((double) minHistory * (double) this.totalIrradiatedEnergy / (double) n);
            for (int i = 0; i < HISTORY_LENGTH - 1; i++) {
                this.laserEnergyHistory[i] = this.laserEnergyHistory[i + 1];
            }
            this.laserEnergyHistory[HISTORY_LENGTH - 1] = n;
        } else {
            resetHistory();
        }

        // 1. Stone / ore mining & storage-block compression
        float hardness = state.getDestroySpeed(level, targetPos);
        if (hardness >= 0.0F
                && !state.hasBlockEntity()
                && isLaserMineableRock(state)
                && this.totalIrradiatedEnergy >= (long) ((hardness + 1.0F) * 100.0F)) {
            Block replacement = ORE_TO_STORAGE_BLOCK.get(state.getBlock());
            level.levelEvent(2001, targetPos, Block.getId(state));
            if (replacement != null) {
                level.setBlock(targetPos, replacement.defaultBlockState(), Block.UPDATE_ALL);
            } else {
                Block.dropResources(state, level, targetPos, null);
                level.removeBlock(targetPos, false);
            }
            resetHistory();
            this.targetBlockId = 0;
            return;
        }

        // 2. Sapling cycling & Clay Tree creation
        int saplingIdx = indexOfSapling(state.getBlock());
        if (saplingIdx >= 0) {
            if (this.totalIrradiatedEnergy >= CLAY_TREE_SAPLING_ENERGY) {
                level.levelEvent(2001, targetPos, Block.getId(state));
                level.setBlock(
                        targetPos,
                        ClayiumBlocks.byPath("clay_tree_sapling").get().defaultBlockState(),
                        Block.UPDATE_ALL
                );
                resetHistory();
                this.targetBlockId = 0;
                return;
            } else if (this.totalIrradiatedEnergy >= 300L && this.totalIrradiatedEnergy < 1000L) {
                Block nextSapling = SAPLING_CYCLE[(saplingIdx + 1) % SAPLING_CYCLE.length];
                level.levelEvent(2001, targetPos, Block.getId(state));
                level.setBlock(targetPos, nextSapling.defaultBlockState(), Block.UPDATE_ALL);
                resetHistory();
                this.targetBlockId = Block.getId(nextSapling.defaultBlockState());
                return;
            }
        }

        // 3. Deliver energy to IClayLaserMachine targets immediately every active tick
        BlockEntity targetBe = level.getBlockEntity(targetPos);
        if (targetBe instanceof IClayLaserMachine machine) {
            machine.irradiateClayLaser(laser, dir.getOpposite());
        }
    }

    private static boolean isLaserMineableRock(BlockState state) {
        return state.instrument() == NoteBlockInstrument.BASEDRUM
                || state.is(Tags.Blocks.ORES)
                || state.is(Tags.Blocks.STONES)
                || state.is(Tags.Blocks.COBBLESTONES);
    }

    private static int indexOfSapling(Block block) {
        for (int i = 0; i < SAPLING_CYCLE.length; i++) {
            if (SAPLING_CYCLE[i] == block) {
                return i;
            }
        }
        return -1;
    }

    private static boolean sameLaser(@Nullable ClayLaser a, @Nullable ClayLaser b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.age == b.age
                && a.numbers[0] == b.numbers[0]
                && a.numbers[1] == b.numbers[1]
                && a.numbers[2] == b.numbers[2];
    }

    public void save(CompoundTag tag) {
        tag.putLong("TotalIrradiatedEnergy", this.totalIrradiatedEnergy);
        ListTag hist = new ListTag();
        for (long v : this.laserEnergyHistory) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Energy", v);
            hist.add(entry);
        }
        tag.put("LaserEnergyHistory", hist);
        tag.putInt("LaserLength", this.laserLength);
        tag.putBoolean("Irradiating", this.irradiating);
        tag.putInt("Direction", this.direction.get3DDataValue());
        tag.putInt("TargetBlockId", this.targetBlockId);
        tag.putBoolean("HasTarget", this.hasTarget);
        if (this.clayLaser != null) {
            CompoundTag lt = new CompoundTag();
            lt.putInt("Age", this.clayLaser.age);
            lt.putIntArray("Numbers", this.clayLaser.numbers);
            tag.put("Laser", lt);
        }
    }

    public void load(CompoundTag tag) {
        this.totalIrradiatedEnergy = tag.getLong("TotalIrradiatedEnergy");
        ListTag hist = tag.getList("LaserEnergyHistory", Tag.TAG_COMPOUND);
        for (int i = 0; i < HISTORY_LENGTH; i++) {
            this.laserEnergyHistory[i] = i < hist.size() ? hist.getCompound(i).getLong("Energy") : 0L;
        }
        this.laserLength = tag.contains("LaserLength") ? tag.getInt("LaserLength") : MAX_LASER_LENGTH;
        this.irradiating = tag.getBoolean("Irradiating");
        this.direction = tag.contains("Direction") ? Direction.from3DDataValue(tag.getInt("Direction")) : Direction.NORTH;
        this.targetBlockId = tag.getInt("TargetBlockId");
        this.hasTarget = tag.getBoolean("HasTarget");
        if (tag.contains("Laser", Tag.TAG_COMPOUND)) {
            CompoundTag lt = tag.getCompound("Laser");
            int age = lt.getInt("Age");
            int[] nums = lt.getIntArray("Numbers");
            this.clayLaser = new ClayLaser(age, nums);
        } else {
            this.clayLaser = null;
        }
    }
}
