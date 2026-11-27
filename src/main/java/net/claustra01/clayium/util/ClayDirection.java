package net.claustra01.clayium.util;

import net.minecraft.core.Direction;

/**
 * Relative-side <-> world-Direction mapping matching 1.7.10 UtilDirection.
 * Direction indices mirror Minecraft's 3D data values: DOWN(0), UP(1), NORTH(2), SOUTH(3), WEST(4), EAST(5).
 */
public final class ClayDirection {
    public enum RelativeSide {
        DOWN(0),
        UP(1),
        FRONT(2),
        BACK(3),
        LEFT(4),
        RIGHT(5);

        private static final RelativeSide[] BY_ID = values();
        private final int id;

        RelativeSide(int id) {
            this.id = id;
        }

        public int id() {
            return this.id;
        }

        public static RelativeSide byId(int id) {
            if (id < 0 || id >= BY_ID.length) {
                throw new IllegalArgumentException("Invalid RelativeSide id: " + id);
            }
            return BY_ID[id];
        }

        public RelativeSide opposite() {
            return byId(OPPOSITES[this.id]);
        }
    }

    private static final int[] OPPOSITES = {1, 0, 3, 2, 5, 4};
    private static final int[] LEFTSIDES = {4, 4, 4, 5, 3, 2};
    private static final int[] RIGHTSIDES = {5, 5, 5, 4, 2, 3};
    private static final int[] UPSIDES = {2, 3, 1, 1, 1, 1};
    private static final int[] DOWNSIDES = {3, 2, 0, 0, 0, 0};

    private ClayDirection() {}

    /**
     * Converts a relative side (relative to a block whose front faces {@code front}) into a world {@link Direction}.
     */
    public static Direction sideToDirection(Direction front, RelativeSide side) {
        int frontIdx = front.get3DDataValue();
        int targetIdx = switch (side) {
            case DOWN -> DOWNSIDES[frontIdx];
            case UP -> UPSIDES[frontIdx];
            case FRONT -> frontIdx;
            case BACK -> OPPOSITES[frontIdx];
            case LEFT -> LEFTSIDES[frontIdx];
            case RIGHT -> RIGHTSIDES[frontIdx];
        };
        return Direction.from3DDataValue(targetIdx);
    }

    /**
     * Converts a world {@link Direction} into a {@link RelativeSide} for a block whose front faces {@code front}.
     */
    public static RelativeSide directionToSide(Direction front, Direction worldDir) {
        int frontIdx = front.get3DDataValue();
        int dirIdx = worldDir.get3DDataValue();
        if (dirIdx == frontIdx) {
            return RelativeSide.FRONT;
        }
        if (DOWNSIDES[frontIdx] == dirIdx) {
            return RelativeSide.DOWN;
        }
        if (UPSIDES[frontIdx] == dirIdx) {
            return RelativeSide.UP;
        }
        if (LEFTSIDES[frontIdx] == dirIdx) {
            return RelativeSide.LEFT;
        }
        if (RIGHTSIDES[frontIdx] == dirIdx) {
            return RelativeSide.RIGHT;
        }
        return RelativeSide.BACK;
    }
}
