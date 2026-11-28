package net.claustra01.clayium.pan;

import java.util.List;

/**
 * Provides conversion rules supplied by a connected PAN Adapter or similar block entity.
 */
public interface IPANConversionProvider {
    List<PANConversion> getConversions();
}
