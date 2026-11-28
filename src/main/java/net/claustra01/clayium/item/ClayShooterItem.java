package net.claustra01.clayium.item;

import net.claustra01.clayium.entity.ClayBallEntity;
import net.claustra01.clayium.entity.TeleportBallEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Clay Shooters (tiers 0..3) and Instant Teleporter.
 * Supports rapid auto-fire ({@code chargeTime == 0}) and bow-charge zoom firing ({@code chargeTime > 0}).
 */
public class ClayShooterItem extends SimpleTieredItem {
    private final int lifespanTicks;
    private final float initialVelocity;
    private final float spread;
    private final int baseDamage;
    private final int shootingFrame;
    private final float shootingRate;
    private final int cooldownTicks;
    private final int chargeTime;
    private final boolean teleporter;

    public ClayShooterItem(
            int durability,
            int tier,
            String iconName,
            int lifespanTicks,
            float initialVelocity,
            float spread,
            int baseDamage,
            int shootingFrame,
            int chargeTime,
            boolean teleporter) {
        super(new Properties().durability(durability), tier, iconName);
        this.lifespanTicks = lifespanTicks;
        this.initialVelocity = initialVelocity;
        this.spread = spread;
        this.baseDamage = baseDamage;
        this.shootingFrame = Math.max(1, shootingFrame);
        this.shootingRate = 3.0F / (float) this.shootingFrame;
        this.cooldownTicks = Math.max(1, this.shootingFrame / 3);
        this.chargeTime = Math.max(0, chargeTime);
        this.teleporter = teleporter;
    }

    public int getLifespanTicks() {
        return this.lifespanTicks;
    }

    public float getInitialVelocity() {
        return this.initialVelocity;
    }

    public float getProjectileSpeed() {
        return this.initialVelocity;
    }

    public float getSpread() {
        return this.spread;
    }

    public int getBaseDamage() {
        return this.baseDamage;
    }

    public int getShootingFrame() {
        return this.shootingFrame;
    }

    public float getShootingRate() {
        return this.shootingRate;
    }

    public int getCooldownTicks() {
        return this.cooldownTicks;
    }

    public int getChargeTime() {
        return this.chargeTime;
    }

    public boolean isCharger() {
        return this.chargeTime > 0;
    }

    public boolean isTeleporter() {
        return this.teleporter;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        if (isCharger()) {
            if (!level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5F, 1.3F);
            }
        } else {
            shootBullet(stack, level, player, hand, 1.0F, false);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int elapsed = getUseDuration(stack, entity) - remainingUseDuration;
        if (isCharger()) {
            if (elapsed == this.chargeTime && !level.isClientSide) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.5F, 0.6F);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_DRAGON_HURT, SoundSource.PLAYERS, 0.2F, 2.5F);
            }
        } else if (elapsed > 0) {
            int prevShots = (int) ((float) (elapsed - 1) * this.shootingRate);
            int curShots = (int) ((float) elapsed * this.shootingRate);
            int count = curShots - prevShots;
            InteractionHand hand = player.getUsedItemHand();
            for (int i = 0; i < count; i++) {
                shootBullet(stack, level, player, hand, 1.0F, false);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (isCharger()) {
            int used = getUseDuration(stack, entity) - timeLeft;
            float f = computeChargeFactor(used, this.chargeTime);
            if (f > 0.0F) {
                shootBullet(stack, level, player, player.getUsedItemHand(), f, f >= 1.0F);
            }
        }
        player.getCooldowns().addCooldown(this, this.cooldownTicks);
    }

    public static float computeChargeFactor(int usedTicks, int chargeTime) {
        if (chargeTime <= 0) {
            return 1.0F;
        }
        float f = (float) usedTicks / (float) chargeTime;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(1.0F, Math.max(0.0F, f));
    }

    public void shootBullet(ItemStack stack, Level level, Player player, InteractionHand hand, float per, boolean critical) {
        float pitch = 5.0F / (level.random.nextFloat() * 0.7F + (float) this.baseDamage * per + 1.0F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SMALL_FALL, SoundSource.PLAYERS, 0.6F, pitch);
        float v = this.initialVelocity * per;
        if (v >= 6.0F) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, Math.min(1.0F, 0.01F * (v - 6.0F)), 1.0F);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, Math.min(1.0F, 0.01F * (v - 6.0F)), 6.0F / (v + 2.0F) + 0.2F);
        }

        if (!level.isClientSide) {
            ClayBallEntity projectile = this.teleporter
                    ? new TeleportBallEntity(level, player, this.lifespanTicks, v, this.spread, (int) ((float) this.baseDamage * per), critical)
                    : new ClayBallEntity(level, player, this.lifespanTicks, v, this.spread, (int) ((float) this.baseDamage * per), critical);
            level.addFreshEntity(projectile);
            if (!player.isCreative()) {
                int cost = (int) (per * (float) this.chargeTime) + 1;
                stack.hurtAndBreak(cost, player, LivingEntity.getSlotForHand(hand));
            }
        }
    }
}
