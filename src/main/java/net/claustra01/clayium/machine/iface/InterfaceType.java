package net.claustra01.clayium.machine.iface;

public enum InterfaceType {
    ITEM("clay_interface", "interface"),
    REDSTONE("redstone_interface", "redstoneinterface"),
    LASER("clay_laser_interface", "laserinterface");

    private final String idStem;
    private final String texture;

    InterfaceType(String idStem, String texture) {
        this.idStem = idStem;
        this.texture = texture;
    }

    public String idStem() {
        return idStem;
    }

    public String texture() {
        return texture;
    }
}
