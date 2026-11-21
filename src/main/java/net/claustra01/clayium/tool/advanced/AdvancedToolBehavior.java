package net.claustra01.clayium.tool.advanced;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.registry.AdvancedToolRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class AdvancedToolBehavior {
    public static InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        int maximumMode = ClayiumConfig.CLAY_STEEL_PICKAXE_RANGE.get();
        int mode = Math.min(stack.getOrDefault(AdvancedToolRegistration.MODE.get(), 0), maximumMode);
        if (!level.isClientSide() && mode == maximumMode && level.getBlockState(context.getClickedPos()).is(Blocks.CLAY)) {
            AdvancedToolPattern pattern = capturePattern(
                    level, context.getClickedPos(), context.getClickedFace(), player, maximumMode);
            stack.set(AdvancedToolRegistration.PATTERN.get(), pattern);
            player.displayClientMessage(
                    Component.translatable("message.clayium.advanced_tool.pattern_saved", pattern.offsets().size()), true);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            int nextMode = mode >= maximumMode ? 0 : mode + 1;
            stack.set(AdvancedToolRegistration.MODE.get(), nextMode);
            player.displayClientMessage(Component.translatable(
                    "message.clayium.advanced_tool.mode", modeName(nextMode, maximumMode)), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static AdvancedToolPattern capturePattern(
            Level level, BlockPos origin, Direction face, Player player, int radius) {
        Basis basis = Basis.forFace(face, player.getDirection());
        List<BlockPos> offsets = new ArrayList<>();
        for (BlockPos position : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                origin.offset(radius, radius, radius))) {
            if (level.isLoaded(position) && level.getBlockState(position).is(Blocks.CLAY)) {
                BlockPos delta = position.subtract(origin);
                offsets.add(new BlockPos(
                        dot(delta, basis.horizontal()),
                        dot(delta, basis.vertical()),
                        dot(delta, basis.normal())));
            }
        }
        offsets.sort(Comparator.comparingInt((BlockPos position) -> position.getY())
                .thenComparingInt(position -> position.getX())
                .thenComparingInt(position -> position.getZ()));
        return new AdvancedToolPattern(offsets);
    }

    public static List<BlockPos> targets(
            ItemStack stack, BlockPos origin, Direction face, Direction playerDirection) {
        int maximumMode = ClayiumConfig.CLAY_STEEL_PICKAXE_RANGE.get();
        int mode = Math.min(stack.getOrDefault(AdvancedToolRegistration.MODE.get(), 0), maximumMode);
        Basis basis = Basis.forFace(face, playerDirection);
        List<BlockPos> localOffsets;
        if (mode == maximumMode) {
            localOffsets = stack.getOrDefault(
                    AdvancedToolRegistration.PATTERN.get(), AdvancedToolPattern.EMPTY).offsets();
        } else {
            localOffsets = new ArrayList<>((mode * 2 + 1) * (mode * 2 + 1));
            for (int vertical = -mode; vertical <= mode; vertical++) {
                for (int horizontal = -mode; horizontal <= mode; horizontal++) {
                    localOffsets.add(new BlockPos(horizontal, vertical, 0));
                }
            }
        }
        return localOffsets.stream()
                .map(local -> origin
                        .relative(basis.horizontal(), local.getX())
                        .relative(basis.vertical(), local.getY())
                        .relative(basis.normal(), local.getZ()))
                .distinct()
                .toList();
    }

    public static void addTooltip(ItemStack stack, List<Component> tooltip) {
        Integer tier = stack.get(net.claustra01.clayium.registry.FoundationDataComponentRegistration.TIER.get());
        if (tier != null) {
            tooltip.add(Component.translatable("tooltip.clayium.tier", tier));
        }
        int maximumMode = ClayiumConfig.CLAY_STEEL_PICKAXE_RANGE.get();
        int mode = Math.min(stack.getOrDefault(AdvancedToolRegistration.MODE.get(), 0), maximumMode);
        tooltip.add(Component.translatable(
                "tooltip.clayium.advanced_tool.mode", modeName(mode, maximumMode)).withStyle(ChatFormatting.AQUA));
        if (mode == maximumMode) {
            int size = stack.getOrDefault(
                    AdvancedToolRegistration.PATTERN.get(), AdvancedToolPattern.EMPTY).offsets().size();
            tooltip.add(Component.translatable("tooltip.clayium.advanced_tool.pattern", size)
                    .withStyle(ChatFormatting.GRAY));
        }
        addDurability(stack, tooltip);
        tooltip.add(Component.translatable("tooltip.clayium.advanced_tool.mode_change")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.clayium.advanced_tool.pattern_help")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static void addDurability(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable(
                "tooltip.clayium.advanced_tool.durability",
                stack.getMaxDamage() - stack.getDamageValue(), stack.getMaxDamage())
                .withStyle(ChatFormatting.GRAY));
    }

    private static Component modeName(int mode, int maximumMode) {
        if (mode == 0 && maximumMode != 0) {
            return Component.translatable("tooltip.clayium.advanced_tool.mode.single");
        }
        if (mode == maximumMode) {
            return Component.translatable("tooltip.clayium.advanced_tool.mode.remembered");
        }
        return Component.translatable("tooltip.clayium.advanced_tool.mode.area", mode * 2 + 1, mode * 2 + 1);
    }

    private static int dot(BlockPos position, Direction direction) {
        return position.getX() * direction.getStepX()
                + position.getY() * direction.getStepY()
                + position.getZ() * direction.getStepZ();
    }

    private record Basis(Direction horizontal, Direction vertical, Direction normal) {
        private static Basis forFace(Direction face, Direction playerDirection) {
            if (face.getAxis() == Direction.Axis.Y) {
                Direction vertical = face == Direction.UP ? playerDirection : playerDirection.getOpposite();
                return new Basis(vertical.getClockWise(), vertical, face);
            }
            return new Basis(face.getCounterClockWise(), Direction.UP, face);
        }
    }

    private AdvancedToolBehavior() {
    }
}
