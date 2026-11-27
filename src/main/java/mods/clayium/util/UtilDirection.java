package mods.clayium.util;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public enum UtilDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0),
    DOWNSIDE(0, -1, 0, 1),
    UPSIDE(0, 1, 0, 1),
    FRONTSIDE(0, 0, -1, 1),
    BACKSIDE(0, 0, 1, 1),
    LEFTSIDE(-1, 0, 0, 1),
    RIGHTSIDE(1, 0, 0, 1),
    UNKNOWN(0, 0, 0);

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final int flag;
    public final int sideFlag;
    public static final UtilDirection[] VALID_DIRECTIONS = new UtilDirection[]{
        DOWN, UP, NORTH, SOUTH, WEST, EAST, DOWNSIDE, UPSIDE, FRONTSIDE, BACKSIDE, LEFTSIDE, RIGHTSIDE
    };
    public static final int[] OPPOSITES = new int[]{1, 0, 3, 2, 5, 4};
    public static final int[] LEFTSIDES = new int[]{4, 4, 4, 5, 3, 2};
    public static final int[] RIGHTSIDES = new int[]{5, 5, 5, 4, 2, 3};
    public static final int[] UPSIDES = new int[]{2, 3, 1, 1, 1, 1};
    public static final int[] DOWNSIDES = new int[]{3, 2, 0, 0, 0, 0};
    public static final int[][] ROTATION_MATRIX = new int[][]{
        {0, 1, 4, 5, 3, 2, 6},
        {0, 1, 5, 4, 2, 3, 6},
        {5, 4, 2, 3, 0, 1, 6},
        {4, 5, 2, 3, 1, 0, 6},
        {2, 3, 1, 0, 4, 5, 6},
        {3, 2, 0, 1, 4, 5, 6},
        {0, 1, 2, 3, 4, 5, 6}
    };
    private static volatile int[][] side2DirectionMap;
    private static volatile int[][] direction2SideMap;

    private UtilDirection(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.flag = 1 << this.ordinal();
        this.sideFlag = 0;
    }

    private UtilDirection(int x, int y, int z, int s) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        this.flag = 1 << this.ordinal();
        this.sideFlag = s;
    }

    public static UtilDirection getOrientation(int id) {
        return id >= 0 && id < VALID_DIRECTIONS.length ? VALID_DIRECTIONS[id] : UNKNOWN;
    }

    public static UtilDirection getSide(int id) {
        if (id >= 0 && id < 6) {
            return VALID_DIRECTIONS[id + 6];
        } else {
            return id >= 6 && id < 12 ? VALID_DIRECTIONS[id] : UNKNOWN;
        }
    }

    public boolean isDirection() {
        return this != UNKNOWN && this.sideFlag == 0;
    }

    public boolean isSide() {
        return this.sideFlag == 1;
    }

    public UtilDirection getOpposite() {
        return this.isSide() ? getSide(OPPOSITES[this.ordinal() - 6]) : getOrientation(OPPOSITES[this.ordinal()]);
    }

    public UtilDirection getRotation(UtilDirection axis) {
        if ((!axis.isDirection() || !this.isSide()) && (!this.isDirection() || !axis.isSide()) && axis != UNKNOWN && this != UNKNOWN) {
            return this.isSide()
                ? getSide(ROTATION_MATRIX[axis.ordinal() - 6][this.ordinal() - 6])
                : getOrientation(ROTATION_MATRIX[axis.ordinal()][this.ordinal()]);
        } else {
            return UNKNOWN;
        }
    }

    public UtilDirection getSideOfDirection(UtilDirection side) {
        if (side.isDirection()) {
            if (this == side) {
                return FRONTSIDE;
            }

            if (getOrientation(DOWNSIDES[this.ordinal()]) == side) {
                return DOWNSIDE;
            }

            if (getOrientation(UPSIDES[this.ordinal()]) == side) {
                return UPSIDE;
            }

            if (getOrientation(LEFTSIDES[this.ordinal()]) == side) {
                return LEFTSIDE;
            }

            if (getOrientation(RIGHTSIDES[this.ordinal()]) == side) {
                return RIGHTSIDE;
            }

            if (getOrientation(OPPOSITES[this.ordinal()]) == side) {
                return BACKSIDE;
            }
        }

        int i = this.isDirection() ? this.ordinal() : this.ordinal() - 6;
        switch (side) {
            case DOWNSIDE:
                return this.isDirection() ? getOrientation(DOWNSIDES[i]) : getSide(DOWNSIDES[i]);
            case UPSIDE:
                return this.isDirection() ? getOrientation(UPSIDES[i]) : getSide(UPSIDES[i]);
            case FRONTSIDE:
                return this.isDirection() ? getOrientation(i) : getSide(i);
            case BACKSIDE:
                return this.isDirection() ? getOrientation(OPPOSITES[i]) : getSide(OPPOSITES[i]);
            case LEFTSIDE:
                return this.isDirection() ? getOrientation(LEFTSIDES[i]) : getSide(LEFTSIDES[i]);
            case RIGHTSIDE:
                return this.isDirection() ? getOrientation(RIGHTSIDES[i]) : getSide(RIGHTSIDES[i]);
            default:
                return UNKNOWN;
        }
    }

    public UtilDirection getSide(UtilDirection side) {
        return this.getSideOfDirection(side);
    }

    public static synchronized int side2Direction(int frontDirection, int side) {
        if (side2DirectionMap == null) {
            side2DirectionMap = new int[12][12];

            for (int i = 0; i < side2DirectionMap.length; i++) {
                for (int j = 0; j < side2DirectionMap[i].length; j++) {
                    side2DirectionMap[i][j] = -1;
                }
            }
        }

        if (frontDirection < 12 && side < 12 && frontDirection >= 0 && side >= 0) {
            if (side2DirectionMap[frontDirection][side] == -1) {
                side2DirectionMap[frontDirection][side] = getOrientation(frontDirection).getSideOfDirection(getSide(side)).ordinal();
            }

            return side2DirectionMap[frontDirection][side];
        } else {
            return getOrientation(frontDirection).getSideOfDirection(getSide(side)).ordinal();
        }
    }

    public static synchronized int direction2Side(int frontDirection, int theDirection) {
        if (direction2SideMap == null) {
            direction2SideMap = new int[12][12];

            for (int i = 0; i < direction2SideMap.length; i++) {
                for (int j = 0; j < direction2SideMap[i].length; j++) {
                    direction2SideMap[i][j] = -1;
                }
            }
        }

        if (frontDirection < 12 && theDirection < 12 && frontDirection >= 0 && theDirection >= 0) {
            if (direction2SideMap[frontDirection][theDirection] == -1) {
                direction2SideMap[frontDirection][theDirection] = getOrientation(frontDirection).getSideOfDirection(getOrientation(theDirection)).ordinal();
            }

            return direction2SideMap[frontDirection][theDirection];
        } else {
            return getOrientation(frontDirection).getSideOfDirection(getOrientation(theDirection)).ordinal();
        }
    }

    public ForgeDirection toForgeDirection() {
        return ForgeDirection.getOrientation(this.ordinal());
    }

    public Vec3 toVec3() {
        return Vec3.func_72443_a((double)this.offsetX, (double)this.offsetY, (double)this.offsetZ);
    }

    public Block getBlock(IBlockAccess world, int x, int y, int z) {
        return world.func_147439_a(x + this.offsetX, y + this.offsetY, z + this.offsetZ);
    }

    public static Block getBlock(IBlockAccess world, int x, int y, int z, UtilDirection direction) {
        return direction.getBlock(world, x, y, z);
    }

    public static Block getBlock(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
        return world.func_147439_a(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    public int getBlockMetadata(IBlockAccess world, int x, int y, int z) {
        return world.func_72805_g(x + this.offsetX, y + this.offsetY, z + this.offsetZ);
    }

    public static int getBlockMetadata(IBlockAccess world, int x, int y, int z, UtilDirection direction) {
        return direction.getBlockMetadata(world, x, y, z);
    }

    public static int getBlockMetadata(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
        return world.func_72805_g(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    public TileEntity getTileEntity(IBlockAccess world, int x, int y, int z) {
        return UtilBuilder.safeGetTileEntity(world, x + this.offsetX, y + this.offsetY, z + this.offsetZ);
    }

    public static TileEntity getTileEntity(IBlockAccess world, int x, int y, int z, UtilDirection direction) {
        return direction.getTileEntity(world, x, y, z);
    }

    public static TileEntity getTileEntity(IBlockAccess world, int x, int y, int z, ForgeDirection direction) {
        return UtilBuilder.safeGetTileEntity(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }
}
