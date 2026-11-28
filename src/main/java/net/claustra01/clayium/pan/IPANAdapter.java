package net.claustra01.clayium.pan;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Context exposed to {@link IPANAdapterConversionFactory} implementations for resolving one page of a PAN Adapter.
 */
public interface IPANAdapter {
    List<ItemStack> getPatternItems();

    List<ItemStack> getSubItems();

    @Nullable
    Level getConnectedLevel();

    BlockPos getConnectedPos();
}
