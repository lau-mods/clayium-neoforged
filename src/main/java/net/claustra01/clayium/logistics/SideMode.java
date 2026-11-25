package net.claustra01.clayium.logistics;

/** A display-level projection of the independent insertion and extraction routes. */
public enum SideMode {
    DISABLED,
    INPUT,
    OUTPUT,
    INPUT_OUTPUT;

    public boolean allowsInsert() {
        return this == INPUT || this == INPUT_OUTPUT;
    }

    public boolean allowsExtract() {
        return this == OUTPUT || this == INPUT_OUTPUT;
    }
}
