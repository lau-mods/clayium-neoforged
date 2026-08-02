/* SPDX-License-Identifier: CC-BY-4.0 */
package net.claustra01.clayium.world.item;

import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.data.ClaySteelToolSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;
import java.util.List;

final class ClaySteelToolHelper {
    private static final ThreadLocal<Boolean> AREA_MINING = ThreadLocal.withInitial(() -> false);

    private ClaySteelToolHelper() {
    }

    static InteractionResult cycleMode(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            ItemStack stack = context.getItemInHand();
            ClaySteelToolSettings settings = stack.getOrDefault(
                    ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(), ClaySteelToolSettings.DEFAULT);
            if (settings.mode() == 2 && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.TERRACOTTA)
                    && context.getPlayer() != null && !context.getPlayer().isShiftKeyDown()) {
                Direction normal = context.getClickedFace();
                Direction horizontal = horizontalAxis(normal, context.getPlayer());
                Direction vertical = verticalAxis(normal, context.getPlayer());
                ArrayList<BlockPos> shape = new ArrayList<>();
                BlockPos origin = context.getClickedPos();
                for (BlockPos position : BlockPos.betweenClosed(origin.offset(-2, -2, -2), origin.offset(2, 2, 2))) {
                    if (context.getLevel().getBlockState(position).is(Blocks.TERRACOTTA)) {
                        BlockPos delta = position.subtract(origin);
                        shape.add(new BlockPos(dot(delta, horizontal), dot(delta, vertical), dot(delta, normal)));
                    }
                }
                stack.set(ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(),
                        new ClaySteelToolSettings(2, shape));
                context.getPlayer().displayClientMessage(
                        Component.translatable("item.clayium_neoforged.clay_steel_tool.customized", shape.size()), true);
                return InteractionResult.SUCCESS;
            }
            int mode = Math.floorMod(settings.mode() + 1, 3);
            stack.set(ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(), settings.withMode(mode));
            if (context.getPlayer() != null) {
                Component modeName = mode == 2 && !settings.customShape().isEmpty()
                        ? Component.translatable("item.clayium_neoforged.clay_steel_tool.mode.custom",
                                settings.customShape().size())
                        : Component.translatable("item.clayium_neoforged.clay_steel_tool.mode.area",
                                mode * 2 + 1, mode * 2 + 1);
                context.getPlayer().displayClientMessage(modeName, true);
            }
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    static void mineArea(ItemStack stack, Level level, BlockState originalState, BlockPos origin,
                         net.minecraft.world.entity.LivingEntity entity) {
        if (AREA_MINING.get() || level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        ClaySteelToolSettings settings = stack.getOrDefault(
                ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(), ClaySteelToolSettings.DEFAULT);
        int mode = settings.mode();
        int radius = Math.min(2, Math.max(0, mode));
        if (radius == 0) {
            return;
        }
        Direction normal = dominantDirection(player.getLookAngle());
        Direction horizontal = horizontalAxis(normal, player);
        Direction vertical = verticalAxis(normal, player);
        AREA_MINING.set(true);
        try {
            Iterable<BlockPos> offsets = mode == 2 && !settings.customShape().isEmpty()
                    ? settings.customShape()
                    : plane(radius);
            for (BlockPos offset : offsets) {
                if (offset.equals(BlockPos.ZERO)) continue;
                BlockPos target = origin.relative(horizontal, offset.getX())
                        .relative(vertical, offset.getY()).relative(normal, offset.getZ());
                BlockState targetState = level.getBlockState(target);
                if (!targetState.isAir()
                        && targetState.getDestroySpeed(level, target) >= 0
                        && stack.isCorrectToolForDrops(targetState)) {
                    player.gameMode.destroyBlock(target);
                }
            }
        } finally {
            AREA_MINING.set(false);
        }
    }

    static void addModeTooltip(ItemStack stack, java.util.List<Component> tooltip) {
        int mode = stack.getOrDefault(ClayiumDataComponents.CLAY_STEEL_TOOL_SETTINGS.get(),
                ClaySteelToolSettings.DEFAULT).mode();
        int size = mode * 2 + 1;
        tooltip.add(Component.translatable("item.clayium_neoforged.clay_steel_tool.area", size, size)
                .withStyle(ChatFormatting.GRAY));
    }

    private static Direction dominantDirection(net.minecraft.world.phys.Vec3 look) {
        return Direction.getNearest((float) look.x, (float) look.y, (float) look.z);
    }

    private static Direction horizontalAxis(Direction normal, net.minecraft.world.entity.player.Player player) {
        return normal.getAxis().isVertical() ? player.getDirection().getClockWise() : normal.getClockWise();
    }

    private static Direction verticalAxis(Direction normal, net.minecraft.world.entity.player.Player player) {
        return normal.getAxis().isVertical() ? player.getDirection() : Direction.UP;
    }

    private static int dot(BlockPos vector, Direction direction) {
        return vector.getX() * direction.getStepX()
                + vector.getY() * direction.getStepY()
                + vector.getZ() * direction.getStepZ();
    }

    private static List<BlockPos> plane(int radius) {
        ArrayList<BlockPos> offsets = new ArrayList<>();
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) offsets.add(new BlockPos(x, y, 0));
        }
        return offsets;
    }
}
