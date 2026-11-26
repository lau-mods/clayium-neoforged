package net.claustra01.clayium.world.operation;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;

public final class BlockOperationService {
    private static final String PROFILE_NAME = "[Clayium]";

    public static BlockOperationResult breakBlock(
            ServerLevel level,
            BlockPos machinePosition,
            BlockPos target,
            UUID owner,
            ItemStack tool,
            Consumer<ItemStack> updateTool,
            Predicate<List<ItemStack>> canStore,
            Consumer<List<ItemStack>> store) {
        if (!level.isLoaded(target)) {
            return BlockOperationResult.UNLOADED;
        }
        BlockState state = level.getBlockState(target);
        if (state.isAir()) {
            return BlockOperationResult.NO_TARGET;
        }

        FakePlayer player = player(level, machinePosition, target, owner, tool);
        if (state.getDestroySpeed(level, target) < 0
                || state.getBlock() instanceof GameMasterBlock && !player.canUseGameMasterBlocks()
                || player.blockActionRestricted(level, target, GameType.SURVIVAL)
                || !level.mayInteract(player, target)) {
            clearPlayer(player);
            return BlockOperationResult.DENIED;
        }
        if (CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, target, state).isCanceled()) {
            clearPlayer(player);
            return BlockOperationResult.DENIED;
        }

        BlockEntity blockEntity = level.getBlockEntity(target);
        ItemStack lootTool = player.getMainHandItem().copy();
        List<ItemStack> drops = new ArrayList<>();
        boolean canHarvest = state.canHarvestBlock(level, target, player);
        if (canHarvest) {
            drops.addAll(Block.getDrops(state, level, target, blockEntity, player, lootTool));
        }
        if (!canStore.test(List.copyOf(drops))) {
            clearPlayer(player);
            return BlockOperationResult.NO_OUTPUT_SPACE;
        }

