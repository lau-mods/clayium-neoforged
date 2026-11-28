package net.claustra01.clayium.item;

import javax.annotation.Nullable;
import net.claustra01.clayium.block.ClayContainerBlock;
import net.claustra01.clayium.blockentity.IRayTracer;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.DirectionMemoryData;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Utility tools (Wrench, Shovels, Pickaxes, Piping/IO/Memory tools, Synchronizer, Direction Memory).
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

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return switch (this.toolKind) {
            case IO_CONFIGURATOR, PIPING_CONFIGURATOR, MEMORY_CARD, SYNCHRONIZER, DIRECTION_MEMORY -> true;
            default -> super.doesSneakBypassUse(stack, level, pos, player);
        };
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (this.toolKind == ToolKind.DIRECTION_MEMORY) {
            return handleDirectionMemory(stack, context);
        }
        if (this.toolKind == ToolKind.CLAY_WRENCH
                || (this.toolKind == ToolKind.PIPING_CONFIGURATOR && context.isSecondaryUseActive())) {
            Level level = context.getLevel();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof ClayContainerBlock)) {
                BlockState rotated = state.rotate(level, pos, Rotation.CLOCKWISE_90);
                if (rotated != state) {
                    if (!level.isClientSide) {
                        level.setBlock(pos, rotated, Block.UPDATE_ALL);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return super.onItemUseFirst(stack, context);
    }

    private static InteractionResult handleDirectionMemory(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        DirectionMemoryData stored = stack.get(ClayiumDataComponents.DIRECTION_MEMORY.get());
        if (stored != null && be instanceof IRayTracer rayTracer && rayTracer.acceptRayTraceMemory(stored)) {
            rayTracer.applyRayTraceMemory(stored);
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("Applied direction memory."));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 hit = context.getClickLocation();
        DirectionMemoryData captured = new DirectionMemoryData(
                eye.x, eye.y, eye.z,
                hit.x, hit.y, hit.z,
                context.getClickedFace()
        );
        stack.set(ClayiumDataComponents.DIRECTION_MEMORY.get(), captured);
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.literal("Saved your direction to memory."));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
