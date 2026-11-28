package net.claustra01.clayium.blockentity;

import javax.annotation.Nullable;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Pure-data resolver for side IO / filter overlay texture names matching 1.7.10 {@code ClayContainer.registerIOIcons}.
 * Returns texture stems under {@code textures/block/<stem>.png}.
 */
public final class ClayContainerIoIcons {
    private static final String[] DEFAULT_INSERT = {"import"};
    private static final String[] DEFAULT_EXTRACT = {"export"};
    private static final String[] MACHINE_INSERT = {"import", "import_energy"};
    private static final String[] ASSEMBLER_INSERT = {"import_1", "import_2", "import", "import_energy"};
    private static final String[] CHEMICAL_EXTRACT = {"export_1", "export_2", "export"};
    private static final String[] COLLECTOR_INSERT = {"import_energy"};
    private static final String[] MINER_INSERT = {"import_energy", "import"};
    private static final String[] FLUID_INSERT = {"import_l", "import"};
    private static final String[] FLUID_EXTRACT = {"export_l", "export"};
    private static final String[] MULTITRACK_INSERT = {
            "import_m0", "import_m1", "import_m2", "import_m3", "import_m4", "import_m5", "import_m6"
    };
    private static final String[] MULTITRACK_EXTRACT = {
            "export_m0", "export_m1", "export_m2", "export_m3", "export_m4", "export_m5", "export_m6"
    };
    private static final String[] RF_EXTRACT = {"export_rf"};
    private static final String[] EMPTY = {};

    private ClayContainerIoIcons() {}

    public static String[] getInsertIcons(ClayContainerBlockEntity container) {
        if (container instanceof ClayInterfaceBlockEntity iface) {
            BlockEntity core = iface.getCore();
            if (core instanceof ClayContainerBlockEntity coreContainer && coreContainer != container) {
                return getInsertIcons(coreContainer);
            }
            return DEFAULT_INSERT;
        }
        if (container instanceof AutoTraderBlockEntity
                || container instanceof AssemblerBlockEntity
                || container instanceof CAInjectorBlockEntity
                || container instanceof PANDuplicatorBlockEntity
                || container instanceof BlastFurnaceBlockEntity
                || container instanceof ChemicalReactorBlockEntity
                || container instanceof ReactorBlockEntity) {
            return ASSEMBLER_INSERT;
        }
        if (container instanceof FluidTranslatorBlockEntity) {
            return FLUID_INSERT;
        }
        if (container instanceof MultitrackBufferBlockEntity) {
            return MULTITRACK_INSERT;
        }
        if (container instanceof SolarClayFabricatorBlockEntity
                || container instanceof ClayFabricatorBlockEntity
                || container instanceof StorageContainerBlockEntity
                || container instanceof VacuumContainerBlockEntity) {
            return DEFAULT_INSERT;
        }
        if (container instanceof SaltExtractorBlockEntity
                || container instanceof CACollectorBlockEntity
                || container instanceof ClayEnergyLaserBlockEntity
                || container instanceof AreaCollectorBlockEntity) {
            return COLLECTOR_INSERT;
        }
        if (container instanceof AreaActivatorBlockEntity) {
            return MACHINE_INSERT;
        }
        if (container instanceof AreaMinerBlockEntity) {
            return MINER_INSERT;
        }
        if (container instanceof TieredClayContainerBlockEntity) {
            return MACHINE_INSERT;
        }
        return DEFAULT_INSERT;
    }

    public static String[] getExtractIcons(ClayContainerBlockEntity container) {
        if (container instanceof ClayInterfaceBlockEntity iface) {
            BlockEntity core = iface.getCore();
            if (core instanceof ClayContainerBlockEntity coreContainer && coreContainer != container) {
                return getExtractIcons(coreContainer);
            }
            return DEFAULT_EXTRACT;
        }
        if (container instanceof BlastFurnaceBlockEntity
                || container instanceof ChemicalReactorBlockEntity
                || container instanceof ReactorBlockEntity) {
            return CHEMICAL_EXTRACT;
        }
        if (container instanceof FluidTranslatorBlockEntity) {
            return FLUID_EXTRACT;
        }
        if (container instanceof MultitrackBufferBlockEntity) {
            return MULTITRACK_EXTRACT;
        }
        if ("rf_generator".equals(ClayContainerBlock.resolveBlockEntityCategory(container.getBlockState().getBlock()))) {
            return RF_EXTRACT;
        }
        if (container instanceof ClayEnergyLaserBlockEntity) {
            return EMPTY;
        }
        return DEFAULT_EXTRACT;
    }

    @Nullable
    public static String getInsertIconName(ClayContainerBlockEntity container, RelativeSide side, boolean pipeMode) {
        int route = container.getInsertRoute(side);
        if (route < 0) {
            return null;
        }
        String[] icons = getInsertIcons(container);
        if (route >= icons.length) {
            return null;
        }
        return pipeMode ? icons[route] + "_p" : icons[route];
    }

    @Nullable
    public static String getExtractIconName(ClayContainerBlockEntity container, RelativeSide side, boolean pipeMode) {
        int route = container.getExtractRoute(side);
        if (route < 0) {
            return null;
        }
        String[] icons = getExtractIcons(container);
        if (route >= icons.length) {
            return null;
        }
        return pipeMode ? icons[route] + "_p" : icons[route];
    }

    @Nullable
    public static String getFilterIconName(ClayContainerBlockEntity container, RelativeSide side) {
        ItemStack filter = container.getFilter(side);
        return filter.isEmpty() ? null : "filter";
    }
}
