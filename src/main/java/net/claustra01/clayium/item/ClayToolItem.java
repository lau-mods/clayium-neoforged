package net.claustra01.clayium.item;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * Utility tools (Wrench, Shovels, Pickaxes, Piping/IO/Memory tools, Synchronizer, Direction Memory).
 * Full active behaviors are implemented in Step 08/11.
 */
public class ClayToolItem extends SimpleTieredItem {
    public static Tier tierWithDurability(Tier base, int uses) {
        return new Tier() {
            @Override
            public int getUses() {
                return uses;
            }

            @Override
            public float getSpeed() {
                return base.getSpeed();
            }

            @Override
            public float getAttackDamageBonus() {
                return base.getAttackDamageBonus();
            }

            @Override
            public TagKey<Block> getIncorrectBlocksForDrops() {
                return base.getIncorrectBlocksForDrops();
            }

            @Override
            public int getEnchantmentValue() {
                return base.getEnchantmentValue();
            }

            @Override
            public Ingredient getRepairIngredient() {
                return base.getRepairIngredient();
            }
        };
    }
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
