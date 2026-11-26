package net.claustra01.clayium.multiblock.careactor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class CaReactorStructureNotifier {
    private static final List<WeakReference<CaReactorCoreBlockEntity>> CORES = new ArrayList<>();

    static void track(CaReactorCoreBlockEntity core) {
        for (WeakReference<CaReactorCoreBlockEntity> reference : CORES) {
            if (reference.get() == core) {
                return;
            }
        }
        CORES.add(new WeakReference<>(core));
    }

    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof Level level) {
            componentChanged(level, event.getPos());
        }
    }

    public static void componentChanged(Level level, BlockPos changedPos) {
        if (level.isClientSide) {
            return;
        }
        CORES.removeIf(reference -> reference.get() == null || reference.get().isRemoved());
        for (WeakReference<CaReactorCoreBlockEntity> reference : CORES) {
            CaReactorCoreBlockEntity core = reference.get();
            if (core != null && core.getLevel() == level && core.observes(changedPos)) {
                core.markStructureDirty();
            }
        }
    }

    private CaReactorStructureNotifier() {
    }
}
