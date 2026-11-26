package net.claustra01.clayium.logistics;

/**
 * Layout of the original ContainerNormalInventory / GuiMultipageContainer (GUI 11):
 * machine area {@code rows*18+18} (minus 20 above six rows), width widened by 18 per column
 * past nine, slots centred at {@code (W - 18*columns)/2 + 1}, first row at y=18 (y=6 above six rows).
 */
public record NormalInventoryLayout(int columns, int rows, int pages) {
    public static final int BUTTON_PREVIOUS = 6;
    public static final int BUTTON_NEXT = 7;

    public int machineWidth() {
        return 176 + Math.max(columns - 9, 0) * 18 + (rows > 6 ? 8 : 0);
    }

    public int machineHeight() {
        return rows * 18 + 18 - (rows > 6 ? 20 : 0);
    }

    public int imageHeight() {
        return machineHeight() + 94;
    }

    public int playerX() {
        return (machineWidth() - 176) / 2;
    }

    public int slotX(int column) {
        return column * 18 + (machineWidth() - 18 * columns) / 2 + 1;
    }

    public int slotY(int row) {
        return row * 18 + (rows > 6 ? 6 : 18);
    }

    public boolean drawNames() {
        return rows <= 6;
    }
}
