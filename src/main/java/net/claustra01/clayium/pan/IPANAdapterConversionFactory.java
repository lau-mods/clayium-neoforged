package net.claustra01.clayium.pan;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Factory translating an adjacent machine, crafting table, or furnace into a {@link PANConversion}.
 */
public interface IPANAdapterConversionFactory {
    boolean matches(Level level, BlockPos pos);

    @Nullable
    PANConversion getConversion(IPANAdapter adapter);
}
