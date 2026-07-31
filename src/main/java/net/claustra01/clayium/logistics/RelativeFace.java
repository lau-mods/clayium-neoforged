/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

import net.minecraft.core.Direction;

/** Original Clayium side order: down, up, front, back, left, right. */
public final class RelativeFace {
    public static final int COUNT = 6;

    private RelativeFace() {
    }

    public static int index(Direction front, Direction worldSide) {
        if (worldSide == Direction.DOWN) {
            return 0;
        }
        if (worldSide == Direction.UP) {
            return 1;
        }
        if (worldSide == front) {
            return 2;
        }
        if (worldSide == front.getOpposite()) {
            return 3;
        }
        return worldSide == front.getCounterClockWise() ? 4 : 5;
    }

    public static Direction world(Direction front, int index) {
        return switch (index) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> front;
            case 3 -> front.getOpposite();
            case 4 -> front.getCounterClockWise();
            case 5 -> front.getClockWise();
            default -> throw new IndexOutOfBoundsException("Relative face: " + index);
        };
    }
}
