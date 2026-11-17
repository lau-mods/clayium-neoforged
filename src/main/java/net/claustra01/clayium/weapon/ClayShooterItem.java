package net.claustra01.clayium.weapon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ClayShooterItem extends Item {
    private static final int MAX_USE_DURATION = 72_000;
    private final ClayShooterSpecification specification;
    private final boolean teleporter;

    public ClayShooterItem(ClayShooterSpecification specification, Properties properties, boolean teleporter) {
        super(properties);
        this.specification = specification;
        this.teleporter = teleporter;
    }

    public ClayShooterSpecification specification() {
        return specification;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        if (level instanceof ServerLevel serverLevel) {
            if (specification.chargeTicks() == 0) {
                fire(serverLevel, player, hand, stack, 1.0F, false);
            } else {
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(),
                        SoundSource.PLAYERS, 0.5F, 1.3F);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
        if (!(living instanceof Player player) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (specification.chargeTicks() != 0) {
            int usedTicks = getUseDuration(stack, living) - remainingUseDuration;
            if (usedTicks == specification.chargeTicks()) {
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_HAT.value(),
                        SoundSource.PLAYERS, 0.5F, 0.6F);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_HURT,
                        SoundSource.PLAYERS, 0.2F, 2.5F);
            }
            return;
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return;
        }
        fire(serverLevel, player, player.getUsedItemHand(), stack, 1.0F, false);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remainingUseDuration) {
        if (specification.chargeTicks() == 0 || !(living instanceof Player player)
                || !(level instanceof ServerLevel serverLevel) || player.getCooldowns().isOnCooldown(this)) {
            return;
        }
        int usedTicks = getUseDuration(stack, living) - remainingUseDuration;
        float chargePower = specification.chargePower(usedTicks);
        fire(serverLevel, player, player.getUsedItemHand(), stack, chargePower, chargePower == 1.0F);
    }

    private void fire(ServerLevel level, Player player, InteractionHand hand, ItemStack stack,
            float chargePower, boolean critical) {
        ClayBall projectile = teleporter
                ? new TeleportBall(level, player, specification, chargePower, critical)
                : new ClayBall(level, player, specification, chargePower, critical);
        projectile.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                specification.initialVelocity() * chargePower,
                specification.diffusion());
        level.addFreshEntity(projectile);

        float velocity = specification.initialVelocity() * chargePower;
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.GENERIC_SMALL_FALL,
                SoundSource.PLAYERS,
                0.6F,
                5.0F / (level.random.nextFloat() * 0.7F + specification.damage() * chargePower + 1.0F));
        if (velocity >= 6.0F) {
            level.playSound(null, player.blockPosition(), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                    SoundSource.PLAYERS, 0.01F * (velocity - 6.0F), 1.0F);
            level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.PLAYERS, 0.01F * (velocity - 6.0F), 6.0F / (velocity + 2.0F) + 0.2F);
        }

        player.getCooldowns().addCooldown(this, specification.cooldownTicks());
        if (!player.hasInfiniteMaterials()) {
            int durabilityCost = 1 + (int) (chargePower * specification.chargeTicks());
            stack.hurtAndBreak(durabilityCost, player, LivingEntity.getSlotForHand(hand));
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

}
