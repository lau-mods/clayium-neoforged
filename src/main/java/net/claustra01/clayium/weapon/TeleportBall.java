package net.claustra01.clayium.weapon;

import java.util.Set;
import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

public final class TeleportBall extends ClayBall {
    public TeleportBall(EntityType<? extends TeleportBall> type, Level level) {
        super(type, level);
    }

    public TeleportBall(Level level, LivingEntity owner, ClayShooterSpecification specification,
            float chargePower, boolean critical) {
        super(WeaponRegistration.TELEPORT_BALL.get(), owner, level);
        configure(specification, chargePower, critical);
    }

    @Override
    protected Item getDefaultItem() {
        return MaterialRegistration.COMPONENTS.get("teleportation_parts").get();
    }

    @Override
    protected void onServerHit(ServerLevel level, HitResult result) {
        super.onServerHit(level, result);
        if (!(getOwner() instanceof ServerPlayer player) || player.level() != level || !player.isAlive()) {
            return;
        }
        Vec3 target = result.getLocation();
        if (result instanceof BlockHitResult blockHit) {
            Direction direction = blockHit.getDirection();
            double horizontalOffset = player.getBbWidth() * 0.5D + 0.01D;
            target = switch (direction) {
                case UP -> target.add(0.0D, 0.01D, 0.0D);
                case DOWN -> target.add(0.0D, -player.getBbHeight() - 0.01D, 0.0D);
                default -> target.add(
                        direction.getStepX() * horizontalOffset,
                        0.0D,
                        direction.getStepZ() * horizontalOffset);
            };
        }
        tryTeleport(player, level, target);
    }

    public static boolean tryTeleport(ServerPlayer player, ServerLevel level, Vec3 requestedTarget) {
        EntityTeleportEvent event = NeoForge.EVENT_BUS.post(new EntityTeleportEvent(
                player, requestedTarget.x, requestedTarget.y, requestedTarget.z));
        if (event.isCanceled()) {
            return false;
        }
        Vec3 target = event.getTarget();
        if (!isSafeDestination(player, level, target)) {
            return false;
        }

        Vec3 origin = player.position();
        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.PORTAL, target.x, target.y + 1.0D, target.z,
                30, 0.35D, 0.9D, 0.35D, 0.2D);
        player.stopRiding();
        player.teleportTo(level, target.x, target.y, target.z, Set.of(), player.getYRot(), player.getXRot());
        player.resetFallDistance();
        level.playSound(null, target.x, target.y, target.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }

    public static boolean isSafeDestination(ServerPlayer player, ServerLevel level, Vec3 target) {
        if (target.y < level.getMinBuildHeight()
                || target.y + player.getBbHeight() > level.getMaxBuildHeight()) {
            return false;
        }
        AABB destination = player.getDimensions(player.getPose()).makeBoundingBox(target);
        return level.getWorldBorder().isWithinBounds(destination)
                && level.hasChunkAt(BlockPos.containing(target))
                && level.noCollision(player, destination);
    }

}
