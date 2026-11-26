package net.claustra01.clayium.weapon;

import net.claustra01.clayium.registry.MaterialRegistration;
import net.claustra01.clayium.registry.WeaponRegistration;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClayBall extends ThrowableItemProjectile {
    public static final int MAX_LIFETIME_TICKS = 1_200;
    private static final EntityDataAccessor<Boolean> CRITICAL =
            SynchedEntityData.defineId(ClayBall.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STRAIGHT_FLIGHT_TICKS =
            SynchedEntityData.defineId(ClayBall.class, EntityDataSerializers.INT);
    private float damage = ClayShooterSpecification.BASIC.damage();

    public ClayBall(EntityType<? extends ClayBall> type, Level level) {
        super(type, level);
    }

    public ClayBall(Level level, LivingEntity owner, ClayShooterSpecification specification,
            float chargePower, boolean critical) {
        this(WeaponRegistration.CLAY_BALL.get(), owner, level);
        configure(specification, chargePower, critical);
    }

    protected ClayBall(EntityType<? extends ClayBall> type, LivingEntity owner, Level level) {
        super(type, owner, level);
    }

    protected final void configure(ClayShooterSpecification specification, float chargePower, boolean critical) {
        entityData.set(STRAIGHT_FLIGHT_TICKS, specification.straightFlightTicks());
        damage = (int) (specification.damage() * chargePower);
        entityData.set(CRITICAL, critical);
    }

    public int straightFlightTicks() {
        return entityData.get(STRAIGHT_FLIGHT_TICKS);
    }

    public float damage() {
        return damage;
    }

    public boolean isCritical() {
        return entityData.get(CRITICAL);
    }

    @Override
    protected Item getDefaultItem() {
        return MaterialRegistration.MATERIAL_ITEMS.get("large_clay_ball").get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CRITICAL, false);
        builder.define(STRAIGHT_FLIGHT_TICKS, ClayShooterSpecification.BASIC.straightFlightTicks());
    }

    @Override
    public void tick() {
        Vec3 incomingMovement = getDeltaMovement();
        super.tick();
        if (isRemoved()) {
            return;
        }
        if (tickCount <= straightFlightTicks()) {
            setDeltaMovement(incomingMovement);
        } else {
            setDeltaMovement(incomingMovement.scale(0.6D).add(0.0D, -0.45D, 0.0D));
        }
        if (level().isClientSide) {
            Entity owner = getOwner();
            if (owner != null) {
                ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(getDefaultItem()));
                Vec3 start = new Vec3(xo, yo, zo);
                Vec3 end = position();
                int steps = Math.max(1, (int) Math.ceil(start.distanceTo(end) / 2.0D));
                for (int step = 1; step <= steps; step++) {
                    Vec3 point = start.lerp(end, (double) step / steps);
                    if (point.distanceToSqr(owner.getEyePosition()) > 4.0D) {
                        level().addParticle(particle, point.x, point.y, point.z, 0.0D, 0.0D, 0.0D);
                    }
                }
            }
        } else if (tickCount >= MAX_LIFETIME_TICKS) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(level() instanceof ServerLevel serverLevel) || isRemoved()) {
            return;
        }
        onServerHit(serverLevel, result);
        ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(getDefaultItem()));
        serverLevel.sendParticles(particle, getX(), getY(), getZ(), 8, 0.12D, 0.12D, 0.12D, 0.0D);
        if (isCritical()) {
            serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY(), getZ(), 10, 0.2D, 0.2D, 0.2D, 0.2D);
        }
        serverLevel.playSound(null, blockPosition(), SoundEvents.STONE_HIT, SoundSource.PLAYERS,
                Math.max(0.06F, damage * 0.06F), 1.4F / (random.nextFloat() * 0.4F + 0.6F));
        discard();
    }

    protected void onServerHit(ServerLevel level, HitResult result) {
        if (result instanceof EntityHitResult entityHit) {
            Entity owner = getOwner();
            entityHit.getEntity().hurt(damageSources().thrown(this, owner), damage);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StraightFlightTicks", straightFlightTicks());
        tag.putFloat("Damage", damage);
        tag.putBoolean("Critical", isCritical());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(STRAIGHT_FLIGHT_TICKS, tag.getInt("StraightFlightTicks"));
        damage = tag.getFloat("Damage");
        entityData.set(CRITICAL, tag.getBoolean("Critical"));
    }
}
