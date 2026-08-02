/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

public enum LogisticsKind {
    BUFFER,
    MULTITRACK_BUFFER,
    DISTRIBUTOR,
    STORAGE_CONTAINER,
    VOID_CONTAINER;

    public int slots(int tier) {
        return switch (this) {
            case BUFFER -> switch (tier) {
                case 4 -> 2;
                case 5 -> 6;
                case 6 -> 12;
                case 7 -> 20;
                case 8 -> 36;
                default -> 54;
            };
            case MULTITRACK_BUFFER -> switch (tier) {
                case 4 -> 2;
                case 5 -> 6;
                case 6 -> 16;
                case 7 -> 20;
                case 8 -> 36;
                default -> 54;
            };
            case DISTRIBUTOR -> switch (tier) {
                case 7 -> 16;
                case 8 -> 24;
                default -> 48;
            };
            case STORAGE_CONTAINER, VOID_CONTAINER -> 1;
        };
    }

    public int transferLimit(int tier) {
        if (this == DISTRIBUTOR) {
            return tier == 7 ? 64 : tier == 8 ? 128 : 512;
        }
        return switch (tier) {
            case 0, 1, 2, 3, 4 -> 1;
            case 5 -> 4;
            case 6 -> 16;
            case 7 -> 64;
            case 8 -> 128;
            case 9 -> 192;
            case 10 -> 256;
            case 11 -> 512;
            case 12 -> 1024;
            default -> 6400;
        };
    }

    public int tracks(int tier) {
        if (this != MULTITRACK_BUFFER) {
            return 0;
        }
        return switch (tier) {
            case 4 -> 2;
            case 5 -> 3;
            case 6 -> 4;
            case 7 -> 5;
            default -> 6;
        };
    }

    public int columns(int tier) {
        return switch (this) {
            case BUFFER -> switch (tier) {
                case 4 -> 2;
                case 5 -> 3;
                case 6 -> 4;
                case 7 -> 5;
                default -> 9;
            };
            case MULTITRACK_BUFFER -> Math.max(1, slots(tier) / tracks(tier));
            case DISTRIBUTOR -> Math.min(8, slots(tier));
            case STORAGE_CONTAINER -> 2;
            case VOID_CONTAINER -> 1;
        };
    }

    public int rows(int tier) {
        return switch (this) {
            case MULTITRACK_BUFFER -> tracks(tier);
            case STORAGE_CONTAINER, VOID_CONTAINER -> 1;
            default -> (slots(tier) + columns(tier) - 1) / columns(tier);
        };
    }
}