        BlockState destroyedState = state.getBlock().playerWillDestroy(level, target, state, player);
        ItemStack usedTool = player.getMainHandItem();
        usedTool.mineBlock(level, destroyedState, target, player);
        boolean removed = destroyedState.onDestroyedByPlayer(
                level, target, player, canHarvest, level.getFluidState(target));
        if (!removed) {
            clearPlayer(player);
            return BlockOperationResult.FAILED;
        }
        destroyedState.getBlock().destroy(level, target, destroyedState);
        if (canHarvest) {
            destroyedState.spawnAfterBreak(level, target, lootTool, true);
            List<ItemEntity> dropEntities = drops.stream()
                    .map(stack -> new ItemEntity(level, target.getX() + 0.5D, target.getY() + 0.5D,
                            target.getZ() + 0.5D, stack.copy()))
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
            BlockDropsEvent dropsEvent = NeoForge.EVENT_BUS.post(
                    new BlockDropsEvent(level, target, state, blockEntity, dropEntities, player, lootTool));
            drops.clear();
            if (!dropsEvent.isCanceled()) {
                dropsEvent.getDrops().stream().map(ItemEntity::getItem).map(ItemStack::copy).forEach(drops::add);
            }
        }
        if (canStore.test(List.copyOf(drops))) {
            store.accept(List.copyOf(drops));
        } else {
            drops.forEach(stack -> Block.popResource(level, target, stack));
        }
        updateTool.accept(player.getMainHandItem().copy());
        clearPlayer(player);
        return BlockOperationResult.SUCCESS;
    }

    public static UseResult useBlock(
            ServerLevel level,
            BlockPos machinePosition,
            BlockPos target,
            Direction clickedFace,
            UUID owner,
            ItemStack useStack) {
        Direction face = clickedFace.getOpposite();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(target).add(
                face.getStepX() * 0.5D, face.getStepY() * 0.5D, face.getStepZ() * 0.5D), face, target, false);
        return useBlock(level, machinePosition, hit, owner, useStack, false);
    }

    public static UseResult useBlock(
            ServerLevel level,
            BlockPos machinePosition,
            BlockHitResult hit,
            UUID owner,
            ItemStack useStack,
            boolean sneaking) {
        BlockPos target = hit.getBlockPos();
        if (!level.isLoaded(target)) {
            return new UseResult(BlockOperationResult.UNLOADED, useStack.copy(), List.of());
        }
        if (level.getBlockState(target).isAir()) {
            return new UseResult(BlockOperationResult.NO_TARGET, useStack.copy(), List.of());
        }
        FakePlayer player = player(level, machinePosition, target, owner, useStack.copy());
        player.setShiftKeyDown(sneaking);
        if (!level.mayInteract(player, target)) {
            clearPlayer(player);
            return new UseResult(BlockOperationResult.DENIED, useStack.copy(), List.of());
        }
        InteractionResult interaction = player.gameMode.useItemOn(
                player, level, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
        if (!interaction.consumesAction()) {
            clearPlayer(player);
            return new UseResult(BlockOperationResult.DENIED, useStack.copy(), List.of());
        }

        ItemStack heldStack = player.getMainHandItem().copy();
        List<ItemStack> returned = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (slot != player.getInventory().selected) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    returned.add(stack.copy());
                }
            }
        }
        clearPlayer(player);
        return new UseResult(BlockOperationResult.SUCCESS, heldStack, List.copyOf(returned));
    }

    public static UseResult useEntity(
            ServerLevel level,
            BlockPos machinePosition,
            BlockPos scanPosition,
            UUID owner,
            ItemStack useStack,
            Vec3 start,
            Vec3 look,
            boolean rayTrace,
            boolean sneaking) {
        Vec3 end = rayTrace ? start.add(look.normalize().scale(5.0D)) : Vec3.atCenterOf(scanPosition);
        AABB search = rayTrace
                ? new AABB(start, end).inflate(1.0D)
                : new AABB(scanPosition).inflate(0.5D);
        Entity target = null;
        double closest = Double.MAX_VALUE;
        for (Entity candidate : level.getEntities((Entity)null, search,
                entity -> entity.isAlive() && !(entity instanceof ItemEntity) && !(entity instanceof FakePlayer))) {
            var intersection = candidate.getBoundingBox().inflate(candidate.getPickRadius()).clip(start, end);
            if (!rayTrace && candidate.getBoundingBox().inflate(0.5D).contains(Vec3.atCenterOf(scanPosition))) {
                intersection = java.util.Optional.of(Vec3.atCenterOf(scanPosition));
            }
            if (intersection.isPresent()) {
                double distance = start.distanceToSqr(intersection.orElseThrow());
                if (distance < closest) {
                    closest = distance;
                    target = candidate;
                }
            }
        }
        if (target == null) {
            return new UseResult(BlockOperationResult.NO_TARGET, useStack.copy(), List.of());
        }
        FakePlayer player = player(level, machinePosition, target.blockPosition(), owner, useStack.copy());
        player.setShiftKeyDown(sneaking);
        if (!level.mayInteract(player, target.blockPosition())) {
            clearPlayer(player);
            return new UseResult(BlockOperationResult.DENIED, useStack.copy(), List.of());
        }
        Vec3 interactionPoint = target.getBoundingBox().getCenter().subtract(target.position());
        if (CommonHooks.onInteractEntityAt(player, target, interactionPoint, InteractionHand.MAIN_HAND) != null) {
            clearPlayer(player);
            return new UseResult(BlockOperationResult.DENIED, useStack.copy(), List.of());
        }
        InteractionResult interaction = target.interactAt(player, interactionPoint, InteractionHand.MAIN_HAND);
        if (!interaction.consumesAction()) {
            interaction = player.interactOn(target, InteractionHand.MAIN_HAND);
        }
        if (!interaction.consumesAction()) {
            clearPlayer(player);
            return new UseResult(BlockOperationResult.NO_TARGET, useStack.copy(), List.of());
        }
        ItemStack heldStack = player.getMainHandItem().copy();
        List<ItemStack> returned = collectReturnedItems(player);
        clearPlayer(player);
        return new UseResult(BlockOperationResult.SUCCESS, heldStack, returned);
    }

    public static BlockOperationResult replaceBlock(
            ServerLevel level,
            BlockPos machinePosition,
            BlockPos target,
            UUID owner,
            BlockState replacement) {
        if (!level.isLoaded(target)) {
            return BlockOperationResult.UNLOADED;
        }
        BlockState state = level.getBlockState(target);
        FakePlayer player = player(level, machinePosition, target, owner, ItemStack.EMPTY);
        if (state.getDestroySpeed(level, target) < 0.0F
                || !level.mayInteract(player, target)
                || CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, target, state).isCanceled()
                || EventHooks.onBlockPlace(player,
                        BlockSnapshot.create(level.dimension(), level, target), Direction.UP)) {
            clearPlayer(player);
            return BlockOperationResult.DENIED;
        }
        level.setBlockAndUpdate(target, replacement);
        clearPlayer(player);
        return BlockOperationResult.SUCCESS;
    }

    public static BlockOperationResult breakBlockToWorld(
            ServerLevel level, BlockPos machinePosition, BlockPos target, UUID owner) {
        return breakBlock(level, machinePosition, target, owner,
                new ItemStack(net.minecraft.world.item.Items.DIAMOND_PICKAXE), stack -> { },
                drops -> true, drops -> drops.forEach(stack -> Block.popResource(level, target, stack)));
    }

    private static List<ItemStack> collectReturnedItems(FakePlayer player) {
        List<ItemStack> returned = new ArrayList<>();
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (slot != player.getInventory().selected) {
                ItemStack stack = player.getInventory().getItem(slot);
                if (!stack.isEmpty()) {
                    returned.add(stack.copy());
                }
            }
        }
        return List.copyOf(returned);
    }

    private static FakePlayer player(
            ServerLevel level, BlockPos machinePosition, BlockPos target, UUID owner, ItemStack mainHandItem) {
        FakePlayer player = FakePlayerFactory.get(level, new GameProfile(owner, PROFILE_NAME));
        clearPlayer(player);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(machinePosition.getX() + 0.5D, machinePosition.getY() + 0.5D,
                machinePosition.getZ() + 0.5D);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(target));
        player.getInventory().selected = 0;
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);
        return player;
    }

    private static void clearPlayer(FakePlayer player) {
        player.stopUsingItem();
        player.setShiftKeyDown(false);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    public record UseResult(BlockOperationResult result, ItemStack heldStack, List<ItemStack> returnedItems) {
    }

    private BlockOperationService() {
    }
}
