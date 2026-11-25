package net.claustra01.clayium.pan;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public final class PanNetwork {
    public static final int MAX_DEPTH = 1_000;
    public static final int MAX_NODES = 10_000;
    public static final int NODES_PER_TICK = 256;
    public static final int REFRESH_INTERVAL = 200;

    private static final Map<Level, Long> CHANGE_VERSIONS = new WeakHashMap<>();
    private static long recipeVersion;

    public static synchronized long changeVersion(Level level) {
        return CHANGE_VERSIONS.getOrDefault(level, 0L);
    }

    public static synchronized void invalidate(Level level) {
        if (!level.isClientSide) {
            CHANGE_VERSIONS.merge(level, 1L, Long::sum);
        }
    }

    public static synchronized long recipeVersion() {
        return recipeVersion;
    }

    public static synchronized void recipesReloaded() {
        recipeVersion++;
    }

    public static Scan begin(Level level, BlockPos origin) {
        return new Scan(level, origin);
    }

    public static final class Scan {
        private final Level level;
        private final ArrayDeque<Node> pending = new ArrayDeque<>();
        private final Set<BlockPos> visited = new HashSet<>();
        private final List<BlockPos> cores = new ArrayList<>();
        private final List<BlockPos> adapters = new ArrayList<>();

        private Scan(Level level, BlockPos origin) {
            this.level = level;
            if (isPanNode(level, origin)) {
                BlockPos immutable = origin.immutable();
                pending.add(new Node(immutable, 0));
                visited.add(immutable);
            }
        }

        public boolean advance(int budget) {
            int processed = 0;
            while (!pending.isEmpty() && visited.size() <= MAX_NODES && processed++ < budget) {
                Node node = pending.removeFirst();
                if (level.getBlockState(node.pos()).getBlock() instanceof PanBlock panBlock) {
                    if (panBlock.kind() == PanBlock.Kind.CORE) {
                        cores.add(node.pos());
                    } else if (panBlock.kind() == PanBlock.Kind.ADAPTER) {
                        adapters.add(node.pos());
                    }
                }
                if (node.depth() >= MAX_DEPTH) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    if (visited.size() >= MAX_NODES) {
                        break;
                    }
                    BlockPos next = node.pos().relative(direction);
                    if (isPanNode(level, next) && visited.add(next)) {
                        pending.addLast(new Node(next.immutable(), node.depth() + 1));
                    }
                }
            }
            return pending.isEmpty() || visited.size() >= MAX_NODES;
        }

        public int nodeCount() {
            return Math.min(visited.size(), MAX_NODES);
        }

        public List<BlockPos> cores() {
            return List.copyOf(cores);
        }

        public List<BlockPos> adapters() {
            return List.copyOf(adapters);
        }
    }

    private static boolean isPanNode(Level level, BlockPos pos) {
        return level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() instanceof PanBlock;
    }

    private record Node(BlockPos pos, int depth) {
    }

    private PanNetwork() {
    }
}
