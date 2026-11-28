package net.claustra01.clayium.entity;

import net.claustra01.clayium.registry.ClayiumEntities;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * High-velocity clay projectile launched by {@link net.claustra01.clayium.item.ClayShooterItem}.
 * Maintains zero gravity and no drag for {@code lifespan} ticks, then rapidly decelerates and drops.
 */
public class ClayBallEntity extends ThrowableItemProjectile {
    protected int age = 0;
    protected int lifespan = 2;
    protected int damage = 5;
    protected boolean critical = false;

    public ClayBallEntity(EntityType<? extends ClayBallEntity> type, Level level) {
        super(type, level);
    }

    public ClayBallEntity(
            Level level,
            LivingEntity shooter,
            int lifespan,
            float initialVelocity,
            float diffusion,
            int damage,
            boolean critical) {
        this(ClayiumEntities.CLAY_BALL.get(), level, shooter, lifespan, initialVelocity, diffusion, damage, critical);
    }

    protected ClayBallEntity(
            EntityType<? extends ClayBallEntity> type,
            Level level,
            LivingEntity shooter,
            int lifespan,
            float initialVelocity,
            float diffusion,
            int damage,
            boolean critical) {
        super(type, shooter, level);
        this.lifespan = Math.max(0, lifespan);
        this.damage = Math.max(0, damage);
        this.critical = critical;
        this.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, initialVelocity, diffusion);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.CLAY_BALL;
    }

    public int getLifespan() {
        return this.lifespan;
    }

    public int getDamage() {
        return this.damage;
    }

    public boolean isCritical() {
        return this.critical;
    }

    @Override
    protected double getDefaultGravity() {
        return this.age < this.lifespan ? 0.0 : 0.45;
    }

    @Override
    public void tick() {
        boolean straightPhase = this.age < this.lifespan;
        super.tick();
        if (!this.isRemoved()) {
            // Undo ThrowableProjectile's default 0.99 drag during straight lifespan; apply 0.6 drag afterwards.
            Vec3 vel = this.getDeltaMovement();
            double factor = this.isInWater() ? (0.8 / 0.8) : (straightPhase ? (1.0 / 0.99) : (0.6 / 0.99));
            if (vel.y >= 0.0) {
                this.setDeltaMovement(vel.scale(factor));
            } else {
                this.setDeltaMovement(vel.x * factor, vel.y, vel.z * factor);
            }
        }
        this.age++;
        if (this.level().isClientSide) {
            spawnFlightParticles();
        }
        if (this.age > 200) {
            onProjectileTerminate();
            this.discard();
        }
    }

    protected void spawnFlightParticles() {
        Vec3 vel = this.getDeltaMovement();
        this.level().addParticle(
                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.CLAY_BALL)),
                this.getX(), this.getY(), this.getZ(),
                vel.x * 0.5, vel.y * 0.5, vel.z * 0.5
        );
        if (this.age < this.lifespan && vel.lengthSqr() > 0.25) {
            this.level().addParticle(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), vel.x * 0.2, vel.y * 0.2, vel.z * 0.2);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        boolean aliveBefore = !(target instanceof LivingEntity living) || living.getHealth() > 0.0F;
        if (!this.level().isClientSide && target != this.getOwner()) {
            Vec3 oldMotion = target.getDeltaMovement();
            target.hurt(this.damageSources().thrown(this, this.getOwner()), (float) this.damage);
            if (target instanceof LivingEntity livingTarget) {
                livingTarget.invulnerableTime = 2;
            }
            target.setDeltaMovement(oldMotion);
        }
        if (target instanceof LivingEntity && aliveBefore) {
            playHitEffects();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.isRemoved()) {
            playImpactEffects();
            boolean shouldDie = true;
            if (result instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity living) {
                shouldDie = living.getHealth() > 0.0F;
            }
            if (shouldDie) {
                onProjectileTerminate();
                if (!this.level().isClientSide) {
                    this.discard();
                }
            }
        }
    }

    protected void playHitEffects() {
        float volDamage = 0.1F * (float) this.damage;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, Math.min(2.0F, volDamage), 0.7F);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, Math.min(2.0F, 0.3F * this.damage), 1.3F);
        if (this.critical) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_TWINKLE, SoundSource.PLAYERS, 2.0F, 1.0F);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), 20, 0.2, 0.2, 0.2, 0.2);
            }
        }
    }

    protected void playImpactEffects() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.CLAY_BALL)),
                    this.getX(), this.getY(), this.getZ(),
                    8, 0.05, 0.05, 0.05, 0.05
            );
            float speed = (float) this.getDeltaMovement().length();
            if (speed >= 0.5F && this.age <= this.lifespan) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                this.level().playSound(
                        null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GENERIC_EXPLODE.value(),
                        SoundSource.PLAYERS,
                        Math.min(2.0F, 0.2F * speed),
                        0.5F / (this.random.nextFloat() * 0.4F + 0.6F)
                );
            }
        }
        this.level().playSound(
                null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SLIME_SQUISH,
                SoundSource.PLAYERS,
                Math.min(2.0F, 0.06F * (float) this.damage),
                1.4F / (this.random.nextFloat() * 0.4F + 0.6F)
        );
    }

    protected void onProjectileTerminate() {
        // Subclasses (e.g. TeleportBallEntity) perform impact teleportation here.
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putShort("Age", (short) this.age);
        tag.putShort("Lifespan", (short) this.lifespan);
        tag.putInt("ClayBallDamage", this.damage);
        tag.putBoolean("Critical", this.critical);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.age = tag.getShort("Age");
        if (tag.contains("Lifespan")) {
            this.lifespan = tag.getShort("Lifespan");
        }
        if (tag.contains("ClayBallDamage")) {
            this.damage = tag.getInt("ClayBallDamage");
        }
        this.critical = tag.getBoolean("Critical");
    }
}
