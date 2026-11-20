package net.claustra01.clayium.machine.crucible;

import net.minecraft.util.StringRepresentable;

public enum QuartzCrucibleStage implements StringRepresentable {
    EMPTY("empty"),
    HEATING("heating"),
    READY("ready");

    private final String serializedName;

    QuartzCrucibleStage(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
