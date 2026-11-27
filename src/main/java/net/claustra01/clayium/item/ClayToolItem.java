package net.claustra01.clayium.item;

import javax.annotation.Nullable;

/**
 * Utility tools (Wrench, Shovels, Pickaxes, Piping/IO/Memory tools, Synchronizer, Direction Memory).
 * Full active behaviors are implemented in Step 08/11.
 */
public class ClayToolItem extends SimpleTieredItem {
    public enum ToolKind {
        CLAY_WRENCH,
        CLAY_SHOVEL,
        CLAY_PICKAXE,
        CLAY_STEEL_PICKAXE,
        CLAY_STEEL_SHOVEL,
        IO_CONFIGURATOR,
        PIPING_CONFIGURATOR,
        MEMORY_CARD,
        SYNCHRONIZER,
        DIRECTION_MEMORY
    }

    private final ToolKind toolKind;

    public ClayToolItem(Properties properties, @Nullable Integer tier, String iconName, ToolKind toolKind) {
        super(properties, tier, iconName);
        this.toolKind = toolKind;
    }

    public ToolKind getToolKind() {
        return this.toolKind;
    }
}
