/*
 * SPDX-License-Identifier: CC-BY-4.0
 */
package net.claustra01.clayium.logistics;

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

    public SideMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
