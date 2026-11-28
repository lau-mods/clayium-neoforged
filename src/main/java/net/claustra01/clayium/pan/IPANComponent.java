package net.claustra01.clayium.pan;

import javax.annotation.Nullable;
import net.claustra01.clayium.blockentity.IPANCoreAccess;

/**
 * Implemented by PAN network consumer components (such as PAN Duplicator)
 * that periodically receive a reference to a connected PAN Core.
 */
public interface IPANComponent {
    void setPANCore(@Nullable IPANCoreAccess core, int remainingTicks);
}
