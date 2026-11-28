package net.claustra01.clayium.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.claustra01.clayium.ClayiumConfig;
import net.claustra01.clayium.ClayiumMod;
import net.claustra01.clayium.component.ClayiumDataComponentTypes.ClaySteelToolConfigData;
import net.claustra01.clayium.registry.ClayiumDataComponents;
import net.claustra01.clayium.util.ClayDirection.RelativeSide;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Shared AoE mining, custom clay-template capture, free clay placement, and BreakSpeed adjustment
 * for {@link ClaySteelPickaxeItem} and {@link ClaySteelShovelItem}, faithfully matching original
 * {@code UtilAdvancedTools} and {@code HarvestCoordClaySteelTools}.
 */
@EventBusSubscriber(modid = ClayiumMod.MOD_ID)
public final class ClaySteelToolHelper {
    private static final Map<UUID, Direction> LAST_HIT_SIDE = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> HARVESTING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ClaySteelToolHelper() {}

    public static boolean isClaySteelTool(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ClaySteelPickaxeItem || stack.getItem() instanceof ClaySteelShovelItem);
    }

    public static int getMaxRange() {
        return Math.max(0, ClayiumConfig.CLAY_STEEL_PICKAXE_RANGE.get());
    }

    public static ClaySteelToolConfigData getConfig(ItemStack stack) {
        ClaySteelToolConfigData data = stack.get(ClayiumDataComponents.CLAY_STEEL_TOOL_CONFIG.get());
        return data != null ? data : new ClaySteelToolConfigData(0, List.of());
    }

    public static void appendModeTooltip(ItemStack stack, List<Component> tooltip) {
        ClaySteelToolConfigData cfg = getConfig(stack);
        int maxRange = getMaxRange();
        if (cfg.mode() == maxRange && !cfg.customOffsets().isEmpty()) {
            tooltip.add(Component.literal("Mode: Custom (" + cfg.customOffsets().size() + ")").withStyle(ChatFormatting.GRAY));
        } else {
            int size = Math.min(cfg.mode(), maxRange) * 2 + 1;
            tooltip.add(Component.literal("Mode: " + cfg.mode() + " (" + size + "x" + size + ")").withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (player != null && event.getFace() != null && isClaySteelTool(player.getMainHandItem())) {
            LAST_HIT_SIDE.put(player.getUUID(), event.getFace());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_HIT_SIDE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack stack = player.getMainHandItem();
        if (!isClaySteelTool(stack)) {
            return;
        }
        Level level = player.level();
        BlockPos origin = event.getPosition().orElse(null);
        if (origin == null) {
            return;
        }
        Direction hitSide = resolveHittingSide(player);
        if (hitSide == null) {
            event.setNewSpeed(0.0F);
            return;
        }

        List<BlockPos> targets = getHarvestedPositions(stack, origin, player, hitSide);
        float reciprocalSum = 0.0F;
        for (BlockPos targetPos : targets) {
            BlockState targetState = level.getBlockState(targetPos);
            if (targetState.isAir()) {
                continue;
            }
            float hardness = targetState.getDestroySpeed(level, targetPos);
            if (hardness < 0.0F) {
                reciprocalSum = Float.POSITIVE_INFINITY;
                break;
            }
            float destroyProgress = targetState.getDestroyProgress(player, level, targetPos);
            float speed = destroyProgress * 30.0F;
            reciprocalSum += (speed <= 0.0F) ? Float.POSITIVE_INFINITY : (1.0F / speed);
        }

        if (reciprocalSum == 0.0F) {
            event.setNewSpeed(Float.POSITIVE_INFINITY);
        } else if (Float.isInfinite(reciprocalSum)) {
            event.setNewSpeed(0.0F);
        } else {
            event.setNewSpeed(1.0F / reciprocalSum);
        }
    }

    public static InteractionResult handleUseOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        ClaySteelToolConfigData cfg = getConfig(stack);
        int maxRange = getMaxRange();
        int mode = cfg.mode();

        if (!player.isShiftKeyDown()) {
            if (mode == maxRange && level.getBlockState(clickedPos).is(Blocks.CLAY)) {
                Vec3[] basis = getEigenVectors(player, context.getClickedFace());
                Vec3[] inv = getInverse(basis);
                if (inv != null) {
                    List<BlockPos> offsets = new ArrayList<>();
                    for (int dx = -maxRange; dx <= maxRange; dx++) {
                        for (int dy = -maxRange; dy <= maxRange; dy++) {
                            for (int dz = -maxRange; dz <= maxRange; dz++) {
                                if (level.getBlockState(clickedPos.offset(dx, dy, dz)).is(Blocks.CLAY)) {
                                    int xx = (int) (inv[0].x * dx + inv[1].x * dy + inv[2].x * dz);
                                    int yy = (int) (inv[0].y * dx + inv[1].y * dy + inv[2].y * dz);
                                    int zz = (int) (inv[0].z * dx + inv[1].z * dy + inv[2].z * dz);
                                    offsets.add(new BlockPos(xx, yy, zz));
                                }
                            }
                        }
                    }
                    stack.set(ClayiumDataComponents.CLAY_STEEL_TOOL_CONFIG.get(), new ClaySteelToolConfigData(mode, offsets));
                    if (!level.isClientSide) {
                        player.sendSystemMessage(Component.literal("Customized."));
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            int nextMode = (mode >= maxRange) ? 0 : mode + 1;
            stack.set(ClayiumDataComponents.CLAY_STEEL_TOOL_CONFIG.get(), new ClaySteelToolConfigData(nextMode, cfg.customOffsets()));
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("Set mode " + nextMode + "."));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (mode == maxRange) {
            BlockPos placePos = clickedPos.relative(context.getClickedFace());
            BlockPlaceContext placeCtx = new BlockPlaceContext(context);
            if (level.getBlockState(placePos).canBeReplaced(placeCtx)) {
                if (!level.isClientSide) {
                    level.setBlock(placePos, Blocks.CLAY.defaultBlockState(), Block.UPDATE_ALL);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public static void handleMineBlock(ItemStack stack, Level level, BlockPos pos, LivingEntity entity) {
        if (!(level instanceof ServerLevel serverLevel) || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (Boolean.TRUE.equals(HARVESTING.get())) {
            return;
        }
        Direction hitSide = resolveHittingSide(player);
        if (hitSide == null) {
            return;
        }

        List<BlockPos> targets = getHarvestedPositions(stack, pos, player, hitSide);
        HARVESTING.set(Boolean.TRUE);
        try {
            int extraBroken = 0;
            for (BlockPos targetPos : targets) {
                if (targetPos.equals(pos)) {
                    continue;
                }
                BlockState targetState = serverLevel.getBlockState(targetPos);
                if (targetState.isAir() || targetState.getDestroySpeed(serverLevel, targetPos) < 0.0F) {
                    continue;
                }
                BlockEntity be = serverLevel.getBlockEntity(targetPos);
                boolean canHarvest = player.hasCorrectToolForDrops(targetState, serverLevel, targetPos);
                if (serverLevel.removeBlock(targetPos, false)) {
                    if (canHarvest) {
                        Block.dropResources(targetState, serverLevel, targetPos, be, player, stack);
                    }
                    extraBroken++;
                }
            }
            if (extraBroken > 0 && !player.isCreative()) {
                stack.hurtAndBreak(extraBroken, serverLevel, player, item -> {});
            }
        } finally {
            HARVESTING.set(Boolean.FALSE);
        }
    }

    @Nullable
    public static Direction resolveHittingSide(Player player) {
        HitResult hit = player.pick(Math.max(5.0, player.blockInteractionRange()), 0.0F, false);
        if (hit instanceof BlockHitResult bhr && hit.getType() == HitResult.Type.BLOCK) {
            LAST_HIT_SIDE.put(player.getUUID(), bhr.getDirection());
            return bhr.getDirection();
        }
        return LAST_HIT_SIDE.get(player.getUUID());
    }

    public static List<BlockPos> getHarvestedPositions(ItemStack stack, BlockPos origin, Player player, Direction side) {
        Vec3[] basis = getEigenVectors(player, side);
        return getHarvestedPositions(stack, origin, basis[0], basis[1], basis[2], getMaxRange());
    }

    public static List<BlockPos> getHarvestedPositions(
            ItemStack stack,
            BlockPos origin,
            Vec3 xxVector,
            Vec3 yyVector,
            Vec3 zzVector,
            int maxRange) {
        ClaySteelToolConfigData cfg = getConfig(stack);
        int mode = cfg.mode();
        List<BlockPos> res = new ArrayList<>();
        if (mode == maxRange && !cfg.customOffsets().isEmpty()) {
            for (BlockPos offset : cfg.customOffsets()) {
                res.add(transformOffset(origin, offset.getX(), offset.getY(), offset.getZ(), xxVector, yyVector, zzVector));
            }
            return res;
        }
        int size = Math.min(mode, maxRange);
        for (int yy = -size; yy <= size; yy++) {
            for (int xx = -size; xx <= size; xx++) {
                res.add(transformOffset(origin, xx, yy, 0, xxVector, yyVector, zzVector));
            }
        }
        return res;
    }

    private static BlockPos transformOffset(BlockPos origin, int xx, int yy, int zz, Vec3 xxVec, Vec3 yyVec, Vec3 zzVec) {
        int x = origin.getX() + (int) Math.round(xxVec.x * xx + yyVec.x * yy + zzVec.x * zz);
        int y = origin.getY() + (int) Math.round(xxVec.y * xx + yyVec.y * yy + zzVec.y * zz);
        int z = origin.getZ() + (int) Math.round(xxVec.z * xx + yyVec.z * yy + zzVec.z * zz);
        return new BlockPos(x, y, z);
    }

    public static Vec3[] getEigenVectors(Player player, Direction clickedSide) {
        Direction xxVector;
        Direction yyVector = Direction.UP;
        Direction zzVector;
        if (clickedSide != Direction.UP && clickedSide != Direction.DOWN) {
            zzVector = clickedSide;
            xxVector = sideOfDirection(clickedSide, RelativeSide.LEFT);
        } else {
            zzVector = Direction.UP;
            float f1 = Mth.cos(-player.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI);
            float f2 = Mth.sin(-player.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI);
            double rootHalf = Math.sqrt(0.5);
            if (f1 >= rootHalf) {
                yyVector = Direction.NORTH;
            }
            if (f1 <= -rootHalf) {
                yyVector = Direction.SOUTH;
            }
            if (f2 >= rootHalf) {
                yyVector = Direction.WEST;
            }
            if (f2 <= -rootHalf) {
                yyVector = Direction.EAST;
            }
            xxVector = sideOfDirection(yyVector, RelativeSide.RIGHT);
            if (clickedSide == Direction.DOWN) {
                yyVector = yyVector.getOpposite();
                zzVector = zzVector.getOpposite();
            }
        }
        return new Vec3[]{toVec3(xxVector), toVec3(yyVector), toVec3(zzVector)};
    }

    @Nullable
    public static Vec3[] getInverse(Vec3[] v) {
        double d = v[0].x * v[1].y * v[2].z
                + v[1].x * v[2].y * v[0].z
                + v[2].x * v[0].y * v[1].z
                - v[0].x * v[2].y * v[1].z
                - v[1].x * v[0].y * v[2].z
                - v[2].x * v[1].y * v[0].z;
        if (d == 0.0) {
            return null;
        }
        return new Vec3[]{
                new Vec3(
                        (v[1].y * v[2].z - v[2].y * v[1].z) / d,
                        (v[2].y * v[0].z - v[0].y * v[2].z) / d,
                        (v[0].y * v[1].z - v[1].y * v[0].z) / d),
                new Vec3(
                        (v[1].z * v[2].x - v[2].z * v[1].x) / d,
                        (v[2].z * v[0].x - v[0].z * v[2].x) / d,
                        (v[0].z * v[1].x - v[1].z * v[0].x) / d),
                new Vec3(
                        (v[1].x * v[2].y - v[2].x * v[1].y) / d,
                        (v[2].x * v[0].y - v[0].x * v[2].y) / d,
                        (v[0].x * v[1].y - v[1].x * v[0].y) / d)
        };
    }

    private static Direction sideOfDirection(Direction front, RelativeSide side) {
        if (side == RelativeSide.LEFT) {
            return front.getClockWise();
        }
        if (side == RelativeSide.RIGHT) {
            return front.getCounterClockWise();
        }
        return front;
    }

    private static Vec3 toVec3(Direction dir) {
        Vec3i n = dir.getNormal();
        return new Vec3(n.getX(), n.getY(), n.getZ());
    }
}
