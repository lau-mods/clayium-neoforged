package net.claustra01.clayium.entity;

import net.claustra01.clayium.registry.ClayiumEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;

/**
 * Projectile fired by the Instant Teleporter: warps the shooter to impact coordinates with no self-damage.
 */
public class TeleportBallEntity extends ClayBallEntity {
    public TeleportBallEntity(EntityType<? extends TeleportBallEntity> type, Level level) {
        super(type, level);
    }

    public TeleportBallEntity(
            Level level,
            LivingEntity shooter,
            int lifespan,
            float initialVelocity,
            float diffusion,
            int damage,
            boolean critical) {
        super(ClayiumEntities.TELEPORT_BALL.get(), level, shooter, lifespan, initialVelocity, diffusion, damage, critical);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_PEARL;
    }

    @Override
    protected void onProjectileTerminate() {
        if (this.level().isClientSide || !(this.getOwner() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.connection.isAcceptingMessages() || player.level() != this.level() || player.isSleeping()) {
            return;
        }
        EntityTeleportEvent.EnderEntity event = EventHooks.onEnderTeleport(player, this.getX(), this.getY(), this.getZ());
        if (event.isCanceled()) {
            return;
        }

        double oldX = player.getX();
        double oldY = player.getY();
        double oldZ = player.getZ();
        if (player.isPassenger()) {
            player.stopRiding();
        }

        double dx = 0.0;
        double dy = 0.0;
        double dz = 0.0;
        int attempt = 0;
        int tx;
        int ty;
        int tz;
        do {
            tx = (int) Math.floor(event.getTargetX() + dx);
            ty = (int) Math.floor(event.getTargetY() + dy);
            tz = (int) Math.floor(event.getTargetZ() + dz);
            attempt++;
            dx = this.random.nextGaussian() * attempt / 10.0;
            dy = this.random.nextGaussian() * attempt / 10.0;
            dz = this.random.nextGaussian() * attempt / 10.0;
        } while ((isSolidAt(tx, ty, tz) || isSolidAt(tx, ty + 1, tz)) && attempt <= 100);

        player.teleportTo(tx + 0.5, ty, tz + 0.5);
        player.resetFallDistance();
        this.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, event.getTargetX(), event.getTargetY() + 1.0, event.getTargetZ(), 30, 0.3, 0.8, 0.3, 0.2);
            double dist = Math.sqrt((event.getTargetX() - oldX) * (event.getTargetX() - oldX)
                    + (event.getTargetY() - oldY) * (event.getTargetY() - oldY)
                    + (event.getTargetZ() - oldZ) * (event.getTargetZ() - oldZ));
            int steps = Math.max(1, Math.min(200, (int) dist));
            for (int i = 0; i < steps; i++) {
                double r = (double) i / (double) steps;
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        oldX + (event.getTargetX() - oldX) * r,
                        oldY + 1.0 + (event.getTargetY() - oldY) * r,
                        oldZ + (event.getTargetZ() - oldZ) * r,
                        1, 0.0, 0.0, 0.0, 0.0
                );
            }
        }
    }

    private boolean isSolidAt(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return !this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty();
    }
}
